package com.hopebaytech.hcfsmgmt.fragment;

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
import com.hopebaytech.hcfsmgmt.info.TransferContentInfo;
import com.hopebaytech.hcfsmgmt.info.UnlockDeviceInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentWaitingFragment extends Fragment {

    public static final String TAG = TransferContentWaitingFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentWaitingFragment.class.getSimpleName();

    private String ACTION_TRANSFER_COMPLETED = "hbt.intent.action.TRANSFER_COMPLETED";

    private TransferCompletedReceiver mTransferCompletedReceiver;

    public static TransferContentWaitingFragment newInstance() {
        return new TransferContentWaitingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TRANSFER_COMPLETED);
        mTransferCompletedReceiver = new TransferCompletedReceiver(getActivity());
        mTransferCompletedReceiver.registerReceiver(filter);

        // TODO call polling api of MgmtCluster.java to check device status
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
                MgmtCluster.getJwtToken(getActivity(), new MgmtCluster.FetchJwtTokenListener() {
                    @Override
                    public void onFetchSuccessful(String jwtToken) {
                        String imei = HCFSMgmtUtils.getDeviceImei(getActivity());
                        MgmtCluster.UnlockDeviceProxy unlockDeviceProxy = new MgmtCluster.UnlockDeviceProxy(jwtToken, imei);
                        unlockDeviceProxy.setOnUnlockDeviceListener(new MgmtCluster.UnlockDeviceProxy.OnUnlockDeviceListener() {
                            @Override
                            public void onUnlockDeviceSuccessful(UnlockDeviceInfo unlockDeviceInfo) {
                                Logs.d(CLASSNAME, "onUnlockDeviceSuccessful", null);
                                getActivity().finish();
                            }

                            @Override
                            public void onUnlockDeviceFailed(UnlockDeviceInfo unlockDeviceInfo) {
                                Logs.e(CLASSNAME, "onUnlockDeviceFailed", null);
                                getActivity().finish();
                            }
                        });
                    }

                    @Override
                    public void onFetchFailed() {
                        Logs.e(CLASSNAME, "onFetchFailed", null);
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
            if (action.equals(ACTION_TRANSFER_COMPLETED)) {
                Logs.w(CLASSNAME, "onReceive", ACTION_TRANSFER_COMPLETED);
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

}
