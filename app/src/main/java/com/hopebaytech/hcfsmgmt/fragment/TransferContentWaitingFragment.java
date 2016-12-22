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
import com.hopebaytech.hcfsmgmt.main.MainApplication;
import com.hopebaytech.hcfsmgmt.misc.JobServiceId;
import com.hopebaytech.hcfsmgmt.misc.TransferStatus;
import com.hopebaytech.hcfsmgmt.service.CheckDeviceTransferredPeriodicService;
import com.hopebaytech.hcfsmgmt.service.UnlockDeviceService;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.PeriodicServiceUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentWaitingFragment extends Fragment {

    public static final String TAG = TransferContentWaitingFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private TransferCompletedReceiver mTransferCompletedReceiver;

    private Context mContext;
    private ProgressDialog mProgressDialog;

    public static TransferContentWaitingFragment newInstance() {
        return new TransferContentWaitingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.d(CLASSNAME, "onCreate", null);

        mContext = getActivity();

        IntentFilter filter = new IntentFilter();
        filter.addAction(TeraIntent.ACTION_TRANSFER_COMPLETED);
        mTransferCompletedReceiver = new TransferCompletedReceiver(mContext);
        mTransferCompletedReceiver.registerReceiver(filter);

        // Start periodic service to check device is transferred or not. If the device is transferred
        // , CheckDeviceTransferredPeriodicService will broadcast a TeraIntent.ACTION_TRANSFER_COMPLETED.
        PeriodicServiceUtils.startPeriodicService(
                mContext,
                Interval.WAIT_RESTORE_AFTER_TRANSFER_DEVICE,
                JobServiceId.CHECK_DEVICE_TRANSFERRED,
                CheckDeviceTransferredPeriodicService.class
        );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logs.d(CLASSNAME, "onCreateView", null);
        return inflater.inflate(R.layout.transfer_content_waiting_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);

        TextView cancel = (TextView) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) mContext).finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onDestroy", null);
        mTransferCompletedReceiver.unregisterReceiver();

        // Stop the transfer data service.
        PeriodicServiceUtils.stopPeriodicService(
                mContext,
                JobServiceId.CHECK_DEVICE_TRANSFERRED
        );

        // If data is restored from another device, the transfer status will be set to
        // TransferStatus.TRANSFERRED and then the TransferContentWaitingFragment will be
        // replaced with TransferContentTransferringFragment or TransferContentDoneFragment, which
        // triggers the onDestroy() callback of TransferContentWaitingFragment to be called.
        int transferStatus = TransferStatus.getTransferStatus(mContext);
        if (transferStatus != TransferStatus.TRANSFERRED) {
            // Remove task from recent apps or cancel by user, it means user don't want to continue
            // the transfer data process. In this case, don't show TransferContentWaitingFragment
            // when launch Tera app.
            TransferStatus.removeTransferStatus(mContext);

            // Start a service to unlock device until device is unlocked.
            PeriodicServiceUtils.startPeriodicService(
                    mContext,
                    Interval.UNLOCK_DEVICE,
                    JobServiceId.UNLOCK_DEVICE,
                    UnlockDeviceService.class
            );
        }
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
            Logs.d(CLASSNAME, "onReceive", "action=" + action);
            if (!action.equals(TeraIntent.ACTION_TRANSFER_COMPLETED)) {
                return;
            }

            TransferStatus.setTransferStatus(mContext, TransferStatus.TRANSFERRED);
            if (MainApplication.Foreground.get().isForeground()) {
                Logs.d(CLASSNAME, "onReceive", "Replace with TransferContentTransferringFragment");
                TransferContentTransferringFragment fragment = TransferContentTransferringFragment.newInstance();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, TransferContentTransferringFragment.TAG);
                ft.commitAllowingStateLoss();
            } else {
                Logs.d(CLASSNAME, "onReceive", "Replace with TransferContentDoneFragment");
                // If Tera app is in background, skip the transferring interlude animation of
                // TransferContentTransferringFragment to TransferContentDoneFragment.
                TransferContentDoneFragment fragment = TransferContentDoneFragment.newInstance();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, TransferContentDoneFragment.TAG);
                ft.commitAllowingStateLoss();

                int flags = NotificationEvent.FLAG_HEADS_UP |
                        NotificationEvent.FLAG_OPEN_APP;
                NotificationEvent.notify(
                        mContext,
                        NotificationEvent.ID_TRANSFER_DATA,
                        "完成轉移程序", Data Catched
                        "系統將於五秒後自動回復到原廠初始狀態。",
                        flags
                );
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
