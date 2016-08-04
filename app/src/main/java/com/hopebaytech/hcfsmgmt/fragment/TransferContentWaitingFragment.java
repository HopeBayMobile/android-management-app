package com.hopebaytech.hcfsmgmt.fragment;

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
import com.hopebaytech.hcfsmgmt.info.TeraIntent;
import com.hopebaytech.hcfsmgmt.info.UnlockDeviceInfo;
import com.hopebaytech.hcfsmgmt.service.MgmtPollingService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.MgmtPollingUtils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentWaitingFragment extends Fragment {

    public static final String TAG = TransferContentWaitingFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentWaitingFragment.class.getSimpleName();

    private TransferCompletedReceiver mTransferCompletedReceiver;

    private ProgressDialog mProgressDialog;

    public static TransferContentWaitingFragment newInstance() {
        return new TransferContentWaitingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(TeraIntent.ACTION_TRANSFER_COMPLETED);
        mTransferCompletedReceiver = new TransferCompletedReceiver(getActivity());
        mTransferCompletedReceiver.registerReceiver(filter);

        // Start polling service to check device status (need to register a BroadcastReceiver for
        // receiving intent with TeraIntent.ACTION_TRANSFER_COMPLETED action.)
        MgmtPollingUtils.startPollingService(getActivity(), 10, MgmtPollingService.class);
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
                final String imei = HCFSMgmtUtils.getDeviceImei(getActivity());
                MgmtCluster.getJwtToken(getActivity(), new MgmtCluster.OnFetchJwtTokenListener() {
                    @Override
                    public void onFetchSuccessful(String jwtToken) {
                        MgmtCluster.UnlockDeviceProxy unlockDeviceProxy = new MgmtCluster.UnlockDeviceProxy(jwtToken, imei);
                        unlockDeviceProxy.setOnUnlockDeviceListener(new MgmtCluster.UnlockDeviceProxy.OnUnlockDeviceListener() {
                            @Override
                            public void onUnlockDeviceSuccessful(UnlockDeviceInfo unlockDeviceInfo) {
                                Logs.d(CLASSNAME, "onUnlockDeviceSuccessful", null);
                                dismissProgressDialog();
                                getActivity().finish();
                            }

                            @Override
                            public void onUnlockDeviceFailed(UnlockDeviceInfo unlockDeviceInfo) {
                                Logs.e(CLASSNAME, "onUnlockDeviceFailed", null);
                                dismissProgressDialog();
                                getActivity().finish();
                            }
                        });
                        unlockDeviceProxy.unlock();
                    }

                    @Override
                    public void onFetchFailed() {
                        Logs.e(CLASSNAME, "onFetchFailed", null);
                        dismissProgressDialog();
                        getActivity().finish();
                    }
                });
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTransferCompletedReceiver.unregisterReceiver();
        MgmtPollingUtils.stopPollingService(getActivity(), MgmtPollingService.class);
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
            mProgressDialog = new ProgressDialog(getActivity());
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
