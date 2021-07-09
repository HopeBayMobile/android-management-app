/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.hopebaytech.hcfsmgmt.misc.TransferStatus;
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
        Logs.d(CLASSNAME, "onCreate", null);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logs.d(CLASSNAME, "onCreateView", null);
        return inflater.inflate(R.layout.transfer_content_done_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);
        mFactoryResetMsg = (TextView) view.findViewById(R.id.factory_reset_message);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logs.d(CLASSNAME, "onActivityCreated", null);

        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 5; i >= 0; i--) {
                        final String seconds = String.valueOf(i);
                        UiHandler.getInstance().post(new Runnable() {
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
                    TransferStatus.removeTransferStatus(mContext);

                    isResetTriggered = true;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onDestroy", null);

        if (!isResetTriggered) {
            // Factory reset
            FactoryResetUtils.reset(mContext);
        }
    }
}
