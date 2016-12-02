package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.TeraStatDAO;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.TeraStatInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Aaron
 *         Created by Aaron on 2016/3/31.
 */
public class TeraCloudConfig {

    public static final String CLASSNAME = TeraCloudConfig.class.getSimpleName();
    public static final String TAG = HCFSMgmtUtils.TAG;

    public static final String HCFS_CONFIG_CURRENT_BACKEND = "current_backend";
    public static final String HCFS_CONFIG_SWIFT_ACCOUNT = "swift_account";
    public static final String HCFS_CONFIG_SWIFT_USER = "swift_user";
    public static final String HCFS_CONFIG_SWIFT_PASS = "swift_pass";
    public static final String HCFS_CONFIG_SWIFT_URL = "swift_url";
    public static final String HCFS_CONFIG_SWIFT_CONTAINER = "swift_container";
    public static final String HCFS_CONFIG_SWIFT_PROTOCOL = "swift_protocol";

    public static void activateTeraCloud(Context context) {
        TeraStatDAO teraStatDAO = TeraStatDAO.getInstance(context);
        TeraStatInfo teraStatInfo = new TeraStatInfo();
        teraStatInfo.setEnabled(true);
        if (teraStatDAO.getCount() == 0) {
            teraStatDAO.insert(teraStatInfo);
        } else {
            teraStatDAO.update(teraStatInfo);
        }
    }

    public static boolean isTeraCloudActivated(Context context) {
        TeraStatDAO teraStatDAO = TeraStatDAO.getInstance(context);
        TeraStatInfo teraStatInfo = teraStatDAO.getFirst();
        if (teraStatInfo != null) {
            return teraStatInfo.isEnabled();
        } else {
            return false;
        }
    }

    public static boolean setHCFSConfig(String key, String value) {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.setHCFSConfig(key, value);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            String logMsg = "key=" + key + ", value=" + value + ", jsonResult=" + jsonResult;

            if (isSuccess) {
                Logs.d(CLASSNAME, "setHCFSConfig", logMsg);
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
        int notify_id = NotificationEvent.ID_NETWORK_STATUS_CHANGED;
        String notify_title = context.getString(R.string.app_name);
        String notify_content = context.getString(R.string.notify_cloud_connected);
        HCFSMgmtUtils.notifyNetworkStatus(context, notify_id, notify_title, notify_content);
        startSyncToCloud();
    }

    public static void stopSyncToCloud() {
        String jsonResult = HCFSApiUtils.setHCFSSyncStatus(0);
        Logs.d(CLASSNAME, "stopSyncToCloud", jsonResult);
    }

    public static void stopSyncToCloud(Context context, String logMsg) {
        Logs.d(CLASSNAME, "stopSyncToCloud", logMsg);
        int notify_id = NotificationEvent.ID_NETWORK_STATUS_CHANGED;
        String notify_title = context.getString(R.string.app_name);
        String notify_content = context.getString(R.string.notify_cloud_disconnected);
        HCFSMgmtUtils.notifyNetworkStatus(context, notify_id, notify_title, notify_content);
        stopSyncToCloud();
    }

    public static boolean storeHCFSConfig(DeviceServiceInfo deviceServiceInfo) {
        boolean isSuccess = true;
        if (!setHCFSConfig(HCFS_CONFIG_CURRENT_BACKEND, deviceServiceInfo.getBackend().getBackendType())) {
            isSuccess = false;
        }
        if (!setHCFSConfig(HCFS_CONFIG_SWIFT_USER, deviceServiceInfo.getBackend().getUser())) {
            isSuccess = false;
        }
        if (!setHCFSConfig(HCFS_CONFIG_SWIFT_CONTAINER, deviceServiceInfo.getBackend().getBucket())) {
            isSuccess = false;
        }
        if (!reloadConfig()) {
            isSuccess = false;
        }
        return isSuccess;
    }

    public static boolean storeHCFSConfigWithoutReload(DeviceServiceInfo deviceServiceInfo) {
        boolean isSuccess = true;
        if (!setHCFSConfig(HCFS_CONFIG_CURRENT_BACKEND, deviceServiceInfo.getBackend().getBackendType())) {
            isSuccess = false;
        }
        if (!setHCFSConfig(HCFS_CONFIG_SWIFT_USER, deviceServiceInfo.getBackend().getUser())) {
            isSuccess = false;
        }
        if (!setHCFSConfig(HCFS_CONFIG_SWIFT_CONTAINER, deviceServiceInfo.getBackend().getBucket())) {
            isSuccess = false;
        }
        return isSuccess;
    }

    public static void resetHCFSConfig() {
        setHCFSConfig(HCFS_CONFIG_CURRENT_BACKEND, "NONE");
        setHCFSConfig(HCFS_CONFIG_SWIFT_ACCOUNT, "");
        setHCFSConfig(HCFS_CONFIG_SWIFT_USER, "");
        setHCFSConfig(HCFS_CONFIG_SWIFT_PASS, "");
        setHCFSConfig(HCFS_CONFIG_SWIFT_URL, "");
        setHCFSConfig(HCFS_CONFIG_SWIFT_CONTAINER, "");
//        setHCFSConfig(HCFS_CONFIG_SWIFT_PROTOCOL, "");
    }

}
