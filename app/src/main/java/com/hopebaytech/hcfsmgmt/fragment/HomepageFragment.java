package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

public class HomepageFragment extends Fragment {

    public static String TAG = HomepageFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private NetworkBroadcastReceiver networkStatusReceiver;
    private Thread uiRefreshThread;
    private TextView cloudStorageUsage;
    private ProgressBar cloudStorageProgressBar;
    private TextView pinnedStorageUsage;
    private ProgressBar pinnedStorageProgressBar;
    private TextView waitToUploadDataUsage;
    private ProgressBar waitToUploadDataUsageProgressBar;
    private TextView network_xfer_up;
    private TextView network_xfer_down;
    private ProgressBar xferProgressBar;

    public static HomepageFragment newInstance() {
        return new HomepageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkStatusReceiver = new NetworkBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_page_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        Activity activity = getActivity();
        if (activity == null || view == null) {
            return;
        }

        LinearLayout cloudStorage = (LinearLayout) view.findViewById(R.id.cloud_storage);
        cloudStorageUsage = (TextView) cloudStorage.findViewById(R.id.textViewUsage);
        cloudStorageProgressBar = (ProgressBar) cloudStorage.findViewById(R.id.progressBar);
        TextView cloudStorageTitle = (TextView) cloudStorage.findViewById(R.id.textViewTitle);
        cloudStorageTitle.setText(getString(R.string.home_page_used_space));
        ImageView cloudStorageImageView = (ImageView) cloudStorage.findViewById(R.id.iconView);
        cloudStorageImageView.setImageResource(R.drawable.cloudspace_128x128);

        LinearLayout pinnedStorage = (LinearLayout) view.findViewById(R.id.pinned_storage);
        pinnedStorageUsage = (TextView) pinnedStorage.findViewById(R.id.textViewUsage);
        pinnedStorageProgressBar = (ProgressBar) pinnedStorage.findViewById(R.id.progressBar);
        pinnedStorageProgressBar.setProgressDrawable(ContextCompat.getDrawable(activity, R.drawable.storage_progressbar));
        TextView pinnedStorageTitle = (TextView) pinnedStorage.findViewById(R.id.textViewTitle);
        pinnedStorageTitle.setText(getString(R.string.home_page_pinned_storage));
        ImageView pinnedStorageImageView = (ImageView) pinnedStorage.findViewById(R.id.iconView);
        pinnedStorageImageView.setImageResource(R.drawable.pinspace_128x128);

        LinearLayout waitToUploadData = (LinearLayout) view.findViewById(R.id.to_be_upload_data);
        waitToUploadDataUsage = (TextView) waitToUploadData.findViewById(R.id.textViewUsage);
        waitToUploadDataUsageProgressBar = (ProgressBar) waitToUploadData.findViewById(R.id.progressBar);
        waitToUploadDataUsageProgressBar.setProgressDrawable(ContextCompat.getDrawable(activity, R.drawable.storage_progressbar));
        TextView waitToUploadDataTitle = (TextView) waitToUploadData.findViewById(R.id.textViewTitle);
        waitToUploadDataTitle.setText(getString(R.string.home_page_to_be_upladed_data));
        ImageView waitToUploadDataUsageImageView = (ImageView) waitToUploadData.findViewById(R.id.iconView);
        waitToUploadDataUsageImageView.setImageResource(R.drawable.uploading_128x128);

        LinearLayout network_xfer_today = (LinearLayout) view.findViewById(R.id.network_xfer_today);
        network_xfer_up = (TextView) network_xfer_today.findViewById(R.id.xfer_up);
        network_xfer_down = (TextView) network_xfer_today.findViewById(R.id.xfer_down);
        xferProgressBar = (ProgressBar) network_xfer_today.findViewById(R.id.progressBar);
        xferProgressBar.setProgressDrawable(ContextCompat.getDrawable(activity, R.drawable.xfer_progressbar));
        TextView network_xfer_today_title = (TextView) network_xfer_today.findViewById(R.id.textViewTitle);
        network_xfer_today_title.setText(getString(R.string.home_page_the_amount_of_network_traffic_today));
        ImageView networkXferImageView = (ImageView) network_xfer_today.findViewById(R.id.iconView);
        networkXferImageView.setImageResource(R.drawable.load_128x128);


    }

    public class NetworkBroadcastReceiver extends BroadcastReceiver {

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
                            networkConnStatusImage.setImageResource(R.drawable.connect_96x96);
                            networkConnStatusText.setText(getString(R.string.home_page_network_status_connected));
                        } else if (netInfo.getState() == NetworkInfo.State.CONNECTING) {
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is connecting");
                            networkConnStatusImage.setImageResource(R.drawable.connect_connecting_96x96);
                            networkConnStatusText.setText(getString(R.string.home_page_network_status_connecting));
                        } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "Network is disconnected");
                            networkConnStatusImage.setImageResource(R.drawable.connect_stop_96x96);
                            networkConnStatusText.setText(getString(R.string.home_page_network_status_disconnected));
                        }
                    } else {
                        networkConnStatusImage.setImageResource(R.drawable.connect_stop_96x96);
                        networkConnStatusText.setText(getString(R.string.home_page_network_status_disconnected));
                    }
                }
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        Activity activity = getActivity();
        if (activity != null) {
            activity.registerReceiver(networkStatusReceiver, filter);
        }

        uiRefreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (statInfo != null) {
                                        cloudStorageUsage.setText(statInfo.getVolUsed() + " / " + statInfo.getCloudTotal());
                                        cloudStorageProgressBar.setProgress(statInfo.getCloudUsedPercentage());

                                        pinnedStorageUsage.setText(statInfo.getPinTotal());
                                        pinnedStorageProgressBar.setProgress(statInfo.getPinnedUsedPercentage());

                                        waitToUploadDataUsage.setText(statInfo.getCacheDirtyUsed());
                                        waitToUploadDataUsageProgressBar.setProgress(statInfo.getDirtyPercentage());

                                        String xferDownload = statInfo.getXferDownload();
                                        String xferUpload = statInfo.getXferUpload();
                                        network_xfer_up.setText(xferUpload);
                                        network_xfer_down.setText(xferDownload);
                                        if (xferDownload.equals("0B") && xferUpload.equals("0B")) {
                                            xferProgressBar.setProgress(0);
                                            xferProgressBar.setSecondaryProgress(0);
                                        } else {
                                            xferProgressBar.setProgress(statInfo.getXterDownloadPercentage());
                                            xferProgressBar.setSecondaryProgress(100);
                                        }
                                    } else {
                                        cloudStorageUsage.setText("-");
                                        cloudStorageProgressBar.setProgress(0);

                                        pinnedStorageUsage.setText("-");
                                        pinnedStorageProgressBar.setProgress(0);

                                        waitToUploadDataUsage.setText("-");
                                        waitToUploadDataUsageProgressBar.setProgress(0);

                                        network_xfer_up.setText("-");
                                        network_xfer_down.setText("-");
                                        xferProgressBar.setProgress(0);
                                        xferProgressBar.setSecondaryProgress(0);
                                    }
                                }
                            });
                        }
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        uiRefreshThread.start();
    }

    @Override
    public void onPause() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.unregisterReceiver(networkStatusReceiver);
        }
        uiRefreshThread.interrupt();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
