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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.Booster;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;
import com.hopebaytech.hcfsmgmt.utils.UiHandler;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class DisableBoosterDialogFragment extends DialogFragment {

    public static final String TAG = DisableBoosterDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    public static final int RESULT_FAILED = -2;

    private Context mContext;

    public static DisableBoosterDialogFragment newInstance() {
        return new DisableBoosterDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        LayoutInflater inflater = ((MainActivity) mContext).getLayoutInflater();
        final View view = inflater.inflate(R.layout.disable_booster_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view)
                .setPositiveButton(R.string._continue, null)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button continueBtn = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                final Button cancelBtn = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                CheckBox agreeCheckbox = (CheckBox) view.findViewById(R.id.agree_checkbox);

                agreeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        continueBtn.setEnabled(isChecked);
                    }
                });

                continueBtn.setEnabled(false);
                continueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        continueBtn.setEnabled(false);
                        cancelBtn.setEnabled(false);

                        disableBooster(view);
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelDisable();
                    }
                });
            }
        });

        return alertDialog;
    }

    private void disableBooster(View view) {
        LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        contentLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                boolean isSuccess = Booster.disableBooster();

                final int resultCode;
                if (isSuccess) {
                    SettingsInfo settingsInfo = new SettingsInfo();
                    settingsInfo.setKey(SettingsFragment.PREF_ENABLE_BOOSTER);
                    settingsInfo.setValue(String.valueOf(false));

                    SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
                    settingsDAO.update(settingsInfo);

                    resultCode = Activity.RESULT_OK;
                } else {
                    resultCode = RESULT_FAILED;
                }

                UiHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
                        dismiss();
                    }
                });
            }
        });

    }

    private void cancelDisable() {
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
        dismiss();
    }

}
