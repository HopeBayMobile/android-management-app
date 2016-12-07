package com.hopebaytech.hcfsmgmt.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.service.PinAndroidFolderService;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.Booster;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.PollingServiceUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

public class HCFSMgmtReceiver extends BroadcastReceiver {

    private final String CLASSNAME = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Logs.init();
        }

        final String action = intent.getAction();
        Logs.d(CLASSNAME, "onReceive", "action=" + action);
        boolean isTeraAppLogin = TeraAppConfig.isTeraAppLogin(context);
        Logs.d(CLASSNAME, "onReceive", "isTeraAppLogin=" + isTeraAppLogin);
        if (isTeraAppLogin) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED: {
                    // Start an alarm to reset xfer
                    HCFSMgmtUtils.startResetXferAlarm(context);

                    // Start an alarm to notify local storage used ratio
                    HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(context);

                    // Start an alarm to notify insufficient pin space
                    HCFSMgmtUtils.startNotifyInsufficientPinSpaceAlarm(context);

                    // Start an alarm to update external app dir
                    HCFSMgmtUtils.startUpdateExternalAppDirAlarm(context);

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
                    return;
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
                    return;
                }
                case TeraIntent.ACTION_RESET_DATA_XFER: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_RESET_DATA_XFER);
                    context.startService(intentService);
                    return;
                }
                case TeraIntent.ACTION_UPDATE_EXTERNAL_APP_DIR: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_UPDATE_EXTERNAL_APP_DIR);
                    context.startService(intentService);
                    return;
                }
                case TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO);
                    context.startService(intentService);
                    return;
                }
                case TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE);
                    context.startService(intentService);
                    return;
                }
                case TeraIntent.ACTION_TOKEN_EXPIRED: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_TOKEN_EXPIRED);
                    context.startService(intentService);
                    return;
                }
                case TeraIntent.ACTION_EXCEED_PIN_MAX: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_EXCEED_PIN_MAX);
                    context.startService(intentService);
                    return;
                }
                case TeraIntent.ACTION_TRANSFER_COMPLETED: {
                    Intent intentService = new Intent(context, TeraMgmtService.class);
                    intentService.setAction(TeraIntent.ACTION_TRANSFER_COMPLETED);
                    context.startService(intentService);
                    return;
                }
            }
        }

        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED:
                // Add uid and pin system app and update app external dir list in uid.db
                Intent bootCompletedIntent = new Intent(context, TeraMgmtService.class);
                bootCompletedIntent.setAction(Intent.ACTION_BOOT_COMPLETED);
                context.startService(bootCompletedIntent);

                // Check restore status, if restore status is MINI_RESTORE_COMPLETED, change it to
                // FULL_RESTORE_IN_PROGRESS. Then, start Tera app.
                Intent checkRestoreStatusIntent = new Intent(context, TeraMgmtService.class);
                checkRestoreStatusIntent.setAction(TeraIntent.ACTION_CHECK_RESTORE_STATUS);
                context.startService(checkRestoreStatusIntent);

                // Start a job service to pin /storage/emulated/0/android folder until pin success
                PollingServiceUtils.startPollingService(
                        context,
                        Interval.PIN_ANDROID_FOLDER,
                        PollingServiceUtils.JOB_ID_PIN_ANDROID_FOLDER,
                        PinAndroidFolderService.class
                );
                return;
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

                    HCFSMgmtUtils.createMinimalApk(context, packageName, false /*blocking*/);
                }
                return;
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
                return;
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
                return;
            }
            case TeraIntent.ACTION_RESTORE_STAGE_1: {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                intentService.setAction(TeraIntent.ACTION_RESTORE_STAGE_1);
                intentService.putExtras(intent.getExtras());
                context.startService(intentService);
                return;
            }
            case TeraIntent.ACTION_RESTORE_STAGE_2: {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                intentService.setAction(TeraIntent.ACTION_RESTORE_STAGE_2);
                intentService.putExtras(intent.getExtras());
                context.startService(intentService);
                return;
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

}
