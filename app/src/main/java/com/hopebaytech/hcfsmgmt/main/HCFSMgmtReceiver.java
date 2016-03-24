package com.hopebaytech.hcfsmgmt.main;

import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.HCFSDBHelper;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.service.HCFSMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class HCFSMgmtReceiver extends BroadcastReceiver {

	private final String CLASSNAME = getClass().getSimpleName();
	private SharedPreferences sharedPreferences;

	@Override
	public void onReceive(Context context, Intent intent) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final String action = intent.getAction();
		HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "action=" + action);
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			/** Detect network status and determine whether sync data to cloud */
			HCFSMgmtUtils.detectNetworkAndSyncDataToCloud(context);

			/* Start an alarm to notify user when data is completed uploaded */
			boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLOAD_COMPLETED, true);
			if (notifyUploadCompletedPref) {
				HCFSMgmtUtils.startNotifyUploadCompletedAlarm(context);
			}

			/** Start an alarm to periodically pin/unpin data type file */
			DataTypeDAO dataTypeDAO = new DataTypeDAO(context);
			if (dataTypeDAO.getCount() != 0) {
				HCFSMgmtUtils.startPinDataTypeFileAlarm(context);
			}

			/** Start an alarm to reset xfer */
			HCFSMgmtUtils.startResetXferAlarm(context);

			/** Start an alarm to notify local storage used ratio */
			HCFSMgmtUtils.startNotifyLocalStorageUsedRatioAlarm(context);

			/** Add uid and pin system app */
			Intent addUidAndPinSystemAppIntent = new Intent(context, HCFSMgmtService.class);
			addUidAndPinSystemAppIntent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_ADD_UID_AND_PIN_SYSTEM_APP_WHEN_BOOT_UP);
			context.startService(addUidAndPinSystemAppIntent);
		} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			HCFSMgmtUtils.detectNetworkAndSyncDataToCloud(context);
		} else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
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
            String packageName = intent.getData().getSchemeSpecificPart();
            Intent intentService = new Intent(context, HCFSMgmtService.class);
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_UNPIN_UDPATE_APP);
            intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
            intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
            context.startService(intentService);
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
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
		} else if (action.equals(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM)) {
			String operation = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION);
			HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onReceive", "operation=" + operation);
			Intent intentService = new Intent(context, HCFSMgmtService.class);
			if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED)) {
				intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED);
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
	}

}
