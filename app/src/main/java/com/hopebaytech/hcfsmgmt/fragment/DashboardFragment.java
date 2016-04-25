package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.util.Locale;

public class DashboardFragment extends Fragment {

    public static String TAG = DashboardFragment.class.getSimpleName();
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

    private NetworkBroadcastReceiver mNetworkStatusReceiver;
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

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
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
        mNetworkStatusReceiver = new NetworkBroadcastReceiver();
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
            String cloudStorageUsage = String.format(Locale.getDefault(), "%s / %s", mStatInfo.getVolUsed(), mStatInfo.getCloudTotal());
            outState.putString(KEY_CLOUD_STORAGE_USAGE, cloudStorageUsage);
            outState.putInt(KEY_CLOUD_STORAGE_USAGE_PROGRESS, mStatInfo.getCloudUsedPercentage());
            outState.putInt(KEY_CLOUD_STORAGE_USAGE_SECONDARY_PROGRESS, 0);

            outState.putString(KEY_PINNED_STORAGE_USAGE, mStatInfo.getPinTotal());
            outState.putInt(KEY_PINNED_STORAGE_USAGE_PROGRESS, mStatInfo.getPinnedUsedPercentage());
            outState.putInt(KEY_PINNED_STORAGE_USAGE_SECONDARY_PROGRESS, 0);

            outState.putString(KEY_WAIT_TO_UPLOAD_DATA_USAGE, mStatInfo.getCacheDirtyUsed());
            outState.putInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_PROGRESS, mStatInfo.getDirtyPercentage());
            outState.putInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_SECONDARY_PROGRESS, 0);

            outState.putString(KEY_NETWORK_XFER_UP, mStatInfo.getXferUpload());
            outState.putString(KEY_NETWORK_XFER_DOWNLOAD, mStatInfo.getXferDownload());
            outState.putInt(KEY_NETWORK_XFER_PROGRESS, mStatInfo.getXterDownloadPercentage());
            outState.putInt(KEY_NETWORK_XFER_SECONDARY_PROGRESS, 100);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onCreateView", null);
        return inflater.inflate(R.layout.dashboard_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onViewCreated", null);

        mNetworkConnStatusImage = (ImageView) view.findViewById(R.id.network_conn_status_icon);
        mNetworkConnStatusText = (TextView) view.findViewById(R.id.network_conn_status);

        LinearLayout cloudStorage = (LinearLayout) view.findViewById(R.id.cloud_storage);
        mCloudStorageUsage = (TextView) cloudStorage.findViewById(R.id.textViewUsage);
        mCloudStorageProgressBar = (ProgressBar) cloudStorage.findViewById(R.id.progressBar);
        TextView cloudStorageTitle = (TextView) cloudStorage.findViewById(R.id.textViewTitle);
        cloudStorageTitle.setText(mContext.getString(R.string.dashboard_used_space));
        ImageView cloudStorageImageView = (ImageView) cloudStorage.findViewById(R.id.iconView);
        cloudStorageImageView.setImageResource(R.drawable.icon_system_used_space);

        LinearLayout pinnedStorage = (LinearLayout) view.findViewById(R.id.pinned_storage);
        mPinnedStorageUsage = (TextView) pinnedStorage.findViewById(R.id.textViewUsage);
        mPinnedStorageProgressBar = (ProgressBar) pinnedStorage.findViewById(R.id.progressBar);
        mPinnedStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.storage_progressbar));
        TextView pinnedStorageTitle = (TextView) pinnedStorage.findViewById(R.id.textViewTitle);
        pinnedStorageTitle.setText(mContext.getString(R.string.dashboard_pinned_storage));
        ImageView pinnedStorageImageView = (ImageView) pinnedStorage.findViewById(R.id.iconView);
        pinnedStorageImageView.setImageResource(R.drawable.icon_system_pinned_space);

        LinearLayout waitToUploadData = (LinearLayout) view.findViewById(R.id.to_be_upload_data);
        mWaitToUploadDataUsage = (TextView) waitToUploadData.findViewById(R.id.textViewUsage);
        mWaitToUploadDataUsageProgressBar = (ProgressBar) waitToUploadData.findViewById(R.id.progressBar);
        mWaitToUploadDataUsageProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.storage_progressbar));
        TextView waitToUploadDataTitle = (TextView) waitToUploadData.findViewById(R.id.textViewTitle);
        waitToUploadDataTitle.setText(mContext.getString(R.string.dashboard_data_to_be_uploaded));
        ImageView waitToUploadDataUsageImageView = (ImageView) waitToUploadData.findViewById(R.id.iconView);
        waitToUploadDataUsageImageView.setImageResource(R.drawable.icon_system_upload_data);

        LinearLayout data_transmission_today = (LinearLayout) view.findViewById(R.id.network_xfer_today);
        mNetworkXferUp = (TextView) data_transmission_today.findViewById(R.id.xfer_up);
        mNetworkXferDown = (TextView) data_transmission_today.findViewById(R.id.xfer_down);
        mXferProgressBar = (ProgressBar) data_transmission_today.findViewById(R.id.progressBar);
        mXferProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.xfer_progressbar));
        TextView network_xfer_today_title = (TextView) data_transmission_today.findViewById(R.id.textViewTitle);
        network_xfer_today_title.setText(mContext.getString(R.string.dashboard_data_transmission_today));
        ImageView networkXferImageView = (ImageView) data_transmission_today.findViewById(R.id.iconView);
        networkXferImageView.setImageResource(R.drawable.icon_system_transmitting);

        if (savedInstanceState != null) {
            mCloudStorageUsage.setText(savedInstanceState.getString(KEY_CLOUD_STORAGE_USAGE));
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onViewCreated", "CloudStorageProgressBar=" + savedInstanceState.getInt(KEY_CLOUD_STORAGE_USAGE_PROGRESS));
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onViewCreated", "CloudStorageSecondaryProgressBar=" + savedInstanceState.getInt(KEY_CLOUD_STORAGE_USAGE_SECONDARY_PROGRESS));
            mCloudStorageProgressBar.setProgress(savedInstanceState.getInt(KEY_CLOUD_STORAGE_USAGE_PROGRESS));
            mCloudStorageProgressBar.setSecondaryProgress(savedInstanceState.getInt(KEY_CLOUD_STORAGE_USAGE_SECONDARY_PROGRESS));

            mPinnedStorageUsage.setText(savedInstanceState.getString(KEY_PINNED_STORAGE_USAGE));
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onViewCreated", "PinnedStorageProgressBar=" + savedInstanceState.getInt(KEY_PINNED_STORAGE_USAGE_PROGRESS));
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onViewCreated", "PinnedStorageSecondaryProgressBar=" + savedInstanceState.getInt(KEY_PINNED_STORAGE_USAGE_SECONDARY_PROGRESS));
            mPinnedStorageProgressBar.setProgress(savedInstanceState.getInt(KEY_PINNED_STORAGE_USAGE_PROGRESS));
            mPinnedStorageProgressBar.setSecondaryProgress(savedInstanceState.getInt(KEY_PINNED_STORAGE_USAGE_SECONDARY_PROGRESS));

            mWaitToUploadDataUsage.setText(savedInstanceState.getString(KEY_WAIT_TO_UPLOAD_DATA_USAGE));
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onViewCreated", "WaitToUploadDataUsageProgressBar=" + savedInstanceState.getInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_PROGRESS));
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onViewCreated", "WaitToUploadDataUsageSecondaryProgressBar=" + savedInstanceState.getInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_SECONDARY_PROGRESS));
            mWaitToUploadDataUsageProgressBar.setProgress(savedInstanceState.getInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_PROGRESS));
            mWaitToUploadDataUsageProgressBar.setSecondaryProgress(savedInstanceState.getInt(KEY_WAIT_TO_UPLOAD_DATA_USAGE_SECONDARY_PROGRESS));

            mNetworkXferUp.setText(savedInstanceState.getString(KEY_NETWORK_XFER_UP));
            mNetworkXferDown.setText(savedInstanceState.getString(KEY_NETWORK_XFER_DOWNLOAD));
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onViewCreated", "XferProgressBar=" + savedInstanceState.getInt(KEY_NETWORK_XFER_PROGRESS));
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onViewCreated", "XferSecondaryProgressBar=" + savedInstanceState.getInt(KEY_NETWORK_XFER_SECONDARY_PROGRESS));
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
                                    String storageUsageText = String.format(Locale.getDefault(), "%s / %s", mStatInfo.getVolUsed(), mStatInfo.getCloudTotal());
                                    mCloudStorageUsage.setText(storageUsageText);
                                    mCloudStorageProgressBar.setProgress(mStatInfo.getCloudUsedPercentage());
                                    mCloudStorageProgressBar.setSecondaryProgress(0);

                                    mPinnedStorageUsage.setText(mStatInfo.getPinTotal());
                                    mPinnedStorageProgressBar.setProgress(mStatInfo.getPinnedUsedPercentage());
                                    mPinnedStorageProgressBar.setSecondaryProgress(0);

                                    mWaitToUploadDataUsage.setText(mStatInfo.getCacheDirtyUsed());
                                    mWaitToUploadDataUsageProgressBar.setProgress(mStatInfo.getDirtyPercentage());
                                    mWaitToUploadDataUsageProgressBar.setSecondaryProgress(0);

                                    String xferDownload = mStatInfo.getXferDownload();
                                    String xferUpload = mStatInfo.getXferUpload();
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
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "Runnable", "run", "UiRefreshThread is interrupted");
                        break;
                    }
                }
            }
        };

        displayNetworkStatus();
    }

    public class NetworkBroadcastReceiver extends BroadcastReceiver {

        private boolean isRegister = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                displayNetworkStatus();
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

    @Override
    public void onResume() {
        super.onResume();
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onResume", null);
        if (mNetworkStatusReceiver != null) {
            if (mIsCurrentVisible) {
                IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                mNetworkStatusReceiver.registerReceiver(mContext, filter);
            }
        }
        if (mUiRefreshThread == null) {
            if (mUiRefreshRunnable != null) {
                if (mIsCurrentVisible) {
                    mUiRefreshThread = new Thread(mUiRefreshRunnable);
                    mUiRefreshThread.start();
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onPause", "UiRefreshThread is started");
                }
            }
        }
    }

    @Override
    public void onPause() {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onPause", null);
        if (mNetworkStatusReceiver != null) {
            mNetworkStatusReceiver.unregisterReceiver(mContext);
        }
        if (mUiRefreshThread != null && !mUiRefreshThread.isInterrupted()) {
            mUiRefreshThread.interrupt();
            mUiRefreshThread = null;
        }
        super.onPause();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "setMenuVisibility", null);
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            mIsCurrentVisible = true;
            if (mNetworkStatusReceiver != null) {
                IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                mNetworkStatusReceiver.registerReceiver(mContext, filter);
            }
            if (mUiRefreshThread == null) {
                if (mUiRefreshRunnable != null) {
                    mUiRefreshThread = new Thread(mUiRefreshRunnable);
                    mUiRefreshThread.start();
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "setMenuVisibility", "UiRefreshThread is started");
                }
            }
        } else {
            mIsCurrentVisible = false;
            if (mNetworkStatusReceiver != null) {
                mNetworkStatusReceiver.unregisterReceiver(mContext);
            }
            if (mUiRefreshThread != null && !mUiRefreshThread.isInterrupted()) {
                mUiRefreshThread.interrupt();
                mUiRefreshThread = null;
            }
        }
    }

    private void displayNetworkStatus() {
        View view = getView();
        if (view != null) {
            ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            mNetworkConnStatusImage = (ImageView) view.findViewById(R.id.network_conn_status_icon);
            mNetworkConnStatusText = (TextView) view.findViewById(R.id.network_conn_status);
            if (netInfo != null) {
                if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is connected");
                    mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_normal);
                    mNetworkConnStatusText.setText(mContext.getString(R.string.dashboard_network_status_connected));
                } else if (netInfo.getState() == NetworkInfo.State.CONNECTING) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is connecting");
                    mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_not_allow);
                    mNetworkConnStatusText.setText(mContext.getString(R.string.dashboard_network_status_connecting));
                } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is disconnected");
                    mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_failed);
                    mNetworkConnStatusText.setText(mContext.getString(R.string.dashboard_network_status_disconnected));
                }
            } else {
                mNetworkConnStatusImage.setImageResource(R.drawable.icon_transmission_failed);
                mNetworkConnStatusText.setText(mContext.getString(R.string.dashboard_network_status_disconnected));
            }
        }
    }

}
