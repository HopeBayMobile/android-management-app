package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/16.
 */
public class TeraAppConfig {

    public static boolean isTeraAppLogin(Context context) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        return sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_TERA_APP_LOGIN, false);
        return true;
    }

    public static void enableApp(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HCFSMgmtUtils.PREF_TERA_APP_LOGIN, true);
        editor.apply();
    }

    public static void disableApp(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HCFSMgmtUtils.PREF_TERA_APP_LOGIN, false);
        editor.apply();
    }

}
