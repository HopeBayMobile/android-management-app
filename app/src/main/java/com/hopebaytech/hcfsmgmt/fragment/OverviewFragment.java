package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;

import java.util.Locale;

public class OverviewFragment extends Fragment {

    public static String TAG = OverviewFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private final String KEY_CLOUD_STORAGE_USAGE = "cloud_storage_usage";
    private final String KEY_CLOUD_STORAGE_USAGE_PROGRESS = "cloud_storage_usage_progress";
    private final String KEY_CLOUD_STORAGE_USAGE_SECONDARY_PROGRESS = "cloud_storage_usage_secondary_progress";
    private final String KEY_PINNED_STORAGE_USAGE = "pinned_storage_usage";
    private final String KEY_PINNED_STORAGE_USAGE_PROGRESS = "pinned_storage_usage_progress";
    private final String KEY_PINNED_STORAGE_USAGE_SECONDARY_PROGRESS = "pinned_storage_usage_secondary_progress";
    private final String KEY_WAIT_TO_UPLOAD_DATA_USAGE = "wait_to_upload_data_usage";
    private final String KEY_WAIT_TO_UPLOAD_DATA_USAGE_PROGRESS = "wait_to_upload_data_usage_progress";
    private final String KEY_WAIT_TO_UPLOAD_DATA_USAGE_SECONDARY_PROGRESS = "wait_to_upload_data_usage_secondary_progress";
    private final String KEY_NETWORK_XFER_UP = "network_xfer_up";
    private final String KEY_NETWORK_XFER_DOWNLOAD = "network_xfer_download";
    private final String KEY_NETWORK_XFER_PROGRESS = "network_xfer_progress";
    private final String KEY_NETWORK_XFER_SECONDARY_PROGRESS = "network_xfer_secondary_progress";

    private Thread mUiRefreshThread;
    private Runnable mUiRefreshRunnable;
    private ImageView mNetworkConnStatusImage;
    private TextView mNetworkConnStatusText;
    private TextView mCloudStorageUsage;
    private ProgressBar mCloudStorageProgressBar;
    private TextView mPinnedStorageUsage;
    private ProgressBar mPinnedStorageProgressBar;
    private TextView mWaitToUploadDataUsage;
    private ProgressBar mWaitToUploadDataUsageProgressBar;
    private TextView mNetworkXferUp;
    private TextView mNetworkXferDown;
    private ProgressBar mXferProgressBar;
    private boolean mIsCurrentVisible;
    private Context mContext;
    private HCFSStatInfo mStatInfo;

    public static OverviewFragment newInstance() {
        return new OverviewFragment();
    }

    @Override
    public void onAttach(Context context) {
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onAttach", null);
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreate", null);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onViewStateRestored", null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onSaveInstanceState", null);
        super.onSaveInstanceState(outState);

        if (mStatInfo != null) {
            String cloudStorageUsage = String.format(Locale.getDefault(), "%s / %s", mStatInfo.getFormatVolUsed(), mStatInfo.getFormatCloudTotal());
            outState.putString(KEY_CLOUD_STORAGE_USAGE, cloudStorageUsage);
            outState.putInt(KEY_CLOUD_STORAGE_USAGE_PROGRESS, mStatInfo.getCloudUsedPercentage());
            outState.putInt(KEY_CLOUD_STORAGE_USAGE_SECONDARY_PROGRESS, 0);

            outState.putString(KEY_PINNED_STORAGE_USAGE, mStatInfo.getFormatPinTotal());
            outState.putInt(KEY_PINNED_STORAGE_USAGE_PROGRESS, mStatInfo.getPinnedUsedPercentage());
            outState.putInt(KEY_PINNED_STORAGE_USAGE_SECONDARY_PROGRESS, 0);

            outState.putString(KEY_WAIT_TO_UPLOAD_DATA_USAGE, mStatInfo.getFormatCacheDirtyUsed());
            outState.putInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_PROGRESS, mStatInfo.getDirtyPercentage());
            outState.putInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_SECONDARY_PROGRESS, 0);

            outState.putString(KEY_NETWORK_XFER_UP, mStatInfo.getFormatXferUpload());
            outState.putString(KEY_NETWORK_XFER_DOWNLOAD, mStatInfo.getFormatXferDownload());
            outState.putInt(KEY_NETWORK_XFER_PROGRESS, mStatInfo.getXterDownloadPercentage());
            outState.putInt(KEY_NETWORK_XFER_SECONDARY_PROGRESS, 100);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateView", null);
        return inflater.inflate(R.layout.overview_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onViewCreated", null);

        mNetworkConnStatusImage = (ImageView) view.findViewById(R.id.network_conn_status_icon);
        mNetworkConnStatusText = (TextView) view.findViewById(R.id.network_conn_status);

        RelativeLayout cloudStorage = (RelativeLayout) view.findViewById(R.id.cloud_storage);
        mCloudStorageUsage = (TextView) cloudStorage.findViewById(R.id.textViewUsage);
        mCloudStorageUsage.setContentDescription(getString(R.string.overview_used_space));
        mCloudStorageProgressBar = (ProgressBar) cloudStorage.findViewById(R.id.progressBar);
        TextView cloudStorageTitle = (TextView) cloudStorage.findViewById(R.id.textViewTitle);
        cloudStorageTitle.setText(mContext.getString(R.string.overview_used_space));
        ImageView cloudStorageImageView = (ImageView) cloudStorage.findViewById(R.id.iconView);
        cloudStorageImageView.setImageResource(R.drawable.icon_system_used_space);

        RelativeLayout pinnedStorage = (RelativeLayout) view.findViewById(R.id.pinned_storage);
        mPinnedStorageUsage = (TextView) pinnedStorage.findViewById(R.id.textViewUsage);
        mPinnedStorageUsage.setContentDescription(getString(R.string.overview_pinned_storage));
        mPinnedStorageProgressBar = (ProgressBar) pinnedStorage.findViewById(R.id.progressBar);
        mPinnedStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.storage_progressbar));
        TextView pinnedStorageTitle = (TextView) pinnedStorage.findViewById(R.id.textViewTitle);
        pinnedStorageTitle.setText(mContext.getString(R.string.overview_pinned_storage));
        ImageView pinnedStorageImageView = (ImageView) pinnedStorage.findViewById(R.id.iconView);
        pinnedStorageImageView.setImageResource(R.drawable.icon_system_pinned_space);

        RelativeLayout waitToUploadData = (RelativeLayout) view.findViewById(R.id.to_be_upload_data);
        mWaitToUploadDataUsage = (TextView) waitToUploadData.findViewById(R.id.textViewUsage);
        mWaitToUploadDataUsage.setContentDescription(getString(R.string.overview_data_to_be_uploaded));
        mWaitToUploadDataUsageProgressBar = (ProgressBar) waitToUploadData.findViewById(R.id.progressBar);
        mWaitToUploadDataUsageProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.storage_progressbar));
        TextView waitToUploadDataTitle = (TextView) waitToUploadData.findViewById(R.id.textViewTitle);
        waitToUploadDataTitle.setText(mContext.getString(R.string.overview_data_to_be_uploaded));
        ImageView waitToUploadDataUsageImageView = (ImageView) waitToUploadData.findViewById(R.id.iconView);
        waitToUploadDataUsageImageView.setImageResource(R.drawable.icon_system_upload_data);

        RelativeLayout data_transmission_today = (RelativeLayout) view.findViewById(R.id.network_xfer_today);
        mNetworkXferUp = (TextView) data_transmission_today.findViewById(R.id.xfer_up);
        mNetworkXferDown = (TextView) data_transmission_today.findViewById(R.id.xfer_down);
        mXferProgressBar = (ProgressBar) data_transmission_today.findViewById(R.id.progressBar);
        mXferProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.xfer_progressbar));
        TextView network_xfer_today_title = (TextView) data_transmission_today.findViewById(R.id.textViewTitle);
        network_xfer_today_title.setText(mContext.getString(R.string.overview_data_transmission_today));
        ImageView networkXferImageView = (ImageView) data_transmission_today.findViewById(R.id.iconView);
        networkXferImageView.setImageResource(R.drawable.icon_system_transmitting);

        if (savedInstanceState != null) {
            mCloudStorageUsage.setText(savedInstanceState.getString(KEY_CLOUD_STORAGE_USAGE));
            mCloudStorageProgressBar.setProgress(savedInstanceState.getInt(KEY_CLOUD_STORAGE_USAGE_PROGRESS));
            mCloudStorageProgressBar.setSecondaryProgress(savedInstanceState.getInt(KEY_CLOUD_STORAGE_USAGE_SECONDARY_PROGRESS));

            mPinnedStorageUsage.setText(savedInstanceState.getString(KEY_PINNED_STORAGE_USAGE));
            mPinnedStorageProgressBar.setProgress(savedInstanceState.getInt(KEY_PINNED_STORAGE_USAGE_PROGRESS));
            mPinnedStorageProgressBar.setSecondaryProgress(savedInstanceState.getInt(KEY_PINNED_STORAGE_USAGE_SECONDARY_PROGRESS));

            mWaitToUploadDataUsage.setText(savedInstanceState.getString(KEY_WAIT_TO_UPLOAD_DATA_USAGE));
            mWaitToUploadDataUsageProgressBar.setProgress(savedInstanceState.getInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_PROGRESS));
            mWaitToUploadDataUsageProgressBar.setSecondaryProgress(savedInstanceState.getInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_SECONDARY_PROGRESS));

            mNetworkXferUp.setText(savedInstanceState.getString(KEY_NETWORK_XFER_UP));
            mNetworkXferDown.setText(savedInstanceState.getString(KEY_NETWORK_XFER_DOWNLOAD));
            mXferProgressBar.setProgress(savedInstanceState.getInt(KEY_NETWORK_XFER_PROGRESS));
            mXferProgressBar.setSecondaryProgress(savedInstanceState.getInt(KEY_NETWORK_XFER_SECONDARY_PROGRESS));
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityCreated", null);

        mUiRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        mStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mStatInfo != null) {
                                    displayNetworkStatus(HCFSConnStatus.getConnStatus(mContext, mStatInfo));

                                    String storageUsageText = String.format(Locale.getDefault(), "%s / %s", mStatInfo.getFormatVolUsed(), mStatInfo.getFormatCloudTotal());
                                    mCloudStorageUsage.setText(storageUsageText);
                                    mCloudStorageProgressBar.setProgress(mStatInfo.getCloudUsedPercentage());
                                    mCloudStorageProgressBar.setSecondaryProgress(0);

                                    mPinnedStorageUsage.setText(mStatInfo.getFormatPinTotal());
                                    mPinnedStorageProgressBar.setProgress(mStatInfo.getPinnedUsedPercentage());
                                    mPinnedStorageProgressBar.setSecondaryProgress(0);

                                    mWaitToUploadDataUsage.setText(mStatInfo.getFormatCacheDirtyUsed());
                                    mWaitToUploadDataUsageProgressBar.setProgress(mStatInfo.getDirtyPercentage());
                                    mWaitToUploadDataUsageProgressBar.setSecondaryProgress(0);

                                    String xferDownload = mStatInfo.getFormatXferDownload();
                                    String xferUpload = mStatInfo.getFormatXferUpload();
                                    mNetworkXferUp.setText(xferUpload);
                                    mNetworkXferDown.setText(xferDownload);
                                    mXferProgressBar.setProgress(mStatInfo.getXterDownloadPercentage());
                                    mXferProgressBar.setSecondaryProgress(100);
                                } else {
                                    mCloudStorageUsage.setText("-");
                                    mCloudStorageProgressBar.setProgress(0);

                                    mPinnedStorageUsage.setText("-");
                                    mPinnedStorageProgressBar.setProgress(0);

                                    mWaitToUploadDataUsage.setText("-");
                                    mWaitToUploadDataUsageProgressBar.setProgress(0);

                                    mNetworkXferUp.setText("-");
                                    mNetworkXferDown.setText("-");
                                    mXferProgressBar.setProgress(0);
                                    mXferProgressBar.setSecondaryProgress(0);
                                }
                            }
                        });
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Logs.d(CLASSNAME, "Runnable", "run", "UiRefreshThread is interrupted");
                        break;
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Logs.d(CLASSNAME, "onResume", null);
        if (mUiRefreshThread == null) {
            if (mUiRefreshRunnable != null) {
                if (mIsCurrentVisible) {
                    mUiRefreshThread = new Thread(mUiRefreshRunnable);
                    mUiRefreshThread.start();
                    Logs.d(CLASSNAME, "onPause", "UiRefreshThread is started");
                }
            }
        }
    }

    @Override
    public void onPause() {
        Logs.d(CLASSNAME, "onPause", null);
        if (mUiRefreshThread != null && !mUiRefreshThread.isInterrupted()) {
            mUiRefreshThread.interrupt();
            mUiRefreshThread = null;
        }
        super.onPause();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        Logs.d(CLASSNAME, "setMenuVisibility", null);
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            mIsCurrentVisible = true;
            if (mUiRefreshThread == null) {
                if (mUiRefreshRunnable != null) {
                    mUiRefreshThread = new Thread(mUiRefreshRunnable);
                    mUiRefreshThread.start();
                    Logs.d(CLASSNAME, "setMenuVisibility", "UiRefreshThread is started");
                }
            }
        } else {
            mIsCurrentVisible = false;
            if (mUiRefreshThread != null && !mUiRefreshThread.isInterrupted()) {
                mUiRefreshThread.interrupt();
                mUiRefreshThread = null;
            }
        }
    }

    private void displayNetworkStatus(int connStatus) {
        switch (connStatus) {
            case HCFSConnStatus.TRANS_FAILED:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_failed);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_failed));
                break;
            case HCFSConnStatus.TRANS_NOT_ALLOWED:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_not_allow);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_not_allowed));
                break;
            case HCFSConnStatus.TRANS_NORMAL:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_normal);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_normal));
                break;
            case HCFSConnStatus.TRANS_IN_PROGRESS:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_transmitting);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_in_progress));
                break;
            case HCFSConnStatus.TRANS_SLOW:
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_slow);
                mNetworkConnStatusText.setText(mContext.getString(R.string.overview_hcfs_conn_status_slow));
                break;
        }
    }

}
