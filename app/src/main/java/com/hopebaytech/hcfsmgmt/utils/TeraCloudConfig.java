/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.text.TextUtils;
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
    public static final String HCFS_CONFIG_GOOGLEDRIVE_FOLDER = "googledrive_folder";

    private class HCFSBackendType {
        public static final String GOOGLEDRIVE = "googledrive";
        public static final String SWIFT = "swift";
        public static final String SWIFTTOKEN = "swifttoken";
    }

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

    public static boolean storeHCFSConfig(DeviceServiceInfo deviceServiceInfo, Context context) {
        boolean isSuccess = true;

        if (!storeHCFSConfigWithoutReload(deviceServiceInfo, context))
            isSuccess = false;

        /* Let HCFS reload configuration */
        if (!reloadConfig())
            isSuccess = false;

        return isSuccess;
    }

    public static boolean storeHCFSConfigWithoutReload(DeviceServiceInfo deviceServiceInfo, Context context) {
        boolean isSuccess = true;
        String nowBackend = deviceServiceInfo.getBackend().getBackendType().toLowerCase();

        if (!setHCFSConfig(HCFS_CONFIG_CURRENT_BACKEND, nowBackend))
            isSuccess = false;

        switch (nowBackend) {
            case HCFSBackendType.GOOGLEDRIVE:
                /* Check backend type and add google drive folder name. */
                String imei = deviceServiceInfo.getImei();
                if (TextUtils.isEmpty(imei)) {
                    imei = HCFSMgmtUtils.getDeviceImei(context);
                }
                String rootFolderName = String.format("tera.%s", imei);
                if (!setHCFSConfig(HCFS_CONFIG_GOOGLEDRIVE_FOLDER, rootFolderName)) {
                    isSuccess = false;
                    Logs.e(CLASSNAME, "setConfig", "Failed to set googledrive folder name");
                }
                break;

            case HCFSBackendType.SWIFTTOKEN:
            case HCFSBackendType.SWIFT:
                /* Set swift backend info */
                if (!setHCFSConfig(HCFS_CONFIG_SWIFT_USER, deviceServiceInfo.getBackend().getUser())) {
                    isSuccess = false;
                }
                if (!setHCFSConfig(HCFS_CONFIG_SWIFT_CONTAINER, deviceServiceInfo.getBackend().getBucket())) {
                    isSuccess = false;
                }
                break;

            default:
                Logs.e(CLASSNAME, "setConfig", "Unsupported backend type %s", nowBackend);
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
