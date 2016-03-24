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

public class DashboardFragment extends Fragment {

    public static String TAG = DashboardFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private NetworkBroadcastReceiver mNetworkStatusReceiver;
    private Thread mUiRefreshThread;
    private TextView mCloudStorageUsage;
    private ProgressBar mCloudStorageProgressBar;
    private TextView mPinnedStorageUsage;
    private ProgressBar mPinnedStorageProgressBar;
    private TextView mWaitToUploadDataUsage;
    private ProgressBar mWaitToUploadDataUsageProgressBar;
    private TextView mNetworkXferUp;
    private TextView mNetworkXferDown;
    private ProgressBar mXferProgressBar;
    private Runnable mUiRefreshRunnable;
    private boolean mIsCurrentVisible;
    private Context mContext;

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkStatusReceiver = new NetworkBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view == null) {
            return;
        }

        LinearLayout cloudStorage = (LinearLayout) view.findViewById(R.id.cloud_storage);
        mCloudStorageUsage = (TextView) cloudStorage.findViewById(R.id.textViewUsage);
        mCloudStorageProgressBar = (ProgressBar) cloudStorage.findViewById(R.id.progressBar);
        TextView cloudStorageTitle = (TextView) cloudStorage.findViewById(R.id.textViewTitle);
        cloudStorageTitle.setText(getString(R.string.dashboard_used_space));
        ImageView cloudStorageImageView = (ImageView) cloudStorage.findViewById(R.id.iconView);
        cloudStorageImageView.setImageResource(R.drawable.icon_system_used_space);

        LinearLayout pinnedStorage = (LinearLayout) view.findViewById(R.id.pinned_storage);
        mPinnedStorageUsage = (TextView) pinnedStorage.findViewById(R.id.textViewUsage);
        mPinnedStorageProgressBar = (ProgressBar) pinnedStorage.findViewById(R.id.progressBar);
        mPinnedStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.storage_progressbar));
        TextView pinnedStorageTitle = (TextView) pinnedStorage.findViewById(R.id.textViewTitle);
        pinnedStorageTitle.setText(getString(R.string.dashboard_pinned_storage));
        ImageView pinnedStorageImageView = (ImageView) pinnedStorage.findViewById(R.id.iconView);
        pinnedStorageImageView.setImageResource(R.drawable.icon_system_pinned_space);

        LinearLayout waitToUploadData = (LinearLayout) view.findViewById(R.id.to_be_upload_data);
        mWaitToUploadDataUsage = (TextView) waitToUploadData.findViewById(R.id.textViewUsage);
        mWaitToUploadDataUsageProgressBar = (ProgressBar) waitToUploadData.findViewById(R.id.progressBar);
        mWaitToUploadDataUsageProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.storage_progressbar));
        TextView waitToUploadDataTitle = (TextView) waitToUploadData.findViewById(R.id.textViewTitle);
        waitToUploadDataTitle.setText(getString(R.string.dashboard_data_to_be_uploaded));
        ImageView waitToUploadDataUsageImageView = (ImageView) waitToUploadData.findViewById(R.id.iconView);
        waitToUploadDataUsageImageView.setImageResource(R.drawable.icon_system_upload_data);

        LinearLayout data_transmission_today = (LinearLayout) view.findViewById(R.id.network_xfer_today);
        mNetworkXferUp = (TextView) data_transmission_today.findViewById(R.id.xfer_up);
        mNetworkXferDown = (TextView) data_transmission_today.findViewById(R.id.xfer_down);
        mXferProgressBar = (ProgressBar) data_transmission_today.findViewById(R.id.progressBar);
        mXferProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.xfer_progressbar));
        TextView network_xfer_today_title = (TextView) data_transmission_today.findViewById(R.id.textViewTitle);
        network_xfer_today_title.setText(getString(R.string.dashboard_data_transmission_today));
        ImageView networkXferImageView = (ImageView) data_transmission_today.findViewById(R.id.iconView);
        networkXferImageView.setImageResource(R.drawable.icon_system_transmitting);

        mUiRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (statInfo != null) {
                                    mCloudStorageUsage.setText(statInfo.getVolUsed() + " / " + statInfo.getCloudTotal());
                                    mCloudStorageProgressBar.setProgress(statInfo.getCloudUsedPercentage());
                                    mCloudStorageProgressBar.setSecondaryProgress(0);

                                    mPinnedStorageUsage.setText(statInfo.getPinTotal());
                                    mPinnedStorageProgressBar.setProgress(statInfo.getPinnedUsedPercentage());
                                    mPinnedStorageProgressBar.setSecondaryProgress(0);

                                    mWaitToUploadDataUsage.setText(statInfo.getCacheDirtyUsed());
                                    mWaitToUploadDataUsageProgressBar.setProgress(statInfo.getDirtyPercentage());
                                    mWaitToUploadDataUsageProgressBar.setSecondaryProgress(0);

                                    String xferDownload = statInfo.getXferDownload();
                                    String xferUpload = statInfo.getXferUpload();
                                    mNetworkXferUp.setText(xferUpload);
                                    mNetworkXferDown.setText(xferDownload);
                                    if (xferDownload.equals("0B") && xferUpload.equals("0B")) {
                                        mXferProgressBar.setProgress(0);
                                        mXferProgressBar.setSecondaryProgress(0);
                                    } else {
                                        mXferProgressBar.setProgress(statInfo.getXterDownloadPercentage());
                                        mXferProgressBar.setSecondaryProgress(100);
                                    }
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
                        break;
                    }
                }
            }
        };

    }

    public class NetworkBroadcastReceiver extends BroadcastReceiver {

        private boolean isRegister = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                View view = getView();
                if (view != null) {
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
                    ImageView networkConnStatusImage = (ImageView) view.findViewById(R.id.network_conn_status_icon);
                    TextView networkConnStatusText = (TextView) view.findViewById(R.id.network_conn_status);
                    if (netInfo != null) {
                        if (netInfo.getState() == NetworkInfo.State.CONNECTED) {
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is connected");
                            networkConnStatusImage.setImageResource(R.drawable.icon_transmission_normal);
                            networkConnStatusText.setText(getString(R.string.dashboard_network_status_connected));
                        } else if (netInfo.getState() == NetworkInfo.State.CONNECTING) {
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is connecting");
                            networkConnStatusImage.setImageResource(R.drawable.icon_transmission_not_allow);
                            networkConnStatusText.setText(getString(R.string.dashboard_network_status_connecting));
                        } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is disconnected");
                            networkConnStatusImage.setImageResource(R.drawable.icon_transmission_failed);
                            networkConnStatusText.setText(getString(R.string.dashboard_network_status_disconnected));
                        }
                    } else {
                        networkConnStatusImage.setImageResource(R.drawable.icon_transmission_failed);
                        networkConnStatusText.setText(getString(R.string.dashboard_network_status_disconnected));
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

    @Override
    public void onResume() {
        super.onResume();
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
                }
            }
        }
    }

    @Override
    public void onPause() {
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
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
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
}
