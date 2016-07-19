package com.hopebaytech.hcfsmgmt.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.FactoryResetUtils;

import java.util.Locale;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentDoneFragment extends Fragment {

    public static final String TAG = TransferContentDoneFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentDoneFragment.class.getSimpleName();

    public static TransferContentDoneFragment newInstance() {
        return new TransferContentDoneFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transfer_content_done_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView factoryResetMsg = (TextView) view.findViewById(R.id.factory_reset_message);
        final Handler uiHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 5; i >= 0; i--) {
                        final String seconds = String.valueOf(i);
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String template = getString(R.string.settings_transfer_content_done_factory_reset);
                                String message = String.format(Locale.getDefault(), template, seconds);
                                factoryResetMsg.setText(message);
                            }
                        });
<<<<<<< 31c734cdc4eb7cc53c71cb4fc4cf730aecc6b62f
=======

>>>>>>> Integrate with polling service and mgmt server
                        Thread.sleep(1000);
                    }
                    // Factory reset
                    FactoryResetUtils.reset(getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
