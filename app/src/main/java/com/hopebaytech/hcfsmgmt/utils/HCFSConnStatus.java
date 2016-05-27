package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;

/**
 * @author Aaron
 * Created by Aaron on 2016/5/4.
 */
public class HCFSConnStatus {

    public static final int TRANS_FAILED = -1;
    public static final int TRANS_NOT_ALLOWED = 0;
    public static final int TRANS_NORMAL = 1;
    public static final int TRANS_IN_PROGRESS = 2;
    public static final int TRANS_SLOW = 3;

    public static final int DATA_TRANSFER_NONE = 0;
    public static final int DATA_TRANSFER_IN_PROGRESS = 1;
    public static final int DATA_TRANSFER_SLOW = 2;

    public static int getConnStatus(Context context, HCFSStatInfo statInfo) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        boolean syncWifiOnlyPref = sharedPreferences.getBoolean(context.getString(R.string.pref_sync_wifi_only), true);
        boolean syncWifiOnlyPref = sharedPreferences.getBoolean(SettingsFragment.PREF_SYNC_WIFI_ONLY, true);
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo == null) {
            return TRANS_NOT_ALLOWED;
        } else {
            if (syncWifiOnlyPref) {
                if (netInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                    return TRANS_NOT_ALLOWED;
                } else {
                    if (statInfo.isCloudConn()) {
                        int dataTransfer = statInfo.getDataTransfer();
                        switch (dataTransfer) {
                            case DATA_TRANSFER_IN_PROGRESS:
                                return TRANS_IN_PROGRESS;
                            case DATA_TRANSFER_SLOW:
                                return TRANS_SLOW;
                            default:
                                return TRANS_NORMAL;
                        }
                    } else {
                        return TRANS_FAILED;
                    }
                }
            } else {
                if (statInfo.isCloudConn()) {
                    int dataTransfer = statInfo.getDataTransfer();
                    switch (dataTransfer) {
                        case DATA_TRANSFER_IN_PROGRESS:
                            return TRANS_IN_PROGRESS;
                        case DATA_TRANSFER_SLOW:
                            return TRANS_SLOW;
                        default:
                            return TRANS_NORMAL;
                    }
                } else {
                    return TRANS_FAILED;
                }
            }
        }
    }

}
