package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Aaron on 2016/3/31.
 */
public class HCFSConfig {

    public static final String CLASSNAME = HCFSConfig.class.getSimpleName();
    public static final String TAG = HCFSMgmtUtils.TAG;

    public static final String HCFS_CONFIG_CURRENT_BACKEND = "current_backend";
    public static final String HCFS_CONFIG_SWIFT_ACCOUNT = "swift_account";
    public static final String HCFS_CONFIG_SWIFT_USER = "swift_user";
    public static final String HCFS_CONFIG_SWIFT_PASS = "swift_pass";
    public static final String HCFS_CONFIG_SWIFT_URL = "swift_url";
    public static final String HCFS_CONFIG_SWIFT_CONTAINER = "swift_container";
    public static final String HCFS_CONFIG_SWIFT_PROTOCOL = "swift_protocol";
    public static final String HCFS_CONFIG_SWIFT_TOKEN = "swift_token";

    public static boolean isActivated(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_IS_HCFS_ACTIVATED, false);
    }

    public static boolean setHCFSConfig(String key, String value) {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.setHCFSConfig(key, value);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            String logMsg = "key=" + key + ", value=" + value + ", jsonResult=" + jsonResult;

            if (isSuccess) {
                HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "setHCFSConfig", logMsg);
            } else {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "setHCFSConfig", logMsg);
            }
        } catch (JSONException e) {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "setHCFSConfig", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static String getHCFSConfig(String key) {
        String resultStr = "";
        try {
            String jsonResult = HCFSApiUtils.getHCFSConfig(key);
            String logMsg = "jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                JSONObject dataObj = jObject.getJSONObject("data");
                resultStr = dataObj.getString(key);
                HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "getHCFSConfig", logMsg);
            } else {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getHCFSConfig", logMsg);
            }
        } catch (JSONException e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getHCFSConfig", Log.getStackTraceString(e));
        }
        return resultStr;
    }

    public static boolean reloadConfig() {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.reloadConfig();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "reloadConfig", "jsonResult=" + jsonResult);
            } else {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "reloadConfig", "jsonResult=" + jsonResult);
            }
        } catch (JSONException e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "reloadConfig", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static void startSyncToCloud() {
        String jsonResult = HCFSApiUtils.setHCFSSyncStatus(1);
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "startSyncToCloud", jsonResult);
    }

    public static void startSyncToCloud(Context context, String logMsg) {
        // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        // Editor editor = sharedPreferences.edit();
        // String key_connected = SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED;
        // String key_disconnected = SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED;
        // boolean is_first_network_connected_received = sharedPreferences.getBoolean(key_connected, true);
        // if (is_first_network_connected_received) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "startSyncToCloud", logMsg);
        int notify_id = HCFSMgmtUtils.NOTIFY_ID_NETWORK_STATUS_CHANGED;
        String notify_title = context.getString(R.string.app_name);
        String notify_content = context.getString(R.string.notify_network_connected);
        HCFSMgmtUtils.notify_network_status(context, notify_id, notify_title, notify_content);
        startSyncToCloud();
        // editor.putBoolean(key_connected, false);
        // }
        // editor.putBoolean(key_disconnected, true);
        // editor.commit();
    }

    public static void stopSyncToCloud() {
        String jsonResult = HCFSApiUtils.setHCFSSyncStatus(0);
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "stopSyncToCloud", jsonResult);
    }

    public static void stopSyncToCloud(Context context, String logMsg) {
        // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        // Editor editor = sharedPreferences.edit();
        // String key_disconnected = SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED;
        // String key_connected = SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED;
        // boolean is_first_network_disconnected_received = sharedPreferences.getBoolean(key_disconnected, true);
        // if (is_first_network_disconnected_received) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "startSyncToCloud", logMsg);
        int notify_id = HCFSMgmtUtils.NOTIFY_ID_NETWORK_STATUS_CHANGED;
        String notify_title = context.getString(R.string.app_name);
        String notify_content = context.getString(R.string.notify_network_disconnected);
        HCFSMgmtUtils.notify_network_status(context, notify_id, notify_title, notify_content);
        stopSyncToCloud();
        // editor.putBoolean(key_disconnected, false);
        // }
        // editor.putBoolean(key_connected, true);
        // editor.commit();
    }

    public static boolean storeHCFSConfig(AuthResultInfo authResultInfo) {
        boolean isFailed = false;
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_CURRENT_BACKEND, authResultInfo.getBackendType())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_ACCOUNT, authResultInfo.getAccount())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_USER, authResultInfo.getUser())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PASS, authResultInfo.getPassword())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_URL, authResultInfo.getBackendUrl())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_CONTAINER, authResultInfo.getBucket())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PROTOCOL, authResultInfo.getProtocol())) {
            isFailed = true;
        }
        if (!reloadConfig()) {
            isFailed = true;
        }
        return isFailed;
    }

    public static void resetHCFSConfig() {
        setHCFSConfig(HCFSConfig.HCFS_CONFIG_CURRENT_BACKEND, "NONE");
        setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_ACCOUNT, "");
        setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_USER, "");
        setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PASS, "");
        setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_URL, "");
        setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_CONTAINER, "");
        setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PROTOCOL, "");
    }

}
