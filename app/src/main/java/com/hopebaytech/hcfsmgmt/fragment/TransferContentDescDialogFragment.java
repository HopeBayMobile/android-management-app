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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
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
import android.widget.Toast;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.main.TransferContentActivity;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentDescDialogFragment extends DialogFragment {

    public static final String TAG = TransferContentDescDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private Context mContext;

    public static TransferContentDescDialogFragment newInstance() {
        return new TransferContentDescDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        final View view = inflater.inflate(R.layout.settings_transfer_content_desc_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view)
                .setPositiveButton(R.string.next, null)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                CheckBox agreeCheckbox = (CheckBox) view.findViewById(R.id.agree_checkbox);

                agreeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            positiveButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                            positiveButton.setEnabled(true);
                        } else {
                            positiveButton.setTextColor(ContextCompat.getColor(mContext, R.color.C5));
                            positiveButton.setEnabled(false);
                        }
                    }
                });

                positiveButton.setEnabled(false);
                positiveButton.setTextColor(ContextCompat.getColor(mContext, R.color.C5));
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int networkCond = 0;
                        boolean showNetworkDialog = true;
                        boolean wifiOnlyEnabled = false;
                        SettingsDAO settingsDAO = SettingsDAO.getInstance(mContext);
                        SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
                        if (settingsInfo != null) {
                            wifiOnlyEnabled = Boolean.valueOf(settingsInfo.getValue());
                        }

                        if (!NetworkUtils.isNetworkConnected(mContext)) {
                            String message = getString(R.string.notify_cloud_disconnected);
                            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                            dismiss();
                            return;
                        }

                        int type = NetworkUtils.getNetworkType(mContext);
                        if (type == ConnectivityManager.TYPE_MOBILE) {
                            if (wifiOnlyEnabled) {
                                networkCond = TransContentNetworkDialogFragment.COND_MOBILE_WITH_WIFI_ONLY;
                            } else {
                                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                                boolean isAsked = settings.getBoolean(SettingsFragment.PREF_ASK_MOBILE_WITHOUT_WIFI_ONLY, false);
                                if (isAsked) {
                                    showNetworkDialog = false;
                                } else {
                                    networkCond = TransContentNetworkDialogFragment.COND_MOBILE_WITHOUT_WIFI_ONLY;
                                }
                            }
                        } else { // wifi network
                            showNetworkDialog = false;
                        }

                        if (showNetworkDialog) {
                            Bundle args = new Bundle();
                            args.putInt(TransContentNetworkDialogFragment.KEY_NETWORK_COND, networkCond);

                            DialogFragment dialogFragment = TransContentNetworkDialogFragment.newInstance();
                            dialogFragment.setArguments(args);
                            dialogFragment.show(getFragmentManager(), TransContentNetworkDialogFragment.TAG);
                        } else {
                            Intent intent = new Intent(mContext, TransferContentActivity.class);
                            startActivity(intent);
                        }
                        dismiss();
                    }
                });

                negativeButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });
            }
        });

        return alertDialog;
    }

}
