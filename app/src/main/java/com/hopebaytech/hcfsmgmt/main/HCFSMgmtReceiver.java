package com.hopebaytech.hcfsmgmt.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.info.TeraIntent;
import com.hopebaytech.hcfsmgmt.service.TeraAPIServer;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;

public class HCFSMgmtReceiver extends BroadcastReceiver {

    private final String CLASSNAME = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String action = intent.getAction();
        Logs.d(CLASSNAME, "onReceive", "action=" + action);
//        boolean isHCFSActivated = TeraCloudConfig.isTeraAppLogin(context);
        boolean isTeraAppLogin = TeraAppConfig.isTeraAppLogin(context);
        if (isTeraAppLogin) {
            Logs.d(CLASSNAME, "onReceive", "isTeraAppLogin=" + isTeraAppLogin);
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                // Detect network status and determine whether sync data to cloud
                boolean syncWifiOnly = true;
                SettingsDAO mSettingsDAO = SettingsDAO.getInstance(context);
                SettingsInfo settingsInfo = mSettingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
                if (settingsInfo != null) {
                    syncWifiOnly = Boolean.valueOf(settingsInfo.getValue());
                }
                HCFSMgmtUtils.changeCloudSyncStatus(context, syncWifiOnly);

                // Start an alarm to reset xfer
                HCFSMgmtUtils.startResetXferAlarm(context);

                // Start an alarm to notify local storage used ratio
                HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(context);

                // Start an alarm to notify insufficient pin space
                HCFSMgmtUtils.startNotifyInsufficientPinSpaceAlarm(context);

                // Start an alarm to update external app dir
                HCFSMgmtUtils.startUpdateExternalAppDirAlarm(context);

                // Set silent Google sign-in to false
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(HCFSMgmtUtils.PREF_CHECK_DEVICE_STATUS, false);
                editor.apply();
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                // Detect network status changed and enable/disable data sync to cloud
                boolean syncWifiOnly = true;
                SettingsDAO mSettingsDAO = new SettingsDAO();
                SettingsInfo settingsInfo = mSettingsDAO.get(SettingsFragment.PREF_SYNC_WIFI_ONLY);
                if (settingsInfo != null) {
                    syncWifiOnly = Boolean.valueOf(settingsInfo.getValue());
                }
                HCFSMgmtUtils.changeCloudSyncStatus(context, syncWifiOnly);

                // Only execute once after system boot-up. Check the device status and execute the corresponding actions
                boolean isChecked = sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_CHECK_DEVICE_STATUS, false);
                if (!isChecked) {
                    Logs.d(CLASSNAME, "onReceive", "isChecked=" + isChecked);
                    if (NetworkUtils.isNetworkConnected(context)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(HCFSMgmtUtils.PREF_CHECK_DEVICE_STATUS, true);
                        editor.apply();

                        Intent intentService = new Intent(context, TeraMgmtService.class);
//                        intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_CHECK_DEVICE_STATUS);
                        intentService.setAction(TeraIntent.ACTION_CHECK_DEVICE_STATUS);
                        context.startService(intentService);
                    }
                }
            }
//            else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
//                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
//                if (!isReplacing) {
//                    Intent intentService = new Intent(mContext, TeraMgmtService.class);
//                    int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
//                    String packageName = intent.getData().getSchemeSpecificPart();
//                    intentService.putExtra(HCFSMgmtUtils.KEY_OPERATION, HCFSMgmtUtils.VALUE_ADD_UID_TO_DATABASE_AND_UNPIN_USER_APP);
//                    intentService.putExtra(HCFSMgmtUtils.KEY_UID, uid);
//                    intentService.putExtra(HCFSMgmtUtils.KEY_PACKAGE_NAME, packageName);
//                    mContext.startService(intentService);
//                }
//            } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
//                String packageName = intent.getData().getSchemeSpecificPart();
//                Intent intentService = new Intent(mContext, TeraMgmtService.class);
//                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
//                intentService.putExtra(HCFSMgmtUtils.KEY_OPERATION, HCFSMgmtUtils.VALUE_PIN_UNPIN_UDPATE_APP);
//                intentService.putExtra(HCFSMgmtUtils.KEY_UID, uid);
//                intentService.putExtra(HCFSMgmtUtils.KEY_PACKAGE_NAME, packageName);
//                mContext.startService(intentService);
//            } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
//                boolean isDataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
//                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
//                if (isDataRemoved && !isReplacing) {
//                    Intent intentService = new Intent(mContext, TeraMgmtService.class);
//                    int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
//                    String packageName = intent.getData().getSchemeSpecificPart();
//                    intentService.putExtra(HCFSMgmtUtils.KEY_OPERATION, HCFSMgmtUtils.VALUE_REMOVE_UID_FROM_DATABASE);
//                    intentService.putExtra(HCFSMgmtUtils.KEY_UID, uid);
//                    intentService.putExtra(HCFSMgmtUtils.KEY_PACKAGE_NAME, packageName);
//                    mContext.startService(intentService);
//                }
//            }
//            else if (action.equals(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM)) {
//                String operation = intent.getStringExtra(TeraIntent.KEY_OPERATION);
//                Logs.d(CLASSNAME, "onReceive", "operation=" + operation);
//                Intent intentService = new Intent(context, TeraMgmtService.class);
//                if (operation.equals(TeraIntent.VALUE_NOTIFY_UPLOAD_COMPLETED)) {
//                    intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_NOTIFY_UPLOAD_COMPLETED);
//                } else if (operation.equals(TeraIntent.VALUE_PIN_DATA_TYPE_FILE)) {
//                    intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_PIN_DATA_TYPE_FILE);
//                } else if (operation.equals(TeraIntent.VALUE_RESET_XFER)) {
//                    intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_RESET_XFER);
//                } else if (operation.equals(TeraIntent.VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
//                    intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);
//                } else if (operation.equals(TeraIntent.VALUE_INSUFFICIENT_PIN_SPACE)) {
//                    intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_INSUFFICIENT_PIN_SPACE);
//                } else {
//                    intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_NONE);
//                }
//                context.startService(intentService);
//            }
            else if (action.equals(TeraIntent.ACTION_PIN_DATA_TYPE_FILE)) {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                intentService.setAction(TeraIntent.ACTION_PIN_DATA_TYPE_FILE);
                context.startService(intentService);
            } else if (action.equals(TeraIntent.ACTION_RESET_XFER)) {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                intentService.setAction(TeraIntent.ACTION_RESET_XFER);
                context.startService(intentService);
            } else if (action.equals(TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                intentService.setAction(TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO);
                context.startService(intentService);
            } else if (action.equals(TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE)) {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                intentService.setAction(TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE);
                context.startService(intentService);
            }
        } else {
            Logs.i(CLASSNAME, "onReceive", "isHCFSActivated=" + isTeraAppLogin);
        }

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Add uid and pin system app and update app external dir list in uid.db
            Intent bootCompletedIntent = new Intent(context, TeraMgmtService.class);
            bootCompletedIntent.setAction(Intent.ACTION_BOOT_COMPLETED);
            context.startService(bootCompletedIntent);

//            // Add uid and pin system app
//            Intent addUidAndPinSystemAppIntent = new Intent(context, TeraMgmtService.class);
//            addUidAndPinSystemAppIntent.putExtra(TeraIntent.KEY_OPERATION,
//            context.startService(addUidAndPinSystemAppIntent);

//            // Update app external dir list in uid.db
//            Intent updateAppExternalDirIntent = new Intent(context, TeraMgmtService.class);
//            addUidAndPinSystemAppIntent.putExtra(TeraIntent.KEY_OPERATION,
//                    TeraIntent.VALUE_UPDATE_APP_EXTERNAL_DIR);
//            context.startService(updateAppExternalDirIntent);

            // start tera api server
            Intent teraAPIServer = new Intent(context, TeraAPIServer.class);
            context.startService(teraAPIServer);
        } else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            // Add uid info of new installed app to database and unpin user app on /data/data and /data/app
            boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (!isReplacing) {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                String packageName = intent.getData().getSchemeSpecificPart();
//                intent.putExtra(TeraIntent.KEY_OPERATION,
//                        TeraIntent.VALUE_ADD_UID_TO_DATABASE_AND_UNPIN_USER_APP);
                intentService.setAction(TeraIntent.ACTION_ADD_UID_TO_DB_AND_UNPIN_USER_APP);
                intentService.putExtra(TeraIntent.KEY_UID, uid);
                intentService.putExtra(TeraIntent.KEY_PACKAGE_NAME, packageName);
                context.startService(intentService);
            }
        } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            // Pin or unpin an update app according to pin_status field in uid.db
            String packageName = intent.getData().getSchemeSpecificPart();
            Intent intentService = new Intent(context, TeraMgmtService.class);
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            intentService.setAction(TeraIntent.ACTION_PIN_UNPIN_UDPATED_APP);
//            intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_PIN_UNPIN_UDPATE_APP);
            intentService.putExtra(TeraIntent.KEY_UID, uid);
            intentService.putExtra(TeraIntent.KEY_PACKAGE_NAME, packageName);
            context.startService(intentService);
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            // Remove uid info of uninstalled app from database
            boolean isDataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
            boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (isDataRemoved && !isReplacing) {
                Intent intentService = new Intent(context, TeraMgmtService.class);
                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                String packageName = intent.getData().getSchemeSpecificPart();
                intentService.setAction(TeraIntent.ACTION_REMOVE_UID_FROM_DB);
//                intentService.putExtra(TeraIntent.KEY_OPERATION,
//                        TeraIntent.VALUE_REMOVE_UID_FROM_DATABASE);
                intentService.putExtra(TeraIntent.KEY_UID, uid);
                intentService.putExtra(TeraIntent.KEY_PACKAGE_NAME, packageName);
                context.startService(intentService);
            }
        }
    }

}
