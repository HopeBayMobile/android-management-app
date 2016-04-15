package com.hopebaytech.hcfsmgmt.utils;

import android.util.Log;

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

    public static boolean isActivated() {
        return !getHCFSConfig(HCFS_CONFIG_SWIFT_ACCOUNT).isEmpty();
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

}
