package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    public static boolean isNetworkConnected(Context context) {
        boolean isConnected;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            isConnected = true;
        } else {
            isConnected = false;
        }
        return isConnected;
    }

    public static boolean isMobileNetworkConnected(Context context) {
        boolean isConnected = false;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            isConnected = true;
        }
        return isConnected;
    }

    public static boolean isWifiNetworkConnected(Context context) {
        boolean isConnected = false;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            isConnected = true;
        }
        return isConnected;
    }

}
