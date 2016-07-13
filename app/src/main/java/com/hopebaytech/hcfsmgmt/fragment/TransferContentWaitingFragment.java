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

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentWaitingFragment extends Fragment {

    public static final String TAG = TransferContentWaitingFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentWaitingFragment.class.getSimpleName();

    public static TransferContentWaitingFragment newInstance() {
        return new TransferContentWaitingFragment();
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
                TransferContentTransferringFragment fragment = TransferContentTransferringFragment.newInstance();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, fragment, TransferContentTransferringFragment.TAG);
                ft.commit();
//                getActivity().finish();
            }
        });

    }

}
