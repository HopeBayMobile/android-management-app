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
package com.hopebaytech.hcfsmgmt.main;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.fragment.RestoreFailedFragment;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.AlarmUtils;
import com.hopebaytech.hcfsmgmt.utils.Booster;
import com.hopebaytech.hcfsmgmt.utils.HCFSEvent;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

import java.io.IOException;

public class HCFSMgmtReceiver extends BroadcastReceiver {

    private final String CLASSNAME = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Logs.init();

            // Start an alarm to send logs
            AlarmUtils.startSendLogsAlarm(context);

            try {
                Runtime.getRuntime().exec("su -c /system/hcfs/tera");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final String action = intent.getAction();
        Logs.d(CLASSNAME, "onReceive", "action=" + action);
        boolean isTeraAppLogin = TeraAppConfig.isTeraAppLogin(context);
        Logs.d(CLASSNAME, "onReceive", "isTeraAppLogin=" + isTeraAppLogin);
        if (isTeraAppLogin) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED: {
                    // Start an alarm to reset xfer
                    AlarmUtils.startResetDataXferAlarm(context);

                    // Start an alarm to monitor local storage used space
                    AlarmUtils.startMonitorLocalStorageUsedSpace(context);

                    // Start an alarm to monitor pin space
                    AlarmUtils.startMonitorPinnedSpace(context);

                    // Start an alarm to update external app dir
                    AlarmUtils.startMonitorExternalAppDirAlarm(context);

                    // Start an alarm to monitor booster used space
                    AlarmUtils.startMonitorBoosterUsedSpace(context);

                    // Set silent Google sign-in to false
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(HCFSMgmtUtils.PREF_CHECK_DEVICE_STATUS, false);
                    editor.apply();

                    // Reset BA logging option to invisible
                    editor.putBoolean(SettingsFragment.PREF_SHOW_BA_LOGGING_OPTION, false);
                    editor.apply();

                    // Show Tera ongoing notification on system boot-up
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_ONGOING_NOTIFICATION);
                    intentService.putExtra(TeraIntent.KEY_ONGOING, true);
                    context.startService(intentService);
                    break;
                }
                case ConnectivityManager.CONNECTIVITY_ACTION: {
                    // Detect network status changed and enable/disable data sync to cloud
                    boolean syncWifiOnly = true;
                    SettingsDAO settingsDAO = SettingsDAO.getInstance(context);
                    SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
                    if (settingsInfo != null) {
                        syncWifiOnly = Boolean.valueOf(settingsInfo.getValue());
                    }
                    HCFSMgmtUtils.changeCloudSyncStatus(context, syncWifiOnly);

                    // Only execute once after system boot-up. Check the device status and execute the corresponding actions
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean isChecked = sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_CHECK_DEVICE_STATUS, false);
                    if (!isChecked) {
                        Logs.d(CLASSNAME, "onReceive", "isChecked=" + isChecked);
                        if (NetworkUtils.isNetworkConnected(context)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(HCFSMgmtUtils.PREF_CHECK_DEVICE_STATUS, true);
                            editor.apply();

                            Intent intentService = new Intent(context, TeraMgmtService.class);
                            intentService.setAction(TeraIntent.ACTION_CHECK_DEVICE_STATUS);
                            context.startService(intentService);
                        }
                    }
                    break;
                }
                case TeraIntent.ACTION_TOKEN_EXPIRED: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_TOKEN_EXPIRED);
                    context.startService(intentService);
                    break;
                }
                case TeraIntent.ACTION_EXCEED_PIN_MAX: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_EXCEED_PIN_MAX);
                    context.startService(intentService);
                    break;
                }

                case TeraIntent.ACTION_ERASE_DATA:
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_ERASE_DATA);
                    context.startService(intentService);
                    break;
            }
        }

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED: {
                // Add uid and pin system app and update app external dir list in uid.db
                Intent bootCompletedIntent = new Intent(context, TeraMgmtService.class);
                bootCompletedIntent.setAction(Intent.ACTION_BOOT_COMPLETED);
                context.startService(bootCompletedIntent);

                // Check restore status, if restore status is MINI_RESTORE_COMPLETED, change it to
                // FULL_RESTORE_IN_PROGRESS. Then, start Tera app.
                Intent checkRestoreStatusIntent = new Intent(context, TeraMgmtService.class);
                checkRestoreStatusIntent.setAction(TeraIntent.ACTION_CHECK_RESTORE_STATUS);
                context.startService(checkRestoreStatusIntent);

                /*
                // Start a job service to pin /storage/emulated/0/android folder until pin success
                PeriodicServiceUtils.startPeriodicService(
                        context,
                        Interval.PIN_ANDROID_FOLDER,
                        JobServiceId.PIN_ANDROID_FOLDER,
                        PinAndroidFolderService.class
                );*/

                // Check booster is valid or not. If not, fix it.
                Intent checkAndFixBoosterIntent = new Intent(context, TeraMgmtService.class);
                checkAndFixBoosterIntent.setAction(TeraIntent.ACTION_CHECK_AND_FIX_BOOSTER);
                context.startService(checkAndFixBoosterIntent);
                break;
            }
            case Intent.ACTION_PACKAGE_ADDED: {
                // Add uid info of new installed app to database and unpin user app on /data/data and /data/app
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (!isReplacing) {
                    HCFSMgmtUtils.notifyAppListChange();
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                    String packageName = intent.getData().getSchemeSpecificPart();
                    intentService.setAction(TeraIntent.ACTION_ADD_UID_INFO_TO_DATABASE);
                    intentService.putExtra(TeraIntent.KEY_UID, uid);
                    intentService.putExtra(TeraIntent.KEY_PACKAGE_NAME, packageName);
                    context.startService(intentService);

                    // Create min apk in service instead
                    // HCFSMgmtUtils.createMinimalApk(context, packageName, false /*blocking*/);
                }
                break;
            }
            case Intent.ACTION_PACKAGE_REPLACED: {
                // Pin or unpin an update app according to pin_status field in uid.db
                HCFSMgmtUtils.notifyAppListChange();
                String packageName = intent.getData().getSchemeSpecificPart();
                Intent intentService = new Intent(context, TeraMgmtService.class);
                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                intentService.setAction(TeraIntent.ACTION_PIN_UNPIN_UDPATED_APP);
                intentService.putExtra(TeraIntent.KEY_UID, uid);
                intentService.putExtra(TeraIntent.KEY_PACKAGE_NAME, packageName);
                context.startService(intentService);

                HCFSMgmtUtils.createMinimalApk(context, packageName, false /*blocking*/);
                break;
            }
            case Intent.ACTION_PACKAGE_REMOVED: {
                // Remove uid info of uninstalled app from database
                boolean isDataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                if (isDataRemoved && !isReplacing) {
                    HCFSMgmtUtils.notifyAppListChange();
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                    String packageName = intent.getData().getSchemeSpecificPart();
                    Booster.clearBoosterPackageRemaining(packageName);
                    intentService.setAction(TeraIntent.ACTION_REMOVE_UID_FROM_DB);
                    intentService.putExtra(TeraIntent.KEY_UID, uid);
                    intentService.putExtra(TeraIntent.KEY_PACKAGE_NAME, packageName);
                    context.startService(intentService);
                }
                break;
            }
            case TeraIntent.ACTION_RESTORE_STAGE_1: {
                handleStage1RestoreEvent(context, intent);
                break;
            }
            case TeraIntent.ACTION_RESTORE_STAGE_2: {
                handleStage2RestoreEvent(context, intent);
                break;
            }
            case TeraIntent.ACTION_BOOSTER_PROCESS_COMPLETED: {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                intentService.setAction(TeraIntent.ACTION_BOOSTER_PROCESS_COMPLETED);
                context.startService(intentService);
                break;
            }
            case TeraIntent.ACTION_BOOSTER_PROCESS_FAILED: {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                intentService.setAction(TeraIntent.ACTION_BOOSTER_PROCESS_FAILED);
                context.startService(intentService);
                break;
            }
        }

    }

    private void handleStage1RestoreEvent(Context context, Intent intent) {
        if (MainApplication.Foreground.get().isForeground()) {
            Logs.d(CLASSNAME, "handleStage1RestoreEvent", "Application is not in foreground.");
            return;
        }

        int status = RestoreStatus.NONE;
        int errorCode = intent.getIntExtra(TeraIntent.KEY_RESTORE_ERROR_CODE, -1);
        Logs.d(CLASSNAME, "handleStage1RestoreEvent", "errorCode=" + errorCode);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (errorCode) {
            case 0:
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_COMPLETED);

                String rebootAction = context.getString(R.string.restore_system_reboot);
                Intent rebootIntent = new Intent(context, TeraMgmtService.class);
                rebootIntent.setAction(TeraIntent.ACTION_MINI_RESTORE_REBOOT_SYSTEM);
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, rebootIntent, 0);
                NotificationCompat.Action action = new NotificationCompat.Action(0, rebootAction, pendingIntent);

                int notifyId = NotificationEvent.ID_ONGOING;
                int flag = NotificationEvent.FLAG_ON_GOING
                        | NotificationEvent.FLAG_HEADS_UP
                        | NotificationEvent.FLAG_OPEN_APP;
                String title = context.getString(R.string.restore_ready_title);
                String message = context.getString(R.string.restore_ready_message);
                NotificationEvent.notify(context, notifyId, title, message, action, flag);
                break;
            case HCFSEvent.ErrorCode.ENOENT:
                status = RestoreStatus.Error.DAMAGED_BACKUP;
                break;
            case HCFSEvent.ErrorCode.ENOSPC:
                status = RestoreStatus.Error.OUT_OF_SPACE;
                break;
            case HCFSEvent.ErrorCode.ENETDOWN:
                status = RestoreStatus.Error.CONN_FAILED;
                break;
        }
        if (errorCode != 0) {
            editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, status);
            RestoreFailedFragment.startFailedNotification(context, status);
        }
        editor.apply();
    }

    private void handleStage2RestoreEvent(Context context, Intent intent) {
        if (MainApplication.Foreground.get().isForeground()) {
            Logs.d("Application is not in foreground.");
            return;
        }

        int status = RestoreStatus.NONE;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int errorCode = intent.getIntExtra(TeraIntent.KEY_RESTORE_ERROR_CODE, -1);
        switch (errorCode) {
            case 0:
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.FULL_RESTORE_COMPLETED);

                int flag = NotificationEvent.FLAG_HEADS_UP | NotificationEvent.FLAG_OPEN_APP |
                        NotificationEvent.FLAG_RESTART_ACTIVITY_TASK;
                int notifyId = NotificationEvent.ID_ONGOING;
                String title = context.getString(R.string.restore_done_title);
                String message = context.getString(R.string.restore_done_message);
                NotificationEvent.notify(context, notifyId, title, message, flag);
                break;
            case HCFSEvent.ErrorCode.ENOENT:
                status = RestoreStatus.Error.DAMAGED_BACKUP;
                break;
            case HCFSEvent.ErrorCode.ENOSPC:
                status = RestoreStatus.Error.OUT_OF_SPACE;
                break;
            case HCFSEvent.ErrorCode.ENETDOWN:
                status = RestoreStatus.Error.CONN_FAILED;
                break;
        }
        if (errorCode != 0) {
            editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, status);
            RestoreFailedFragment.startFailedNotification(context, status);
        }
        editor.apply();
    }
}
