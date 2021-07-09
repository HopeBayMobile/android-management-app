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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/4.
 */
public class HCFSConnStatus {

    public static final int TRANS_FAILED = -1;
    public static final int TRANS_NOT_ALLOWED = 0;
    public static final int TRANS_NORMAL = 1;
    public static final int TRANS_IN_PROGRESS = 2;
    public static final int TRANS_SLOW = 3;
    public static final int TRANS_RECONNECTING = 4;

    private static final int DATA_TRANSFER_NONE = 0;
    private static final int DATA_TRANSFER_IN_PROGRESS = 1;
    private static final int DATA_TRANSFER_SLOW = 2;

    /*
    * First check network connection status of device, and then check HCFS conn.
    * */
    public static int getConnStatus(Context context, HCFSStatInfo statInfo) {
        boolean syncWifiOnlyPref = true;
        SettingsDAO mSettingsDAO = SettingsDAO.getInstance(context);
        SettingsInfo settingsInfo = mSettingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
        if (settingsInfo != null) {
            syncWifiOnlyPref = Boolean.valueOf(settingsInfo.getValue());
        }
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (netInfo == null) {
            return TRANS_NOT_ALLOWED;
        } else {
            if (syncWifiOnlyPref) {
                if (netInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                    return TRANS_NOT_ALLOWED;
                } else {
                    return getHCFSConnStatus(context, statInfo);
                }
            } else {
                return getHCFSConnStatus(context, statInfo);
            }
        }
    }

    /*
    * Just check Connection status of HCFS
    * */
    public static int getHCFSConnStatus(Context context, HCFSStatInfo statInfo) {
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
            // Check if it is retrying connecting
            if (statInfo.isRetryConn())
                return TRANS_RECONNECTING;
            else
                return TRANS_FAILED;
        }
    }

    public static boolean isAvailable(Context context, HCFSStatInfo statInfo) {
        int connStatus = getConnStatus(context, statInfo);
        return connStatus == HCFSConnStatus.TRANS_NORMAL ||
                connStatus == HCFSConnStatus.TRANS_IN_PROGRESS ||
                connStatus == HCFSConnStatus.TRANS_SLOW;
    }

    public static boolean isHCFSConnAvailable(Context context, HCFSStatInfo statInfo) {
        int connStatus = getHCFSConnStatus(context, statInfo);
        return connStatus == HCFSConnStatus.TRANS_NORMAL ||
                connStatus == HCFSConnStatus.TRANS_IN_PROGRESS ||
                connStatus == HCFSConnStatus.TRANS_SLOW;
    }

}
