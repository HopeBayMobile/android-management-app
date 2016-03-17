package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import android.util.SparseIntArray;
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
import com.hopebaytech.hcfsmgmt.utils.DisplayType;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
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
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileManagementFragment extends Fragment {

    public static final String TAG = FileManagementFragment.class.getSimpleName();
    private static final String KEY_ARGUMENT_IS_SDCARD1 = "key_argument_is_sdcard1";
    private static final String KEY_ARGUMENT_SDCARD1_PATH = "key_argument_sdcard1_path";
    private final String CLASSNAME = getClass().getSimpleName();
    private final String EXTERNAL_ANDROID_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
    private final int GRID_LAYOUT_SPAN_COUNT = 3;

    private RecyclerView mRecyclerView;
    private SectionedRecyclerViewAdapter mSectionedRecyclerViewAdapter;
    private DividerItemDecoration mDividerItemDecoration;
    private ArrayAdapter<String> mSpinnerAdapter;
    private HandlerThread mHandlerThread;
    private Thread mApiExecutorThread;
    private Thread mUiAutoRefreshThread;
    private Handler mWorkerHandler;
    private DataTypeDAO mDataTypeDAO;
    private UidDAO mUidDAO;
    private ProgressBar mProgressCircle;
    private Spinner mSpinner;
    private SparseArray<ItemInfo> mWaitToExecuteSparseArr;
    private SparseArray<Integer> mPrevLocationStatusSparseArr;
    private SparseArray<Boolean> mPrevPinStatusSparseArr;
    /** Only used when user switch to "Display by file" */
    private HorizontalScrollView mFilePathNavigationScrollView;
    /** Only used when user switch to "Display by file" */
    private LinearLayout mFilePathNavigationLayout;
    /** Only used when user switch to "Display by file" */
    private File mCurrentFile;

    private enum DISPLAY_TYPE {
        GRID, LINEAR
    }

    private String mFileRootDirName;
    private String FILE_ROOT_DIR_PATH;
    private boolean isSDCard1 = false;
    private boolean isFragmentFirstLoaded = true;
    private boolean isRecyclerViewScrollDown;
    private DISPLAY_TYPE mDisplayType = DISPLAY_TYPE.LINEAR;
    /** Only used when user switch to "Display by file" */
    private Map<String, Boolean> mDataTypePinStatusMap = new HashMap<>();

    public static FileManagementFragment newInstance(boolean isSDCard1) {
        FileManagementFragment fragment = new FileManagementFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_ARGUMENT_IS_SDCARD1, isSDCard1);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static FileManagementFragment newInstance(boolean isSDCard1, String SDCard1Path) {
        FileManagementFragment fragment = new FileManagementFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_ARGUMENT_IS_SDCARD1, isSDCard1);
        bundle.putString(KEY_ARGUMENT_SDCARD1_PATH, SDCard1Path);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = getActivity();
        if (activity != null) {
            Bundle bundle = getArguments();
            isSDCard1 = bundle.getBoolean(KEY_ARGUMENT_IS_SDCARD1);
            if (isSDCard1) {
                FILE_ROOT_DIR_PATH = bundle.getString(KEY_ARGUMENT_SDCARD1_PATH);
            } else {
                FILE_ROOT_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            }

            mDividerItemDecoration = new DividerItemDecoration(activity, LinearLayoutManager.VERTICAL);

            mDataTypeDAO = new DataTypeDAO(activity);
            mUidDAO = new UidDAO(activity);

            String[] spinner_array;
            if (isSDCard1) {
                mFileRootDirName = getString(R.string.file_management_sdcard1_storatge_name) + "/";
                spinner_array = new String[]{getString(R.string.file_management_spinner_files)};
            } else {
                mFileRootDirName = getString(R.string.file_management_internal_storatge_name) + "/";
                spinner_array = getResources().getStringArray(R.array.file_management_spinner);
            }
            mSpinnerAdapter = new ArrayAdapter<>(activity, R.layout.file_management_spinner, spinner_array);
            mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mHandlerThread = new HandlerThread(FileManagementFragment.class.getSimpleName());
            mHandlerThread.start();
            mWorkerHandler = new Handler(mHandlerThread.getLooper());

            mWaitToExecuteSparseArr = new SparseArray<>();
            mPrevLocationStatusSparseArr = new SparseArray<>();
            mPrevPinStatusSparseArr = new SparseArray<>();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_management_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mApiExecutorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                /** Process user requests every one second */
                while (true) {
                    try {
                        Activity activity = getActivity();
                        if (activity != null) {
                            for (int i = 0; i < mWaitToExecuteSparseArr.size(); i++) {
                                int key = mWaitToExecuteSparseArr.keyAt(i);
                                ItemInfo itemInfo = mWaitToExecuteSparseArr.get(key);
                                long lastProcessTime = itemInfo.getLastProcessTime();
                                if (itemInfo instanceof AppInfo) {
                                    AppInfo appInfo = (AppInfo) itemInfo;

                                    UidInfo uidInfo = new UidInfo();
                                    uidInfo.setPackageName(appInfo.getPackageName());
                                    uidInfo.setIsPinned(appInfo.isPinned());
                                    mUidDAO.update(uidInfo, UidDAO.PIN_STATUS_COLUMN);

                                    Intent intent = new Intent(activity, HCFSMgmtService.class);
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_APP);
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_NAME, appInfo.getItemName());
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_PACKAGE_NAME, appInfo.getPackageName());
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_DATA_DIR, appInfo.getDataDir());
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_SOURCE_DIR, appInfo.getSourceDir());
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_EXTERNAL_DIR, appInfo.getExternalDir());
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_PIN_STATUS, appInfo.isPinned());
                                    activity.startService(intent);
                                } else if (itemInfo instanceof DataTypeInfo) {
                                    /** Nothing to do here */
                                } else if (itemInfo instanceof FileDirInfo) {
                                    FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                                    Intent intent = new Intent(activity, HCFSMgmtService.class);
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_FILE_DIRECTORY);
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_FILEAPTH, fileDirInfo.getFilePath());
                                    intent.putExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_PIN_STATUS, fileDirInfo.isPinned());
                                    activity.startService(intent);
                                }
                                if (mWaitToExecuteSparseArr.get(key).getLastProcessTime() == lastProcessTime) {
                                    mWaitToExecuteSparseArr.remove(key);
                                }
                            }
                        }
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreate", "mApiExecutorThread is interrupted");
                        break;
                    }
                }
            }
        });
        mApiExecutorThread.start();

        if (mUiAutoRefreshThread == null) {
            mUiAutoRefreshThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (mSectionedRecyclerViewAdapter != null) {
                                try {
                                    int firstVisiblePosition = findRecyclerViewFirstVisibleItemPosition();
                                    int lastVisiblePosition = findRecyclerViewLastVisibleItemPosition();
                                    final SectionedRecyclerViewAdapter adapter = (SectionedRecyclerViewAdapter) mRecyclerView.getAdapter();
                                    ArrayList<ItemInfo> itemInfoList = adapter.getSubAdapterItemInfoList();
                                    if (firstVisiblePosition == 0) {
                                        final Section section = adapter.getSections().get(0);
                                        if (section.viewHolder != null) {
                                            StatFs statFs = new StatFs(FILE_ROOT_DIR_PATH);
                                            final long totalStorageSpace = statFs.getTotalBytes();
                                            final long availableStorageSpace = statFs.getAvailableBytes();
                                            Activity activity = getActivity();
                                            if (activity != null) {
                                                activity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        float toShowValue = ((totalStorageSpace - availableStorageSpace) * 1f / totalStorageSpace) * 100f;
                                                        CircleDisplay cd = section.viewHolder.circleDisplay;
                                                        cd.showValue(toShowValue, 100f, totalStorageSpace, false);

                                                        if (isSDCard1) {
                                                            section.viewHolder.totalStorageSpace
                                                                    .setText(UnitConverter.convertByteToProperUnit(totalStorageSpace));
                                                            section.viewHolder.availableStorageSpace
                                                                    .setText(UnitConverter.convertByteToProperUnit(availableStorageSpace));
                                                        } else {
                                                            section.viewHolder.totalStorageSpace
                                                                    .setText(UnitConverter.convertByteToProperUnit(totalStorageSpace));
                                                            section.viewHolder.availableStorageSpace
                                                                    .setText(UnitConverter.convertByteToProperUnit(availableStorageSpace));
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }

                                    for (int i = firstVisiblePosition + 1; i < lastVisiblePosition + 1; i++) {
                                        Integer currentLocation = mPrevLocationStatusSparseArr.get(i);
                                        Boolean currentPinned = mPrevPinStatusSparseArr.get(i);

                                        boolean isNeedToChangePinImage = false;
                                        if (currentLocation != null && currentPinned != null) {
                                            if (!currentPinned.booleanValue() || currentLocation.intValue() != LocationStatus.LOCAL) {
                                                isNeedToChangePinImage = true;
                                            }
                                        } else {
                                            isNeedToChangePinImage = true;
                                        }

                                        if (isNeedToChangePinImage) {
                                            // Log.w(HCFSMgmtUtils.TAG, "realPos=" + i + ", currentLocation=" + currentLocation + ", currentPinned=" +
                                            // currentPinned);
                                            int index = i - 1;
                                            if (index >= itemInfoList.size()) {
                                                continue;
                                            }

                                            final ItemInfo itemInfo = itemInfoList.get(index);
                                            final int location = itemInfo.getLocationStatus();
                                            boolean isPinned = false;
                                            if (itemInfo instanceof AppInfo) {
                                                AppInfo appInfo = (AppInfo) itemInfo;
                                                isPinned = HCFSMgmtUtils.isAppPinned(appInfo, mUidDAO);
                                            } else if (itemInfo instanceof DataTypeInfo) {
                                                /** the pin status of DataTypeInfo has got in getListOfDataType() */
                                                isPinned = itemInfo.isPinned();
                                            } else if (itemInfo instanceof FileDirInfo) {
                                                FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                                                isPinned = HCFSMgmtUtils.isPathPinned(fileDirInfo.getFilePath());
                                            }
                                            itemInfo.setPinned(isPinned);
                                            // Random random = new Random();
                                            // final int location = random.nextInt(3) + 1;
                                            // boolean isPinned = random.nextBoolean();
                                            // itemInfo.setPinned(isPinned);

                                            boolean changePinImage = false;
                                            if (currentPinned != null && currentLocation != null) {
                                                if (isPinned != currentPinned.booleanValue()) {
                                                    changePinImage = true;
                                                } else {
                                                    if (currentLocation.intValue() != location) {
                                                        changePinImage = true;
                                                    }
                                                }
                                            }

                                            if (changePinImage) {
                                                RecyclerView.ViewHolder viewHolder = itemInfo.getViewHolder();
                                                if (viewHolder != null) {
                                                    if (viewHolder instanceof LinearRecyclerViewAdapter.LinearRecyclerViewHolder) {
                                                        final LinearRecyclerViewAdapter.LinearRecyclerViewHolder holder = (LinearRecyclerViewAdapter.LinearRecyclerViewHolder) viewHolder;
                                                        if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
                                                            final Activity activity = getActivity();
                                                            if (activity != null) {
                                                                activity.runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        Drawable pinViewDrawable = HCFSMgmtUtils.getPinUnpinImage(activity,
                                                                                itemInfo.isPinned(), location);
                                                                        holder.pinView.setImageDrawable(pinViewDrawable);
                                                                        holder.pinView.setContentDescription(getPinViewContentDescription(itemInfo.isPinned(), location));
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            mPrevPinStatusSparseArr.put(i, isPinned);
                                            mPrevLocationStatusSparseArr.put(i, location);
                                        }
                                    }
                                } catch (NullPointerException e) {
                                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onCreate", Log.getStackTraceString(e));
                                }
                            }
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreate", "mUiAutoRefreshThread is interrupted");
                            break;
                        }
                    }
                }
            });
            mUiAutoRefreshThread.start();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        /* Interrupt mApiExecutorThread */
        if (mApiExecutorThread != null) {
            mApiExecutorThread.interrupt();
            mApiExecutorThread = null;
        }

		/* Interrupt mUiAutoRefreshThread */
        if (mUiAutoRefreshThread != null) {
            mUiAutoRefreshThread.interrupt();
            mUiAutoRefreshThread = null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view == null) {
            return;
        }
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        mProgressCircle = (ProgressBar) view.findViewById(R.id.progress_circle);
        mFilePathNavigationLayout = (LinearLayout) view.findViewById(R.id.file_path_layout);
        mFilePathNavigationScrollView = (HorizontalScrollView) view.findViewById(R.id.file_path_navigation_scrollview);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
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
                mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
                mRecyclerView.addItemDecoration(mDividerItemDecoration);
                break;
            case GRID:
                mRecyclerView.setLayoutManager(new GridLayoutManager(activity, GRID_LAYOUT_SPAN_COUNT));
                mSectionedRecyclerViewAdapter.setGridLayoutManagerSpanSize();
                break;
        }
        mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);

        mSpinner = (Spinner) view.findViewById(R.id.spinner);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSectionedRecyclerViewAdapter.init();
                String itemName = parent.getSelectedItem().toString();
                if (itemName.equals(getString(R.string.file_management_spinner_apps))) {
                    mFilePathNavigationLayout.setVisibility(View.GONE);
                    showTypeContent(R.string.file_management_spinner_apps);
                } else if (itemName.equals(getString(R.string.file_management_spinner_data_type))) {
                    mFilePathNavigationLayout.setVisibility(View.GONE);
                    showTypeContent(R.string.file_management_spinner_data_type);
                } else if (itemName.equals(getString(R.string.file_management_spinner_files))) {
                    String logMsg = "FILE_ROOT_DIR_PATH=" + FILE_ROOT_DIR_PATH;
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityCreated", logMsg);
                    mWorkerHandler.post(new Runnable() {
                        public void run() {
                            DataTypeInfo imageTypeInfo = mDataTypeDAO.get(DataTypeDAO.DATA_TYPE_IMAGE);
                            DataTypeInfo videoTypeInfo = mDataTypeDAO.get(DataTypeDAO.DATA_TYPE_VIDEO);
                            DataTypeInfo audioTypeInfo = mDataTypeDAO.get(DataTypeDAO.DATA_TYPE_AUDIO);
                            mDataTypePinStatusMap.put(imageTypeInfo.getDataType(), imageTypeInfo.isPinned());
                            mDataTypePinStatusMap.put(videoTypeInfo.getDataType(), videoTypeInfo.isPinned());
                            mDataTypePinStatusMap.put(audioTypeInfo.getDataType(), audioTypeInfo.isPinned());
                        }
                    });

                    mCurrentFile = new File(FILE_ROOT_DIR_PATH);
                    mFilePathNavigationLayout.removeAllViews();
                    FilePathNavigationView currentPathView = new FilePathNavigationView(activity);
                    currentPathView.setText(mFileRootDirName);
                    currentPathView.setCurrentFilePath(mCurrentFile.getAbsolutePath());
                    mFilePathNavigationLayout.addView(currentPathView);
                    mFilePathNavigationLayout.setVisibility(View.VISIBLE);
                    showTypeContent(R.string.file_management_spinner_files);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageView refresh = (ImageView) view.findViewById(R.id.refresh);
        refresh.setOnClickListener(new OnClickListener() {

            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastClickTime) > 3000) {
                    mSectionedRecyclerViewAdapter.init();
                    if (!isSDCard1) {
                        String itemName = mSpinner.getSelectedItem().toString();
                        if (itemName.equals(getString(R.string.file_management_spinner_apps))) {
                            showTypeContent(R.string.file_management_spinner_apps);
                        } else if (itemName.equals(getString(R.string.file_management_spinner_data_type))) {
                            showTypeContent(R.string.file_management_spinner_data_type);
                        } else if (itemName.equals(getString(R.string.file_management_spinner_files))) {
                            showTypeContent(R.string.file_management_spinner_files);
                        }
                    } else {
                        showTypeContent(R.string.file_management_spinner_files);
                    }
                    Snackbar.make(getView(), getString(R.string.file_management_snackbar_refresh), Snackbar.LENGTH_SHORT).show();
                    lastClickTime = currentTime;
                }
            }
        });

        final ImageView displayType = (ImageView) view.findViewById(R.id.display_type);
        displayType.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(activity, displayType);
                popupMenu.getMenuInflater().inflate(R.menu.file_management_top_popup_menu, popupMenu.getMenu());
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
                            mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
                            mRecyclerView.addItemDecoration(mDividerItemDecoration);
                        } else if (item.getItemId() == R.id.menu_grid) {
                            if (mDisplayType == DISPLAY_TYPE.GRID) {
                                return false;
                            }
                            mDisplayType = DISPLAY_TYPE.GRID;

                            mSectionedRecyclerViewAdapter.init();
                            ArrayList<ItemInfo> itemInfoList = mSectionedRecyclerViewAdapter.getSubAdapterItemInfoList();
                            mSectionedRecyclerViewAdapter.setBaseAdapter(new GridRecyclerViewAdapter(itemInfoList));
                            mRecyclerView.setLayoutManager(new GridLayoutManager(activity, GRID_LAYOUT_SPAN_COUNT));
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
        mPrevLocationStatusSparseArr.clear();
        mPrevPinStatusSparseArr.clear();
        mSectionedRecyclerViewAdapter.clearSubAdpater();
        mSectionedRecyclerViewAdapter.notifySubAdapterDataSetChanged();

        mProgressCircle.setVisibility(View.VISIBLE);
        mWorkerHandler.post(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }

                ArrayList<ItemInfo> itemInfoList = null;
                switch (resource_string_id) {
                    case R.string.file_management_spinner_apps:
                        itemInfoList = DisplayType.getListOfInstalledApps(activity, DisplayType.APP_USER);
                        break;
                    case R.string.file_management_spinner_data_type:
                        itemInfoList = DisplayType.getListOfDataType(activity, mDataTypeDAO);
                        break;
                    case R.string.file_management_spinner_files:
                        itemInfoList = DisplayType.getListOfFileDirs(activity, mCurrentFile);
                        break;
                }

                if (!isRunOnCorrectDisplayType(resource_string_id)) {
                    return;
                }

                final ArrayList<ItemInfo> itemInfos = itemInfoList;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSectionedRecyclerViewAdapter.setSubAdapterItems(itemInfos);
                            mSectionedRecyclerViewAdapter.notifySubAdapterDataSetChanged();
                            if (itemInfos != null) {
                                View view = getView();
                                if (itemInfos.size() != 0) {
                                    if (view != null) {
                                        view.findViewById(R.id.no_file_layout).setVisibility(View.GONE);
                                    }
                                } else {
                                    if (view != null) {
                                        view.findViewById(R.id.no_file_layout).setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                            mProgressCircle.setVisibility(View.GONE);
                        }
                    });

            }
        });
    }

//	private void getAppPackageSize(PackageManager pm, ApplicationInfo packageInfo) {
//		try {
//			Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
//			getPackageSizeInfo.invoke(pm, packageInfo.packageName, new IPackageStatsObserver.Stub() {
//				@Override
//				public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
//					HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "getAppPackageSize", "codeSize=" + pStats.codeSize);
//				}
//			});
//		} catch (Exception e) {
//			HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getAppPackageSize", Log.getStackTraceString(e));
//		}
//	}

    private boolean isInstalledOnExternalStorage(ApplicationInfo packageInfo) {
        return (packageInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0;
    }

    public class GridRecyclerViewAdapter extends RecyclerView.Adapter<GridRecyclerViewAdapter.GridRecyclerViewHolder> {

        private ArrayList<ItemInfo> mItems;
        private SparseIntArray mPinStatusSparseArr;
        private LruCache<String, Bitmap> mMemoryCache;
        private ThreadPoolExecutor mExecutor;
        // private ExecutorService executor;

        public GridRecyclerViewAdapter() {
            mItems = new ArrayList<>();
            init();
        }

        public GridRecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
            this.mItems = (items == null) ? new ArrayList<ItemInfo>() : items;
            init();
        }

        public void init() {
            // executor = Executors.newCachedThreadPool();
            int N = Runtime.getRuntime().availableProcessors();
            mExecutor = new ThreadPoolExecutor(N, N * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            mExecutor.allowCoreThreadTimeOut(true);
            mExecutor.prestartCoreThread();

            mPinStatusSparseArr = new SparseIntArray();

            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8;
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    /** The cache size will be measured in kilobytes rather than number of items. */
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        public void shutdownExcecutor() {
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "GridRecyclerViewAdapter", "shutdownExecutor", null);
            mExecutor.shutdownNow();
        }

        public ArrayList<ItemInfo> getItemInfoList() {
            return mItems;
        }

        private void clear() {
            if (mItems != null) {
                for (ItemInfo itemInfo : mItems) {
                    if (itemInfo.pinImageThread != null && itemInfo.pinImageThread.isAlive()) {
                        itemInfo.pinImageThread.interrupt();
                    }
                }
                mItems.clear();
            }

            if (mPinStatusSparseArr != null) {
                mPinStatusSparseArr.clear();
            }

            if (mMemoryCache != null) {
                mMemoryCache.evictAll();
            }
        }

        public void setItemData(@Nullable ArrayList<ItemInfo> items) {
            this.mItems = (items == null) ? new ArrayList<ItemInfo>() : items;
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onBindViewHolder(final GridRecyclerViewHolder holder, final int position) {
            final ItemInfo itemInfo = mItems.get(position);
            holder.setItemInfo(itemInfo);
            holder.pinImageView.setVisibility(View.GONE);
            holder.gridTextView.setText(itemInfo.getItemName());
            holder.gridImageView.setImageDrawable(null);

            Bitmap bitmap = mMemoryCache.get(position + "_cache");
            if (bitmap != null) {
                holder.gridImageView.setImageBitmap(bitmap);
            } else {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isPositionVisible(position)) {
                            final Bitmap bitmap = itemInfo.getIconImage();
                            if (bitmap != null) {
                                mMemoryCache.put(position + "_cache", bitmap);
                            }
                            Activity activity = getActivity();
                            if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            holder.gridImageView.setImageBitmap(bitmap);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }

            if (!isSDCard1) {
                holder.pinImageView.setVisibility(View.GONE);
                int status = mPinStatusSparseArr.get(position);
                if (status != LocationStatus.UNKOWN) {
                    displayPinImage(itemInfo, holder);
                } else {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isPositionVisible(position)) {
                                final int status = itemInfo.getLocationStatus();
                                if (itemInfo instanceof AppInfo) {
                                    AppInfo appInfo = (AppInfo) itemInfo;
                                    boolean isAppPinned = HCFSMgmtUtils.isAppPinned(appInfo, mUidDAO);
                                    itemInfo.setPinned(isAppPinned);
                                } else if (itemInfo instanceof DataTypeInfo) {
                                    // DataTypeInfo dataTypeInfo = (DataTypeInfo) item;
                                } else if (itemInfo instanceof FileDirInfo) {
                                    FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                                    boolean isPinned = HCFSMgmtUtils.isPathPinned(fileDirInfo.getFilePath());
                                    itemInfo.setPinned(isPinned);
                                }

                                mPinStatusSparseArr.put(position, status);
                                Activity activity = getActivity();
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            displayPinImage(itemInfo, holder);
                                        }
                                    });
                                }
                            }
                        }
                    });

                }
            }

        }

        private void displayPinImage(ItemInfo itemInfo, GridRecyclerViewHolder holder) {
            if (itemInfo.isPinned()) {
                holder.pinImageView.setVisibility(View.VISIBLE);
            } else {
                holder.pinImageView.setVisibility(View.GONE);
            }
        }

        @Override
        public GridRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_management_grid_item, parent, false);
            return new GridRecyclerViewHolder(view);
        }

        public class GridRecyclerViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {

            protected View rootView;
            protected ImageView gridImageView;
            protected ImageView pinImageView;
            protected TextView gridTextView;
            protected ItemInfo itemInfo;

            public GridRecyclerViewHolder(View itemView) {
                super(itemView);
                rootView = itemView;
                gridImageView = (ImageView) itemView.findViewById(R.id.gridImage);
                pinImageView = (ImageView) itemView.findViewById(R.id.pinImage);
                gridTextView = (TextView) itemView.findViewById(R.id.gridText);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.gridItemLayout) {
                    if (itemInfo instanceof AppInfo) {

                    } else if (itemInfo instanceof DataTypeInfo) {

                    } else if (itemInfo instanceof FileDirInfo) {
                        final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                        if (fileDirInfo.getCurrentFile().isDirectory()) {
                            /** Set the first visible item position to previous FilePathNavigationView when navigating to next directory level */
                            GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
                            int firstVisibleItemPosition = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
                            int childCount = mFilePathNavigationLayout.getChildCount();
                            FilePathNavigationView filePathNavigationViewPrev = (FilePathNavigationView) mFilePathNavigationLayout
                                    .getChildAt(childCount - 1);
                            filePathNavigationViewPrev.setFirstVisibleItemPosition(firstVisibleItemPosition);

							/** Add the current FilePathNavigationView to navigation layout */
                            mCurrentFile = fileDirInfo.getCurrentFile();
                            Activity activity = getActivity();
                            if (activity != null) {
                                FilePathNavigationView filePathNavigationView = new FilePathNavigationView(activity);
                                String currentPath = mCurrentFile.getAbsolutePath();
                                String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
                                filePathNavigationView.setText(navigationText);
                                filePathNavigationView.setCurrentFilePath(currentPath);
                                mFilePathNavigationLayout.addView(filePathNavigationView);
                            }
                            mFilePathNavigationScrollView.post(new Runnable() {
                                public void run() {
                                    mFilePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
                                }
                            });

							/** Show the file list of the entered directory */
                            showTypeContent(R.string.file_management_spinner_files);
                        } else {
                            Activity activity = getActivity();
                            if (activity != null) {
                                /** Build the intent */
                                String mimeType = fileDirInfo.getMimeType();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                if (mimeType != null) {
                                    intent.setDataAndType(Uri.fromFile(fileDirInfo.getCurrentFile()), mimeType);
                                } else {
                                    intent.setData(Uri.fromFile(fileDirInfo.getCurrentFile()));
                                }

                                /** Verify it resolves */
                                PackageManager packageManager = activity.getPackageManager();
                                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                                boolean isIntentSafe = activities.size() > 0;

                                /** Start an activity if it's safe */
                                if (isIntentSafe) {
                                    startActivity(intent);
                                } else {
                                    View view = getView();
                                    if (view != null) {
                                        Snackbar.make(view, getString(R.string.file_management_snackbar_unknown_type_file), Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public boolean onLongClick(View v) {
                Activity activity = getActivity();
                if (activity == null) {
                    return true;
                }

                PopupMenu popupMenu = new PopupMenu(activity, v);
                final boolean isPinned = !itemInfo.isPinned();
                if (!isSDCard1) {
                    if (isPinned) {
                        popupMenu.getMenu().add(getString(R.string.file_management_popup_menu_pin));
                    } else {
                        popupMenu.getMenu().add(getString(R.string.file_management_popup_menu_unpin));
                    }
                }
                popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (!isSDCard1) {
                            if (itemInfo instanceof AppInfo) {
                                final AppInfo appInfo = (AppInfo) itemInfo;
                                final boolean isPinned = !appInfo.isPinned();
                                appInfo.setPinned(isPinned);
                                appInfo.setLastProcessTime(System.currentTimeMillis());
                                mWaitToExecuteSparseArr.put(appInfo.hashCode(), appInfo);
                                setGridItemPinUnpinIcon(isPinned);
                            } else if (itemInfo instanceof DataTypeInfo) {
                                DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
                                boolean isPinned = !dataTypeInfo.isPinned();
                                dataTypeInfo.setPinned(isPinned);
                                dataTypeInfo.setLastProcessTime(System.currentTimeMillis());
                                mWaitToExecuteSparseArr.put(dataTypeInfo.hashCode(), dataTypeInfo);
                                setGridItemPinUnpinIcon(isPinned);
                            } else if (itemInfo instanceof FileDirInfo) {
                                FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                                final boolean isPinned = !fileDirInfo.isPinned();
                                fileDirInfo.setPinned(isPinned);
                                fileDirInfo.setLastProcessTime(System.currentTimeMillis());
                                mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
                                setGridItemPinUnpinIcon(isPinned);
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

            private void setGridItemPinUnpinIcon(boolean isPinned) {
                if (isPinned) {
                    pinImageView.setVisibility(View.VISIBLE);
                } else {
                    pinImageView.setVisibility(View.GONE);
                }
            }

        }

    }

    public class LinearRecyclerViewAdapter extends RecyclerView.Adapter<LinearRecyclerViewAdapter.LinearRecyclerViewHolder> {

        private ArrayList<ItemInfo> mItemInfoList;
        private ThreadPoolExecutor mExecutor;
        private LruCache<String, Bitmap> mMemoryCache;
        // private ExecutorService executor;

        public LinearRecyclerViewAdapter() {
            mItemInfoList = new ArrayList<>();
            init();
        }

        public LinearRecyclerViewAdapter(@Nullable ArrayList<ItemInfo> items) {
            this.mItemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
            init();
        }

        public void init() {
            // executor = Executors.newCachedThreadPool();
            int N = Runtime.getRuntime().availableProcessors();
            mExecutor = new ThreadPoolExecutor(N, N * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            mExecutor.allowCoreThreadTimeOut(true);
            mExecutor.prestartCoreThread();

            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8;
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    /** The cache size will be measured in kilobytes rather than number of items. */
                    return bitmap.getByteCount() / 1024;
                }

            };
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
            final ItemInfo itemInfo = mItemInfoList.get(position);
            itemInfo.setViewHolder(holder);
            holder.setItemInfo(itemInfo);
            holder.itemName.setText(itemInfo.getItemName());
//            holder.imageView.setImageDrawable(null);
            if (itemInfo instanceof DataTypeInfo) {
                final DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
                if (dataTypeInfo.isPinned()) {
                    if (dataTypeInfo.getDatePinned() != 0) {
                        Date date = new Date(dataTypeInfo.getDatePinned());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                        String displayTime = sdf.format(date);
                        String displayText = String.format(getString(R.string.file_management_date_since_pinned), displayTime);
                        holder.datePinnedTextView.setText(displayText);
                        holder.datePinnedTextView.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                holder.datePinnedTextView.setVisibility(View.GONE);
            }

            Bitmap bitmap = mMemoryCache.get(position + "_cache");
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isPositionVisible(position)) {
                            final Bitmap bitmap = itemInfo.getIconImage();
                            if (bitmap != null) {
                                mMemoryCache.put(position + "_cache", bitmap);
                            }
                            Activity activity = getActivity();
                            if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
                                if (activity != null) {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            holder.imageView.setImageBitmap(bitmap);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }

            if (!isSDCard1) {
                holder.pinView.setImageDrawable(null);
                Integer status = mPrevLocationStatusSparseArr.get(position + 1);
                Boolean isPinned = mPrevPinStatusSparseArr.get(position + 1);
                if (isPinned != null && isPinned.booleanValue() && status != null && status == LocationStatus.LOCAL) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        holder.pinView.setImageDrawable(HCFSMgmtUtils.getPinUnpinImage(activity, itemInfo.isPinned(), status));
                        holder.pinView.setContentDescription(getPinViewContentDescription(itemInfo.isPinned(), status));
                    }
                } else {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (isPositionVisible(position)) {
                                final int status = itemInfo.getLocationStatus();
                                if (itemInfo instanceof AppInfo) {
                                    AppInfo appInfo = (AppInfo) itemInfo;
                                    boolean isAppPinned = HCFSMgmtUtils.isAppPinned(appInfo, mUidDAO);
                                    itemInfo.setPinned(isAppPinned);
                                } else if (itemInfo instanceof DataTypeInfo) {
                                    /** the pin status of DataTypeInfo has got in getListOfDataType() */
                                } else if (itemInfo instanceof FileDirInfo) {
                                    FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                                    boolean isPinned = HCFSMgmtUtils.isPathPinned(fileDirInfo.getFilePath());
                                    itemInfo.setPinned(isPinned);
                                }

                                mPrevPinStatusSparseArr.put(position + 1, itemInfo.isPinned());
                                mPrevLocationStatusSparseArr.put(position + 1, status);
                                final Activity activity = getActivity();
                                if (holder.getItemInfo().getItemName().equals(itemInfo.getItemName())) {
                                    if (activity != null) {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Drawable pinDrawable = HCFSMgmtUtils.getPinUnpinImage(activity, itemInfo.isPinned(), status);
                                                holder.pinView.setImageDrawable(pinDrawable);
                                                holder.pinView.setContentDescription(getPinViewContentDescription(itemInfo.isPinned(), status));
                                            }
                                        });
                                    }
                                }

                            }
                        }
                    });
                }
            } else {
                holder.pinView.setVisibility(View.GONE);
            }
        }

        @Nullable
        @Override
        public LinearRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Activity activity = getActivity();
            if (activity != null) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_management_linear_item, parent, false);
                return new LinearRecyclerViewHolder(activity, view);
            }
            return null;
        }

        private void setItemData(@Nullable ArrayList<ItemInfo> items) {
            this.mItemInfoList = (items == null) ? new ArrayList<ItemInfo>() : items;
        }

        private void clear() {
            if (mItemInfoList != null) {
                for (ItemInfo itemInfo : mItemInfoList) {
                    if (itemInfo.pinImageThread != null) {
                        itemInfo.pinImageThread.interrupt();
                    }
                }
                mItemInfoList.clear();
            }

            if (mPrevLocationStatusSparseArr != null) {
                mPrevLocationStatusSparseArr.clear();
            }

            if (mPrevPinStatusSparseArr != null) {
                mPrevPinStatusSparseArr.clear();
            }

            if (mMemoryCache != null) {
                mMemoryCache.evictAll();
            }
        }

        public class LinearRecyclerViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

            private Activity mActivity;
            private String id = UUID.randomUUID().toString();
            protected View rootView;
            protected TextView itemName;
            protected ImageView imageView;
            protected ImageView pinView;
            protected TextView datePinnedTextView;
            protected ItemInfo itemInfo;

            public LinearRecyclerViewHolder(Activity activity, View itemView) {
                super(itemView);
                mActivity = activity;
                rootView = itemView;
                itemName = (TextView) itemView.findViewById(R.id.itemName);
                imageView = (ImageView) itemView.findViewById(R.id.iconView);
                pinView = (ImageView) itemView.findViewById(R.id.pinView);
                datePinnedTextView = (TextView) itemView.findViewById(R.id.datePinned);

                pinView.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }

            public void setItemInfo(ItemInfo itemInfo) {
                this.itemInfo = itemInfo;
            }

            public ItemInfo getItemInfo() {
                return itemInfo;
            }

            public String getId() {
                return id;
            }

            private void displayPinImageOnClick(final ItemInfo itemInfo) {
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        final Drawable drawable = itemInfo.getPinUnpinImage();
                        mActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                pinView.setImageDrawable(drawable);
                            }
                        });
                    }
                });
            }

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.pinView) {
                    if (itemInfo instanceof AppInfo) {
                        final AppInfo appInfo = (AppInfo) itemInfo;
                        boolean isPinned = !appInfo.isPinned();
                        appInfo.setPinned(isPinned);
                        appInfo.setLastProcessTime(System.currentTimeMillis());
                        mWaitToExecuteSparseArr.put(appInfo.hashCode(), appInfo);
                        displayPinImageOnClick(appInfo);
                    } else if (itemInfo instanceof DataTypeInfo) {
                        Activity activity = getActivity();
                        if (activity == null) {
                            return;
                        }

                        final DataTypeInfo dataTypeInfo = (DataTypeInfo) itemInfo;
                        final boolean isPinned = !dataTypeInfo.isPinned();
                        dataTypeInfo.setPinned(isPinned);
                        dataTypeInfo.setDateUpdated(0);
                        if (isPinned) {
                            pinView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.pinned));
                        } else {
                            pinView.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.unpinned_local));
                        }

                        final String dataTypeText;
                        if (dataTypeInfo.getDataType().equals(DataTypeDAO.DATA_TYPE_IMAGE)) {
                            dataTypeText = getString(R.string.file_management_list_data_type_image);
                        } else if (dataTypeInfo.getDataType().equals(DataTypeDAO.DATA_TYPE_VIDEO)) {
                            dataTypeText = getString(R.string.file_management_list_data_type_video);
                        } else if (dataTypeInfo.getDataType().equals(DataTypeDAO.DATA_TYPE_AUDIO)) {
                            dataTypeText = getString(R.string.file_management_list_data_type_audio);
                        } else {
                            dataTypeText = "";
                        }

                        if (!dataTypeText.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle(dataTypeText);
                            if (dataTypeInfo.isPinned()) {
                                builder.setMessage(getString(R.string.file_management_alert_dialog_message_pin_datatype));
                                builder.setPositiveButton(getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dataTypeInfo.setDateUpdated(0);
                                        dataTypeInfo.setDatePinned(0);
                                        mExecutor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                mDataTypeDAO.update(dataTypeInfo);
                                            }
                                        });
                                    }
                                });
                                builder.setNegativeButton(getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        long currentTimeMilis = System.currentTimeMillis();
                                        dataTypeInfo.setDateUpdated(currentTimeMilis);
                                        dataTypeInfo.setDatePinned(currentTimeMilis);
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                                        String displayTime = sdf.format(new Date(currentTimeMilis));
                                        String displayText = String.format(getString(R.string.file_management_date_since_pinned), displayTime);
                                        datePinnedTextView.setText(displayText);
                                        datePinnedTextView.setVisibility(View.VISIBLE);
                                        mExecutor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                mDataTypeDAO.update(dataTypeInfo);
                                            }
                                        });
                                    }
                                });
                            } else {
                                builder.setMessage(getString(R.string.file_management_alert_dialog_message_unpin_datatype));
                                builder.setPositiveButton(getString(R.string.alert_dialog_confirm), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dataTypeInfo.setDateUpdated(0);
                                        datePinnedTextView.setVisibility(View.GONE);
                                        mExecutor.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                mDataTypeDAO.update(dataTypeInfo.getDataType(), dataTypeInfo, DataTypeDAO.PIN_STATUS_COLUMN);
                                                mDataTypeDAO.update(dataTypeInfo.getDataType(), dataTypeInfo, DataTypeDAO.DATE_UPDATED_COLUMN);
                                            }
                                        });
                                    }
                                });
                            }
                            builder.setCancelable(false);
                            builder.show();
                        }
                    } else if (itemInfo instanceof FileDirInfo) {
                        final FileDirInfo fileDirInfo = (FileDirInfo) itemInfo;
                        final boolean isPinned = !fileDirInfo.isPinned();
                        if (isPinned) {
                            fileDirInfo.setPinned(isPinned);
                            fileDirInfo.setLastProcessTime(System.currentTimeMillis());
                            mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
                            displayPinImageOnClick(fileDirInfo);
                        } else {
                            if (fileDirInfo.getMimeType() != null) {
                                if (fileDirInfo.getMimeType().contains(DataTypeDAO.DATA_TYPE_IMAGE)) {
                                    if (mDataTypePinStatusMap.get(DataTypeDAO.DATA_TYPE_IMAGE)) {
                                        Activity activity = getActivity();
                                        if (activity != null) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                            builder.setTitle(fileDirInfo.getItemName());
                                            builder.setMessage(getString(R.string.file_management_whether_allowed_to_unpin_image));
                                            builder.setPositiveButton(getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    fileDirInfo.setPinned(isPinned);
                                                    fileDirInfo.setLastProcessTime(System.currentTimeMillis());
                                                    mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
                                                    displayPinImageOnClick(fileDirInfo);
                                                }
                                            });
                                            builder.setNegativeButton(getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.show();
                                            return;
                                        }
                                    }
                                } else if (fileDirInfo.getMimeType().contains(DataTypeDAO.DATA_TYPE_VIDEO)) {
                                    if (mDataTypePinStatusMap.get(DataTypeDAO.DATA_TYPE_VIDEO)) {
                                        Activity activity = getActivity();
                                        if (activity != null) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                            builder.setTitle(fileDirInfo.getItemName());
                                            builder.setMessage(getString(R.string.file_management_whether_allowed_to_unpin_video));
                                            builder.setPositiveButton(getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    fileDirInfo.setPinned(isPinned);
                                                    fileDirInfo.setLastProcessTime(System.currentTimeMillis());
                                                    mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
                                                    displayPinImageOnClick(fileDirInfo);
                                                }
                                            });
                                            builder.setNegativeButton(getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.show();
                                            return;
                                        }
                                    }
                                } else if (fileDirInfo.getMimeType().contains(DataTypeDAO.DATA_TYPE_AUDIO)) {
                                    if (mDataTypePinStatusMap.get(DataTypeDAO.DATA_TYPE_AUDIO)) {
                                        Activity activity = getActivity();
                                        if (activity != null) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                            builder.setTitle(fileDirInfo.getItemName());
                                            builder.setMessage(getString(R.string.file_management_whether_allowed_to_unpin_audio));
                                            builder.setPositiveButton(getString(R.string.alert_dialog_yes), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    fileDirInfo.setPinned(isPinned);
                                                    fileDirInfo.setLastProcessTime(System.currentTimeMillis());
                                                    mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
                                                    displayPinImageOnClick(fileDirInfo);
                                                }
                                            });
                                            builder.setNegativeButton(getString(R.string.alert_dialog_no), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.show();
                                            return;
                                        }
                                    }
                                }
                            }

                            if (fileDirInfo.getFilePath().contains(EXTERNAL_ANDROID_PATH)) {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setTitle(fileDirInfo.getItemName());
                                    builder.setMessage(getString(R.string.file_management_cannot_unpin_files_in_android_folder));
                                    builder.setPositiveButton(getString(R.string.alert_dialog_confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    builder.setCancelable(false);
                                    builder.show();
                                    return;
                                }
                            }
                            fileDirInfo.setPinned(isPinned);
                            fileDirInfo.setLastProcessTime(System.currentTimeMillis());
                            mWaitToExecuteSparseArr.put(fileDirInfo.hashCode(), fileDirInfo);
                            displayPinImageOnClick(fileDirInfo);
                        }
                    }
                } else if (v.getId() == R.id.linearItemLayout) {
                    if (itemInfo instanceof AppInfo) {

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
                            Activity activity = getActivity();
                            if (activity != null) {
                                FilePathNavigationView filePathNavigationView = new FilePathNavigationView(activity);
                                String currentPath = mCurrentFile.getAbsolutePath();
                                String navigationText = currentPath.substring(currentPath.lastIndexOf("/") + 1) + "/";
                                filePathNavigationView.setText(navigationText);
                                filePathNavigationView.setCurrentFilePath(currentPath);
                                mFilePathNavigationLayout.addView(filePathNavigationView);
                            }
                            mFilePathNavigationScrollView.post(new Runnable() {
                                public void run() {
                                    mFilePathNavigationScrollView.fullScroll(View.FOCUS_RIGHT);
                                }
                            });

							/** Show the file list of the entered directory */
                            showTypeContent(R.string.file_management_spinner_files);
                        } else {
                            Activity activity = getActivity();
                            if (activity != null) {
                                /** Build the intent */
                                String mimeType = fileDirInfo.getMimeType();
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                if (mimeType != null) {
                                    intent.setDataAndType(Uri.fromFile(fileDirInfo.getCurrentFile()), mimeType);
                                } else {
                                    intent.setData(Uri.fromFile(fileDirInfo.getCurrentFile()));
                                }

                                /** Verify it resolves */
                                PackageManager packageManager = activity.getPackageManager();
                                List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                                boolean isIntentSafe = activities.size() > 0;

                                /** Start an activity if it's safe */
                                if (isIntentSafe) {
                                    startActivity(intent);
                                } else {
                                    View view = getView();
                                    if (view != null) {
                                        Snackbar.make(view, getString(R.string.file_management_snackbar_unknown_type_file), Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }

                        }
                    }
                }
            }

        }

    }

    public class SectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private boolean mValid = true;
        private static final int SECTION_TYPE = 0;
        @SuppressWarnings("rawtypes")
        private RecyclerView.Adapter mBaseAdapter;
        private SparseArray<Section> mSections = new SparseArray<>();
        private long localStorageSpace = -1;
        private long totalStorageSpace = -1;
        private long availableStorageSpace = -1;
        public boolean isFirstCircleAnimated = true;

        @SuppressWarnings("rawtypes")
        public SectionedRecyclerViewAdapter(RecyclerView.Adapter mBaseAdapter) {
            this.mBaseAdapter = mBaseAdapter;
            registerAdapterDataObserver(mBaseAdapter);
        }

        private void init() {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "SectionedRecyclerViewAdapter", "init", null);
            localStorageSpace = -1;
            totalStorageSpace = -1;
            availableStorageSpace = -1;
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
                ((GridRecyclerViewAdapter) mBaseAdapter).shutdownExcecutor();
            } else {
                ((LinearRecyclerViewAdapter) mBaseAdapter).shutdownExecutor();
            }
        }

        private ArrayList<ItemInfo> getSubAdapterItemInfoList() {
            ArrayList<ItemInfo> itemInfos;
            if (mBaseAdapter instanceof GridRecyclerViewAdapter) {
                itemInfos = ((GridRecyclerViewAdapter) mBaseAdapter).getItemInfoList();
            } else {
                itemInfos = ((LinearRecyclerViewAdapter) mBaseAdapter).getItemInfoList();
            }
            return itemInfos;
        }

        private void setSubAdapterItems(ArrayList<ItemInfo> itemInfos) {
            if (mBaseAdapter instanceof GridRecyclerViewAdapter) {
                ((GridRecyclerViewAdapter) mBaseAdapter).setItemData(itemInfos);
            } else {
                ((LinearRecyclerViewAdapter) mBaseAdapter).setItemData(itemInfos);
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
                    // Log.d(HCFSMgmtUtils.TAG, "onChanged: " + mBaseAdapter.getItemCount());
                    notifyDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount() > 0;
                    // Log.d(HCFSMgmtUtils.TAG, "onItemRangeChanged");
                    notifyItemRangeChanged(positionStart, itemCount);
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount() > 0;
                    // Log.d(HCFSMgmtUtils.TAG, "onItemRangeInserted");
                    notifyItemRangeInserted(positionStart, itemCount);
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount() > 0;
                    // Log.d(HCFSMgmtUtils.TAG, "onItemRangeRemoved");
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

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

            if (isSectionHeaderPosition(position)) {
                if (isFragmentFirstLoaded) {
                    isFragmentFirstLoaded = false;
                    return;
                }

                final SectionedViewHolder sectionViewHolder = (SectionedViewHolder) viewHolder;
                final String calculatingText = getString(R.string.file_management_section_item_calculating);
                mSections.get(position).viewHolder = sectionViewHolder;
                mWorkerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Activity activity = getActivity();
                        if (activity == null) {
                            return;
                        }

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String storageType;
                                if (isSDCard1) {
                                    storageType = getString(R.string.file_management_sdcard1_storatge_name);
                                } else {
                                    storageType = getString(R.string.file_management_internal_storatge_name);
                                }
                                sectionViewHolder.storageType.setText(storageType);
                                sectionViewHolder.totalStorageSpace.setText(calculatingText);
                                sectionViewHolder.availableStorageSpace.setText(calculatingText);
                            }
                        });

                        if (totalStorageSpace == -1 || availableStorageSpace == -1 || localStorageSpace != -1) {
                            StatFs statFs = new StatFs(FILE_ROOT_DIR_PATH);
                            localStorageSpace = totalStorageSpace = statFs.getTotalBytes();
                            availableStorageSpace = statFs.getAvailableBytes();
                        }


                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                float toShowValue = ((totalStorageSpace - availableStorageSpace) * 1f / totalStorageSpace) * 100f;
                                CircleDisplay cd = sectionViewHolder.circleDisplay;
                                int width, height;
                                width = height = (int) (sectionViewHolder.rootView.getHeight() * 0.9);
                                cd.resize(width, height);
                                cd.setValueWidthPercent(25f);
                                cd.setPercentTextSize(16f);
                                cd.setCapacityTextSize(12f);
                                cd.setArcColor(ContextCompat.getColor(activity, R.color.colorFileManagementCircleArc));
                                cd.setWholeCircleColor(ContextCompat.getColor(activity, R.color.colorFileManagementCircleWhole));
                                cd.setPercentTextColor(ContextCompat.getColor(activity, R.color.colorFileManagementCircleText));
                                cd.showValue(toShowValue, 100f, totalStorageSpace, isFirstCircleAnimated);

                                if (isSDCard1) {
                                    sectionViewHolder.totalStorageSpace.setText(UnitConverter.convertByteToProperUnit(totalStorageSpace));
                                    sectionViewHolder.availableStorageSpace.setText(UnitConverter.convertByteToProperUnit(availableStorageSpace));
                                } else {
                                    sectionViewHolder.totalStorageSpace.setText(UnitConverter.convertByteToProperUnit(totalStorageSpace));
                                    sectionViewHolder.availableStorageSpace.setText(UnitConverter.convertByteToProperUnit(availableStorageSpace));
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
                Activity activity = getActivity();
                if (activity != null) {
                    View view = LayoutInflater.from(activity).inflate(R.layout.file_management_sction_item, parent, false);
                    return new SectionedViewHolder(view);
                } else {
                    return null;
                }
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

		/** Close database */
        if (mDataTypeDAO != null) {
            mDataTypeDAO.close();
        }

		/** Stop the threads in the threading pool of executor */
        mSectionedRecyclerViewAdapter.shutdownSubAdapterExecutor();

		/** Stop handlerThread */
        mHandlerThread.quit();
        mHandlerThread.interrupt();
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
            showTypeContent(R.string.file_management_spinner_files);

            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRecyclerView.getLayoutManager().scrollToPosition(firstVisibleItemPosition);
                    }
                });
            }
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
        String selctedItemName = mSpinner.getSelectedItem().toString();
        if (selctedItemName.equals(getString(R.string.file_management_spinner_files))) {
            if (!mCurrentFile.getAbsolutePath().equals(FILE_ROOT_DIR_PATH)) {
                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.no_file_layout).setVisibility(View.GONE);
                }

                mCurrentFile = mCurrentFile.getParentFile();
                int childCount = mFilePathNavigationLayout.getChildCount();
                FilePathNavigationView filePathNavigationView = (FilePathNavigationView) mFilePathNavigationLayout.getChildAt(childCount - 2);
                final int firstVisibleItemPosition = filePathNavigationView.firstVisibleItemPosition;
                mFilePathNavigationLayout.removeViewAt(childCount - 1);
                showTypeContent(R.string.file_management_spinner_files);

                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollToPosition(firstVisibleItemPosition);
                        }
                    });
                }
            } else {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, DashboardFragment.newInstance(), DashboardFragment.TAG);
                ft.commit();
            }
        } else {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, DashboardFragment.newInstance(), DashboardFragment.TAG);
            ft.commit();
        }
        return true;
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
        return selectedItemName.equals(getString(typeStringResourceID));
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

}
