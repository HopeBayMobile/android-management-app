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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/16.
 */
public class TeraAppConfig {

    public static boolean isTeraAppLogin(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_TERA_APP_LOGIN, false);
        // Vince 可跳過 login
        //return true;
    }

    public static void enableApp(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HCFSMgmtUtils.PREF_TERA_APP_LOGIN, true);
        editor.apply();

        // Change sync status according to sync_wifi_only option
        boolean syncWifiOnly = true;
        SettingsDAO settingsDAO = SettingsDAO.getInstance(context);
        SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
        if (settingsInfo != null) {
            syncWifiOnly = Boolean.valueOf(settingsInfo.getValue());
        }
        HCFSMgmtUtils.changeCloudSyncStatus(context, syncWifiOnly);
    }

    public static void disableApp(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HCFSMgmtUtils.PREF_TERA_APP_LOGIN, false);
        editor.apply();
    }

}
