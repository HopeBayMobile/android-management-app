package com.hopebaytech.hcfsmgmt.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.service.HCFSMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

public class HCFSMgmtReceiver extends BroadcastReceiver {

    private final String CLASSNAME = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String action = intent.getAction();
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "action=" + action);
        boolean isHCFSActivated = HCFSConfig.isActivated(context);
        if (isHCFSActivated) {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "isHCFSActivated=" + isHCFSActivated);
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                /** Detect network status and determine whether sync data to cloud */
//                boolean syncWifiOnly = sharedPreferences.getBoolean(context.getString(R.string.pref_sync_wifi_only), true);
                boolean syncWifiOnly = sharedPreferences.getBoolean(SettingsFragment.PREF_SYNC_WIFI_ONLY, true);
                HCFSMgmtUtils.changeCloudSyncStatus(context, syncWifiOnly);

                /** Start an alarm to notify user when data is completed uploaded */
//                boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLOAD_COMPLETED, true);
//                if (notifyUploadCompletedPref) {
//                    HCFSMgmtUtils.startNotifyUploadCompletedAlarm(mContext);
//                }

                /** Start an alarm to periodically pin/unpin data type file */
//                DataTypeDAO dataTypeDAO = DataTypeDAO.getInstance(mContext);
//                if (dataTypeDAO.getCount() != 0) {
//                    HCFSMgmtUtils.startPinDataTypeFileAlarm(mContext);
//                }

                /** Start an alarm to reset xfer */
                HCFSMgmtUtils.startResetXferAlarm(context);

                /** Start an alarm to notify local storage used ratio */
                HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(context);

                /** Set silent Google sign-in to false */
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(HCFSMgmtUtils.PREF_SILENT_SIGN_IN, false);
                editor.apply();
            } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                /** Detect network status changed and enable/disable data sync to cloud */
//                boolean syncWifiOnly = sharedPreferences.getBoolean(context.getString(R.string.pref_sync_wifi_only), true);
                boolean syncWifiOnly = sharedPreferences.getBoolean(SettingsFragment.PREF_SYNC_WIFI_ONLY, true);
                HCFSMgmtUtils.changeCloudSyncStatus(context, syncWifiOnly);

                /** Execute silent Google sign-in */
                boolean isSilentSignIn = sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_SILENT_SIGN_IN, false);
                if (!isSilentSignIn) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "isSilentSignIn=" + isSilentSignIn);
                    if (NetworkUtils.isNetworkConnected(context)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(HCFSMgmtUtils.PREF_SILENT_SIGN_IN, true);
                        editor.apply();

                        Intent intentService = new Intent(context, HCFSMgmtService.class);
                        intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_SILENT_SIGN_IN);
                        context.startService(intentService);
                    }
                }
            }
//            else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
//                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
//                if (!isReplacing) {
//                    Intent intentService = new Intent(mContext, HCFSMgmtService.class);
//                    int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
//                    String packageName = intent.getData().getSchemeSpecificPart();
//                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_ADD_UID_TO_DATABASE_AND_UNPIN_USER_APP);
//                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
//                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
//                    mContext.startService(intentService);
//                }
//            } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
//                String packageName = intent.getData().getSchemeSpecificPart();
//                Intent intentService = new Intent(mContext, HCFSMgmtService.class);
//                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
//                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_UNPIN_UDPATE_APP);
//                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
//                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
//                mContext.startService(intentService);
//            } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
//                boolean isDataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
//                boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
//                if (isDataRemoved && !isReplacing) {
//                    Intent intentService = new Intent(mContext, HCFSMgmtService.class);
//                    int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
//                    String packageName = intent.getData().getSchemeSpecificPart();
//                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_REMOVE_UID_FROM_DATABASE);
//                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
//                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
//                    mContext.startService(intentService);
//                }
//            }
            else if (action.equals(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM)) {
                String operation = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION);
                HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "operation=" + operation);
                Intent intentService = new Intent(context, HCFSMgmtService.class);
                if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLOAD_COMPLETED)) {
                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLOAD_COMPLETED);
                } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE)) {
                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE);
                } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_RESET_XFER)) {
                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_RESET_XFER);
                } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);
                } else {
                    intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NONE);
                }
                context.startService(intentService);
            }
        } else {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "isHCFSActivated=" + isHCFSActivated);
        }

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            /** Add uid and pin system app */
            Intent addUidAndPinSystemAppIntent = new Intent(context, HCFSMgmtService.class);
            addUidAndPinSystemAppIntent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_ADD_UID_AND_PIN_SYSTEM_APP_WHEN_BOOT_UP);
            context.startService(addUidAndPinSystemAppIntent);
        } else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            /** Add uid info of new installed app to database and unpin user app on /data/data and /data/app */
            boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (!isReplacing) {
                Intent intentService = new Intent(context, HCFSMgmtService.class);
                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                String packageName = intent.getData().getSchemeSpecificPart();
                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_ADD_UID_TO_DATABASE_AND_UNPIN_USER_APP);
                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
                context.startService(intentService);
            }
        } else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
            /** Pin or unpin an update app according to pin_status field in uid.db */
            String packageName = intent.getData().getSchemeSpecificPart();
            Intent intentService = new Intent(context, HCFSMgmtService.class);
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_UNPIN_UDPATE_APP);
            intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
            intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
            context.startService(intentService);
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            /** Remove uid info of uninstalled app from database */
            boolean isDataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
            boolean isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (isDataRemoved && !isReplacing) {
                Intent intentService = new Intent(context, HCFSMgmtService.class);
                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                String packageName = intent.getData().getSchemeSpecificPart();
                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_REMOVE_UID_FROM_DATABASE);
                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
                intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
                context.startService(intentService);
            }
        }
    }

}
