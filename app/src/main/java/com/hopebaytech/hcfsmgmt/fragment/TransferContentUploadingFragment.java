/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.TransferContentInfo;
import com.hopebaytech.hcfsmgmt.main.MainApplication;
import com.hopebaytech.hcfsmgmt.misc.TransferStatus;
import com.hopebaytech.hcfsmgmt.utils.Booster;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;
import com.hopebaytech.hcfsmgmt.utils.UiHandler;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentUploadingFragment extends Fragment {

    public static final String TAG = TransferContentUploadingFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

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

        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                TransferStatus.setTransferStatus(mContext, TransferStatus.TRANSFERRING);
                Booster.disableBoosterWhenSyncData(mContext);

                final int code = HCFSMgmtUtils.startUploadTeraData();
                UiHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if (code == 1) { // The system is clean now. That is, there is no dirty data.
                            TransferContentWaitingFragment fragment = TransferContentWaitingFragment.newInstance();

                            Logs.d(CLASSNAME, "onActivityCreated", "Replace with TransferContentWaitingFragment");
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_container, fragment, TransferContentWaitingFragment.TAG);
                            ft.commit();
                        } else if (code == 0) { // Setting sync point completed, data start to be synced.
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
                });
            }
        });

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
                            Thread.sleep(Interval.UPDATE_TERA_CONN_STATUS_IN_TRANSFER_CONTENT);
                        }
                    } catch (InterruptedException e) {
                        Logs.d(CLASSNAME, "onStart", "mTeraConnStatusThread is interrupted.");
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

        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // If sync all data completed, the transfer status will be set to
                // TransferStatus.WAIT_DEVICE and then the TransferContentUploadingFragment will be
                // replaced with TransferContentWaitingFragment, which trigger the onDestroy()
                // callback of TransferContentUploadingFragment to be called. In this case, we don't
                // need to remove the transfer status. It makes sure that Tera app can show the
                // TransferContentWaitingFragment if user launch Tera app again.
                int transferStatus = TransferStatus.getTransferStatus(mContext);
                if (transferStatus != TransferStatus.WAIT_DEVICE) {
                    TransferStatus.removeTransferStatus(mContext);
                }

                HCFSMgmtUtils.stopUploadTeraData();
                Booster.enableBoosterAfterSyncData(mContext);
            }
        });
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
            Logs.d(CLASSNAME, "onReceive", "action=" + action);
            if (!action.equals(TeraIntent.ACTION_UPLOAD_COMPLETED)) {
                return;
            }

            MgmtCluster.getJwtToken(mContext, new MgmtCluster.OnFetchJwtTokenListener() {
                @Override
                public void onFetchSuccessful(String jwtToken) {
                    String imei = HCFSMgmtUtils.getDeviceImei(mContext);
                    MgmtCluster.TransferReadyProxy transferProxy = new MgmtCluster.TransferReadyProxy(jwtToken, imei);
                    transferProxy.setOnTransferContentListener(new MgmtCluster.TransferReadyProxy.OnTransferContentListener() {
                        @Override
                        public void onTransferSuccessful(TransferContentInfo transferContentInfo) {
                            TransferStatus.setTransferStatus(mContext, TransferStatus.WAIT_DEVICE);

                            Logs.d(CLASSNAME, "onTransferSuccessful", "Replace with TransferContentWaitingFragment");
                            TransferContentWaitingFragment fragment = TransferContentWaitingFragment.newInstance();
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_container, fragment, TransferContentWaitingFragment.TAG);
                            ft.commitAllowingStateLoss();

                            if (!MainApplication.Foreground.get().isForeground()) {
                                int flags = NotificationEvent.FLAG_HEADS_UP |
                                        NotificationEvent.FLAG_OPEN_APP;
                                NotificationEvent.notify(
                                        mContext,
                                        NotificationEvent.ID_TRANSFER_DATA,
                                        R.string.settings_transfer_content_notification_transfer_completed_title,
                                        R.string.settings_transfer_content_notification_transfer_completed_message,
                                        flags
                                );
                            }
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
