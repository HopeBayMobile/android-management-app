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
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import com.hopebaytech.hcfsmgmt.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppAvailableDialogFragment extends AppCompatDialogFragment {


    public AppAvailableDialogFragment() {
        // Required empty public constructor
    }

    public interface AppAvailableDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNeutralClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
        public void onDialogSingleChoiceItemsClick(DialogFragment dialog, Boolean isChecked);
    }

    AppAvailableDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            // Instantiate the AppAvailableDialogListener so we can send events to the host
            mListener = (AppAvailableDialogListener) activity;
        } catch (ClassCastException e) {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(android.content.DialogInterface dialog, int keyCode,
                                 android.view.KeyEvent event) {

                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    //This is the filter
                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        getActivity().finish();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] checkBoxItem = {getString(R.string.app_available_dialog_dont_show_again)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.app_available_dialog_title)
                .setMultiChoiceItems(checkBoxItem, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        mListener.onDialogSingleChoiceItemsClick(AppAvailableDialogFragment.this, isChecked);
                    }
                })
                .setPositiveButton(R.string.app_available_dialog_positive, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(AppAvailableDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.app_available_dialog_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(AppAvailableDialogFragment.this);
                    }
                });

        return builder.create();
        //return super.onCreateDialog(savedInstanceState);
    }
}

