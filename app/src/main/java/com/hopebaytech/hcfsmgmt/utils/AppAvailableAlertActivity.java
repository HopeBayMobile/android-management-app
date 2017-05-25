package com.hopebaytech.hcfsmgmt.utils;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hopebaytech.hcfsmgmt.fragment.AppAvailableDialogFragment;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

/**
 * Created by rondou.chen on 2017/5/23.
 */

public class AppAvailableAlertActivity extends FragmentActivity
                                        implements AppAvailableDialogFragment.AppAvailableDialogListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showAppAvailableDialog();
    }

    @Override
    public void onBackPressed() {
        Log.d("Rondou", "onBackPressed");

        super.onBackPressed();
    }

    public void showAppAvailableDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new AppAvailableDialogFragment();
        dialog.show(getSupportFragmentManager(), "AppAvailableDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        finish();
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
        finish();
    }
}
