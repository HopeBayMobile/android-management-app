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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.TransferContentInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentUploadingFragment extends Fragment {

    public static final String TAG = TransferContentUploadingFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentUploadingFragment.class.getSimpleName();

    private String ACTION_UPLOAD_COMPLETED = "hbt.intent.action.UPLOAD_COMPLETED";

    private TextView mErrorMsg;
    private RelativeLayout mProgressLayout;
    private UploadCompletedReceiver mUploadCompletedReceiver;

    public static TransferContentUploadingFragment newInstance() {
        return new TransferContentUploadingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPLOAD_COMPLETED);
        mUploadCompletedReceiver = new UploadCompletedReceiver(getActivity());
        mUploadCompletedReceiver.registerReceiver(filter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent();
                intent.setAction(ACTION_UPLOAD_COMPLETED);
                getActivity().sendBroadcast(intent);
            }
        }).start();
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
                getActivity().finish();
            }
        });

        mErrorMsg = (TextView) view.findViewById(R.id.error_msg);
        mProgressLayout = (RelativeLayout) view.findViewById(R.id.progressbar_layout);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUploadCompletedReceiver.unregisterReceiver();
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
            if (action.equals(ACTION_UPLOAD_COMPLETED)) {
                Logs.d(CLASSNAME, "onReceive", ACTION_UPLOAD_COMPLETED);
                MgmtCluster.getJwtToken(getActivity(), new MgmtCluster.FetchJwtTokenListener() {
                    @Override
                    public void onFetchSuccessful(String jwtToken) {
                        String imei = HCFSMgmtUtils.getDeviceImei(getActivity());
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
                                mProgressLayout.setVisibility(View.GONE);
                            }
                        });
                        transferProxy.transfer();
                    }

                    @Override
                    public void onFetchFailed() {
                        mErrorMsg.setText(R.string.settings_transfer_content_failed);
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

}
