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
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
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
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IMgmtBinder;
import com.hopebaytech.hcfsmgmt.interfaces.IPinUnpinListener;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.DisplayTypeFactory;
import com.hopebaytech.hcfsmgmt.utils.ExecutorFactory;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MemoryCacheFactory;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class FileMgmtFragment extends Fragment {

    public static final String TAG = FileMgmtFragment.class.getSimpleName();
    private static final String KEY_ARGUMENT_IS_SDCARD1 = "key_argument_is_sdcard1";
    private static final String KEY_ARGUMENT_SDCARD1_PATH = "key_argument_sdcard1_path";
    private final String CLASSNAME = getClass().getSimpleName();
    private final String EXTERNAL_ANDROID_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
    private final int GRID_LAYOUT_SPAN_COUNT = 3;

    private View mView;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressCircle;
    private LinearLayout mEmptyFolder;
    private Spinner mSpinner;
    private ImageView mRefresh;
    private ImageView mLayoutType;
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
    private Handler mUiHandler;

    private SparseArray<ItemInfo> mWaitToExecuteSparseArr;
    private Map<String, Boolean> mPinUnpinFileMap;
    private Map<String, AppInfo> mPinUnpinAppMap;
    private Map<String, Boolean> mPinUnpinTypeMap;
    private String FILE_ROOT_DIR_PATH;
    private String mFileRootDirName;
    private boolean isSDCard1 = false;
    private boolean mRecyclerViewScrollDown;
    private boolean mCurrentVisible;
    private boolean mServiceBound;
    private TeraMgmtService mMgmtService;
    private DISPLAY_TYPE mDisplayType;
    //    private ExternalStorageObserver mExternalStorageObserver;

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

    private enum DISPLAY_TYPE {
        GRID, LINEAR
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
//    private Map<String, Boolean> mDataTypePinStatusMap = new HashMap<>();

    private Runnable mAutoUiRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(Interval.AUTO_REFRESH_UI);
                    if (mProgressCircle.getVisibility() == View.GONE &&
                            mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE &&
                            mWaitToExecuteSparseArr.size() == 0 &&
                            mPinUnpinAppMap.size() == 0 &&
                            mPinUnpinFileMap.size() == 0) {
                        notifyRecyclerViewItemChanged();
                    }
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
                    if (mSectionedRecyclerViewAdapter != null && mWaitToExecuteSparseArr.size() == 0) {
                        try {
                            boolean isProcessDone = true;
                            String selectedItemName = mSpinner.getSelectedItem().toString();
                            if (selectedItemName.equals(getString(R.string.file_mgmt_spinner_apps))) {
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
                            } else if (selectedItemName.equals(getString(R.string.file_mgmt_spinner_data_type))) {
                                // Check pin/unpin process finished in display-by-files page
//                                for (String dataType : mPinUnpinTypeMap.keySet()) {
//                                    boolean isExpectedPinned = mPinUnpinTypeMap.get(dataType);
//                                    mPinUnpinTypeMap.remove(dataType);
//                                    needToProcessPin = true;
//                                }
                            } else if (selectedItemName.equals(getString(R.string.file_mgmt_spinner_files))) {
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
                            } else {
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
            } else if (itemInfo instanceof FileDirInfo) {
                mPinUnpinFileMap.remove(((FileDirInfo) itemInfo).getFilePath());
            }

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    FileMgmtFileDirDialogFragment fileDialogFragment = (FileMgmtFileDirDialogFragment)
                            getFragmentManager().findFragmentByTag(FileMgmtFileDirDialogFragment.TAG);
                    FileMgmtAppDialogFragment appDialogFragment = (FileMgmtAppDialogFragment)
                            getFragmentManager().findFragmentByTag(FileMgmtAppDialogFragment.TAG);
                    if (fileDialogFragment != null || appDialogFragment != null) {
                        ImageView pinIcon;
                        if (fileDialogFragment != null) {
                            pinIcon = (ImageView) fileDialogFragment.getDialog().findViewById(R.id.file_dir_pin_icon);
                        } else {
                            pinIcon = (ImageView) appDialogFragment.getDialog().findViewById(R.id.app_pin_icon);
                        }
                        boolean isPinned = itemInfo.isPinned();
                        pinIcon.setImageDrawable(itemInfo.getPinUnpinImage(isPinned));
                        pinIcon.setContentDescription(getPinViewContentDescription(isPinned));
                    } else {
                        if (itemInfo.getViewHolder() instanceof LinearRecyclerViewAdapter.LinearRecyclerViewHolder) {
                            boolean isPinned = itemInfo.isPinned();
                            LinearRecyclerViewAdapter.LinearRecyclerViewHolder holder =
                                    (LinearRecyclerViewAdapter.LinearRecyclerViewHolder) itemInfo.getViewHolder();
                            holder.pinView.setImageDrawable(itemInfo.getPinUnpinImage(isPinned));
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
                final ItemInfo itemInfo = mWaitToExecuteSparseArr.get(key);
                long lastProcessTime = itemInfo.getLastProcessTime();
                if (itemInfo instanceof AppInfo) {
                    final UidDAO uidDAO = UidDAO.getInstance(mContext);
                    AppInfo appInfo = (AppInfo) itemInfo;
                    UidInfo uidInfo = new UidInfo(appInfo);
                    uidDAO.update(uidInfo, UidDAO.PIN_STATUS_COLUMN);

                    mMgmtService.pinOrUnpinApp((AppInfo) itemInfo, new IPinUnpinListener() {
                        @Override
                        public void onPinUnpinSuccessful(final ItemInfo itemInfo) {
                            showPinUnpinResultToast(true /* isSuccess */, itemInfo.isPinned());
                        }

                        @Override
                        public void onPinUnpinFailed(final ItemInfo itemInfo) {
                            showPinUnpinResultToast(false /* isSuccess */, itemInfo.isPinned());

                            mSectionedRecyclerViewAdapter.
                                    getSubAdapterItemInfoList().
                                    get(itemInfo.getPosition()).
                                    setPinned(!itemInfo.isPinned());

                            itemInfo.setPinned(!itemInfo.isPinned());

                            // Update pin status to uid.db
                            UidInfo uidInfo = new UidInfo((AppInfo) itemInfo);
                            uidDAO.update(uidInfo, UidDAO.PIN_STATUS_COLUMN);

                            processPinUnpinFailed(itemInfo);
                        }
                    });
                } else if (itemInfo instanceof DataTypeInfo) {
                    // Nothing to do here
                } else if (itemInfo instanceof FileDirInfo) {
                    FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                    mMgmtService.pinOrUnpinFileDirectory(fileDirInfo, new IPinUnpinListener() {
                        @Override
                        public void onPinUnpinSuccessful(final ItemInfo itemInfo) {
                            showPinUnpinResultToast(true /* isSuccess */, itemInfo.isPinned());
                        }

                        @Override
                        public void onPinUnpinFailed(final ItemInfo itemInfo) {
                            showPinUnpinResultToast(false /* isSuccess */, itemInfo.isPinned());

                            mSectionedRecyclerViewAdapter.
                                    getSubAdapterItemInfoList().
                                    get(itemInfo.getPosition()).
                                    setPinned(!itemInfo.isPinned());

                            itemInfo.setPinned(!itemInfo.isPinned());
                            processPinUnpinFailed(itemInfo);
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
                FileMgmtFileDirDialogFragment fileDialogFragment = (FileMgmtFileDirDialogFragment)
                        getFragmentManager().findFragmentByTag(FileMgmtFileDirDialogFragment.TAG);
                FileMgmtAppDialogFragment appDialogFragment = (FileMgmtAppDialogFragment)
                        getFragmentManager().findFragmentByTag(FileMgmtAppDialogFragment.TAG);
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
                FileMgmtFileDirDialogFragment fileDialogFragment = (FileMgmtFileDirDialogFragment)
                        getFragmentManager().findFragmentByTag(FileMgmtFileDirDialogFragment.TAG);
                FileMgmtAppDialogFragment appDialogFragment = (FileMgmtAppDialogFragment)
                        getFragmentManager().findFragmentByTag(FileMgmtAppDialogFragment.TAG);
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

    public static FileMgmtFragment newInstance(boolean isSDCard1) {
        FileMgmtFragment fragment = new FileMgmtFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_ARGUMENT_IS_SDCARD1, isSDCard1);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static FileMgmtFragment newInstance(boolean isSDCard1, String SDCard1Path) {
        FileMgmtFragment fragment = new FileMgmtFragment();
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

        mUiHandler = new Handler();

        Bundle bundle = getArguments();
        isSDCard1 = bundle.getBoolean(KEY_ARGUMENT_IS_SDCARD1);
        if (isSDCard1) {
            FILE_ROOT_DIR_PATH = bundle.getString(KEY_ARGUMENT_SDCARD1_PATH);
        } else {
            FILE_ROOT_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

//        mExternalStorageObserver = new ExternalStorageObserver(FILE_ROOT_DIR_PATH);
//        mExternalStorageObserver.startWatching();

        String[] spinner_array;
        if (isSDCard1) {
            mFileRootDirName = mContext.getString(R.string.file_mgmt_sdcard1_storage_name) + "/";
            spinner_array = new String[]{mContext.getString(R.string.file_mgmt_spinner_files)};
        } else {
            mFileRootDirName = mContext.getString(R.string.file_mgmt_internal_storage_name) + "/";
            spinner_array = mContext.getResources().getStringArray(R.array.file_mgmt_spinner);
        }
        mSpinnerAdapter = new ArrayAdapter<String>(mContext, R.layout.file_mgmt_spinner, spinner_array) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setId(position);
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setContentDescription(textView.getText());
                return textView;
            }
        };
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mHandlerThread = new HandlerThread(FileMgmtFragment.class.getSimpleName());
        mHandlerThread.start();
        mWorkerHandler = new Handler(mHandlerThread.getLooper());

        mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        mWaitToExecuteSparseArr = new SparseArray<>();
        mPinUnpinFileMap = new ConcurrentHashMap<>();
        mPinUnpinAppMap = new ConcurrentHashMap<>();
        mPinUnpinTypeMap = new ConcurrentHashMap<>();

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
        return inflater.inflate(R.layout.file_mgmt_fragment, container, false);
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
                if (mCurrentVisible) {
                    mAutoUiRefreshThread = new Thread(mAutoUiRefreshRunnable);
                    mAutoUiRefreshThread.start();
                }
            }
        }

        // Start mProcessPinThread
        if (mProcessPinThread == null) {
            if (mCheckPinStatusRunnable != null) {
                if (mCurrentVisible) {
                    mProcessPinThread = new Thread(mCheckPinStatusRunnable);
                    mProcessPinThread.start();
                }
            }
        }

        // Start mApiExecutorThread
        if (mApiExecutorThread == null) {
            if (mApiExecutorRunnable != null) {
                if (mCurrentVisible) {
                    mApiExecutorThread = new Thread(mApiExecutorRunnable);
                    mApiExecutorThread.start();
                }
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();

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
        mEmptyFolder = (LinearLayout) view.findViewById(R.id.no_file_layout);
        mProgressCircle = (ProgressBar) view.findViewById(R.id.progress_circle);
        mFilePathNavigationLayout = (LinearLayout) view.findViewById(R.id.file_path_layout);
        mFilePathNavigationScrollView = (HorizontalScrollView) view.findViewById(R.id.file_path_navigation_scrollview);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mRefresh = (ImageView) view.findViewById(R.id.refresh);
        mLayoutType = (ImageView) view.findViewById(R.id.layout_type);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSnackbar = Snackbar.make(mView, "", Snackbar.LENGTH_INDEFINITE);

        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mRecyclerViewScrollDown = (dy >= 0);
            }
        });

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int ordinal = sharedPreferences.getInt(HCFSMgmtUtils.PREF_APP_FILE_DISPLAY_LAYOUT, DISPLAY_TYPE.GRID.ordinal());
        mDisplayType = DISPLAY_TYPE.values()[ordinal];
        switch (mDisplayType) {
            case LINEAR:
                mLayoutType.setImageResource(R.drawable.icon_btn_tab_gridview_light);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                mRecyclerView.addItemDecoration(mDividerItemDecoration);
                mSectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter(new LinearRecyclerViewAdapter());
                break;
            case GRID:
                mLayoutType.setImageResource(R.drawable.icon_btn_tab_listview_light);
                mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, GRID_LAYOUT_SPAN_COUNT));
                mSectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter(new GridRecyclerViewAdapter());
                mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();
                break;
        }
        mSectionedRecyclerViewAdapter.setSections(new Section[]{new Section(0)});
        mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);

        mSpinner.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSectionedRecyclerViewAdapter.init();
                mFilePathNavigationLayout.removeAllViews();

                String itemName = parent.getSelectedItem().toString();
                if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_apps))) {
                    showTypeContent(R.string.file_mgmt_spinner_apps);
                } else if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_data_type))) {
                    showTypeContent(R.string.file_mgmt_spinner_data_type);
                } else if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_files))) {
                    String logMsg = "FILE_ROOT_DIR_PATH=" + FILE_ROOT_DIR_PATH;
                    Logs.d(CLASSNAME, "onActivityCreated", logMsg);
//                    mWorkerHandler.post(new Runnable() {
//                        public void run() {
//                            DataTypeDAO dataTypeDAO = DataTypeDAO.newInstance(mContext);
//                            DataTypeInfo imageTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_IMAGE);
//                            DataTypeInfo videoTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_VIDEO);
//                            DataTypeInfo audioTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_AUDIO);
//                            mDataTypePinStatusMap.put(imageTypeInfo.getDataType(), imageTypeInfo.isPinned());
//                            mDataTypePinStatusMap.put(videoTypeInfo.getDataType(), videoTypeInfo.isPinned());
//                            mDataTypePinStatusMap.put(audioTypeInfo.getDataType(), audioTypeInfo.isPinned());
//                        }
//                    });

                    mCurrentFile = new File(FILE_ROOT_DIR_PATH);
                    FilePathNavigationView currentPathView = new FilePathNavigationView(mContext);
                    currentPathView.setText(Html.fromHtml("<u>" + mFileRootDirName + "</u>"));
                    currentPathView.setCurrentFilePath(mCurrentFile.getAbsolutePath());
                    mFilePathNavigationLayout.addView(currentPathView);
                    showTypeContent(R.string.file_mgmt_spinner_files);
                }
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
                    if (!isSDCard1) {
                        String itemName = mSpinner.getSelectedItem().toString();
                        if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_apps))) {
                            showTypeContent(R.string.file_mgmt_spinner_apps);
                        } else if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_data_type))) {
                            showTypeContent(R.string.file_mgmt_spinner_data_type);
                        } else if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_files))) {
                            showTypeContent(R.string.file_mgmt_spinner_files);
                        }
                    } else {
                        showTypeContent(R.string.file_mgmt_spinner_files);
                    }
                    lastClickTime = currentTime;
                }
            }
        });

        mLayoutType.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ItemInfo> itemInfoList;
                switch (mDisplayType) {
                    case LINEAR:
                        mLayoutType.setImageResource(R.drawable.icon_btn_tab_listview_light);

                        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, GRID_LAYOUT_SPAN_COUNT));
                        mRecyclerView.removeItemDecoration(mDividerItemDecoration);

                        itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                        mSectionedRecyclerViewAdapter.setBaseAdapter(new GridRecyclerViewAdapter(itemInfoList));
                        mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();

                        mDisplayType = DISPLAY_TYPE.GRID;
                        break;
                    case GRID:
                        mLayoutType.setImageResource(R.drawable.icon_btn_tab_gridview_light);

                        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        mRecyclerView.addItemDecoration(mDividerItemDecoration);

                        itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                        mSectionedRecyclerViewAdapter.setBaseAdapter(new LinearRecyclerViewAdapter(itemInfoList));

                        mDisplayType = DISPLAY_TYPE.LINEAR;
                        break;
                }
                mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(HCFSMgmtUtils.PREF_APP_FILE_DISPLAY_LAYOUT, mDisplayType.ordinal());
                editor.apply();
            }
        });
    }

    public void showTypeContent(final int resourceStringId) {
        Logs.d(CLASSNAME, "showTypeContent", null);

        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ItemInfo> itemInfoList;
                switch (resourceStringId) {
                    case R.string.file_mgmt_spinner_apps:
                        itemInfoList = DisplayTypeFactory.getListOfInstalledApps(mContext, DisplayTypeFactory.APP_USER);
                        break;
                    case R.string.file_mgmt_spinner_data_type:
                        itemInfoList = DisplayTypeFactory.getListOfDataType(mContext);
                        break;
                    case R.string.file_mgmt_spinner_files:
                        itemInfoList = DisplayTypeFactory.getListOfFileDirs(mContext, mCurrentFile);
                        break;
                    default:
                        itemInfoList = new ArrayList<>();
                }

                if (!isRunOnCorrectDisplayType(resourceStringId)) {
                    return;
                }

                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSectionedRecyclerViewAdapter.setSubAdapterItems(itemInfoList);
//                        mSectionedRecyclerViewAdapter.mBaseAdapter.notifyDataSetChanged();
                        mSectionedRecyclerViewAdapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }

    public class GridRecyclerViewAdapter extends RecyclerView.Adapter<GridRecyclerViewAdapter.GridRecyclerViewHolder> {

        private ArrayList<ItemInfo> mItemInfoList;
        private LruCache<Integer, Bitmap> mMemoryCache;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_mgmt_grid_item, parent, false);
            return new GridRecyclerViewHolder(view);
        }

        class GridRecyclerViewHolder extends RecyclerViewHolder implements OnClickListener, OnLongClickListener {

            private View rootView;
            private TextView itemName;
            private ImageView iconView;

            private GridRecyclerViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                iconView = (ImageView) itemView.findViewById(R.id.iconView);
                itemName = (TextView) itemView.findViewById(R.id.itemName);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.gridItemLayout) {
                    onItemClick(itemInfo);
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
            public void showPinnedDate(long currentTimeMillis) {

            }

            @Override
            public void hidePinnedDate() {

            }

            @Override
            public boolean onLongClick(View v) {
                if (itemInfo instanceof FileDirInfo) {
                    FileMgmtFileDirDialogFragment dialogFragment = FileMgmtFileDirDialogFragment.newInstance();
                    dialogFragment.setViewHolder(this);
                    dialogFragment.show(getFragmentManager(), FileMgmtFileDirDialogFragment.TAG);
                    return true;
                }
                return false;
            }

        }

    }

    public class LinearRecyclerViewAdapter extends RecyclerView.Adapter<LinearRecyclerViewAdapter.LinearRecyclerViewHolder> {

        private ArrayList<ItemInfo> mItemInfoList;
        private ThreadPoolExecutor mExecutor;
        private LruCache<Integer, Bitmap> mMemoryCache;

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

            // Display the beginning date of pining for data type */
//            if (itemInfo instanceof DataTypeInfo) {
//                final DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
//                if (dataTypeInfo.isPinned()) {
//                    if (dataTypeInfo.getDatePinned() != 0) {
//                        Date date = new Date(dataTypeInfo.getDatePinned());
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
//                        String displayTime = sdf.format(date);
//                        String displayText = String.format(mContext.getString(R.string.file_mgmt_date_since_pinned), displayTime);
//                        holder.datePinnedTextView.setText(displayText);
//                        holder.datePinnedTextView.setVisibility(View.VISIBLE);
//                    }
//                }
//            } else {
//                holder.datePinnedTextView.setVisibility(View.GONE);
//            }

            displayItem(position, holder, mMemoryCache, mExecutor);
        }

        @Nullable
        @Override
        public LinearRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Logs.d(CLASSNAME, "LinearRecyclerViewAdapter", "onCreateViewHolder");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_mgmt_linear_item, parent, false);
            return new LinearRecyclerViewHolder(view, mExecutor);
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
            private TextView datePinnedTextView;

            private LinearRecyclerViewHolder(View itemView, ThreadPoolExecutor executor) {
                super(itemView, executor);
                rootView = itemView;
                itemName = (TextView) itemView.findViewById(R.id.itemName);
                iconView = (ImageView) itemView.findViewById(R.id.iconView);
                pinView = (ImageView) itemView.findViewById(R.id.pinView);
                datePinnedTextView = (TextView) itemView.findViewById(R.id.datePinned);

                pinView.setOnClickListener(this);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.pinView) {
                    // Pin/Unpin the selected item */
                    boolean isPinned = !itemInfo.isPinned();
                    Logs.d(CLASSNAME, "onClick", "isPinned=" + isPinned);
                    boolean allowPinUnpin = pinUnpinItem(isPinned);
                    if (allowPinUnpin) {
                        pinView.setImageDrawable(itemInfo.getPinUnpinImage(isPinned));
                    }
                } else if (v.getId() == R.id.linearItemLayout) {
                    onItemClick(itemInfo);
                }
            }

            @Override
            public boolean onLongClick(View v) {
                if (itemInfo instanceof FileDirInfo) {
                    FileMgmtFileDirDialogFragment dialogFragment = FileMgmtFileDirDialogFragment.newInstance();
                    dialogFragment.setViewHolder(this);
                    dialogFragment.show(getFragmentManager(), FileMgmtFileDirDialogFragment.TAG);
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
            public void showPinnedDate(long currentTimeMillis) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                String displayTime = sdf.format(new Date(currentTimeMillis));
                String displayText = String.format(mContext.getString(R.string.file_mgmt_date_since_pinned), displayTime);
                datePinnedTextView.setText(displayText);
                datePinnedTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void hidePinnedDate() {
                datePinnedTextView.setVisibility(View.GONE);
            }

        }

    }

    public class SectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private boolean hasChildItem = true;
        private static final int SECTION_TYPE = 0;
        private RecyclerView.Adapter mBaseAdapter;
        private SparseArray<Section> mSections = new SparseArray<>();
        private boolean isFirstCircleAnimated = true;

        private boolean isDataObserverRegistered;
        private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                hasChildItem = mBaseAdapter.getItemCount() > 0;
                Logs.d(CLASSNAME, "SectionedRecyclerViewAdapter", "onChanged", "mBaseAdapter.getItemCount()=" + mBaseAdapter.getItemCount());
                Logs.d(CLASSNAME, "SectionedRecyclerViewAdapter", "onChanged", "hasChildItem=" + hasChildItem);
                checkSubAdapterItem();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                hasChildItem = mBaseAdapter.getItemCount() > 0;
                Logs.d(CLASSNAME, "SectionedRecyclerViewAdapter", "onItemRangeChanged", "hasChildItem=" + hasChildItem);
                checkSubAdapterItem();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                hasChildItem = mBaseAdapter.getItemCount() > 0;
                Logs.d(CLASSNAME, "SectionedRecyclerViewAdapter", "onItemRangeInserted", "hasChildItem=" + hasChildItem);
                checkSubAdapterItem();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                hasChildItem = mBaseAdapter.getItemCount() > 0;
                Logs.d(CLASSNAME, "SectionedRecyclerViewAdapter", "onItemRangeRemoved", "hasChildItem=" + hasChildItem);
                checkSubAdapterItem();
            }
        };

        private SectionedRecyclerViewAdapter(final RecyclerView.Adapter mBaseAdapter) {
            this.mBaseAdapter = mBaseAdapter;
            registerAdapterDataObserver();
        }

        private void init() {
            Logs.d(CLASSNAME, "SectionedRecyclerViewAdapter", "init", null);
            isFirstCircleAnimated = true;
            shutdownSubAdapterExecutor();
            subAdapterInit();
        }

        private void registerAdapterDataObserver() {
            if (isDataObserverRegistered) {
                unregisterAdapterDataObserver(mDataObserver);
                isDataObserverRegistered = false;
            }
            registerAdapterDataObserver(mDataObserver);
            isDataObserverRegistered = true;
        }

        private void subAdapterInit() {
            if (mBaseAdapter instanceof GridRecyclerViewAdapter) {
                ((GridRecyclerViewAdapter) mBaseAdapter).init();
            } else {
                ((LinearRecyclerViewAdapter) mBaseAdapter).init();
            }
        }

        private void checkSubAdapterItem() {
            if (mBaseAdapter.getItemCount() != 0) {
                mEmptyFolder.setVisibility(View.GONE);
            } else {
                mEmptyFolder.setVisibility(View.VISIBLE);
            }
        }

        private void shutdownSubAdapterExecutor() {
            Logs.d(CLASSNAME, "SectionedRecyclerViewAdapter", "shutdownSubAdapterExecutor", null);
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
                mSections.get(position).viewHolder = sectionViewHolder;
                mWorkerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String storageType;
                                if (isSDCard1) {
                                    storageType = mContext.getString(R.string.file_mgmt_sdcard1_storage_name);
                                } else {
                                    storageType = mContext.getString(R.string.file_mgmt_internal_storage_name);
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
                View view = LayoutInflater.from(mContext).inflate(R.layout.file_mgmt_section_item, parent, false);
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
            registerAdapterDataObserver();
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

        private class SectionedViewHolder extends RecyclerView.ViewHolder {

            private View rootView;
            private CircleDisplay circleDisplay;
            private TextView storageType;
            private TextView totalSpace;
            private TextView freeSpace;

            private SectionedViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                circleDisplay = (CircleDisplay) itemView.findViewById(R.id.circle_display_view);
                storageType = (TextView) itemView.findViewById(R.id.storage_type);
                totalSpace = (TextView) itemView.findViewById(R.id.total_space);
                freeSpace = (TextView) itemView.findViewById(R.id.free_space);
            }

        }

    }

    public class Section {
        private int firstPosition;
        private int sectionedPosition;
        private SectionedRecyclerViewAdapter.SectionedViewHolder viewHolder;

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
    public void onDestroy() {
        super.onDestroy();

        // Interrupt the threads in the threading pool of executor
        mSectionedRecyclerViewAdapter.shutdownSubAdapterExecutor();

        // Interrupt mHandlerThread
        mHandlerThread.quit();
        mHandlerThread.interrupt();

        // Unregister mAddRemovePackageReceiver
        if (mAddRemovePackageReceiver != null) {
            mAddRemovePackageReceiver.unregisterReceiver(mContext);
        }

//        /** Stop watching external storage */
//        if (mExternalStorageObserver != null) {
//            mExternalStorageObserver.stopWatching();
//            mExternalStorageObserver = null;
//        }
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
            showTypeContent(R.string.file_mgmt_spinner_files);

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
        String selectedItemName = mSpinner.getSelectedItem().toString();
        if (selectedItemName.equals(mContext.getString(R.string.file_mgmt_spinner_files))) {
            if (!mCurrentFile.getAbsolutePath().equals(FILE_ROOT_DIR_PATH)) {
                View view = getView();
                if (view != null) {
                    mEmptyFolder.setVisibility(View.GONE);
                }

                mCurrentFile = mCurrentFile.getParentFile();
                int childCount = mFilePathNavigationLayout.getChildCount();
                FilePathNavigationView filePathNavigationView = (FilePathNavigationView) mFilePathNavigationLayout.getChildAt(childCount - 2);
                final int firstVisibleItemPosition = filePathNavigationView.firstVisibleItemPosition;
                mFilePathNavigationLayout.removeViewAt(childCount - 1);
                showTypeContent(R.string.file_mgmt_spinner_files);

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
    private boolean isRunOnCorrectDisplayType(int typeStringResourceID) {
        String selectedItemName = mSpinner.getSelectedItem().toString();
        return selectedItemName.equals(mContext.getString(typeStringResourceID));
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
            mCurrentVisible = true;
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
                            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RequestCode.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
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
        } else {
            mCurrentVisible = false;
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
            String spinnerAppsTitle = mContext.getString(R.string.file_mgmt_spinner_apps);
            Logs.d(CLASSNAME, "onReceive", "action=" + action);
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                boolean isDataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

                if (!isDataRemoved || isReplacing || mSpinner == null ||
                        !mSpinner.getSelectedItem().toString().equals(spinnerAppsTitle)) {
                    return;
                }

                ArrayList<ItemInfo> itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                for (int i = 0; i < itemInfoList.size(); i++) {
                    ItemInfo itemInfo = itemInfoList.get(i);
                    if (itemInfo instanceof AppInfo) {
                        AppInfo appInfo = (AppInfo) itemInfo;
                        if (appInfo.getPackageName().equals(packageName)) {
                            itemInfoList.remove(i);
                            mSectionedRecyclerViewAdapter.notifyItemRemoved(i + 1);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            } else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (isReplacing || mSpinner == null ||
                        !mSpinner.getSelectedItem().toString().equals(spinnerAppsTitle)) {
                    return;
                }

                mWorkerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final AppInfo appInfo = new AppInfo(context);
                        try {
                            PackageManager pm = context.getPackageManager();
                            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                            appInfo.setUid(applicationInfo.uid);
                            appInfo.setApplicationInfo(applicationInfo);
                            appInfo.setName(applicationInfo.loadLabel(pm).toString());
                        } catch (PackageManager.NameNotFoundException e) {
                            Logs.e(CLASSNAME, "AddRemovePackageReceiver", "onReceive", Log.getStackTraceString(e));
                            return;
                        }

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                int pos = 0;
                                mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList().add(pos, appInfo);
                                mSectionedRecyclerViewAdapter.notifyItemInserted(pos + 1);
                            }
                        });
                    }
                });
            }
        }

        public void registerReceiver(Context context, IntentFilter intentFilter) {
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
        } else if (itemInfo instanceof FileDirInfo) {
            FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
            isPinned = HCFSMgmtUtils.isPathPinned(fileDirInfo.getFilePath());
        }
        return isPinned;
    }

    private void onItemClick(ItemInfo itemInfo) {
        if (itemInfo instanceof AppInfo) {
            FileMgmtAppDialogFragment dialogFragment = FileMgmtAppDialogFragment.newInstance();
            dialogFragment.setViewHolder(itemInfo.getViewHolder());
            dialogFragment.show(getFragmentManager(), FileMgmtAppDialogFragment.TAG);
        } else if (itemInfo instanceof DataTypeInfo) {

        } else if (itemInfo instanceof FileDirInfo) {
            final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
            if (fileDirInfo.isDirectory()) {
                // Set the first visible item position to previous FilePathNavigationView when navigating to next directory level
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int childCount = mFilePathNavigationLayout.getChildCount();
                FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) mFilePathNavigationLayout
                        .getChildAt(childCount - 1);
                filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);

                // Add the current FilePathNavigationView to navigation layout
                mCurrentFile = new File(fileDirInfo.getFilePath());
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
                showTypeContent(R.string.file_mgmt_spinner_files);
            } else {
                // Build the intent
                String mimeType = fileDirInfo.getMimeType();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(fileDirInfo.getFilePath());
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
                        Snackbar.make(view, mContext.getString(R.string.file_mgmt_snackbar_unknown_type_file), Snackbar.LENGTH_SHORT).show();
                    }
                }

            }
        }
    }

    public abstract class RecyclerViewHolder extends RecyclerView.ViewHolder {

        ItemInfo itemInfo;
        private ThreadPoolExecutor executor;

        private RecyclerViewHolder(View itemView) {
            super(itemView);
        }

        private RecyclerViewHolder(View itemView, ThreadPoolExecutor executor) {
            this(itemView);
            this.executor = executor;
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
            if (itemInfo instanceof AppInfo) {
                showProgress();

                final AppInfo appInfo = (AppInfo) itemInfo;
                appInfo.setLastProcessTime(System.currentTimeMillis());

                mPinUnpinAppMap.put(appInfo.getPackageName(), appInfo);
                try {
                    // The subject to clone AppInfo is in order to keep the pin status.
                    mWaitToExecuteSparseArr.put(appInfo.hashCode(), (AppInfo) appInfo.clone());
                } catch (CloneNotSupportedException e) {
                    Logs.e(CLASSNAME, "pinUnpinItem", Log.getStackTraceString(e));
                }
            } else if (itemInfo instanceof DataTypeInfo) {
                /*
                final DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
                dataTypeInfo.setDateUpdated(0);

                final String dataTypeText;
                if (dataTypeInfo.getDataType().equals(DataTypeDAO.DATA_TYPE_IMAGE)) {
                    dataTypeText = mContext.getString(R.string.file_mgmt_list_data_type_image);
                } else if (dataTypeInfo.getDataType().equals(DataTypeDAO.DATA_TYPE_VIDEO)) {
                    dataTypeText = mContext.getString(R.string.file_mgmt_list_data_type_video);
                } else if (dataTypeInfo.getDataType().equals(DataTypeDAO.DATA_TYPE_AUDIO)) {
                    dataTypeText = mContext.getString(R.string.file_mgmt_list_data_type_audio);
                } else {
                    dataTypeText = "";
                }

                if (!dataTypeText.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(dataTypeText);
                    if (dataTypeInfo.isPinned()) {
                        builder.setMessage(mContext.getString(R.string.file_mgmt_alert_dialog_message_pin_datatype));
                        builder.setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dataTypeInfo.setDateUpdated(0);
                                dataTypeInfo.setDatePinned(0);
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataTypeDAO.getInstance(mContext).update(dataTypeInfo);
                                    }
                                });
                                mPinUnpinTypeMap.put(dataTypeInfo.getDataType(), isPinned);
                            }
                        });
                        builder.setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                long currentTimeMillis = System.currentTimeMillis();
                                dataTypeInfo.setDateUpdated(currentTimeMillis);
                                dataTypeInfo.setDatePinned(currentTimeMillis);
                                showPinnedDate(currentTimeMillis);

                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataTypeDAO.getInstance(mContext).update(dataTypeInfo);
                                    }
                                });
                                mPinUnpinTypeMap.put(dataTypeInfo.getDataType(), isPinned);
                            }
                        });
                    } else {
                        builder.setMessage(mContext.getString(R.string.file_mgmt_alert_dialog_message_unpin_datatype));
                        builder.setPositiveButton(mContext.getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dataTypeInfo.setDateUpdated(0);
                                hidePinnedDate();
                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        DataTypeDAO.getInstance(mContext).update(dataTypeInfo.getDataType(), dataTypeInfo, DataTypeDAO.PIN_STATUS_COLUMN);
                                        DataTypeDAO.getInstance(mContext).update(dataTypeInfo.getDataType(), dataTypeInfo, DataTypeDAO.DATE_UPDATED_COLUMN);
                                    }
                                });
                                mPinUnpinTypeMap.put(dataTypeInfo.getDataType(), isPinned);
                            }
                        });
                    }
                    builder.setCancelable(false);
                    builder.show();
                }
                */
            } else if (itemInfo instanceof FileDirInfo) {
                final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                boolean isNeedToProcess = true;
                if (!isPinned) {
                    if (fileDirInfo.getFilePath().contains(EXTERNAL_ANDROID_PATH)) {
                        isNeedToProcess = false;

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(fileDirInfo.getName());
                        builder.setMessage(mContext.getString(R.string.file_mgmt_cannot_unpin_files_in_android_folder));
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
                    fileDirInfo.setLastProcessTime(System.currentTimeMillis());

                    Logs.d(CLASSNAME, "pinUnpinItem", "itemInfo.isPinned()=" + itemInfo.isPinned()
                            + ", fileDirInfo.isPinned()=" + fileDirInfo.isPinned() + ", isPinned=" + isPinned);
                    mPinUnpinFileMap.put(fileDirInfo.getFilePath(), isPinned);
                    try {
                        // The subject to clone AppInfo is in order to keep the pin status.
                        mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), (FileDirInfo) fileDirInfo.clone());
                    } catch (CloneNotSupportedException e) {
                        Logs.e(CLASSNAME, "pinUnpinItem", Log.getStackTraceString(e));
                    }
                } else {
                    fileDirInfo.setPinned(!isPinned);
                    allowPinUnpin = false;
                }

            }
            return allowPinUnpin;
        }

        abstract void setIconBitmap(Bitmap bitmap);

        abstract void setIconDrawable(Drawable drawable);

        abstract void setIconAlpha(int alpha);

        abstract void showPinnedDate(long currentTimeMillis);

        abstract void hidePinnedDate();

    }

    private void displayItem(final int position, final RecyclerViewHolder holder,
                             final LruCache<Integer, Bitmap> memoryCache, ThreadPoolExecutor executor) {

        if (!isPositionVisible(position)) {
            return;
        }

        final ItemInfo itemInfo = holder.getItemInfo();
        final Bitmap cacheBitmap = memoryCache.get(itemInfo.hashCode());
        if (cacheBitmap != null) {
            holder.setIconBitmap(cacheBitmap);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (holder.getItemInfo().getName().equals(itemInfo.getName())) {
                        final int alpha = itemInfo.getIconAlpha();
                        final boolean isPinned = isItemPinned(itemInfo);
                        // A workaround to solve the problem that pin icons refresh abnormally when
                        // user pin/unpin items.
                        if (mWaitToExecuteSparseArr.get(itemInfo.hashCode()) != null) {
                            return;
                        }
                        itemInfo.setPinned(isPinned);
                        itemInfo.setPosition(position);

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.setIconAlpha(alpha);
                                if (holder instanceof LinearRecyclerViewAdapter.LinearRecyclerViewHolder) {
                                    boolean isDirectory = false;
                                    if (itemInfo instanceof FileDirInfo) {
                                        isDirectory = ((FileDirInfo) itemInfo).isDirectory();
                                    }

                                    ImageView pinView = ((LinearRecyclerViewAdapter.LinearRecyclerViewHolder) holder).pinView;
                                    if (isDirectory) {
                                        pinView.setVisibility(View.GONE);
                                    } else {
                                        // Display pinned/unpinned item image
                                        pinView.setVisibility(View.VISIBLE);
                                        pinView.setImageDrawable(itemInfo.getPinUnpinImage(isPinned));
                                        pinView.setContentDescription(getPinViewContentDescription(isPinned));
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else {
            holder.setIconDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default_gray));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (holder.getItemInfo().getName().equals(itemInfo.getName())) {
                        final boolean isPinned = isItemPinned(itemInfo);
                        itemInfo.setPinned(isPinned);
                        itemInfo.setPosition(position);

                        final int alpha = itemInfo.getIconAlpha();
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.setIconAlpha(alpha);
                            }
                        });
                        final Bitmap iconBitmap = itemInfo.getIconImage();
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bitmap = iconBitmap;
                                if (!itemInfo.isPinned()) {
                                    Bitmap adjustBitmap = ((BitmapDrawable) adjustImageSaturation(iconBitmap)).getBitmap();
                                    if (adjustBitmap != null) {
                                        bitmap = adjustBitmap;
                                    }
                                }
                                if (bitmap != null) {
                                    holder.setIconBitmap(bitmap);
                                    memoryCache.put(itemInfo.hashCode(), bitmap);
                                }
                            }
                        });

                        // Display pinned/unpinned item image
                        if (holder instanceof LinearRecyclerViewAdapter.LinearRecyclerViewHolder) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    boolean isDirectory = false;
                                    if (itemInfo instanceof FileDirInfo) {
                                        isDirectory = ((FileDirInfo) itemInfo).isDirectory();
                                    }

                                    ImageView pinView = ((LinearRecyclerViewAdapter.LinearRecyclerViewHolder) holder).pinView;
                                    if (isDirectory) {
                                        pinView.setVisibility(View.GONE);
                                    } else {
                                        // Display pinned/unpinned item image
                                        pinView.setVisibility(View.VISIBLE);
                                        pinView.setImageDrawable(itemInfo.getPinUnpinImage(isPinned));
                                        pinView.setContentDescription(getPinViewContentDescription(isPinned));
                                    }
                                }
                            });
                        }

                    }
                }
            });

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    //    public class ExternalStorageObserver extends FileObserver {
//
//        public ExternalStorageObserver(String path) {
//            super(path, FileObserver.ALL_EVENTS);
//            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "ExternalStorageObserver", "ExternalStorageObserver", "path=" + path);
//        }
//
//        @Override
//        public void onEvent(int event, String path) {
//            switch (event) {
//                case FileObserver.CREATE:
//                    HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "ExternalStorageObserver", "onEvent", "path=" + path + ", event=CREATE");
//                    break;
//                case FileObserver.DELETE:
//                    HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "ExternalStorageObserver", "onEvent", "path=" + path + ", event=DELETE");
//                    break;
//                default:
//                    HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "ExternalStorageObserver", "onEvent", "path=" + path + ", event=" + event);
//            }
//        }
//
//    }

}
