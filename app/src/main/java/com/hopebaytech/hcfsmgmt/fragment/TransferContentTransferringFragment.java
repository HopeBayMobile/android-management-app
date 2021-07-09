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
