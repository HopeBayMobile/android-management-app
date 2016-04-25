package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.customview.CircleDisplay;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.LocationStatus;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.service.HCFSMgmtService;
import com.hopebaytech.hcfsmgmt.utils.DisplayTypeFactory;
import com.hopebaytech.hcfsmgmt.utils.ExecutorFactory;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.MemoryCacheFactory;
import com.hopebaytech.hcfsmgmt.utils.UnitConverter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
    private final int INTERVAL_AUTO_UI_REFRESH = 1000;
    private final int INTERVAL_EXECUTE_API = 1000;
    private final int INTERVAL_REFRESH = 2000;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private SectionedRecyclerViewAdapter mSectionedRecyclerViewAdapter;
    private DividerItemDecoration mDividerItemDecoration;
    private ArrayAdapter<String> mSpinnerAdapter;
    private HandlerThread mHandlerThread;
    private Thread mApiExecutorThread;
    private Thread mAutoUiRefreshThread;
    private Handler mWorkerHandler;
    private ProgressBar mProgressCircle;
    private LinearLayout mEmptyFolder;
    private Spinner mSpinner;
    private ImageView mRefresh;
    private ImageView mLayoutType;
    private SparseArray<ItemInfo> mWaitToExecuteSparseArr;
    private Map<Integer, Boolean> mItemPinStatusMap;
    private Map<String, Boolean> mPinUnpinFileMap;
    private Map<String, AppInfo> mPinUnpinAppMap;
    private Map<String, Boolean> mPinUnpinTypeMap;
    private AddRemovePackageBroadcastReceiver mAddRemovePackageStatusReceiver;
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

    private String mFileRootDirName;
    private String FILE_ROOT_DIR_PATH;
    private boolean isSDCard1 = false;
    private boolean isRecyclerViewScrollDown;
    private boolean isCurrentVisible;
    private DISPLAY_TYPE mDisplayType = DISPLAY_TYPE.GRID;

//    private Map<String, Boolean> mDataTypePinStatusMap = new HashMap<>();

    private Runnable mAutoUiRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(INTERVAL_AUTO_UI_REFRESH);
                    if (mSectionedRecyclerViewAdapter != null) {
                        try {
                            int firstVisiblePosition = findRecyclerViewFirstVisibleItemPosition();
                            int lastVisiblePosition = findRecyclerViewLastVisibleItemPosition();
                            final SectionedRecyclerViewAdapter adapter = (SectionedRecyclerViewAdapter) mRecyclerView.getAdapter();
                            ArrayList<ItemInfo> itemInfoList = adapter.getSubAdapterItemInfoList();
                            /** Update section value (storage usage) */
                            if (firstVisiblePosition == 0) {
                                final Section section = adapter.getSections().get(0);
                                if (section.viewHolder != null) {
                                    StatFs statFs = new StatFs(FILE_ROOT_DIR_PATH);
                                    final long totalStorageSpace = statFs.getTotalBytes();
                                    final long availableStorageSpace = statFs.getAvailableBytes();
                                    ((Activity) mContext).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSectionedRecyclerViewAdapter.updateSection(section.viewHolder, totalStorageSpace, availableStorageSpace, false);
                                        }
                                    });
                                }
                            }

                            boolean isNeedToHideProgress = true;
                            boolean isNeedToProcess = false;
                            String selectedItemName = mSpinner.getSelectedItem().toString();
                            Map<String, Boolean> finishedPinUnpinAppMap = new HashMap<>();
                            Map<String, Boolean> finishedPinUnpinFileMap = new HashMap<>();
                            Map<String, Boolean> finishedPinUnpinTypeMap = new HashMap<>();
                            if (selectedItemName.equals(getString(R.string.file_mgmt_spinner_apps))) {
                                /** Check pin/unpin process finished in display-by-app page */
                                for (String packageName : mPinUnpinAppMap.keySet()) {
                                    AppInfo appInfo = mPinUnpinAppMap.get(packageName);
                                    boolean isRealPinned = HCFSMgmtUtils.isAppPinned(mContext, appInfo);
                                    boolean isExpectedPinned = appInfo.isPinned();

                                    HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "mAutoUiRefreshRunnable", "packageName=" + packageName + ", isRealPinned=" + isRealPinned + ", isExpectedPinned=" + isExpectedPinned);

                                    if (isRealPinned == isExpectedPinned) {
                                        /** Remove packageName from mPinUnpinAppMap if the pin status has became what user expected */
                                        mPinUnpinAppMap.remove(packageName);
                                        finishedPinUnpinAppMap.put(packageName, isExpectedPinned);
                                        isNeedToProcess = true;
                                    }
                                }
                                if (mPinUnpinAppMap.size() != 0) {
                                    isNeedToHideProgress = false;
                                }
                            } else if (selectedItemName.equals(getString(R.string.file_mgmt_spinner_data_type))) {
                                /** Check pin/unpin process finished in display-by-files page */
                                for (String dataType : mPinUnpinTypeMap.keySet()) {
                                    boolean isExpectedPinned = mPinUnpinTypeMap.get(dataType);
                                    mPinUnpinTypeMap.remove(dataType);
                                    finishedPinUnpinTypeMap.put(dataType, isExpectedPinned);
                                    isNeedToProcess = true;
                                }
                            } else if (selectedItemName.equals(getString(R.string.file_mgmt_spinner_files))) {
                                /** Check pin/unpin process finished in display-by-files page */
                                for (String path : mPinUnpinFileMap.keySet()) {
                                    boolean isRealPinned = HCFSMgmtUtils.isPathPinned(path);
                                    boolean isExpectedPinned = mPinUnpinFileMap.get(path);
                                    if (path.startsWith(mCurrentFile.getAbsolutePath())) {
                                        if (isRealPinned == isExpectedPinned) {
                                            /** Remove pinUnpinPath from mPinUnpinFileMap if the pin status has became what user expected */
                                            mPinUnpinFileMap.remove(path);
                                            finishedPinUnpinFileMap.put(path, isExpectedPinned);
                                            isNeedToProcess = true;
                                        } else {
                                            isNeedToHideProgress = false;
                                        }
                                    }
                                }
                            } else {
                                continue;
                            }

                            if (isNeedToProcess) {
                                /** Update icon image and pin/unpin image */
                                for (int i = firstVisiblePosition + 1; i < lastVisiblePosition + 1; i++) {
                                    int index = i - 1;
                                    if (index >= itemInfoList.size()) {
                                        break;
                                    }

                                    boolean isExpectedPinned = false;
                                    final ItemInfo itemInfo = itemInfoList.get(index);
                                    if (itemInfo instanceof AppInfo) {
                                        AppInfo appInfo = (AppInfo) itemInfo;
                                        boolean isContainInMap = false;
                                        if (finishedPinUnpinAppMap.containsKey(appInfo.getPackageName())) {
                                            isExpectedPinned = finishedPinUnpinAppMap.get(appInfo.getPackageName());
                                            isContainInMap = true;
                                        }
                                        if (!isContainInMap) {
                                            continue;
                                        }
                                    } else if (itemInfo instanceof DataTypeInfo) {
                                        DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
                                        boolean isContainInMap = false;
                                        if (finishedPinUnpinTypeMap.containsKey(dataTypeInfo.getDataType())) {
                                            isExpectedPinned = finishedPinUnpinTypeMap.get(dataTypeInfo.getDataType());
                                            isContainInMap = true;
                                        }
                                        if (!isContainInMap) {
                                            continue;
                                        }
                                    } else if (itemInfo instanceof FileDirInfo) {
                                        FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                                        boolean isContainInMap = false;
                                        for (String finishPinUnpinPath : finishedPinUnpinFileMap.keySet()) {
                                            if (fileDirInfo.getFilePath().startsWith(finishPinUnpinPath)) {
                                                isExpectedPinned = finishedPinUnpinFileMap.get(finishPinUnpinPath);
                                                isContainInMap = true;
                                                break;
                                            }
                                        }
                                        if (!isContainInMap) {
                                            continue;
                                        }
                                    }

                                    final boolean isItemExpectedPinned = isExpectedPinned;
                                    RecyclerView.ViewHolder viewHolder = itemInfo.getViewHolder();
                                    if (viewHolder != null) {
                                        if (viewHolder instanceof LinearRecyclerViewAdapter.LinearRecyclerViewHolder) {
                                            final LinearRecyclerViewAdapter.LinearRecyclerViewHolder holder = (LinearRecyclerViewAdapter.LinearRecyclerViewHolder) viewHolder;
                                            if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
                                                final Bitmap iconBitmap = itemInfo.getIconImage();
                                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        final int alpha = isItemExpectedPinned ? 255 : 100;
                                                        Bitmap bitmap;
                                                        if (itemInfo.isPinned()) {
                                                            bitmap = iconBitmap;
                                                        } else {
                                                            bitmap = ((BitmapDrawable) adjustImageSaturation(iconBitmap)).getBitmap();
                                                        }
                                                        if (bitmap != null) {
                                                            holder.setIconBitmap(bitmap);
                                                        }
                                                        holder.setIconAlpha(alpha);

                                                        holder.pinView.setImageDrawable(HCFSMgmtUtils.getPinUnpinImage(mContext, isItemExpectedPinned));
                                                        holder.pinView.setContentDescription(getPinViewContentDescription(isItemExpectedPinned));
                                                    }
                                                });
                                            }
                                        } else if (viewHolder instanceof GridRecyclerViewAdapter.GridRecyclerViewHolder) {
                                            final GridRecyclerViewAdapter.GridRecyclerViewHolder holder = (GridRecyclerViewAdapter.GridRecyclerViewHolder) viewHolder;
                                            if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
                                                final Bitmap iconBitmap = itemInfo.getIconImage();
                                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        final int alpha = isItemExpectedPinned ? 255 : 100;
                                                        Bitmap bitmap;
                                                        if (itemInfo.isPinned()) {
                                                            bitmap = iconBitmap;
                                                        } else {
                                                            bitmap = ((BitmapDrawable) adjustImageSaturation(iconBitmap)).getBitmap();
                                                        }
                                                        if (bitmap != null) {
                                                            holder.setIconBitmap(bitmap);
                                                            holder.setIconAlpha(alpha);
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                                if (isNeedToHideProgress) {
                                    hidePorgressCircle();
                                }
                            }
                        } catch (NullPointerException e) {
                            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "AutoUiRefreshRunnable", Log.getStackTraceString(e));
                        }
                    }
                } catch (InterruptedException e) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "AutoUiRefreshRunnable", "mAutoUiRefreshThread is interrupted");
                    break;
                }
            }
        }
    };

    private Runnable mApiExecutorRunnable = new Runnable() {
        @Override
        public void run() {
            /** Process user requests every one second */
            while (true) {
                try {
                    for (int i = 0; i < mWaitToExecuteSparseArr.size(); i++) {
                        int key = mWaitToExecuteSparseArr.keyAt(i);
                        ItemInfo itemInfo = mWaitToExecuteSparseArr.get(key);
                        long lastProcessTime = itemInfo.getLastProcessTime();
                        if (itemInfo instanceof AppInfo) {
                            AppInfo appInfo = (AppInfo) itemInfo;

                            UidInfo uidInfo = new UidInfo();
                            uidInfo.setPackageName(appInfo.getPackageName());
                            uidInfo.setIsPinned(appInfo.isPinned());
                            UidDAO.getInstance(mContext).update(uidInfo, UidDAO.PIN_STATUS_COLUMN);

                            Intent intent = new Intent(mContext, HCFSMgmtService.class);
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_APP);
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_NAME, appInfo.getItemName());
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_PACKAGE_NAME, appInfo.getPackageName());
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_DATA_DIR, appInfo.getDataDir());
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_SOURCE_DIR, appInfo.getSourceDir());
//                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_EXTERNAL_DIR, appInfo.getExternalDirList());
                            intent.putStringArrayListExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_EXTERNAL_DIR, appInfo.getExternalDirList());
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_PIN_STATUS, appInfo.isPinned());
                            mContext.startService(intent);
                        } else if (itemInfo instanceof DataTypeInfo) {
                            /** Nothing to do here */
                        } else if (itemInfo instanceof FileDirInfo) {
                            FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                            Intent intent = new Intent(mContext, HCFSMgmtService.class);
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_FILE_DIRECTORY);
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_FILEAPTH, fileDirInfo.getFilePath());
                            intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_PIN_STATUS, fileDirInfo.isPinned());
                            mContext.startService(intent);
                        }
                        if (mWaitToExecuteSparseArr.get(key).getLastProcessTime() == lastProcessTime) {
                            mWaitToExecuteSparseArr.remove(key);
                        }
                    }
                    Thread.sleep(INTERVAL_EXECUTE_API);
                } catch (InterruptedException e) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreate", "mApiExecutorThread is interrupted");
                    break;
                } catch (NullPointerException e) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreate", "mApiExecutorThread is interrupted");
                    break;
                }
            }
        }
    };

    private void showProgressCircle() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressCircle.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hidePorgressCircle() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
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

        Bundle bundle = getArguments();
        isSDCard1 = bundle.getBoolean(KEY_ARGUMENT_IS_SDCARD1);
        if (isSDCard1) {
            FILE_ROOT_DIR_PATH = bundle.getString(KEY_ARGUMENT_SDCARD1_PATH);
        } else {
            FILE_ROOT_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        String[] spinner_array;
        if (isSDCard1) {
            mFileRootDirName = mContext.getString(R.string.file_mgmt_sdcard1_storage_name) + "/";
            spinner_array = new String[]{mContext.getString(R.string.file_mgmt_spinner_files)};
        } else {
            mFileRootDirName = mContext.getString(R.string.file_mgmt_internal_storage_name) + "/";
            spinner_array = mContext.getResources().getStringArray(R.array.file_mgmt_spinner);
        }
        mSpinnerAdapter = new ArrayAdapter<>(mContext, R.layout.file_mgmt_spinner, spinner_array);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mHandlerThread = new HandlerThread(FileMgmtFragment.class.getSimpleName());
        mHandlerThread.start();
        mWorkerHandler = new Handler(mHandlerThread.getLooper());

        mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);
        mWaitToExecuteSparseArr = new SparseArray<>();
        mItemPinStatusMap = new ConcurrentHashMap<>();
        mPinUnpinFileMap = new ConcurrentHashMap<>();
        mPinUnpinAppMap = new ConcurrentHashMap<>();
        mPinUnpinTypeMap = new ConcurrentHashMap<>();

        /** Register mAddRemovePackageStatusReceiver */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        mAddRemovePackageStatusReceiver = new AddRemovePackageBroadcastReceiver();
        mAddRemovePackageStatusReceiver.registerReceiver(mContext, filter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_mgmt_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        /** Start mAutoUiRefreshThread */
        if (mAutoUiRefreshThread == null) {
            if (mAutoUiRefreshRunnable != null) {
                if (isCurrentVisible) {
                    mAutoUiRefreshThread = new Thread(mAutoUiRefreshRunnable);
                    mAutoUiRefreshThread.start();
                }
            }
        }

        /** Start mApiExecutorThread */
        if (mApiExecutorThread == null) {
            if (mApiExecutorRunnable != null) {
                if (isCurrentVisible) {
                    mApiExecutorThread = new Thread(mApiExecutorRunnable);
                    mApiExecutorThread.start();
                }
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        /** Interrupt mApiExecutorThread */
        if (mApiExecutorThread != null) {
            mApiExecutorThread.interrupt();
            mApiExecutorThread = null;
        }

        /** Interrupt mAutoUiRefreshThread */
        if (mAutoUiRefreshThread != null) {
            mAutoUiRefreshThread.interrupt();
            mAutoUiRefreshThread = null;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

//        View view = getView();
//        if (view == null) {
//            return;
//        }

//        mEmptyFolder = (LinearLayout) view.findViewById(R.id.no_file_layout);
//        mProgressCircle = (ProgressBar) view.findViewById(R.id.progress_circle);
//        mFilePathNavigationLayout = (LinearLayout) view.findViewById(R.id.file_path_layout);
//        mFilePathNavigationScrollView = (HorizontalScrollView) view.findViewById(R.id.file_path_navigation_scrollview);

//        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isRecyclerViewScrollDown = (dy >= 0);
            }
        });
        switch (mDisplayType) {
            case LINEAR:
                mSectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter(new LinearRecyclerViewAdapter());
                break;
            case GRID:
                mSectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter(new GridRecyclerViewAdapter());
                break;
        }
        mSectionedRecyclerViewAdapter.setSections(new Section[]{new Section(0)});
        switch (mDisplayType) {
            case LINEAR:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                mRecyclerView.addItemDecoration(mDividerItemDecoration);
                break;
            case GRID:
                mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, GRID_LAYOUT_SPAN_COUNT));
                mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();
                break;
        }
        mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);

        mSpinner.getBackground().setColorFilter(ContextCompat.getColor(mContext, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSectionedRecyclerViewAdapter.init();
                String itemName = parent.getSelectedItem().toString();
                if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_apps))) {
                    mFilePathNavigationLayout.setVisibility(View.GONE);
                    showTypeContent(R.string.file_mgmt_spinner_apps);
                } else if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_data_type))) {
                    mFilePathNavigationLayout.setVisibility(View.GONE);
                    showTypeContent(R.string.file_mgmt_spinner_data_type);
                } else if (itemName.equals(mContext.getString(R.string.file_mgmt_spinner_files))) {
                    String logMsg = "FILE_ROOT_DIR_PATH=" + FILE_ROOT_DIR_PATH;
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityCreated", logMsg);
//                    mWorkerHandler.post(new Runnable() {
//                        public void run() {
//                            DataTypeDAO dataTypeDAO = DataTypeDAO.getInstance(mContext);
//                            DataTypeInfo imageTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_IMAGE);
//                            DataTypeInfo videoTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_VIDEO);
//                            DataTypeInfo audioTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_AUDIO);
//                            mDataTypePinStatusMap.put(imageTypeInfo.getDataType(), imageTypeInfo.isPinned());
//                            mDataTypePinStatusMap.put(videoTypeInfo.getDataType(), videoTypeInfo.isPinned());
//                            mDataTypePinStatusMap.put(audioTypeInfo.getDataType(), audioTypeInfo.isPinned());
//                        }
//                    });

                    mCurrentFile = new File(FILE_ROOT_DIR_PATH);
                    mFilePathNavigationLayout.removeAllViews();
                    FilePathNavigationView currentPathView = new FilePathNavigationView(mContext);
                    currentPathView.setText(mFileRootDirName);
                    currentPathView.setCurrentFilePath(mCurrentFile.getAbsolutePath());
                    mFilePathNavigationLayout.addView(currentPathView);
                    mFilePathNavigationLayout.setVisibility(View.VISIBLE);
                    showTypeContent(R.string.file_mgmt_spinner_files);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//        ImageView refresh = (ImageView) view.findViewById(R.id.refresh);
        mRefresh.setOnClickListener(new OnClickListener() {

            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastClickTime) > INTERVAL_REFRESH) {
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
                    Snackbar.make(getView(), mContext.getString(R.string.file_mgmt_snackbar_refresh), Snackbar.LENGTH_SHORT).show();
                    lastClickTime = currentTime;
                }
            }
        });

//        final ImageView displayType = (ImageView) view.findViewById(R.id.display_type);
        mLayoutType.setOnClickListener (new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, mLayoutType);
                popupMenu.getMenuInflater().inflate(R.menu.file_mgmt_layout_popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_list) {
                            if (mDisplayType == DISPLAY_TYPE.LINEAR) {
                                return false;
                            }
                            mDisplayType = DISPLAY_TYPE.LINEAR;

                            mSectionedRecyclerViewAdapter.init();
                            ArrayList<ItemInfo> itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                            mSectionedRecyclerViewAdapter.setBaseAdapter(new LinearRecyclerViewAdapter(itemInfoList));
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                            mRecyclerView.addItemDecoration(mDividerItemDecoration);
                        } else if (item.getItemId() == R.id.menu_grid) {
                            if (mDisplayType == DISPLAY_TYPE.GRID) {
                                return false;
                            }
                            mDisplayType = DISPLAY_TYPE.GRID;

                            mSectionedRecyclerViewAdapter.init();
                            ArrayList<ItemInfo> itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                            mSectionedRecyclerViewAdapter.setBaseAdapter(new GridRecyclerViewAdapter(itemInfoList));
                            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, GRID_LAYOUT_SPAN_COUNT));
                            mRecyclerView.removeItemDecoration(mDividerItemDecoration);
                            mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();
                        }
                        mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);
                        mSectionedRecyclerViewAdapter.notifySubAdapterDataSetChanged();
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    public void showTypeContent(final int resource_string_id) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "showTypeContent", null);
        mSectionedRecyclerViewAdapter.clearSubAdpater();
        mSectionedRecyclerViewAdapter.notifySubAdapterDataSetChanged();

        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<ItemInfo> itemInfoList = null;
                switch (resource_string_id) {
                    case R.string.file_mgmt_spinner_apps:
                        itemInfoList = DisplayTypeFactory.getListOfInstalledApps(mContext, DisplayTypeFactory.APP_USER);
                        break;
                    case R.string.file_mgmt_spinner_data_type:
                        itemInfoList = DisplayTypeFactory.getListOfDataType(mContext);
                        break;
                    case R.string.file_mgmt_spinner_files:
                        itemInfoList = DisplayTypeFactory.getListOfFileDirs(mContext, mCurrentFile);
                        break;
                }

                if (!isRunOnCorrectDisplayType(resource_string_id)) {
                    return;
                }

                final ArrayList<ItemInfo> finalItemInfoList = itemInfoList;
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSectionedRecyclerViewAdapter.setSubAdapterItems(finalItemInfoList);
                        mSectionedRecyclerViewAdapter.notifySubAdapterDataSetChanged();
                        if (finalItemInfoList != null) {
                            View view = getView();
                            if (finalItemInfoList.size() != 0) {
                                if (view != null) {
                                    mEmptyFolder.setVisibility(View.GONE);
                                }
                            } else {
                                if (view != null) {
                                    mEmptyFolder.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });

            }
        });
    }

    private boolean isInstalledOnExternalStorage(ApplicationInfo packageInfo) {
        return (packageInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0;
    }

    public class GridRecyclerViewAdapter extends RecyclerView.Adapter<GridRecyclerViewAdapter.GridRecyclerViewHolder> {

        private ArrayList<ItemInfo> itemInfoList;
        private LruCache<String, Bitmap> mMemoryCache;
        private ThreadPoolExecutor mExecutor;

        public GridRecyclerViewAdapter() {
            itemInfoList = new ArrayList<>();
            init();
        }

        public GridRecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
            this.itemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
            init();
        }

        public void init() {
            mExecutor = ExecutorFactory.createThreadPoolExecutor();
            mMemoryCache = MemoryCacheFactory.createMemoryCache();
        }

        public void shutdownExecutor() {
            mExecutor.shutdownNow();
        }

        public ArrayList<ItemInfo> getItemInfoList() {
            return itemInfoList;
        }

        private void clear() {
            if (itemInfoList != null) {
                itemInfoList.clear();
            }

            if (mMemoryCache != null) {
                mMemoryCache.evictAll();
            }
        }

        public void setItemData(@Nullable ArrayList<ItemInfo> items) {
            this.itemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
        }

        @Override
        public int getItemCount() {
            return itemInfoList.size();
        }

        @Override
        public void onBindViewHolder(final GridRecyclerViewHolder holder, final int position) {
            final ItemInfo itemInfo = itemInfoList.get(position);
            itemInfo.setViewHolder(holder);
            holder.setItemInfo(itemInfo);
            holder.itemName.setText(itemInfo.getItemName());

            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (isPositionVisible(position)) {
                        if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
                            boolean isPinned = isItemPinned(itemInfo);
                            itemInfo.setPinned(isPinned);
                            showImageIcon(itemInfo, mMemoryCache, holder);
                        }
                    }
                }
            });
        }

        @Override
        public GridRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_mgmt_grid_item, parent, false);
            return new GridRecyclerViewHolder(view);
        }

        public class GridRecyclerViewHolder extends RecyclerView.ViewHolder implements IRecyclerViewHolder, OnClickListener, OnLongClickListener {

            protected View rootView;
            protected TextView itemName;
            protected ImageView iconView;
            protected ItemInfo itemInfo;

            public GridRecyclerViewHolder(View itemView) {
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
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                final boolean isPinned = !itemInfo.isPinned();
                if (!isSDCard1) {
                    if (isPinned) {
                        popupMenu.getMenu().add(mContext.getString(R.string.file_mgmt_popup_menu_pin));
                    } else {
                        popupMenu.getMenu().add(mContext.getString(R.string.file_mgmt_popup_menu_unpin));
                    }
                }
                popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String itemTitle = item.getTitle().toString();
                        if (itemTitle.equals(mContext.getString(R.string.file_mgmt_popup_menu_pin)) ||
                                itemTitle.equals(mContext.getString(R.string.file_mgmt_popup_menu_unpin))) {
                            if (!isSDCard1) {
                                /** Pin/Unpin the selected item */
                                pinUnpinItem(itemInfo, GridRecyclerViewHolder.this, mExecutor);
                            }
                        }
                        return true;
                    }
                });
                popupMenu.show();
                return true;
            }

            public void setItemInfo(ItemInfo itemInfo) {
                this.itemInfo = itemInfo;
            }

            public ItemInfo getItemInfo() {
                return itemInfo;
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
            public void changePinImage(ItemInfo itemInfo) {

            }

            @Override
            public void showPinnedDate(long currentTimeMillis) {

            }

            @Override
            public void hidePinnedDate() {

            }

        }

    }

    public class LinearRecyclerViewAdapter extends RecyclerView.Adapter<LinearRecyclerViewAdapter.LinearRecyclerViewHolder> {

        private ArrayList<ItemInfo> mItemInfoList;
        private ThreadPoolExecutor mExecutor;
        private LruCache<String, Bitmap> mMemoryCache;

        public LinearRecyclerViewAdapter() {
            mItemInfoList = new ArrayList<>();
            init();
        }

        public LinearRecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
            this.mItemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
            init();
        }

        public void init() {
            mExecutor = ExecutorFactory.createThreadPoolExecutor();
            mMemoryCache = MemoryCacheFactory.createMemoryCache();
        }

        public void shutdownExecutor() {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "LinearRecyclerViewAdapter", "shutdownExecutor", null);
            mExecutor.shutdownNow();
        }

        @Override
        public int getItemCount() {
            return mItemInfoList.size();
        }

        public ArrayList<ItemInfo> getItemInfoList() {
            return mItemInfoList;
        }

        @Override
        public void onBindViewHolder(final LinearRecyclerViewHolder holder, final int position) {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "LinearRecyclerViewAdapter", "onBindViewHolder");

            final ItemInfo itemInfo = mItemInfoList.get(position);
            itemInfo.setViewHolder(holder);
            holder.setItemInfo(itemInfo);
            holder.itemName.setText(itemInfo.getItemName());

//            final Bitmap cacheBitmap = mMemoryCache.get(itemInfo.hashCode() + "_cache");
//            if (cacheBitmap != null) {
//                Boolean isPinned = mItemPinStatusMap.get(itemInfo.hashCode());
//                if (isPinned != null) {
//                    itemInfo.setPinned(isPinned);
//                    final int alpha = itemInfo.isPinned() ? 255 : 50;
//                    holder.setIconBitmap(cacheBitmap);
//                    holder.setIconAlpha(alpha);
//                } else {
//                    holder.setIconBitmap(cacheBitmap);
//                    mExecutor.execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (isPositionVisible(position)) {
//                                if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
//                                    boolean isPinned = isItemPinned(itemInfo);
//                                    itemInfo.setPinned(isPinned);
//                                    mItemPinStatusMap.put(itemInfo.hashCode(), itemInfo.isPinned());
//                                    final int alpha = itemInfo.isPinned() ? 255 : 50;
//                                    ((Activity) mContext).runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            holder.setIconAlpha(alpha);
//
//                                            /** Display pinned/unpinned image of item */
//                                            if (!isSDCard1) {
//                                                holder.pinView.setImageDrawable(HCFSMgmtUtils.getPinUnpinImage(mContext, itemInfo.isPinned()));
//                                                holder.pinView.setContentDescription(getPinViewContentDescription(itemInfo.isPinned()));
//                                            } else {
//                                                holder.pinView.setVisibility(View.GONE);
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//                        }
//                    });
//                }
//            } else {
//                holder.setIconDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default_gray));
//                mExecutor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (isPositionVisible(position)) {
//                            if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
//                                boolean isPinned = isItemPinned(itemInfo);
//                                itemInfo.setPinned(isPinned);
//                                mItemPinStatusMap.put(itemInfo.hashCode(), itemInfo.isPinned());
//                                final int alpha = itemInfo.isPinned() ? 255 : 50;
//                                final Bitmap iconBitmap = itemInfo.getIconImage();
//                                ((Activity) mContext).runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        holder.setIconAlpha(alpha);
//                                        Bitmap bitmap;
//                                        if (itemInfo.isPinned()) {
//                                            bitmap = iconBitmap;
//                                        } else {
//                                            bitmap = ((BitmapDrawable) adjustImageSaturation(iconBitmap)).getBitmap();
//                                        }
//                                        if (bitmap != null) {
//                                            holder.setIconBitmap(bitmap);
//                                            mMemoryCache.put(itemInfo.hashCode() + "_cache", bitmap);
//                                        }
//
//                                        /** Display pinned/unpinned image of item */
//                                        if (!isSDCard1) {
//                                            holder.pinView.setImageDrawable(HCFSMgmtUtils.getPinUnpinImage(mContext, itemInfo.isPinned()));
//                                            holder.pinView.setContentDescription(getPinViewContentDescription(itemInfo.isPinned()));
//                                        } else {
//                                            holder.pinView.setVisibility(View.GONE);
//                                        }
//                                    }
//                                });
//                            }
//                        }
//                    }
//                });
//
//            }

            /** Display the beginning date of pining for data type */
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

            holder.pinView.setImageDrawable(null);
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (isPositionVisible(position)) {
                        if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
                            boolean isPinned = isItemPinned(itemInfo);
                            itemInfo.setPinned(isPinned);
                            showImageIcon(itemInfo, mMemoryCache, holder);

                            /** Display pinned/unpinned image of item */
                            if (!isSDCard1) {
                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.pinView.setImageDrawable(HCFSMgmtUtils.getPinUnpinImage(mContext, itemInfo.isPinned()));
                                        holder.pinView.setContentDescription(getPinViewContentDescription(itemInfo.isPinned()));
                                    }
                                });
                            } else {
                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.pinView.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }

        @Nullable
        @Override
        public LinearRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "LinearRecyclerViewAdapter", "onCreateViewHolder");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_mgmt_linear_item, parent, false);
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

        public class LinearRecyclerViewHolder extends RecyclerView.ViewHolder implements IRecyclerViewHolder, OnClickListener, OnLongClickListener {

            protected View rootView;
            protected TextView itemName;
            protected ImageView iconView;
            protected ImageView pinView;
            protected TextView datePinnedTextView;
            protected ItemInfo itemInfo;

            public LinearRecyclerViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                itemName = (TextView) itemView.findViewById(R.id.itemName);
                iconView = (ImageView) itemView.findViewById(R.id.iconView);
                pinView = (ImageView) itemView.findViewById(R.id.pinView);
                datePinnedTextView = (TextView) itemView.findViewById(R.id.datePinned);

                pinView.setOnClickListener(this);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            public void setItemInfo(ItemInfo itemInfo) {
                this.itemInfo = itemInfo;
            }

            public ItemInfo getItemInfo() {
                return itemInfo;
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.pinView) {
                    /** Pin/Unpin the selected item */
                    pinUnpinItem(itemInfo, this, mExecutor);
                } else if (v.getId() == R.id.linearItemLayout) {
                    onItemClick(itemInfo);
                }
            }

            @Override
            public boolean onLongClick(View v) {
                if (itemInfo instanceof AppInfo) {
                    FileMgmtDialogFragment dialogFragment = FileMgmtDialogFragment.newInstance();
                    dialogFragment.setItemInfo(itemInfo);
                    dialogFragment.show(getFragmentManager(), FileMgmtDialogFragment.TAG);
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
            public void changePinImage(ItemInfo itemInfo) {
                pinView.setImageDrawable(itemInfo.getPinUnpinImage());
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

        private boolean mValid = true;
        private static final int SECTION_TYPE = 0;
        @SuppressWarnings("rawtypes")
        private RecyclerView.Adapter mBaseAdapter;
        private SparseArray<Section> mSections = new SparseArray<>();
        private long totalStorageSpace;
        private long availableStorageSpace;
        public boolean isFirstCircleAnimated = true;

        @SuppressWarnings("rawtypes")
        public SectionedRecyclerViewAdapter(RecyclerView.Adapter mBaseAdapter) {
            this.mBaseAdapter = mBaseAdapter;
            registerAdapterDataObserver(mBaseAdapter);
        }

        private void init() {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "SectionedRecyclerViewAdapter", "init", null);
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
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "SectionedRecyclerViewAdapter", "shutdownSubAdapterExecutor", null);
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

        private void clearSubAdpater() {
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
                    mValid = mBaseAdapter.getItemCount() > 0;
                    notifyDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount() > 0;
                    notifyItemRangeChanged(positionStart, itemCount);
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount() > 0;
                    notifyItemRangeInserted(positionStart, itemCount);
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount() > 0;
                    notifyItemRangeRemoved(positionStart, itemCount);
                }
            });
        }

        public void setGridLayoutManagerSpanSize() {
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
            return (mValid ? mSections.size() + mBaseAdapter.getItemCount() : 0);
        }

        public void updateSection(SectionedViewHolder sectionViewHolder, long totalStorageSpace, long availableStorageSpace, boolean showAnimation) {
            float toShowValue = ((totalStorageSpace - availableStorageSpace) * 1f / totalStorageSpace) * 100f;
            CircleDisplay cd = sectionViewHolder.circleDisplay;
            cd.showValue(toShowValue, 100f, totalStorageSpace, showAnimation);

            if (isSDCard1) {
                sectionViewHolder.totalStorageSpace.setText(UnitConverter.convertByteToProperUnit(totalStorageSpace));
                sectionViewHolder.availableStorageSpace.setText(UnitConverter.convertByteToProperUnit(availableStorageSpace));
            } else {
                sectionViewHolder.totalStorageSpace.setText(UnitConverter.convertByteToProperUnit(totalStorageSpace));
                sectionViewHolder.availableStorageSpace.setText(UnitConverter.convertByteToProperUnit(availableStorageSpace));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
            if (isSectionHeaderPosition(position)) {
                final SectionedViewHolder sectionViewHolder = (SectionedViewHolder) viewHolder;
                final String calculatingText = mContext.getString(R.string.file_mgmt_section_item_calculating);
                mSections.get(position).viewHolder = sectionViewHolder;
                mWorkerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String storageType;
                                if (isSDCard1) {
                                    storageType = mContext.getString(R.string.file_mgmt_sdcard1_storage_name);
                                } else {
                                    storageType = mContext.getString(R.string.file_mgmt_internal_storage_name);
                                }
                                sectionViewHolder.storageType.setText(storageType);
                                sectionViewHolder.totalStorageSpace.setText(calculatingText);
                                sectionViewHolder.availableStorageSpace.setText(calculatingText);
                            }
                        });

                        StatFs statFs = new StatFs(FILE_ROOT_DIR_PATH);
                        totalStorageSpace = statFs.getTotalBytes();
                        availableStorageSpace = statFs.getAvailableBytes();
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateSection(sectionViewHolder, totalStorageSpace, availableStorageSpace, isFirstCircleAnimated);
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

        @SuppressWarnings("rawtypes")
        public void setBaseAdapter(RecyclerView.Adapter mBaseAdapter) {
            this.mBaseAdapter = mBaseAdapter;
            registerAdapterDataObserver(mBaseAdapter);
        }

        public SparseArray<Section> getSections() {
            return mSections;
        }

        public void setSections(Section[] sections) {
            mSections.clear();

            Arrays.sort(sections, new Comparator<Section>() {
                @Override
                public int compare(Section o, Section o1) {
                    return (o.firstPosition == o1.firstPosition) ? 0 : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
                }
            });

            /** offset positions for the headers we're adding */
            int offset = 0;
            for (Section section : sections) {
                section.sectionedPosition = section.firstPosition + offset;
                mSections.append(section.sectionedPosition, section);
                ++offset;
            }

        }

        public boolean isSectionHeaderPosition(int position) {
            return mSections.get(position) != null;
        }

        public int sectionedPositionToPosition(int sectionedPosition) {
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

        public class SectionedViewHolder extends RecyclerView.ViewHolder {

            public View rootView;
            public CircleDisplay circleDisplay;
            public TextView storageType;
            public TextView totalStorageSpace;
            public TextView availableStorageSpace;

            public SectionedViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                circleDisplay = (CircleDisplay) itemView.findViewById(R.id.iconView);
                storageType = (TextView) itemView.findViewById(R.id.storage_type);
                totalStorageSpace = (TextView) itemView.findViewById(R.id.total_storage_space);
                availableStorageSpace = (TextView) itemView.findViewById(R.id.available_storage_space);
            }

        }

    }

    public class Section {
        public int firstPosition;
        public int sectionedPosition;
        public SectionedRecyclerViewAdapter.SectionedViewHolder viewHolder;

        public Section(int firstPosition) {
            this.firstPosition = firstPosition;
        }

    }

    public class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private final int[] ATTRS = new int[]{android.R.attr.listDivider};
        public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
        public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
        private Drawable mDivider;
        private int mOrientation;

        public DividerItemDecoration(Context context, int orientation) {
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
        public void onDraw(Canvas c, RecyclerView parent) {
            if (mOrientation == VERTICAL_LIST) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
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

        public void drawHorizontal(Canvas c, RecyclerView parent) {
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
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
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

        /** Interrupt the threads in the threading pool of executor */
        mSectionedRecyclerViewAdapter.shutdownSubAdapterExecutor();

        /** Interrupt mHandlerThread */
        mHandlerThread.quit();
        mHandlerThread.interrupt();

        /** Unregister mAddRemovePackageStatusReceiver */
        if (mAddRemovePackageStatusReceiver != null) {
            mAddRemovePackageStatusReceiver.unregisterReceiver(mContext);
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

            setTextColor(Color.WHITE);
            setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int startIndex = mFilePathNavigationLayout.indexOfChild(this) + 1;
            int childCount = mFilePathNavigationLayout.getChildCount();
            mFilePathNavigationLayout.removeViews(startIndex, childCount - startIndex);

            mCurrentFile = new File(currentFilePath);
            showTypeContent(R.string.file_mgmt_spinner_files);

            ((Activity) mContext).runOnUiThread(new Runnable() {
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

                ((Activity) mContext).runOnUiThread(new Runnable() {
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
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "isViewHolderThreadNeedToExecute", Log.getStackTraceString(e));
        }

        if (isRecyclerViewScrollDown) {
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

    private String getPinViewContentDescription(boolean isPinned, int location) {
        String contentDescription = "-1";
        if (isPinned && location == LocationStatus.LOCAL) {
            contentDescription = "0";
        } else if (isPinned && (location == LocationStatus.HYBRID || location == LocationStatus.CLOUD)) {
            contentDescription = "1";
        } else if (!isPinned && location == LocationStatus.LOCAL) {
            contentDescription = "2";
        } else if (!isPinned && location == LocationStatus.HYBRID) {
            contentDescription = "3";
        } else if (!isPinned && location == LocationStatus.CLOUD) {
            contentDescription = "4";
        }
        return contentDescription;
    }

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
            isCurrentVisible = true;
            if (mApiExecutorThread == null) {
                mApiExecutorThread = new Thread(mApiExecutorRunnable);
                mApiExecutorThread.start();
            }

            if (mAutoUiRefreshThread == null) {
                mAutoUiRefreshThread = new Thread(mAutoUiRefreshRunnable);
                mAutoUiRefreshThread.start();
            }
        } else {
            isCurrentVisible = false;
            if (mApiExecutorThread != null && !mApiExecutorThread.isInterrupted()) {
                mApiExecutorThread.interrupt();
                mApiExecutorThread = null;
            }

            if (mAutoUiRefreshThread != null && !mAutoUiRefreshThread.isInterrupted()) {
                mAutoUiRefreshThread.interrupt();
                mAutoUiRefreshThread = null;
            }
        }

    }

    public class AddRemovePackageBroadcastReceiver extends BroadcastReceiver {

        private boolean isRegister = false;

        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onReceive", "action=" + action);
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                boolean isDataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (isDataRemoved && !isReplacing) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (mSpinner != null) {
                        if (mSpinner.getSelectedItem().toString().equals(getString(R.string.file_mgmt_spinner_apps))) {
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
                                    return;
                                }
                            }
                        }
                    }
                }
            } else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (!isReplacing) {
                    if (mSpinner != null) {
                        if (mSpinner.getSelectedItem().toString().equals(getString(R.string.file_mgmt_spinner_apps))) {
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
                                        appInfo.setItemName(applicationInfo.loadLabel(pm).toString());
                                    } catch (PackageManager.NameNotFoundException e) {
                                        HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "AddRemovePackageBroadcastReceiver", "onReceive", Log.getStackTraceString(e));
                                    }
                                    final AppInfo finalAppInfo = appInfo;
                                    ((Activity) context).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (finalAppInfo != null) {
                                                ArrayList<ItemInfo> itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                                                itemInfoList.add(0, finalAppInfo);
                                                mSectionedRecyclerViewAdapter.setSubAdapterItems(itemInfoList);
                                                mSectionedRecyclerViewAdapter.notifyItemInserted(1);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
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
        /** Get the pinned/unpinned status of item */
        boolean isPinned = false;
        if (itemInfo instanceof AppInfo) {
            AppInfo appInfo = (AppInfo) itemInfo;
            isPinned = HCFSMgmtUtils.isAppPinned(mContext, appInfo);
        } else if (itemInfo instanceof DataTypeInfo) {
            /** The pin status of DataTypeInfo has got in getListOfDataType() */
            isPinned = itemInfo.isPinned();
        } else if (itemInfo instanceof FileDirInfo) {
            FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
            isPinned = HCFSMgmtUtils.isPathPinned(fileDirInfo.getFilePath());
        }
        return isPinned;
    }

    /**
     * Display icon image of item
     */
    private void showImageIcon(final ItemInfo itemInfo, final LruCache<String, Bitmap> mMemoryCache, final IRecyclerViewHolder holder) {
        final int alpha = itemInfo.isPinned() ? 255 : 50;
        final Bitmap cacheBitmap = mMemoryCache.get(itemInfo.hashCode() + "_cache");
        if (cacheBitmap != null) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder.setIconBitmap(cacheBitmap);
                    holder.setIconAlpha(alpha);
                }
            });
        } else {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder.setIconDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_doc_default_gray));
                    holder.setIconAlpha(alpha);
                }
            });
            final Bitmap iconBitmap = itemInfo.getIconImage();
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap;
                    if (itemInfo.isPinned()) {
                        bitmap = iconBitmap;
                    } else {
                        bitmap = ((BitmapDrawable) adjustImageSaturation(iconBitmap)).getBitmap();
                    }
//                    holder.setIconAlpha(alpha);
                    if (bitmap != null) {
                        holder.setIconBitmap(bitmap);
                        mMemoryCache.put(itemInfo.hashCode() + "_cache", bitmap);
                    }
                }
            });
        }
    }

    private void onItemClick(ItemInfo itemInfo) {
        if (itemInfo instanceof AppInfo) {
            FileMgmtDialogFragment dialogFragment = FileMgmtDialogFragment.newInstance();
            dialogFragment.setItemInfo(itemInfo);
            dialogFragment.show(getFragmentManager(), FileMgmtDialogFragment.TAG);
        } else if (itemInfo instanceof DataTypeInfo) {

        } else if (itemInfo instanceof FileDirInfo) {
            final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
            if (fileDirInfo.getCurrentFile().isDirectory()) {
                /** Set the first visible item position to previous FilePathNavigationView when navigating to next directory level */
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int childCount = mFilePathNavigationLayout.getChildCount();
                FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) mFilePathNavigationLayout
                        .getChildAt(childCount - 1);
                filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);

                /** Add the current FilePathNavigationView to navigation layout */
                mCurrentFile = fileDirInfo.getCurrentFile();
                FilePathNavigationView filePathNavigationView = new FilePathNavigationView(mContext);
                String currentPath = mCurrentFile.getAbsolutePath();
                String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
                filePathNavigationView.setText(navigationText);
                filePathNavigationView.setCurrentFilePath(currentPath);
                mFilePathNavigationLayout.addView(filePathNavigationView);
                mFilePathNavigationScrollView.post(new Runnable() {
                    public void run() {
                        mFilePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
                    }
                });

                /** Show the file list of the entered directory */
                showTypeContent(R.string.file_mgmt_spinner_files);
            } else {
                /** Build the intent */
                String mimeType = fileDirInfo.getMimeType();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (mimeType != null) {
                    intent.setDataAndType(Uri.fromFile(fileDirInfo.getCurrentFile()), mimeType);
                } else {
                    intent.setData(Uri.fromFile(fileDirInfo.getCurrentFile()));
                }

                /** Verify it resolves */
                PackageManager packageManager = mContext.getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;

                /** Start an activity if it's safe */
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

    private void pinUnpinItem(ItemInfo itemInfo, final IRecyclerViewHolder holder, final ThreadPoolExecutor executor) {
        if (itemInfo instanceof AppInfo) {
            showProgressCircle();

            final AppInfo appInfo = (AppInfo) itemInfo;
            boolean isPinned = !appInfo.isPinned();
            appInfo.setPinned(isPinned);
            appInfo.setLastProcessTime(System.currentTimeMillis());
            holder.changePinImage(appInfo);

            mPinUnpinAppMap.put(appInfo.getPackageName(), appInfo);
            mWaitToExecuteSparseArr.put(appInfo.hashCode(), appInfo);
        } else if (itemInfo instanceof DataTypeInfo) {
            final DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
            final boolean isPinned = !dataTypeInfo.isPinned();
            dataTypeInfo.setPinned(isPinned);
            dataTypeInfo.setDateUpdated(0);
            holder.changePinImage(dataTypeInfo);

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
                    builder.setPositiveButton(mContext.getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
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
                    builder.setNegativeButton(mContext.getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            long currentTimeMillis = System.currentTimeMillis();
                            dataTypeInfo.setDateUpdated(currentTimeMillis);
                            dataTypeInfo.setDatePinned(currentTimeMillis);
                            holder.showPinnedDate(currentTimeMillis);

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
                    builder.setPositiveButton(mContext.getString(R.string.alert_dialog_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dataTypeInfo.setDateUpdated(0);
                            holder.hidePinnedDate();
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
        } else if (itemInfo instanceof FileDirInfo) {
            final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
            final boolean isPinned = !fileDirInfo.isPinned();
            boolean isNeedToProcess = true;

            if (!isPinned) {
//                if (fileDirInfo.getMimeType() != null) {
//                    if (fileDirInfo.getMimeType().contains(DataTypeDAO.DATA_TYPE_IMAGE)) {
//                        if (mDataTypePinStatusMap.get(DataTypeDAO.DATA_TYPE_IMAGE)) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                            builder.setTitle(fileDirInfo.getItemName());
//                            builder.setMessage(mContext.getString(R.string.file_mgmt_whether_allowed_to_unpin_image));
//                            builder.setPositiveButton(mContext.getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    fileDirInfo.setPinned(isPinned);
//                                    fileDirInfo.setLastProcessTime(System.currentTimeMillis());
//                                    mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
//                                    holder.changePinImage(fileDirInfo);
//                                }
//                            });
//                            builder.setNegativeButton(mContext.getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            });
//                            builder.setCancelable(false);
//                            builder.show();
//                            return;
//                        }
//                    } else if (fileDirInfo.getMimeType().contains(DataTypeDAO.DATA_TYPE_VIDEO)) {
//                        if (mDataTypePinStatusMap.get(DataTypeDAO.DATA_TYPE_VIDEO)) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                            builder.setTitle(fileDirInfo.getItemName());
//                            builder.setMessage(mContext.getString(R.string.file_mgmt_whether_allowed_to_unpin_video));
//                            builder.setPositiveButton(mContext.getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    fileDirInfo.setPinned(isPinned);
//                                    fileDirInfo.setLastProcessTime(System.currentTimeMillis());
//                                    holder.changePinImage(fileDirInfo);
//                                    mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
//                                }
//                            });
//                            builder.setNegativeButton(mContext.getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            });
//                            builder.setCancelable(false);
//                            builder.show();
//                            return;
//                        }
//                    } else if (fileDirInfo.getMimeType().contains(DataTypeDAO.DATA_TYPE_AUDIO)) {
//                        if (mDataTypePinStatusMap.get(DataTypeDAO.DATA_TYPE_AUDIO)) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//                            builder.setTitle(fileDirInfo.getItemName());
//                            builder.setMessage(mContext.getString(R.string.file_mgmt_whether_allowed_to_unpin_audio));
//                            builder.setPositiveButton(mContext.getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    fileDirInfo.setPinned(isPinned);
//                                    fileDirInfo.setLastProcessTime(System.currentTimeMillis());
//                                    holder.changePinImage(fileDirInfo);
//                                    mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
//                                }
//                            });
//                            builder.setNegativeButton(mContext.getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            });
//                            builder.setCancelable(false);
//                            builder.show();
//                            return;
//                        }
//                    }
//                }

                if (fileDirInfo.getFilePath().contains(EXTERNAL_ANDROID_PATH)) {
                    isNeedToProcess = false;

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(fileDirInfo.getItemName());
                    builder.setMessage(mContext.getString(R.string.file_mgmt_cannot_unpin_files_in_android_folder));
                    builder.setPositiveButton(mContext.getString(R.string.alert_dialog_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
            }

            if (isNeedToProcess) {
                showProgressCircle();
                fileDirInfo.setPinned(isPinned);
                fileDirInfo.setLastProcessTime(System.currentTimeMillis());
                holder.changePinImage(fileDirInfo);

                mPinUnpinFileMap.put(fileDirInfo.getFilePath(), isPinned);
                mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
            }

        }
    }

    public interface IRecyclerViewHolder {

        void setIconBitmap(Bitmap bitmap);

        void setIconDrawable(Drawable drawable);

        void setIconAlpha(int alpha);

        void changePinImage(ItemInfo itemInfo);

        void showPinnedDate(long currentTimeMillis);

        void hidePinnedDate();

    }

}
