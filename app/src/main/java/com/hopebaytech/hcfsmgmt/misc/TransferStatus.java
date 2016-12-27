package com.hopebaytech.hcfsmgmt.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hopebaytech.hcfsmgmt.main.TransferContentActivity;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/21.
 */
public class TransferStatus {

    public static final int NONE = 0;
    public static final int TRANSFERRING = 1;
    public static final int WAIT_DEVICE = 2;
    public static final int TRANSFERRED = 3;

    public static void setTransferStatus(Context context, int status) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(TransferContentActivity.PREF_TRANSFER_STATUS, status);
        editor.apply();
    }

    public static int getTransferStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(TransferContentActivity.PREF_TRANSFER_STATUS, NONE);
    }

    public static void removeTransferStatus(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(TransferContentActivity.PREF_TRANSFER_STATUS);
        editor.apply();
    }

}
