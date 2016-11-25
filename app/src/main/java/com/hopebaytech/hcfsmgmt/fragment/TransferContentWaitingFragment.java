package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.info.UnlockDeviceInfo;
import com.hopebaytech.hcfsmgmt.service.TransferDataPollingService;
import com.hopebaytech.hcfsmgmt.utils.Booster;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.PollingServiceUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;
import com.hopebaytech.hcfsmgmt.utils.UiHandler;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentWaitingFragment extends Fragment {

    public static final String TAG = TransferContentWaitingFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentWaitingFragment.class.getSimpleName();

    private TransferCompletedReceiver mTransferCompletedReceiver;

    private Context mContext;
    private ProgressDialog mProgressDialog;

    public static TransferContentWaitingFragment newInstance() {
        return new TransferContentWaitingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        IntentFilter filter = new IntentFilter();
        filter.addAction(TeraIntent.ACTION_TRANSFER_COMPLETED);
        mTransferCompletedReceiver = new TransferCompletedReceiver(mContext);
        mTransferCompletedReceiver.registerReceiver(filter);

        // Start polling service to check device status (need to register a BroadcastReceiver for
        // receiving intent with TeraIntent.ACTION_TRANSFER_COMPLETED action.)
        PollingServiceUtils.startPollingService(
                mContext,
                Interval.WAIT_RESTORE_AFTER_TRANSFER_DEVICE,
                PollingServiceUtils.JOB_ID_TRANSFER_DATA,
                TransferDataPollingService.class
        );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transfer_content_waiting_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                final String imei = HCFSMgmtUtils.getDeviceImei(mContext);
                MgmtCluster.getJwtToken(mContext, new MgmtCluster.OnFetchJwtTokenListener() {
                    @Override
                    public void onFetchSuccessful(String jwtToken) {
                        MgmtCluster.UnlockDeviceProxy unlockDeviceProxy = new MgmtCluster.UnlockDeviceProxy(jwtToken, imei);
                        unlockDeviceProxy.setOnUnlockDeviceListener(new MgmtCluster.UnlockDeviceProxy.OnUnlockDeviceListener() {
                            @Override
                            public void onUnlockDeviceSuccessful(UnlockDeviceInfo unlockDeviceInfo) {
                                Logs.d(CLASSNAME, "onUnlockDeviceSuccessful", null);
                                ThreadPool.getInstance().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
                                        SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_ENABLE_BOOSTER);
                                        if (settingsInfo == null || !Boolean.valueOf(settingsInfo.getValue())) {
                                            if (!Booster.isBoosterMounted()) {
                                                Booster.mountBooster();
                                                Booster.enableApps(mContext);
                                            }
                                            settingsInfo = new SettingsInfo();
                                            settingsInfo.setKey(SettingsFragment.PREF_ENABLE_BOOSTER);
                                            settingsInfo.setValue(String.valueOf(true));
                                            settingsDAO.update(settingsInfo);
                                        }
                                        UiHandler.getInstance().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                dismissProgressDialog();
                                                ((Activity) mContext).finish();
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onUnlockDeviceFailed(UnlockDeviceInfo unlockDeviceInfo) {
                                Logs.e(CLASSNAME, "onUnlockDeviceFailed", null);
                                dismissProgressDialog();
                                ((Activity) mContext).finish();
                            }
                        });
                        unlockDeviceProxy.unlock();
                    }

                    @Override
                    public void onFetchFailed() {
                        Logs.e(CLASSNAME, "onFetchFailed", null);
                        dismissProgressDialog();
                        ((Activity) mContext).finish();
                    }
                });
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTransferCompletedReceiver.unregisterReceiver();
    }

    public class TransferCompletedReceiver extends BroadcastReceiver {

        private Context mContext;

        private boolean isRegister = false;

        public TransferCompletedReceiver(Context context) {
            this.mContext = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(TeraIntent.ACTION_TRANSFER_COMPLETED)) {
                Logs.d(CLASSNAME, "onReceive", TeraIntent.ACTION_TRANSFER_COMPLETED);

                Logs.d(CLASSNAME, "onReceive", "Replace with TransferContentTransferringFragment");
                TransferContentTransferringFragment fragment = TransferContentTransferringFragment.newInstance();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, TransferContentTransferringFragment.TAG);
                ft.commit();
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

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(getString(R.string.cancel_processing_msg));
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
