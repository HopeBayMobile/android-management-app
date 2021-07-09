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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/15.
 */
public class LocalSpaceUsageRatioDialogFragment extends DialogFragment {

    public static final String TAG = AppDialogFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();
    private Context mContext;

    public static LocalSpaceUsageRatioDialogFragment newInstance() {
        return new LocalSpaceUsageRatioDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_usage_ratio_selection_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String defaultValue = getResources().getString(R.string.default_notify_used_ratio);
        String getRatio = defaultValue.concat("%");

        SettingsDAO mSettingsDAO = SettingsDAO.getInstance(mContext);
        SettingsInfo settingsInfo = mSettingsDAO.get(SettingsFragment.PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO);
        if (settingsInfo != null) {
            getRatio = settingsInfo.getValue().concat("%");
        }
        final String ratio = getRatio;
//        final List<LinearLayout> radioButtonLayoutList = new ArrayList<>();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView radioName = (TextView) v.findViewById(R.id.radio_name);

                Intent data = new Intent();
                data.putExtra(SettingsFragment.KEY_RATIO, radioName.getText().toString());
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);

                dismiss();
            }
        };

        final LinearLayout radioButtonContainer = (LinearLayout) view.findViewById(R.id.radio_btn_container);
        String[] ratioNameArr = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_name);
        for (String ratioName: ratioNameArr) {
            LinearLayout radioButtonLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.settings_radio_button, radioButtonContainer, false);
            radioButtonLayout.setContentDescription(ratioName);
            radioButtonLayout.setOnClickListener(listener);

            TextView radioName = (TextView) radioButtonLayout.findViewById(R.id.radio_name);
            radioName.setText(ratioName);

            if (ratioName.equals(ratio)) {
                ImageView radioImage = (ImageView) radioButtonLayout.findViewById(R.id.radio_image);
                radioImage.setImageResource(R.drawable.icon_btn_selected);
            }

            radioButtonContainer.addView(radioButtonLayout);
        }

        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}
