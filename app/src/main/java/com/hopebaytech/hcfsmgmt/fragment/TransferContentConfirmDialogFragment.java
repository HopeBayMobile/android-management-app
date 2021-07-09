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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.main.TransferContentActivity;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.util.List;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/11.
 */
public class TransferContentConfirmDialogFragment extends DialogFragment {

    public static final String TAG = TransferContentConfirmDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private Context mContext;

    public static TransferContentConfirmDialogFragment newInstance() {
        return new TransferContentConfirmDialogFragment();
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
        final View view = inflater.inflate(R.layout.settings_transfer_content_confirm_dialog, null);
        TextView username = (TextView) view.findViewById(R.id.username);

        final TextView errorMsg = (TextView) view.findViewById(R.id.error_msg);
        errorMsg.setVisibility(View.GONE);

        AccountDAO accountDAO = AccountDAO.getInstance(mContext);
        List<AccountInfo> accountInfoList = accountDAO.getAll();
        if (accountInfoList.size() != 0) {
            AccountInfo info = accountInfoList.get(0);
            username.setText(info.getEmail());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view)
                .setPositiveButton(R.string.transfer, null)
                .setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                final EditText password = (EditText) view.findViewById(R.id.password);

                positiveButton.setEnabled(false);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isConnected = NetworkUtils.isNetworkConnected(mContext);
                        if (isConnected) {
                            errorMsg.setVisibility(View.GONE);

                            Intent intent = new Intent(mContext, TransferContentActivity.class);
                            startActivity(intent);
                            dismiss();
                        } else {
                            errorMsg.setVisibility(View.VISIBLE);
                            errorMsg.setText(R.string.activate_alert_dialog_message);
                        }
                    }
                });

                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });

                password.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() != 0) {
                            positiveButton.setEnabled(true);
                            positiveButton.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                        } else {
                            positiveButton.setEnabled(false);
                            positiveButton.setTextColor(ContextCompat.getColor(mContext, R.color.C5));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        return alertDialog;
    }

}
