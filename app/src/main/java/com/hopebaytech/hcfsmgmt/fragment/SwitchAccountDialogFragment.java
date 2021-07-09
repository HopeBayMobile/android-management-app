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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.ChangeAccountActivity;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/15.
 */
public class SwitchAccountDialogFragment extends DialogFragment {

    public static final String TAG = SwitchAccountDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;
    private Activity mActivity;

    public static SwitchAccountDialogFragment newInstance() {
        return new SwitchAccountDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(getString(R.string.alert_dialog_title_warning));
        builder.setMessage(getString(R.string.settings_change_account_warning_msg));
        builder.setPositiveButton(getString(R.string._continue), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TeraAppConfig.disableApp(mActivity);

                Intent intent = new Intent(mActivity, ChangeAccountActivity.class);
                mActivity.startActivity(intent);
                mActivity.finish();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();

        return builder.create();
    }
}
