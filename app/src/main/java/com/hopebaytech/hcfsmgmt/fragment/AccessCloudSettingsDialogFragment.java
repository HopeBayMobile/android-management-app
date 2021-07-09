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
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.TransferContentActivity;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/23.
 */
public class AccessCloudSettingsDialogFragment extends DialogFragment {

    public static final String TAG = AccessCloudSettingsDialogFragment.class.getSimpleName();
    private final String CLASSNAME = getClass().getSimpleName();

    private Context mContext;

    public static AccessCloudSettingsDialogFragment newInstance() {
        return new AccessCloudSettingsDialogFragment();
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
        View view = inflater.inflate(R.layout.access_cloud_settings_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view)
                .setPositiveButton(R.string.sync_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                    }
                })
                .setNegativeButton(R.string.connect_to_wifi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
        return builder.create();
    }
}
