package com.hopebaytech.hcfsmgmt.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentTransferringFragment extends Fragment {

    public static final String TAG = TransferContentTransferringFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentTransferringFragment.class.getSimpleName();

    public static TransferContentTransferringFragment newInstance() {
        return new TransferContentTransferringFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Logs.d(CLASSNAME, "onCreate", "Replace with TransferContentDoneFragment");
                TransferContentDoneFragment fragment = TransferContentDoneFragment.newInstance();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, TransferContentDoneFragment.TAG);
                ft.commit();
            }
        }, 5000);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transfer_content_transferring_fragment, container, false);
    }

}
