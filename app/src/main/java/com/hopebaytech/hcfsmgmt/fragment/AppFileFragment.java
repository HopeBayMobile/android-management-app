package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.Html;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.customview.CircleDisplay;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IMgmtBinder;
import com.hopebaytech.hcfsmgmt.interfaces.IPinUnpinListener;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.DisplayTypeFactory;
import com.hopebaytech.hcfsmgmt.utils.ExecutorFactory;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MemoryCacheFactory;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class AppFileFragment extends Fragment {

    public static final String TAG = AppFileFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    public static final String KEY_ARGUMENT_APP_FILE = "key_argument_app_file";
    private static final String KEY_ARGUMENT_IS_SDCARD1 = "key_argument_is_sdcard1";
    private static final String KEY_ARGUMENT_SDCARD1_PATH = "key_argument_sdcard1_path";
    public static final String KEY_ARGUMENT_ALLOW_PIN_UNPIN_APPS = "key_argument_allow_pin_unpin_apps";
    private final String EXTERNAL_ANDROID_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
    private final int GRID_LAYOUT_SPAN_COUNT = 3;

    private View mView;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressCircle;
    private TextView mNoDataMsg;
    private Spinner mSpinner;
    private ImageView mRefresh;
    private ImageView mChangeLayout;
    private Snackbar mSnackbar;
    private Toast mToast;

    private AddRemovePackageReceiver mAddRemovePackageReceiver;
    private SectionedRecyclerViewAdapter mSectionedRecyclerViewAdapter;
    private DividerItemDecoration mDividerItemDecoration;
    private ArrayAdapter<String> mSpinnerAdapter;

    private HandlerThread mHandlerThread;
    private Thread mApiExecutorThread;
    private Thread mAutoUiRefreshThread;
    private Thread mProcessPinThread;
    private Handler mWorkerHandler;
    private Handler mUiHandler = new Handler();

    private SparseArray<ItemInfo> mWaitToExecuteSparseArr = new SparseArray<>();
    private SparseArray<Boolean> mPinStatusSparseArr = new SparseArray<>();
    private Map<String, Boolean> mPinUnpinFileMap = new ConcurrentHashMap<>();
    private Map<String, AppInfo> mPinUnpinAppMap = new ConcurrentHashMap<>();
    private Map<String, Boolean> mPinUnpinTypeMap = new ConcurrentHashMap<>();
    private String mFileRootDirPath;
    private String mFileRootDirName;
    private boolean isSDCard1 = false;

    /**
     * Recycler view is scrolled down or not
     */
    private boolean mRecyclerViewScrollDown;

    /**
     * The current fragment is visible or not
     */
    private boolean mFragmentVisible;

    /**
     * The {@link TeraMgmtService} is bound or not
     */
    private boolean mServiceBound;

    /**
     * Need to filter pin status or not
     */
    private boolean mFilterByPin;

    /**
     * Allow users to pin/unpin apps or not
     */
    private boolean mAllowPinUnpinApps;

    /**
     * Show app/file content process is now executing or not
     */
    private boolean mShowingContent;

    private TeraMgmtService mMgmtService;

    /**
     * @see LayoutType#GRID
     * @see LayoutType#LINEAR
     */
    private LayoutType mLayoutType;

    /**
     * @see DisplayType#BY_APP
     * @see DisplayType#BY_FILE
     */
    private int mDisplayType;

    /**
     * @see SortType#BY_NAME
     * @see SortType#BY_INSTALLED_TIME
     * @see SortType#BY_MODIFIED_TIME
     * @see SortType#BY_SIZE
     */
    private int mSortType;

    /**
     * Only used when user switch to "Display by file"
     */
    private HorizontalScrollView mFilePathNavigationScrollView;
    /**
     * Only used when user switch to "Display by file"
     */
    private LinearLayout mFilePathNavigationLayout;
    /**
     * Only used when user switch to "Display by file"
     */
    private File mCurrentFile;

    private enum LayoutType {
        GRID, LINEAR
    }

    static class DisplayType {
        static final int BY_APP = 1;
        static final int BY_FILE = 2;
    }

    public static class SortType {
        public static final int BY_NAME = 1;
        public static final int BY_INSTALLED_TIME = 2;
        public static final int BY_MODIFIED_TIME = 3;
        public static final int BY_SIZE = 4;
    }

    private View.OnClickListener mPermissionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String packageName = getContext().getPackageName();
            Intent teraPermissionSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
            teraPermissionSettings.addCategory(Intent.CATEGORY_DEFAULT);
            teraPermissionSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(teraPermissionSettings);
        }
    };

    private Runnable mAutoUiRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    if (mProgressCircle.getVisibility() == View.GONE &&
                            mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE &&
                            mWaitToExecuteSparseArr.size() == 0 &&
                            mPinUnpinAppMap.size() == 0 &&
                            mPinUnpinFileMap.size() == 0 &&
                            !mShowingContent) {
                        notifyRecyclerViewItemChanged();
                    }
                    Thread.sleep(Interval.AUTO_REFRESH_UI);
                } catch (InterruptedException e) {
                    Logs.d(CLASSNAME, "AutoUiRefreshRunnable", "mAutoUiRefreshThread is interrupted");
                    break;
                }
            }
        }
    };

    private Runnable mCheckPinStatusRunnable = new Runnable() {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(Interval.CHECK_PIN_STATUS);
                    if (mSectionedRecyclerViewAdapter == null || mWaitToExecuteSparseArr.size() != 0) {
                        continue;
                    }

                    try {
                        boolean isProcessDone = true;
                        switch (mDisplayType) {
                            case DisplayType.BY_APP:
                                // Check pin/unpin process finished in display-by-app page
                                for (String packageName : mPinUnpinAppMap.keySet()) {
                                    AppInfo appInfo = mPinUnpinAppMap.get(packageName);
                                    boolean isRealPinned = HCFSMgmtUtils.isAppPinned(mContext, appInfo);
                                    boolean isExpectedPinned = appInfo.isPinned();

                                    if (isRealPinned == isExpectedPinned) {
                                        // Remove packageName from mPinUnpinAppMap if the pin status
                                        // has became what user expected
                                        mPinUnpinAppMap.remove(packageName);
                                    }
                                }
                                if (mPinUnpinAppMap.size() != 0) {
                                    isProcessDone = false;
                                }
                                break;
                            case DisplayType.BY_FILE:
                                // Check pin/unpin process finished in display-by-files page
                                for (String path : mPinUnpinFileMap.keySet()) {
                                    boolean isRealPinned = HCFSMgmtUtils.isPathPinned(path);
                                    boolean isExpectedPinned = mPinUnpinFileMap.get(path);
                                    if (path.equals(mCurrentFile.getAbsolutePath()) ||
                                            path.startsWith(mCurrentFile.getAbsolutePath().concat("/"))) {
                                        if (isRealPinned == isExpectedPinned) {
                                            // Remove pinUnpinPath from mPinUnpinFileMap if the pin
                                            // status has became what user expected
                                            mPinUnpinFileMap.remove(path);
                                        } else {
                                            isProcessDone = false;
                                        }
                                    }
                                }
                                break;
                            default:
                                continue;
                        }

                        if (mProgressCircle.getVisibility() == View.VISIBLE) {
                            Logs.d(CLASSNAME, "mCheckPinStatusRunnable", "hideProgress=" + isProcessDone);
                            if (isProcessDone) {
                                dismissProgress();
                                notifyRecyclerViewItemChanged();
                            }
                        }
                    } catch (NullPointerException e) {
                        Logs.e(CLASSNAME, "mCheckPinStatusRunnable", Log.getStackTraceString(e));
                    }
                } catch (InterruptedException e) {
                    Logs.d(CLASSNAME, "mCheckPinStatusRunnable", "mCheckPinStatusRunnable is interrupted");
                    break;
                }
            }
        }
    };

    private Runnable mApiExecutorRunnable = new Runnable() {

        private boolean isProcessing;

        private void processPinUnpinFailed(final ItemInfo itemInfo) {
            if (itemInfo instanceof AppInfo) {
                mPinUnpinAppMap.remove(((AppInfo) itemInfo).getPackageName());
            } else if (itemInfo instanceof FileInfo) {
                mPinUnpinFileMap.remove(((FileInfo) itemInfo).getFilePath());
            }

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    FileDialogFragment fileDialogFragment = (FileDialogFragment)
                            getFragmentManager().findFragmentByTag(FileDialogFragment.TAG);
                    AppDialogFragment appDialogFragment = (AppDialogFragment)
                            getFragmentManager().findFragmentByTag(AppDialogFragment.TAG);
                    if (fileDialogFragment != null || appDialogFragment != null) {
                        ImageView pinIcon;
                        if (fileDialogFragment != null) {
                            pinIcon = (ImageView) fileDialogFragment.getDialog().findViewById(R.id.file_dir_pin_icon);
                        } else {
                            pinIcon = (ImageView) appDialogFragment.getDialog().findViewById(R.id.app_pin_icon);
                        }
                        boolean isPinned = itemInfo.isPinned();
                        pinIcon.setImageDrawable(itemInfo.getPinViewImage(isPinned));
                        pinIcon.setContentDescription(getPinViewContentDescription(isPinned));
                    } else {
                        if (itemInfo.getViewHolder() instanceof LinearRecyclerViewAdapter.LinearRecyclerViewHolder) {
                            boolean isPinned = itemInfo.isPinned();
                            LinearRecyclerViewAdapter.LinearRecyclerViewHolder holder =
                                    (LinearRecyclerViewAdapter.LinearRecyclerViewHolder) itemInfo.getViewHolder();
                            holder.pinView.setImageDrawable(itemInfo.getPinViewImage(isPinned));
                            holder.pinView.setContentDescription(getPinViewContentDescription(isPinned));
                        }
                    }
                    dismissProgress();
                }
            });
        }

        private void processRequests() {
            List<Integer> removeList = new ArrayList<>();
            for (int i = 0; i < mWaitToExecuteSparseArr.size(); i++) {
                int key = mWaitToExecuteSparseArr.keyAt(i);
                final ItemInfo cloneItemInfo = mWaitToExecuteSparseArr.get(key);
                long lastProcessTime = cloneItemInfo.getLastProcessTime();
                if (cloneItemInfo instanceof AppInfo) {
                    final UidDAO uidDAO = UidDAO.getInstance(mContext);
                    AppInfo appInfo = (AppInfo) cloneItemInfo;
                    UidInfo uidInfo = new UidInfo(appInfo);
                    uidDAO.update(uidInfo, UidDAO.PIN_STATUS_COLUMN);

                    mMgmtService.pinOrUnpinApp((AppInfo) cloneItemInfo, new IPinUnpinListener() {
                        @Override
                        public void onPinUnpinSuccessful(final ItemInfo cloneItemInfo) {
                            showPinUnpinResultToast(true /* isSuccess */, cloneItemInfo.isPinned());

                            ItemInfo realItemInfo = mSectionedRecyclerViewAdapter
                                    .getSubAdapterItemInfoList()
                                    .get(cloneItemInfo.getPosition());
                            realItemInfo.setProcessing(false);
                        }

                        @Override
                        public void onPinUnpinFailed(final ItemInfo cloneItemInfo) {
                            showPinUnpinResultToast(false /* isSuccess */, cloneItemInfo.isPinned());

                            ItemInfo realItemInfo = mSectionedRecyclerViewAdapter
                                    .getSubAdapterItemInfoList()
                                    .get(cloneItemInfo.getPosition());
                            realItemInfo.setPinned(!cloneItemInfo.isPinned());

                            // Update pin status to uid.db
                            cloneItemInfo.setPinned(!cloneItemInfo.isPinned());
                            UidInfo uidInfo = new UidInfo((AppInfo) cloneItemInfo);
                            uidDAO.update(uidInfo, UidDAO.PIN_STATUS_COLUMN);

                            processPinUnpinFailed(cloneItemInfo);
                            realItemInfo.setProcessing(false);
                        }
                    });
                } else if (cloneItemInfo instanceof DataTypeInfo) {
                    // Nothing to do here
                } else if (cloneItemInfo instanceof FileInfo) {
                    FileInfo fileDirInfo = (FileInfo) cloneItemInfo;
                    mMgmtService.pinOrUnpinFileDirectory(fileDirInfo, new IPinUnpinListener() {
                        @Override
                        public void onPinUnpinSuccessful(final ItemInfo cloneItemInfo) {
                            showPinUnpinResultToast(true /* isSuccess */, cloneItemInfo.isPinned());

                            ItemInfo realItemInfo = mSectionedRecyclerViewAdapter
                                    .getSubAdapterItemInfoList()
                                    .get(cloneItemInfo.getPosition());
                            realItemInfo.setProcessing(false);
                        }

                        @Override
                        public void onPinUnpinFailed(final ItemInfo cloneItemInfo) {
                            showPinUnpinResultToast(false /* isSuccess */, cloneItemInfo.isPinned());

                            ItemInfo realItemInfo = mSectionedRecyclerViewAdapter
                                    .getSubAdapterItemInfoList()
                                    .get(cloneItemInfo.getPosition());
                            realItemInfo.setPinned(!cloneItemInfo.isPinned());

                            cloneItemInfo.setPinned(!cloneItemInfo.isPinned());
                            processPinUnpinFailed(cloneItemInfo);
                            cloneItemInfo.setProcessing(false);
                        }
                    });
                }
                if (mWaitToExecuteSparseArr.get(key).getLastProcessTime() == lastProcessTime) {
                    removeList.add(key);
                }
            }
            for (int key : removeList) {
                mWaitToExecuteSparseArr.remove(key);
            }
        }

        @Override
        public void run() {
            // Process user requests every one second
            while (true) {
                try {
                    if (!isProcessing) {
                        isProcessing = true;
                        processRequests();
                        isProcessing = false;
                    }
                    Thread.sleep(Interval.EXECUTE_PIN_API);
                } catch (InterruptedException e) {
                    Logs.d(CLASSNAME, "onCreate", "mApiExecutorThread is interrupted");
                    // Process the remaining user requests
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            processRequests();
                        }
                    }).start();
                    break;
                } catch (NullPointerException e) {
                    Logs.e(CLASSNAME, "onCreate", Log.getStackTraceString(e));
                    isProcessing = false;
                    break;
                }
            }
        }
    };

    private void showPinUnpinResultToast(final boolean isSuccess, final boolean isPinned) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                int message;
                if (isSuccess) {
                    if (isPinned) {
                        message = R.string.toast_pin_success;
                    } else {
                        message = R.string.toast_unpin_success;
                    }
                } else {
                    if (isPinned) {
                        message = R.string.toast_pin_failure;
                    } else {
                        message = R.string.toast_unpin_failure;
                    }
                }

                if (mToast == null) {
                    mToast = Toast.makeText(mContext, null, Toast.LENGTH_LONG);
                }
                mToast.setText(message);
                mToast.cancel();
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mToast.show();
                    }
                }, 50);
            }
        });

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            IMgmtBinder binder = (IMgmtBinder) service;
            mMgmtService = binder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mServiceBound = false;
        }
    };

    private void showProgress() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                FileDialogFragment fileDialogFragment = (FileDialogFragment)
                        getFragmentManager().findFragmentByTag(FileDialogFragment.TAG);
                AppDialogFragment appDialogFragment = (AppDialogFragment)
                        getFragmentManager().findFragmentByTag(AppDialogFragment.TAG);
                if (fileDialogFragment != null || appDialogFragment != null) {
                    ProgressBar progress;
                    if (fileDialogFragment != null) {
                        Dialog dialog = fileDialogFragment.getDialog();
                        progress = (ProgressBar) dialog.findViewById(R.id.progress_circle);
                    } else {
                        Dialog dialog = appDialogFragment.getDialog();
                        progress = (ProgressBar) dialog.findViewById(R.id.progress_circle);
                    }
                    progress.setVisibility(View.VISIBLE);
                }
                // Always show progress on the recyclerView, prevent user from not knowing whether
                // pin/unpin process is done or not when the app/file info dialog dismiss
                mProgressCircle.setVisibility(View.VISIBLE);
            }
        });
    }

    private void dismissProgress() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                FileDialogFragment fileDialogFragment = (FileDialogFragment)
                        getFragmentManager().findFragmentByTag(FileDialogFragment.TAG);
                AppDialogFragment appDialogFragment = (AppDialogFragment)
                        getFragmentManager().findFragmentByTag(AppDialogFragment.TAG);
                if (fileDialogFragment != null || appDialogFragment != null) {
                    ProgressBar progress;
                    if (fileDialogFragment != null) {
                        Dialog dialog = fileDialogFragment.getDialog();
                        progress = (ProgressBar) dialog.findViewById(R.id.progress_circle);
                    } else {
                        Dialog dialog = appDialogFragment.getDialog();
                        progress = (ProgressBar) dialog.findViewById(R.id.progress_circle);
                    }
                    progress.setVisibility(View.GONE);
                }
                mProgressCircle.setVisibility(View.GONE);
            }
        });
    }

    public static AppFileFragment newInstance(boolean isSDCard1) {
        AppFileFragment fragment = new AppFileFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_ARGUMENT_IS_SDCARD1, isSDCard1);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static AppFileFragment newInstance(boolean isSDCard1, String SDCard1Path) {
        AppFileFragment fragment = new AppFileFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_ARGUMENT_IS_SDCARD1, isSDCard1);
        bundle.putString(KEY_ARGUMENT_SDCARD1_PATH, SDCard1Path);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize display type and external file root path
        Bundle args = getArguments();
        mDisplayType = args.getInt(KEY_ARGUMENT_APP_FILE);
        isSDCard1 = args.getBoolean(KEY_ARGUMENT_IS_SDCARD1);
        if (isSDCard1) {
            mFileRootDirPath = args.getString(KEY_ARGUMENT_SDCARD1_PATH);
        } else {
            mFileRootDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        // Initialize sort type
        mSortType = SortType.BY_NAME;

        // Initialize layout type (linear or grid)
        int ordinal;
        int defaultLayout = LayoutType.GRID.ordinal();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (mDisplayType == DisplayType.BY_APP) {
            ordinal = sharedPreferences.getInt(HCFSMgmtUtils.PREF_APP_DISPLAY_LAYOUT, defaultLayout);
        } else { // DISPLAY_BY_FILE
            ordinal = sharedPreferences.getInt(HCFSMgmtUtils.PREF_FILE_DISPLAY_LAYOUT, defaultLayout);
        }
        mLayoutType = LayoutType.values()[ordinal];

        // Initialize spinner adapter
        String[] spinnerArray;
        if (isSDCard1) {
            mFileRootDirName = mContext.getString(R.string.app_file_sdcard1_storage_name) + "/";
            spinnerArray = new String[]{mContext.getString(R.string.app_file_spinner_files)};
        } else {
            mFileRootDirName = mContext.getString(R.string.app_file_internal_storage_name) + "/";
            switch (mDisplayType) {
                case DisplayType.BY_FILE:
                    spinnerArray = mContext.getResources().getStringArray(R.array.file_sort_spinner);
                    break;
                default: // Display by app
                    spinnerArray = mContext.getResources().getStringArray(R.array.app_sort_spinner);
            }
        }
        mSpinnerAdapter = new ArrayAdapter<String>(mContext, R.layout.app_file_spinner_item, spinnerArray) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setId(position);
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setContentDescription(textView.getText());
                return textView;
            }
        };
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Initialized the worker handler
        mHandlerThread = new HandlerThread(AppFileFragment.class.getSimpleName());
        mHandlerThread.start();
        mWorkerHandler = new Handler(mHandlerThread.getLooper());

        // Initialize the divider item decoration
        mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);

        // RegisterProxy mAddRemovePackageReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        mAddRemovePackageReceiver = new AddRemovePackageReceiver();
        mAddRemovePackageReceiver.registerReceiver(mContext, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.app_file_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to TeraMgmtService
        Intent intent = new Intent(mContext, TeraMgmtService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unbind from the service
        if (mServiceBound) {
            mContext.unbindService(mConnection);
            mServiceBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (mSnackbar != null) {
                mSnackbar.dismiss();
            }
        }

        // Start mAutoUiRefreshThread
        if (mAutoUiRefreshThread == null) {
            if (mAutoUiRefreshRunnable != null) {
                if (mFragmentVisible) {
                    mAutoUiRefreshThread = new Thread(mAutoUiRefreshRunnable);
                    mAutoUiRefreshThread.start();
                }
            }
        }

        // Start mProcessPinThread
        if (mProcessPinThread == null) {
            if (mCheckPinStatusRunnable != null) {
                if (mFragmentVisible) {
                    mProcessPinThread = new Thread(mCheckPinStatusRunnable);
                    mProcessPinThread.start();
                }
            }
        }

        // Start mApiExecutorThread
        if (mApiExecutorThread == null) {
            if (mApiExecutorRunnable != null) {
                if (mFragmentVisible) {
                    mApiExecutorThread = new Thread(mApiExecutorRunnable);
                    mApiExecutorThread.start();
                }
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Logs.d(CLASSNAME, "onPause", null);

        // Interrupt mApiExecutorThread
        if (mApiExecutorThread != null) {
            mApiExecutorThread.interrupt();
            mApiExecutorThread = null;
        }

        // Interrupt mAutoUiRefreshThread
        if (mAutoUiRefreshThread != null) {
            mAutoUiRefreshThread.interrupt();
            mAutoUiRefreshThread = null;
        }

        // Interrupt mProcessPinThread
        if (mProcessPinThread != null) {
            mProcessPinThread.interrupt();
            mProcessPinThread = null;
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;
        mNoDataMsg = (TextView) view.findViewById(R.id.no_data_msg);
        mProgressCircle = (ProgressBar) view.findViewById(R.id.progress_circle);
        mFilePathNavigationLayout = (LinearLayout) view.findViewById(R.id.file_path_layout);
        mFilePathNavigationScrollView = (HorizontalScrollView) view.findViewById(R.id.file_path_navigation_scrollview);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mRefresh = (ImageView) view.findViewById(R.id.refresh);
        mChangeLayout = (ImageView) view.findViewById(R.id.change_layout);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = ((MainActivity) mContext).findViewById(android.R.id.content);
        mSnackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);

        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mRecyclerViewScrollDown = (dy >= 0);
            }
        });
        mRecyclerView.addItemDecoration(mDividerItemDecoration);

        switch (mLayoutType) {
            case LINEAR:
                mChangeLayout.setImageResource(R.drawable.icon_btn_tab_gridview_light);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                mSectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter(new LinearRecyclerViewAdapter());
                break;
            case GRID:
                Logs.w(CLASSNAME, "onActivityCreated", null);
                mChangeLayout.setImageResource(R.drawable.icon_btn_tab_listview_light);
                mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, GRID_LAYOUT_SPAN_COUNT));
                mSectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter(new GridRecyclerViewAdapter());
                mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();
                break;
        }
        mSectionedRecyclerViewAdapter.setSections(new Section[]{new Section(0)});
        mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);

        switch (mDisplayType) {
            case DisplayType.BY_APP:
                mFilePathNavigationLayout.setVisibility(View.GONE);
                break;
            case DisplayType.BY_FILE:
                mCurrentFile = new File(mFileRootDirPath);
                mFilePathNavigationLayout.removeAllViews();
                FilePathNavigationView currentPathView = new FilePathNavigationView(mContext);
                currentPathView.setText(Html.fromHtml("<u>" + mFileRootDirName + "</u>"));
                currentPathView.setCurrentFilePath(mCurrentFile.getAbsolutePath());
                mFilePathNavigationLayout.addView(currentPathView);
                mFilePathNavigationLayout.setVisibility(View.VISIBLE);
                break;
        }

        mSpinner.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSectionedRecyclerViewAdapter.init();
                String itemName = parent.getSelectedItem().toString();
                if (itemName.equals(mContext.getString(R.string.app_file_spinner_sort_by_name))) {
                    mSortType = SortType.BY_NAME;
                } else if (itemName.equals(mContext.getString(R.string.app_file_spinner_sort_by_installed_time))) {
                    mSortType = SortType.BY_INSTALLED_TIME;
                } else if (itemName.equals(mContext.getString(R.string.app_file_spinner_sort_by_modified_time))) {
                    mSortType = SortType.BY_MODIFIED_TIME;
                } else if (itemName.equals(mContext.getString(R.string.app_file_spinner_sort_by_size))) {
                    mSortType = SortType.BY_SIZE;
                }
                showContent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRefresh.setOnClickListener(new OnClickListener() {

            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastClickTime) > Interval.NOT_ALLOW_REFRESH) {
                    mSectionedRecyclerViewAdapter.init();
                    showContent();
                    lastClickTime = currentTime;
                }
            }
        });

        mChangeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ItemInfo> itemInfoList;
                switch (mLayoutType) {
                    case LINEAR:
                        mChangeLayout.setImageResource(R.drawable.icon_btn_tab_listview_light);
                        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, GRID_LAYOUT_SPAN_COUNT));

                        itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                        mSectionedRecyclerViewAdapter.setBaseAdapter(new GridRecyclerViewAdapter(itemInfoList));
                        mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();

                        mLayoutType = LayoutType.GRID;
                        break;
                    case GRID:
                        mChangeLayout.setImageResource(R.drawable.icon_btn_tab_gridview_light);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

                        itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                        mSectionedRecyclerViewAdapter.setBaseAdapter(new LinearRecyclerViewAdapter(itemInfoList));

                        mLayoutType = LayoutType.LINEAR;
                        break;
                }
                mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (mDisplayType == DisplayType.BY_APP) {
                    editor.putInt(HCFSMgmtUtils.PREF_APP_DISPLAY_LAYOUT, mLayoutType.ordinal());
                } else { // Display by file
                    editor.putInt(HCFSMgmtUtils.PREF_FILE_DISPLAY_LAYOUT, mLayoutType.ordinal());
                }
                editor.apply();
            }
        });
    }

    public void showContent() {
        if (!isRunOnCorrectSortType(mSortType)) {
            return;
        }

        mShowingContent = true;
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {

                ArrayList<ItemInfo> itemInfoList = null;
                switch (mDisplayType) {
                    case DisplayType.BY_APP:
                        itemInfoList = DisplayTypeFactory.getListOfInstalledApps(mContext, DisplayTypeFactory.APP_USER, mFilterByPin);
                        break;
                    case DisplayType.BY_FILE:
                        itemInfoList = DisplayTypeFactory.getListOfFileDirs(mContext, mCurrentFile, mFilterByPin);
                        break;
                }
                DisplayTypeFactory.sort(itemInfoList, mSortType);

                final ArrayList<ItemInfo> finalItemInfoList = itemInfoList;
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSectionedRecyclerViewAdapter.setSubAdapterItems(finalItemInfoList);
                        mSectionedRecyclerViewAdapter.notifySubAdapterDataSetChanged();
                        mShowingContent = false;

                        if (finalItemInfoList == null) {
                            return;
                        }

                        if (!finalItemInfoList.isEmpty()) {
                            mNoDataMsg.setVisibility(View.GONE);
                            return;
                        }

                        if (mFilterByPin) {
                            return;
                        }

                        switch (mDisplayType) {
                            case DisplayType.BY_APP:
                                mNoDataMsg.setText(R.string.app_file_hint_no_apps);
                                break;
                            case DisplayType.BY_FILE:
                                mNoDataMsg.setText(R.string.app_file_hint_no_files);
                                break;
                        }
                        mNoDataMsg.setVisibility(View.VISIBLE);
                    }
                });

            }
        });
    }

    private class GridRecyclerViewAdapter extends RecyclerView.Adapter<GridRecyclerViewAdapter.GridRecyclerViewHolder> {

        private ArrayList<ItemInfo> mItemInfoList;
        private LruCache<Integer, Drawable> mMemoryCache;
        private ThreadPoolExecutor mExecutor;

        private GridRecyclerViewAdapter() {
            mItemInfoList = new ArrayList<>();
            init();
        }

        private GridRecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
            this.mItemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
            init();
        }

        public void init() {
            mExecutor = ExecutorFactory.createThreadPoolExecutor();
            mMemoryCache = MemoryCacheFactory.createMemoryCache();
        }

        private void shutdownExecutor() {
            mExecutor.shutdownNow();
        }

        private ArrayList<ItemInfo> getItemInfoList() {
            return mItemInfoList;
        }

        private void clear() {
            if (mItemInfoList != null) {
                mItemInfoList.clear();
            }

            if (mMemoryCache != null) {
                mMemoryCache.evictAll();
            }
        }

        private void setItemData(@Nullable ArrayList<ItemInfo> items) {
            this.mItemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
        }

        @Override
        public int getItemCount() {
            return mItemInfoList.size();
        }

        @Override
        public void onBindViewHolder(GridRecyclerViewHolder holder, int position) {
            final ItemInfo itemInfo = mItemInfoList.get(position);
            itemInfo.setViewHolder(holder);
            holder.setItemInfo(itemInfo);
            holder.itemName.setText(itemInfo.getName());
            holder.rootView.setContentDescription(itemInfo.getName());

            displayItem(position, holder, mMemoryCache, mExecutor);
        }

        @Override
        public GridRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_file_grid_item, parent, false);
            return new GridRecyclerViewHolder(view);
        }

        class GridRecyclerViewHolder extends RecyclerViewHolder implements OnClickListener, OnLongClickListener {

            private View rootView;
            private ImageView iconView;
            private ImageView pinView;
            private TextView itemName;

            private GridRecyclerViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                iconView = (ImageView) itemView.findViewById(R.id.item_icon);
                pinView = (ImageView) itemView.findViewById(R.id.pin_view);
                itemName = (TextView) itemView.findViewById(R.id.item_name);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.gridItemLayout) {
                    onItemClick(this, itemInfo);
                }
            }

            @Override
            public void setIconBitmap(Bitmap bitmap) {
                iconView.setImageBitmap(bitmap);
            }

            @Override
            public void setIconDrawable(Drawable drawable) {
                iconView.setImageDrawable(drawable);
            }

            @Override
            public void setIconAlpha(int alpha) {
                iconView.setImageAlpha(alpha);
            }

            @Override
            ImageView getPinView() {
                return pinView;
            }

            @Override
            View getItemView() {
                return rootView;
            }

            @Override
            public boolean onLongClick(View v) {
                if (itemInfo instanceof FileInfo) {
                    FileDialogFragment dialogFragment = FileDialogFragment.newInstance();
                    dialogFragment.setViewHolder(this);
                    dialogFragment.show(getFragmentManager(), FileDialogFragment.TAG);
                    return true;
                }
                return false;
            }

        }

    }

    public class LinearRecyclerViewAdapter extends RecyclerView.Adapter<LinearRecyclerViewAdapter.LinearRecyclerViewHolder> {

        private ArrayList<ItemInfo> mItemInfoList;
        private ThreadPoolExecutor mExecutor;
        private LruCache<Integer, Drawable> mMemoryCache;

        private LinearRecyclerViewAdapter() {
            mItemInfoList = new ArrayList<>();
            init();
        }

        private LinearRecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
            this.mItemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
            init();
        }

        public void init() {
            mExecutor = ExecutorFactory.createThreadPoolExecutor();
            mMemoryCache = MemoryCacheFactory.createMemoryCache();
        }

        private void shutdownExecutor() {
            mExecutor.shutdownNow();
        }

        @Override
        public int getItemCount() {
            return mItemInfoList.size();
        }

        private ArrayList<ItemInfo> getItemInfoList() {
            return mItemInfoList;
        }

        @Override
        public void onBindViewHolder(final LinearRecyclerViewHolder holder, int position) {
            final ItemInfo itemInfo = mItemInfoList.get(position);
            itemInfo.setViewHolder(holder);
            holder.setItemInfo(itemInfo);
            holder.itemName.setText(itemInfo.getName());

            displayItem(position, holder, mMemoryCache, mExecutor);
        }

        @Nullable
        @Override
        public LinearRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Logs.d(CLASSNAME, "LinearRecyclerViewAdapter", "onCreateViewHolder");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_file_linear_item, parent, false);
            return new LinearRecyclerViewHolder(view);
        }

        private void setItemData(@Nullable ArrayList<ItemInfo> items) {
            this.mItemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
        }

        private void clear() {
            if (mItemInfoList != null) {
                mItemInfoList.clear();
            }

            if (mMemoryCache != null) {
                mMemoryCache.evictAll();
            }
        }

        class LinearRecyclerViewHolder extends RecyclerViewHolder implements OnClickListener, OnLongClickListener {

            private View rootView;
            private TextView itemName;
            private ImageView iconView;
            private ImageView pinView;

            private LinearRecyclerViewHolder(View itemView) {
                super(itemView);

                rootView = itemView;
                itemName = (TextView) itemView.findViewById(R.id.item_name);
                iconView = (ImageView) itemView.findViewById(R.id.item_icon);
                pinView = (ImageView) itemView.findViewById(R.id.pinView);

                pinView.setOnClickListener(this);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.pinView) {
                    itemInfo.setProcessing(true);

                    // Pin/unpin the selected item
                    boolean isPinned = !itemInfo.isPinned();
                    boolean allowPinUnpin = pinUnpinItem(isPinned);
                    if (allowPinUnpin) {
                        pinView.setImageDrawable(itemInfo.getPinViewImage(isPinned));
                    } else {
                        itemInfo.setProcessing(false);
                    }
                } else if (v.getId() == R.id.linearItemLayout) {
                    onItemClick(this, itemInfo);
                }
            }

            @Override
            public boolean onLongClick(View v) {
                if (itemInfo instanceof FileInfo) {
                    FileDialogFragment dialogFragment = FileDialogFragment.newInstance();
                    dialogFragment.setViewHolder(this);
                    dialogFragment.show(getFragmentManager(), FileDialogFragment.TAG);
                    return true;
                }
                return false;
            }

            @Override
            public void setIconBitmap(Bitmap bitmap) {
                iconView.setImageBitmap(bitmap);
            }

            @Override
            public void setIconDrawable(Drawable drawable) {
                iconView.setImageDrawable(drawable);
            }

            @Override
            public void setIconAlpha(int alpha) {
                iconView.setImageAlpha(alpha);
            }

            @Override
            ImageView getPinView() {
                return pinView;
            }

            @Override
            View getItemView() {
                return rootView;
            }

        }

    }

    public class SectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private boolean hasChildItem = true;
        private static final int SECTION_TYPE = 0;
        private RecyclerView.Adapter mBaseAdapter;
        private SparseArray<Section> mSections = new SparseArray<>();
        private boolean isFirstCircleAnimated = true;

        @SuppressWarnings("rawtypes")
        private SectionedRecyclerViewAdapter(RecyclerView.Adapter mBaseAdapter) {
            this.mBaseAdapter = mBaseAdapter;
            registerAdapterDataObserver(mBaseAdapter);
        }

        private void init() {
            Logs.d(CLASSNAME, "SectionedRecyclerViewAdapter", "init", null);
            isFirstCircleAnimated = true;
            shutdownSubAdapterExecutor();
            subAdapterInit();
        }

        private void subAdapterInit() {
            if (mBaseAdapter instanceof GridRecyclerViewAdapter) {
                ((GridRecyclerViewAdapter) mBaseAdapter).init();
            } else {
                ((LinearRecyclerViewAdapter) mBaseAdapter).init();
            }
        }

        private void notifySubAdapterDataSetChanged() {
            mBaseAdapter.notifyDataSetChanged();
        }

        private void shutdownSubAdapterExecutor() {
            if (mBaseAdapter instanceof GridRecyclerViewAdapter) {
                ((GridRecyclerViewAdapter) mBaseAdapter).shutdownExecutor();
            } else {
                ((LinearRecyclerViewAdapter) mBaseAdapter).shutdownExecutor();
            }
        }

        private ArrayList<ItemInfo> getSubAdapterItemInfoList() {
            ArrayList<ItemInfo> itemInfoList;
            if (mBaseAdapter instanceof GridRecyclerViewAdapter) {
                itemInfoList = ((GridRecyclerViewAdapter) mBaseAdapter).getItemInfoList();
            } else {
                itemInfoList = ((LinearRecyclerViewAdapter) mBaseAdapter).getItemInfoList();
            }
            return itemInfoList;
        }

        private void setSubAdapterItems(ArrayList<ItemInfo> itemInfoList) {
            if (mBaseAdapter instanceof GridRecyclerViewAdapter) {
                ((GridRecyclerViewAdapter) mBaseAdapter).setItemData(itemInfoList);
            } else {
                ((LinearRecyclerViewAdapter) mBaseAdapter).setItemData(itemInfoList);
            }
        }

        private void clearSubAdapter() {
            if (mBaseAdapter instanceof GridRecyclerViewAdapter) {
                ((GridRecyclerViewAdapter) mBaseAdapter).clear();
            } else {
                ((LinearRecyclerViewAdapter) mBaseAdapter).clear();
            }
        }

        @SuppressWarnings("rawtypes")
        private void registerAdapterDataObserver(final RecyclerView.Adapter mBaseAdapter) {
            mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    // if item count > 0 or mFilterByPin is true, show section and item list. Else,
                    // do not show action or item list.
                    hasChildItem = mBaseAdapter.getItemCount() > 0 || mFilterByPin;
                    notifyDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    // if item count > 0 or mFilterByPin is true, show section and item list. Else,
                    // do not show action or item list.
                    hasChildItem = mBaseAdapter.getItemCount() > 0 || mFilterByPin;
                    notifyItemRangeChanged(positionStart, itemCount);
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    // if item count > 0 or mFilterByPin is true, show section and item list. Else,
                    // do not show action or item list.
                    hasChildItem = mBaseAdapter.getItemCount() > 0 || mFilterByPin;
                    notifyItemRangeInserted(positionStart, itemCount);
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    // if item count > 0 or mFilterByPin is true, show section and item list. Else,
                    // do not show action or item list.
                    hasChildItem = mBaseAdapter.getItemCount() > 0 || mFilterByPin;
                    notifyItemRangeRemoved(positionStart, itemCount);
                }
            });
        }

        private void setGridLayoutManagerSpanSize() {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) (mRecyclerView.getLayoutManager());
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (isSectionHeaderPosition(position)) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }

        @Override
        public int getItemCount() {
            return (hasChildItem ? mSections.size() + mBaseAdapter.getItemCount() : 0);
        }

        private void updateSection(SectionedViewHolder sectionViewHolder, long totalSpace, long freeSpace, boolean showAnimation) {
            float toShowValue = ((totalSpace - freeSpace) * 1f / totalSpace) * 100f;
            CircleDisplay cd = sectionViewHolder.circleDisplay;
            cd.showValue(toShowValue, 100f, totalSpace, showAnimation);

            if (isSDCard1) {
                sectionViewHolder.totalSpace.setText(UnitConverter.convertByteToProperUnit(totalSpace));
                sectionViewHolder.freeSpace.setText(UnitConverter.convertByteToProperUnit(freeSpace));
            } else {
                sectionViewHolder.totalSpace.setText(UnitConverter.convertByteToProperUnit(totalSpace));
                sectionViewHolder.freeSpace.setText(UnitConverter.convertByteToProperUnit(freeSpace));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
            if (isSectionHeaderPosition(position)) {
                final SectionedViewHolder sectionViewHolder = (SectionedViewHolder) viewHolder;
//                mSections.get(position).viewHolder = sectionViewHolder;
                mWorkerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String storageType;
                                if (isSDCard1) {
                                    storageType = mContext.getString(R.string.app_file_sdcard1_storage_name);
                                } else {
                                    storageType = mContext.getString(R.string.app_file_internal_storage_name);
                                }
                                sectionViewHolder.storageType.setText(storageType);
                            }
                        });

                        final HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (hcfsStatInfo != null) {
                                    long totalSpace = hcfsStatInfo.getTeraTotal();
                                    long freeSpace = totalSpace - hcfsStatInfo.getTeraUsed();
                                    updateSection(sectionViewHolder, totalSpace, freeSpace, isFirstCircleAnimated);
                                }
                                if (isFirstCircleAnimated) {
                                    isFirstCircleAnimated = false;
                                }
                            }
                        });
                    }
                });
            } else {
                mBaseAdapter.onBindViewHolder(viewHolder, sectionedPositionToPosition(position));
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
            if (typeView == SECTION_TYPE) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.app_file_section_item, parent, false);
                return new SectionedViewHolder(view);
            } else {
                return mBaseAdapter.onCreateViewHolder(parent, typeView - 1);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return isSectionHeaderPosition(position) ? SECTION_TYPE : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1;
        }

        @Override
        public long getItemId(int position) {
            return isSectionHeaderPosition(position) ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                    : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
        }

        private void setBaseAdapter(RecyclerView.Adapter mBaseAdapter) {
            this.mBaseAdapter = mBaseAdapter;
            registerAdapterDataObserver(mBaseAdapter);
        }

        public SparseArray<Section> getSections() {
            return mSections;
        }

        private void setSections(Section[] sections) {
            mSections.clear();

            Arrays.sort(sections, new Comparator<Section>() {
                @Override
                public int compare(Section o, Section o1) {
                    return (o.firstPosition == o1.firstPosition) ? 0 : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
                }
            });

            // offset positions for the headers we're adding
            int offset = 0;
            for (Section section : sections) {
                section.sectionedPosition = section.firstPosition + offset;
                mSections.append(section.sectionedPosition, section);
                ++offset;
            }

        }

        private boolean isSectionHeaderPosition(int position) {
            return mSections.get(position) != null;
        }

        private int sectionedPositionToPosition(int sectionedPosition) {
            if (isSectionHeaderPosition(sectionedPosition)) {
                return RecyclerView.NO_POSITION;
            }

            int offset = 0;
            for (int i = 0; i < mSections.size(); i++) {
                if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                    break;
                }
                --offset;
            }

            return sectionedPosition + offset;
        }

        private class SectionedViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

            private CircleDisplay circleDisplay;
            private TextView storageType;
            private TextView totalSpace;
            private TextView freeSpace;
            private LinearLayout allItem;
            private LinearLayout pinnedItem;
            private ImageView allItemImage;
            private ImageView pinnedItemImage;
            private TextView allItemText;
            private TextView pinnedItemText;

            private SectionedViewHolder(View itemView) {
                super(itemView);
                circleDisplay = (CircleDisplay) itemView.findViewById(R.id.circle_display_view);
                storageType = (TextView) itemView.findViewById(R.id.storage_type);
                totalSpace = (TextView) itemView.findViewById(R.id.total_space);
                freeSpace = (TextView) itemView.findViewById(R.id.free_space);
                allItem = (LinearLayout) itemView.findViewById(R.id.all_item);
                pinnedItem = (LinearLayout) itemView.findViewById(R.id.pinned_item);
                allItemImage = (ImageView) allItem.findViewById(R.id.all_item_img);
                pinnedItemImage = (ImageView) pinnedItem.findViewById(R.id.pinned_item_img);
                allItemText = (TextView) allItem.findViewById(R.id.all_item_text);
                pinnedItemText = (TextView) pinnedItem.findViewById(R.id.pinned_item_text);

                switch (mDisplayType) {
                    case DisplayType.BY_APP:
                        allItemText.setText(R.string.app_file_section_item_all_apps);
                        pinnedItemText.setText(R.string.app_file_section_item_all_pinned_apps);
                        break;
                    case DisplayType.BY_FILE:
                        allItemText.setText(R.string.app_file_section_item_all_files);
                        pinnedItemText.setText(R.string.app_file_section_item_all_pinned_files);
                        break;
                }

                allItem.setOnClickListener(this);
                pinnedItem.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Drawable unSelectedDrawable = ContextCompat.getDrawable(mContext, R.drawable.icon_btn_unselected);
                allItemImage.setImageDrawable(unSelectedDrawable);
                pinnedItemImage.setImageDrawable(unSelectedDrawable);

                Drawable selectedDrawable = ContextCompat.getDrawable(mContext, R.drawable.icon_btn_selected);
                switch (v.getId()) {
                    case R.id.all_item:
                        allItemImage.setImageDrawable(selectedDrawable);
                        mFilterByPin = false;
                        break;
                    case R.id.pinned_item:
                        pinnedItemImage.setImageDrawable(selectedDrawable);
                        mFilterByPin = true;
                        break;
                }
                showContent();
            }

        }

    }

    public class Section {

        private int firstPosition;
        private int sectionedPosition;

        public Section(int firstPosition) {
            this.firstPosition = firstPosition;
        }

    }

    public class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{android.R.attr.listDivider};
        private static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
        private static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
        private Drawable mDivider;
        private int mOrientation;

        private DividerItemDecoration(Context context, int orientation) {
            final TypedArray typedArray = context.obtainStyledAttributes(ATTRS);
            mDivider = typedArray.getDrawable(0);
            typedArray.recycle();
            setOrientation(orientation);
        }

        public void setOrientation(int orientation) {
            if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
                throw new IllegalArgumentException("Invalid orientation");
            }
            mOrientation = orientation;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }

        private void drawVertical(Canvas c, RecyclerView parent) {
            boolean isGridLayout = false;
            if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
                isGridLayout = true;
            }

            final int left = parent.getPaddingLeft();
            final int right = parent.getWidth() - parent.getPaddingRight();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                final int top = child.getBottom() + params.bottomMargin + Math.round(ViewCompat.getTranslationY(child));
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);

                // Only draw divider in the storage usage section item which is the first item
                if (isGridLayout) {
                    break;
                }
            }
        }

        private void drawHorizontal(Canvas c, RecyclerView parent) {
            final int top = parent.getPaddingTop();
            final int bottom = parent.getHeight() - parent.getPaddingBottom();

            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                final int left = child.getRight() + params.rightMargin + Math.round(ViewCompat.getTranslationX(child));
                final int right = left + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (mOrientation == VERTICAL_LIST) {
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logs.d(CLASSNAME, "onDestroyView", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onDestroy", null);

        // Interrupt the threads in the threading pool of executor
        mSectionedRecyclerViewAdapter.shutdownSubAdapterExecutor();

        // Interrupt mHandlerThread
        mHandlerThread.quit();
        mHandlerThread.interrupt();

        // Unregister mAddRemovePackageReceiver
        if (mAddRemovePackageReceiver != null) {
            mAddRemovePackageReceiver.unregisterReceiver(mContext);
        }

    }

    public class FilePathNavigationView extends TextView implements OnClickListener {

        private String currentFilePath;
        private int firstVisibleItemPosition;

        public FilePathNavigationView(Context context) {
            super(context);

            int[] attrs = new int[]{android.R.attr.selectableItemBackground};
            TypedArray typedArray = context.obtainStyledAttributes(attrs);
            Drawable drawableFromTheme = typedArray.getDrawable(0);
            typedArray.recycle();
            setBackground(drawableFromTheme);

            int padding = (int) getResources().getDimension(R.dimen.fragment_vertical_padding);
            setPadding(0, padding, 0, padding);

            setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int startIndex = mFilePathNavigationLayout.indexOfChild(this) + 1;
            int childCount = mFilePathNavigationLayout.getChildCount();
            mFilePathNavigationLayout.removeViews(startIndex, childCount - startIndex);

            mCurrentFile = new File(currentFilePath);
            showContent();

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRecyclerView.getLayoutManager().scrollToPosition(firstVisibleItemPosition);
                }
            });
        }

        public String getCurrentFilePath() {
            return currentFilePath;
        }

        public void setCurrentFilePath(String currentFilePath) {
            this.currentFilePath = currentFilePath;
        }

        public int getFirstVisibleItemPosition() {
            return firstVisibleItemPosition;
        }

        public void setFirstVisibleItemPosition(int firstVisibleItemPosition) {
            this.firstVisibleItemPosition = firstVisibleItemPosition;
        }

    }

    public boolean onBackPressed() {
        boolean isProcessed = false;
        if (mDisplayType == DisplayType.BY_FILE) {
            if (!mCurrentFile.getAbsolutePath().equals(mFileRootDirPath)) {
                View view = getView();
                if (view != null) {
                    mNoDataMsg.setVisibility(View.GONE);
                }

                mCurrentFile = mCurrentFile.getParentFile();
                int childCount = mFilePathNavigationLayout.getChildCount();
                FilePathNavigationView filePathNavigationView = (FilePathNavigationView) mFilePathNavigationLayout.getChildAt(childCount - 2);
                final int firstVisibleItemPosition = filePathNavigationView.firstVisibleItemPosition;
                mFilePathNavigationLayout.removeViewAt(childCount - 1);
                showContent();

                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.scrollToPosition(firstVisibleItemPosition);
                    }
                });

                isProcessed = true;
            }
        }
        return isProcessed;
    }

    public void setSDCard1(boolean isSDCard1) {
        this.isSDCard1 = isSDCard1;
    }

    private boolean isPositionVisible(int position) {
        int firstVisibleItemPosition;
        int lastVisibleItemPosition;
        try {
            firstVisibleItemPosition = findRecyclerViewFirstVisibleItemPosition();
            lastVisibleItemPosition = findRecyclerViewLastVisibleItemPosition();
        } catch (NullPointerException e) {
            firstVisibleItemPosition = lastVisibleItemPosition = position;
            Logs.e(CLASSNAME, "isPositionVisible", Log.getStackTraceString(e));
        }

        if (mRecyclerViewScrollDown) {
            if (position >= firstVisibleItemPosition - 4) {
                return true;
            }
        } else {
            if (position <= lastVisibleItemPosition + 4) {
                return true;
            }
        }
        return false;
    }

    private void notifyRecyclerViewItemChanged() {
        try {
            if (mSectionedRecyclerViewAdapter != null) {
                int firstVisiblePosition = findRecyclerViewFirstVisibleItemPosition();
                int lastVisiblePosition = findRecyclerViewLastVisibleItemPosition();
                final SectionedRecyclerViewAdapter adapter = (SectionedRecyclerViewAdapter) mRecyclerView.getAdapter();
                for (int pos = firstVisiblePosition; pos < lastVisiblePosition + 1; pos++) {
                    adapter.notifyItemChanged(pos);
                }
            }
        } catch (IllegalStateException e) {
            Logs.w(CLASSNAME, "notifyRecyclerViewItemChanged", Log.getStackTraceString(e));
        }
    }

    private int findRecyclerViewFirstVisibleItemPosition() throws NullPointerException {
        int firstVisibleItemPosition;
        LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            firstVisibleItemPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else {
            firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        return firstVisibleItemPosition;
    }

    private int findRecyclerViewLastVisibleItemPosition() throws NullPointerException {
        int lastVisibleItemPosition;
        LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
        } else {
            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }
        return lastVisibleItemPosition;
    }

    /**
     * For quick switch between different types
     */
    private boolean isRunOnCorrectSortType(int sortType) {
        String sortTypeName;
        switch (sortType) {
            case SortType.BY_NAME:
                sortTypeName = mContext.getString(R.string.app_file_spinner_sort_by_name);
                break;
            case SortType.BY_INSTALLED_TIME:
                sortTypeName = mContext.getString(R.string.app_file_spinner_sort_by_installed_time);
                break;
            case SortType.BY_MODIFIED_TIME:
                sortTypeName = mContext.getString(R.string.app_file_spinner_sort_by_modified_time);
                break;
            case SortType.BY_SIZE:
                sortTypeName = mContext.getString(R.string.app_file_spinner_sort_by_size);
                break;
            default:
                return false;
        }
        return mSpinner.getSelectedItem().toString().equals(sortTypeName);
    }

//    private String getPinViewContentDescription(boolean isPinned, int location) {
//        String contentDescription = "-1";
//        if (isPinned && location == LocationStatus.LOCAL) {
//            contentDescription = "0";
//        } else if (isPinned && (location == LocationStatus.HYBRID || location == LocationStatus.CLOUD)) {
//            contentDescription = "1";
//        } else if (!isPinned && location == LocationStatus.LOCAL) {
//            contentDescription = "2";
//        } else if (!isPinned && location == LocationStatus.HYBRID) {
//            contentDescription = "3";
//        } else if (!isPinned && location == LocationStatus.CLOUD) {
//            contentDescription = "4";
//        }
//        return contentDescription;
//    }

    private String getPinViewContentDescription(boolean isPinned) {
        String contentDescription;
        if (isPinned) {
            contentDescription = "1";
        } else {
            contentDescription = "0";
        }
        return contentDescription;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if (menuVisible) {
            mFragmentVisible = true;
            if (mApiExecutorThread == null) {
                mApiExecutorThread = new Thread(mApiExecutorRunnable);
                mApiExecutorThread.start();
            }

            if (mAutoUiRefreshThread == null) {
                mAutoUiRefreshThread = new Thread(mAutoUiRefreshRunnable);
                mAutoUiRefreshThread.start();
            }

            if (mProcessPinThread == null) {
                mProcessPinThread = new Thread(mCheckPinStatusRunnable);
                mProcessPinThread.start();
            }

            // Request READ_EXTERNAL_STORAGE_PERMISSION
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getString(R.string.alert_dialog_title_warning));
                    builder.setMessage(getString(R.string.require_read_external_storage_permission));
                    builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) mContext,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    RequestCode.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                } else {
                    mSnackbar.setText(R.string.require_read_external_storage_permission);
                    mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                    mSnackbar.setAction(R.string.go, mPermissionListener);
                    mSnackbar.show();
                }
            }

            mWorkerHandler.post(new Runnable() {
                @Override
                public void run() {
                    SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
                    SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_ALLOW_PIN_UNPIN_APPS);
                    if (settingsInfo != null) {
                        mAllowPinUnpinApps = Boolean.valueOf(settingsInfo.getValue());
                    }
                }
            });
        } else {
            mFragmentVisible = false;
            if (mApiExecutorThread != null && !mApiExecutorThread.isInterrupted()) {
                mApiExecutorThread.interrupt();
                mApiExecutorThread = null;
            }

            if (mAutoUiRefreshThread != null && !mAutoUiRefreshThread.isInterrupted()) {
                mAutoUiRefreshThread.interrupt();
                mAutoUiRefreshThread = null;
            }

            if (mProcessPinThread != null && !mProcessPinThread.isInterrupted()) {
                mProcessPinThread.interrupt();
                mProcessPinThread = null;
            }

            if (mSnackbar != null) {
                mSnackbar.dismiss();
            }

        }

    }

    public class AddRemovePackageReceiver extends BroadcastReceiver {

        private boolean isRegister = false;

        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            Logs.d(CLASSNAME, "onReceive", "action=" + action);
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                boolean isDataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (!isDataRemoved || isReplacing || mSpinner == null || mDisplayType != DisplayType.BY_APP) {
                    return;
                }

                String packageName = intent.getData().getSchemeSpecificPart();
                ArrayList<ItemInfo> itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                for (int i = 0; i < itemInfoList.size(); i++) {
                    ItemInfo itemInfo = itemInfoList.get(i);
                    if (!(itemInfo instanceof AppInfo)) {
                        return;
                    }

                    AppInfo appInfo = (AppInfo) itemInfo;
                    if (appInfo.getPackageName().equals(packageName)) {
                        itemInfoList.remove(i);
                        mSectionedRecyclerViewAdapter.notifyItemRemoved(i + 1);
                        break;
                    }
                }
            } else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (isReplacing || mSpinner == null || mDisplayType != DisplayType.BY_APP) {
                    return;
                }

                mWorkerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        AppInfo appInfo = null;
                        try {
                            PackageManager pm = context.getPackageManager();
                            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                            appInfo = new AppInfo(context);
                            appInfo.setUid(applicationInfo.uid);
                            appInfo.setApplicationInfo(applicationInfo);
                            appInfo.setName(applicationInfo.loadLabel(pm).toString());
                        } catch (PackageManager.NameNotFoundException e) {
                            Logs.e(CLASSNAME, "AddRemovePackageBroadcastReceiver", "onReceive", Log.getStackTraceString(e));
                        }

                        final AppInfo finalAppInfo = appInfo;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalAppInfo == null) {
                                    return;
                                }

                                ArrayList<ItemInfo> itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                                itemInfoList.add(0, finalAppInfo);
                                mSectionedRecyclerViewAdapter.setSubAdapterItems(itemInfoList);
                                mSectionedRecyclerViewAdapter.notifyItemInserted(1);
                            }
                        });
                    }
                });
            }
        }

        public void registerReceiver(Context context, IntentFilter
                intentFilter) {
            if (!isRegister) {
                if (context != null) {
                    context.registerReceiver(this, intentFilter);
                }
                isRegister = true;
            }
        }

        public void unregisterReceiver(Context context) {
            if (isRegister) {
                if (context != null) {
                    context.unregisterReceiver(this);
                    isRegister = false;
                }
            }
        }

    }

    private Drawable adjustImageSaturation(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
        return adjustImageSaturation(drawable);
    }

    private Drawable adjustImageSaturation(Drawable drawable) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.5f);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        drawable.setColorFilter(filter);
        return drawable;
    }

    private boolean isItemPinned(ItemInfo itemInfo) {
        // Get the pinned/unpinned status of item
        boolean isPinned = false;
        if (itemInfo instanceof AppInfo) {
            AppInfo appInfo = (AppInfo) itemInfo;
            isPinned = HCFSMgmtUtils.isAppPinned(mContext, appInfo);
        } else if (itemInfo instanceof DataTypeInfo) {
            // The pin status of DataTypeInfo has got in getListOfDataType()
            isPinned = itemInfo.isPinned();
        } else if (itemInfo instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) itemInfo;
            isPinned = HCFSMgmtUtils.isPathPinned(fileInfo.getFilePath());
        }
        return isPinned;
    }

    private void onItemClick(RecyclerViewHolder holder, ItemInfo itemInfo) {
        if (itemInfo instanceof AppInfo) {
            Bundle args = new Bundle();
            args.putBoolean(KEY_ARGUMENT_ALLOW_PIN_UNPIN_APPS, mAllowPinUnpinApps);

            AppDialogFragment dialogFragment = AppDialogFragment.newInstance();
            dialogFragment.setArguments(args);
            dialogFragment.setViewHolder(itemInfo.getViewHolder());
            dialogFragment.show(getFragmentManager(), AppDialogFragment.TAG);
        } else if (itemInfo instanceof DataTypeInfo) {

        } else if (itemInfo instanceof FileInfo) {
            final FileInfo fileInfo = (FileInfo) itemInfo;
            if (fileInfo.isDirectory()) {
                // Set the first visible item position to previous FilePathNavigationView when navigating to next directory level
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int childCount = mFilePathNavigationLayout.getChildCount();
                FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) mFilePathNavigationLayout
                        .getChildAt(childCount - 1);
                filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);

                // Add the current FilePathNavigationView to navigation layout
                mCurrentFile = new File(fileInfo.getFilePath());
                FilePathNavigationView filePathNavigationView = new FilePathNavigationView(mContext);
                String currentPath = mCurrentFile.getAbsolutePath();
                String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
                filePathNavigationView.setText(Html.fromHtml("<u>" + navigationText + "</u>"));
                filePathNavigationView.setCurrentFilePath(currentPath);
                mFilePathNavigationLayout.addView(filePathNavigationView);
                mFilePathNavigationScrollView.post(new Runnable() {
                    public void run() {
                        mFilePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
                    }
                });

                // Show the file list of the entered directory
                showContent();

                // Disable the item view to prevent user click many times at the same time
                holder.getItemView().setEnabled(false);
            } else {
                // Build the intent
                String mimeType = fileInfo.getMimeType();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(fileInfo.getFilePath());
                Logs.d(CLASSNAME, "onItemClick", "Uri.fromFile(file)=" + Uri.fromFile(file));
                if (mimeType != null) {
                    intent.setDataAndType(Uri.fromFile(file), mimeType);
                } else {
                    intent.setData(Uri.fromFile(file));
                }

                // Verify it resolves
                PackageManager packageManager = mContext.getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;

                // Start an activity if it's safe
                if (isIntentSafe) {
                    startActivity(intent);
                } else {
                    View view = getView();
                    if (view != null) {
                        Snackbar.make(view, mContext.getString(R.string.app_file_snackbar_unknown_type_file), Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public abstract class RecyclerViewHolder extends RecyclerView.ViewHolder {

        ItemInfo itemInfo;

        private RecyclerViewHolder(View itemView) {
            super(itemView);
        }

        void setItemInfo(ItemInfo itemInfo) {
            this.itemInfo = itemInfo;
        }

        ItemInfo getItemInfo() {
            return this.itemInfo;
        }

        boolean pinUnpinItem(final boolean isPinned) {
            boolean allowPinUnpin = true;
            itemInfo.setPinned(isPinned);
            mPinStatusSparseArr.put(itemInfo.hashCode(), isPinned);
            if (itemInfo instanceof AppInfo) {
                showProgress();

                // Before unpin apps, check minimal apk is exist or not. If not, not allow user to
                // unpin this app.
                final AppInfo appInfo = (AppInfo) itemInfo;
                if (!isPinned) {
                    int messageResId = 0;
                    int code = HCFSMgmtUtils.checkMinimalApk(mContext, appInfo.getPackageName(), true);
                    switch (code) {
                        case 0: // minimal apk is not exist
                            HCFSMgmtUtils.createMinimalApk(mContext, appInfo.getPackageName(), false);
                        case -1: // unknown error
                        case 2: // create minimal apk in progress
                            allowPinUnpin = false;
                            messageResId = R.string.app_file_cannot_unpin_apps;
                            break;
                        case 1: // minimal apk is exist
                            break;
                    }

                    if (!allowPinUnpin) {
                        itemInfo.setPinned(!isPinned);

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(appInfo.getName());
                        builder.setMessage(mContext.getString(messageResId));
                        builder.setPositiveButton(mContext.getString(R.string.confirm), null);
                        builder.setCancelable(false);
                        builder.show();
                        return allowPinUnpin;
                    }
                }

                appInfo.setLastProcessTime(System.currentTimeMillis());
                mPinUnpinAppMap.put(appInfo.getPackageName(), appInfo);
                try {
                    // The subject to clone AppInfo is in order to keep the pin status.
                    mWaitToExecuteSparseArr.put(appInfo.hashCode(), (AppInfo) appInfo.clone());
                } catch (CloneNotSupportedException e) {
                    Logs.e(CLASSNAME, "pinUnpinItem", Log.getStackTraceString(e));
                }
            } else if (itemInfo instanceof FileInfo) {
                final FileInfo fileInfo = (FileInfo) itemInfo;
                boolean isNeedToProcess = true;
                if (!isPinned) {
                    if (fileInfo.getFilePath().contains(EXTERNAL_ANDROID_PATH)) {
                        isNeedToProcess = false;

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(fileInfo.getName());
                        builder.setMessage(mContext.getString(R.string.app_file_cannot_unpin_files_in_android_folder));
                        builder.setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    }
                }

                if (isNeedToProcess) {
                    showProgress();
                    fileInfo.setLastProcessTime(System.currentTimeMillis());
                    mPinUnpinFileMap.put(fileInfo.getFilePath(), isPinned);
                    try {
                        // The subject to clone AppInfo is in order to keep the pin status.
                        mWaitToExecuteSparseArr.put(fileInfo.hashCode(), (FileInfo) fileInfo.clone());
                    } catch (CloneNotSupportedException e) {
                        Logs.e(CLASSNAME, "pinUnpinItem", Log.getStackTraceString(e));
                    }
                } else {
                    fileInfo.setPinned(!isPinned);
                    mPinStatusSparseArr.put(itemInfo.hashCode(), !isPinned);
                    allowPinUnpin = false;
                }

            }
            return allowPinUnpin;
        }

        abstract void setIconBitmap(Bitmap bitmap);

        abstract void setIconDrawable(Drawable drawable);

        abstract void setIconAlpha(int alpha);

        abstract ImageView getPinView();

        abstract View getItemView();

    }

    private void displayPinView(RecyclerViewHolder holder, ItemInfo itemInfo) {
        ImageView pinView = holder.getPinView();

        if (!mAllowPinUnpinApps && itemInfo instanceof AppInfo) {
            pinView.setVisibility(View.GONE);
            return;
        }

        boolean isDirectory = false;
        if (itemInfo instanceof FileInfo) {
            isDirectory = ((FileInfo) itemInfo).isDirectory();
        }

        if (isDirectory) {
            pinView.setVisibility(View.GONE);
        } else {
            // Display pinned/unpinned item image
            pinView.setVisibility(View.VISIBLE);
            pinView.setImageDrawable(itemInfo.getPinViewImage(itemInfo.isPinned()));
            pinView.setContentDescription(getPinViewContentDescription(itemInfo.isPinned()));
        }
    }

    private void displayItem(final int position,
                             final RecyclerViewHolder holder,
                             final LruCache<Integer, Drawable> memoryCache,
                             ThreadPoolExecutor executor) {

        final ItemInfo itemInfo = holder.getItemInfo();
        holder.getItemView().setEnabled(true);
        if (holder.getItemInfo().hashCode() != itemInfo.hashCode() || itemInfo.isProcessing()) {
            return;
        }

        // Display cached item icon or set default icon if icon is not cached.
        final Drawable cacheDrawable = memoryCache.get(itemInfo.hashCode());
        if (cacheDrawable == null) {
            holder.setIconDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default_gray));
        } else {
            holder.setIconDrawable(cacheDrawable);
        }

        // Display cached pin view image or hide pin view by default if not cached.
        Boolean isPinned = mPinStatusSparseArr.get(itemInfo.hashCode());
        if (isPinned == null) {
            holder.getPinView().setVisibility(View.GONE);
        } else {
            // Set the cached pin status to a clone item info to prevent from changing the real pin
            // status.
            ItemInfo cloneItem = cloneItemInfo(itemInfo);
            cloneItem.setPinned(isPinned);
            displayPinView(holder, cloneItem);
        }

        // Get latest image of item icon and pin view
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (holder.getItemInfo().hashCode() != itemInfo.hashCode() || itemInfo.isProcessing()) {
                    return;
                }

                final int alpha = itemInfo.getIconAlpha();
                final boolean isPinned = isItemPinned(itemInfo);
                itemInfo.setPinned(isPinned);
                itemInfo.setPosition(position);

                // Display image of item icon
                Drawable drawable;
                if (alpha == ItemInfo.ICON_TRANSPARENT) {
                    if (cacheDrawable == null) {
                        drawable = itemInfo.getIconDrawable();
                    } else {
                        drawable = cacheDrawable;
                    }
                    drawable = adjustImageSaturation(drawable);
                } else { // ItemInfo.ICON_COLORFUL
                    if (cacheDrawable != null) {
                        if (cacheDrawable.getAlpha() == ItemInfo.ICON_TRANSPARENT) {
                            // The cache drawable is transparent, so we need to get colorful
                            // drawable.
                            drawable = itemInfo.getIconDrawable();
                        } else { // ItemInfo.ICON_COLORFUL
                            drawable = cacheDrawable;
                        }
                    } else {
                        drawable = itemInfo.getIconDrawable();
                    }
                }

                final Drawable iconDrawable = drawable;
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (holder.getItemInfo().hashCode() != itemInfo.hashCode() || itemInfo.isProcessing()) {
                            return;
                        }

                        holder.setIconAlpha(alpha);
                        holder.setIconDrawable(iconDrawable);
                        memoryCache.put(itemInfo.hashCode(), iconDrawable);

                        // Display image of item pin view
                        displayPinView(holder, itemInfo);
                        mPinStatusSparseArr.put(itemInfo.hashCode(), isPinned);
                    }
                });
            }
        });

    }

    private ItemInfo cloneItemInfo(ItemInfo itemInfo) {
        if (itemInfo instanceof AppInfo) {
            try {
                itemInfo = (AppInfo) ((AppInfo) itemInfo).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else if (itemInfo instanceof FileInfo) {
            try {
                itemInfo = (FileInfo) ((FileInfo) itemInfo).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return itemInfo;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case RequestCode.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        mSnackbar.setText(R.string.require_read_external_storage_permission);
                        mSnackbar.setDuration(Snackbar.LENGTH_LONG);
                        mSnackbar.setAction(null, null);
                    } else {
                        mSnackbar.setText(R.string.require_read_external_storage_permission);
                        mSnackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
                        mSnackbar.setAction(R.string.go, mPermissionListener);
                    }
                    mSnackbar.show();
                }
                break;
        }

    }

}
