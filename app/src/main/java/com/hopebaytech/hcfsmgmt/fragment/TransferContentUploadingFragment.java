package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.TransferContentInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentUploadingFragment extends Fragment {

    public static final String TAG = TransferContentUploadingFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private final int INTERVAL_TERA_CONN_STATUS = 5000;

    private UploadCompletedReceiver mUploadCompletedReceiver;
    private Thread mTeraConnStatusThread;

    private Context mContext;
    private TextView mErrorMsg;
    private TextView mTeraConnStatus;
    private RelativeLayout mProgressLayout;

    public static TransferContentUploadingFragment newInstance() {
        return new TransferContentUploadingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transfer_content_uploading_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HCFSMgmtUtils.stopUploadTeraData();
                ((Activity) mContext).finish();
            }
        });

        mErrorMsg = (TextView) view.findViewById(R.id.error_msg);
        mTeraConnStatus = (TextView) view.findViewById(R.id.tera_conn_status);
        mProgressLayout = (RelativeLayout) view.findViewById(R.id.progressbar_layout);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int code = HCFSMgmtUtils.startUploadTeraData();
        if (code == 1) {
            TransferContentWaitingFragment fragment = TransferContentWaitingFragment.newInstance();

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, TransferContentWaitingFragment.TAG);
            ft.commit();
        } else if (code == 0) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(TeraIntent.ACTION_UPLOAD_COMPLETED);
            mUploadCompletedReceiver = new UploadCompletedReceiver(mContext);
            mUploadCompletedReceiver.registerReceiver(filter);
        } else {
            mErrorMsg.setText(R.string.settings_transfer_content_failed);
            mTeraConnStatus.setVisibility(View.GONE);
            mProgressLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mTeraConnStatusThread == null) {
            mTeraConnStatusThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            HCFSStatInfo hcfsStatInfo = HCFSMgmtUtils.getHCFSStatInfo();
                            final int connStatus = HCFSConnStatus.getConnStatus(mContext, hcfsStatInfo);
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displayNetworkStatus(connStatus);
                                }
                            });
                            Thread.sleep(INTERVAL_TERA_CONN_STATUS);
                        }
                    } catch (InterruptedException e) {
                        Logs.w(CLASSNAME, "onStart", Log.getStackTraceString(e));
                    }
                }
            });
            mTeraConnStatusThread.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mTeraConnStatusThread != null) {
            mTeraConnStatusThread.interrupt();
            mTeraConnStatusThread = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUploadCompletedReceiver.unregisterReceiver();
        HCFSMgmtUtils.stopUploadTeraData();
    }

    public class UploadCompletedReceiver extends BroadcastReceiver {

        private Context mContext;

        private boolean isRegister = false;

        public UploadCompletedReceiver(Context context) {
            this.mContext = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(TeraIntent.ACTION_UPLOAD_COMPLETED)) {
                Logs.d(CLASSNAME, "onReceive", TeraIntent.ACTION_UPLOAD_COMPLETED);
                MgmtCluster.getJwtToken(mContext, new MgmtCluster.OnFetchJwtTokenListener() {
                    @Override
                    public void onFetchSuccessful(String jwtToken) {
                        String imei = HCFSMgmtUtils.getDeviceImei(mContext);
                        MgmtCluster.TransferContentProxy transferProxy = new MgmtCluster.TransferContentProxy(jwtToken, imei);
                        transferProxy.setOnTransferContentListener(new MgmtCluster.TransferContentProxy.OnTransferContentListener() {
                            @Override
                            public void onTransferSuccessful(TransferContentInfo transferContentInfo) {
                                TransferContentWaitingFragment fragment = TransferContentWaitingFragment.newInstance();

                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.replace(R.id.fragment_container, fragment, TransferContentWaitingFragment.TAG);
                                ft.commit();
                            }

                            @Override
                            public void onTransferFailed(TransferContentInfo transferContentInfo) {
                                mErrorMsg.setText(R.string.settings_transfer_content_failed);
                                mTeraConnStatus.setVisibility(View.GONE);
                                mProgressLayout.setVisibility(View.GONE);
                            }
                        });
                        transferProxy.transfer();
                    }

                    @Override
                    public void onFetchFailed() {
                        mErrorMsg.setText(R.string.settings_transfer_content_failed);
                        mTeraConnStatus.setVisibility(View.GONE);
                        mProgressLayout.setVisibility(View.GONE);
                    }
                });
            }

        }

        public void registerReceiver(IntentFilter intentFilter) {
            if (!isRegister) {
                mContext.registerReceiver(this, intentFilter);
                isRegister = true;
            }
        }

        public void unregisterReceiver() {
            if (isRegister) {
                mContext.unregisterReceiver(this);
                isRegister = false;
            }
        }

    }

    private void displayNetworkStatus(int connStatus) {
        switch (connStatus) {
            case HCFSConnStatus.TRANS_FAILED:
                mTeraConnStatus.setText(mContext.getString(R.string.overview_hcfs_conn_status_failed));
                break;
            case HCFSConnStatus.TRANS_NOT_ALLOWED:
                mTeraConnStatus.setText(mContext.getString(R.string.overview_hcfs_conn_status_not_allowed));
                break;
            case HCFSConnStatus.TRANS_NORMAL:
                mTeraConnStatus.setText(mContext.getString(R.string.overview_hcfs_conn_status_normal));
                break;
            case HCFSConnStatus.TRANS_IN_PROGRESS:
                mTeraConnStatus.setText(mContext.getString(R.string.overview_hcfs_conn_status_in_progress));
                break;
            case HCFSConnStatus.TRANS_SLOW:
                mTeraConnStatus.setText(mContext.getString(R.string.overview_hcfs_conn_status_slow));
                break;
        }
    }


}
