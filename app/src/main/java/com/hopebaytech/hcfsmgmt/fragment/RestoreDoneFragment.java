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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/31.
 */
public class RestoreDoneFragment extends Fragment {

    public static final String TAG = RestoreDoneFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private Context mContext;

    public static RestoreDoneFragment newInstance() {
        return new RestoreDoneFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logs.d(CLASSNAME, "onCreate", null);
        mContext = getActivity();

        removeRestoreStatus();
    }

    private void removeRestoreStatus() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(HCFSMgmtUtils.PREF_RESTORE_STATUS);
        editor.apply();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logs.d(CLASSNAME, "onCreateView", null);
        return inflater.inflate(R.layout.restore_done_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logs.d(CLASSNAME, "onViewCreated", null);

        TextView exit = (TextView) view.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
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
    }

}
