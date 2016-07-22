package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Aaron
 *         Created by Aaron on 2016/3/31.
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

    public static boolean isActivated(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, false);
    }

    public static boolean setHCFSConfig(String key, String value) {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.setHCFSConfig(key, value);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            String logMsg = "key=" + key + ", value=" + value + ", jsonResult=" + jsonResult;

            if (isSuccess) {
                Logs.i(CLASSNAME, "setHCFSConfig", logMsg);
            } else {
                Logs.e(CLASSNAME, "setHCFSConfig", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "setHCFSConfig", Log.getStackTraceString(e));
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
                Logs.i(CLASSNAME, "getHCFSConfig", logMsg);
            } else {
                Logs.e(CLASSNAME, "getHCFSConfig", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "getHCFSConfig", Log.getStackTraceString(e));
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
                Logs.i(CLASSNAME, "reloadConfig", "jsonResult=" + jsonResult);
            } else {
                Logs.e(CLASSNAME, "reloadConfig", "jsonResult=" + jsonResult);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "reloadConfig", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static void startSyncToCloud() {
        String jsonResult = HCFSApiUtils.setHCFSSyncStatus(1);
        Logs.d(CLASSNAME, "startSyncToCloud", jsonResult);
    }

    public static void startSyncToCloud(Context context, String logMsg) {
        Logs.d(CLASSNAME, "startSyncToCloud", logMsg);
        int notify_id = HCFSMgmtUtils.NOTIFY_ID_NETWORK_STATUS_CHANGED;
        String notify_title = context.getString(R.string.app_name);
        String notify_content = context.getString(R.string.notify_network_connected);
        HCFSMgmtUtils.notifyNetworkStatus(context, notify_id, notify_title, notify_content);
        startSyncToCloud();
    }

    public static void stopSyncToCloud() {
        String jsonResult = HCFSApiUtils.setHCFSSyncStatus(0);
        Logs.d(CLASSNAME, "stopSyncToCloud", jsonResult);
    }

    public static void stopSyncToCloud(Context context, String logMsg) {
        Logs.d(CLASSNAME, "stopSyncToCloud", logMsg);
        int notify_id = HCFSMgmtUtils.NOTIFY_ID_NETWORK_STATUS_CHANGED;
        String notify_title = context.getString(R.string.app_name);
        String notify_content = context.getString(R.string.notify_network_disconnected);
        HCFSMgmtUtils.notifyNetworkStatus(context, notify_id, notify_title, notify_content);
        stopSyncToCloud();
    }

    public static boolean storeHCFSConfig(RegisterResultInfo registerResultInfo) {
        boolean isFailed = false;
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_CURRENT_BACKEND, registerResultInfo.getBackendType())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_ACCOUNT, registerResultInfo.getAccount())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_USER, registerResultInfo.getUser())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PASS, registerResultInfo.getPassword())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_URL, registerResultInfo.getBackendUrl())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_CONTAINER, registerResultInfo.getBucket())) {
            isFailed = true;
        }
        if (!setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PROTOCOL, registerResultInfo.getProtocol())) {
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
