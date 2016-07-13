package com.hopebaytech.hcfsmgmt.fragment;

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
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.interfaces.IFetchJwtTokenListener;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentUploadingFragment extends Fragment {

    public static final String TAG = TransferContentUploadingFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentUploadingFragment.class.getSimpleName();

    public static TransferContentUploadingFragment newInstance() {
        return new TransferContentUploadingFragment();
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
                TransferContentWaitingFragment fragment = TransferContentWaitingFragment.newInstance();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, TransferContentWaitingFragment.TAG);
                ft.commit();
//                getActivity().finish();
            }
        });

        MgmtCluster.getJwtToken(getActivity(), new IFetchJwtTokenListener() {
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
                        TextView errorMsg = (TextView) view.findViewById(R.id.error_msg);
                        errorMsg.setText("Transfer failed");
                    }
                });
                transferProxy.transfer();
            }

            @Override
            public void onFetchFailed() {
                TextView errorMsg = (TextView) view.findViewById(R.id.error_msg);
                errorMsg.setText("Transfer failed");
            }
        });

    }

}
