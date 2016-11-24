package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.FactoryResetUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;
import com.hopebaytech.hcfsmgmt.utils.UiHandler;

import java.util.Locale;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentDoneFragment extends Fragment {

    public static final String TAG = TransferContentDoneFragment.class.getSimpleName();
    private final String CLASSNAME = TransferContentDoneFragment.class.getSimpleName();

    private boolean isResetTriggered;

    private Context mContext;
    private TextView mFactoryResetMsg;

    public static TransferContentDoneFragment newInstance() {
        return new TransferContentDoneFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transfer_content_done_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFactoryResetMsg = (TextView) view.findViewById(R.id.factory_reset_message);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 5; i >= 0; i--) {
                        final String seconds = String.valueOf(i);
                        UiHandler.getInstace().post(new Runnable() {
                            @Override
                            public void run() {
                                String template = getString(R.string.settings_transfer_content_done_factory_reset);
                                String message = String.format(Locale.getDefault(), template, seconds);
                                mFactoryResetMsg.setText(message);
                            }
                        });
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    Logs.e(CLASSNAME, "onViewCreated", Log.getStackTraceString(e));
                } finally {
                    // Factory reset
                    FactoryResetUtils.reset(mContext);

                    isResetTriggered = true;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!isResetTriggered) {
            // Factory reset
            FactoryResetUtils.reset(mContext);
        }
    }
}
