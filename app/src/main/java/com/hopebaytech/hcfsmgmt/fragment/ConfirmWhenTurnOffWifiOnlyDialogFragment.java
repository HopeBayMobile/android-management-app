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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/9/26.
 */
public class ConfirmWhenTurnOffWifiOnlyDialogFragment extends DialogFragment {

    public static final String TAG = ConfirmWhenTurnOffWifiOnlyDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private Context mContext;

    private boolean mNeverAsk;

    public static ConfirmWhenTurnOffWifiOnlyDialogFragment newInstance() {
        return new ConfirmWhenTurnOffWifiOnlyDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View view = inflater.inflate(R.layout.confirm_when_turn_off_wifi_only_dialog, null);
        CheckBox askCheckbox = (CheckBox) view.findViewById(R.id.ask_checkbox);

        askCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Logs.d(CLASSNAME, "onCheckedChanged", "isChecked=" + isChecked);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(SettingsFragment.PREF_ASK_CONFIRM_TURN_OFF_WIFI_ONLY, isChecked);
                editor.apply();

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view)
                .setPositiveButton(R.string.yes, null)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
//                positiveButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
//                        dismiss();
//                    }
//                });

                Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setTextColor(ContextCompat.getColor(mContext, R.color.C5));
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, null);
                        dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }
}
