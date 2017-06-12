package com.hopebaytech.hcfsmgmt.utils;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.hopebaytech.hcfsmgmt.fragment.AppAvailableDialogFragment;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by rondou.chen on 2017/5/23.
 */

public class AppAvailableAlertActivity extends FragmentActivity
                                        implements AppAvailableDialogFragment.AppAvailableDialogListener {

    private static final String CLASSNAME = AppAvailableAlertActivity.class.getSimpleName();
    PendingIntent pendingIntent;

    public final String APP_AVAILABLE_DIALOG_DONT_SHOW_AGAIN = "app_available_dialog_dont_show_again";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pendingIntent = (PendingIntent) getIntent().getParcelableExtra("orIntent");

        showAppAvailableDialog();
    }

    @Override
    public void onBackPressed() {
        Logs.d(CLASSNAME, "onBackPressed", "");

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
        try {
            Intent intent = new Intent();
            pendingIntent.send(this, 0, intent);
        }catch (PendingIntent.CanceledException e) {
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
    }

    @Override
    public void onDialogSingleChoiceItemsClick(DialogFragment dialog, Boolean isChecked) {
        Logs.d(CLASSNAME, "onDialogSingleChoiceItemsClick", "isChecked = " + isChecked);
        if (isChecked) {
            Settings.System.putInt(getContentResolver(), APP_AVAILABLE_DIALOG_DONT_SHOW_AGAIN, 1);
        } else {
            Settings.System.putInt(getContentResolver(), APP_AVAILABLE_DIALOG_DONT_SHOW_AGAIN, 0);
        }

        Logs.d(CLASSNAME, "onDialogSingleChoiceItemsClick", "checkStatus = " +
                Settings.System.getInt(getContentResolver(), APP_AVAILABLE_DIALOG_DONT_SHOW_AGAIN, 0));
    }
}
