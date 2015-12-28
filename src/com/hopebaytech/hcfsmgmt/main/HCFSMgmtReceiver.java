package com.hopebaytech.hcfsmgmt.main;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.services.HCFSMgmtService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

public class HCFSMgmtReceiver extends BroadcastReceiver {

	 private SharedPreferences sharedPreferences;

	@Override
	public void onReceive(Context mContext, Intent intent) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		final String action = intent.getAction();
		Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtReceiver action: " + action);
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			// Detect network status and determine whether sync data to cloud 
			detectNetworkStatusAndSyncToCloud(mContext);
			
			// Start a notification alarm for completed data upload
			boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLAOD_COMPLETED, true);
			if (notifyUploadCompletedPref) {
				HCFSMgmtUtils.startNotifyUploadCompletedAlarm(mContext);
			}
			
			// Start an alarm for periodic pin/unpin data type file 
			if (HCFSMgmtUtils.DEFAULT_PINNED_STATUS) {
				HCFSMgmtUtils.startPinDataTypeFileAlarm(mContext);
			}
			
			// Start reset xfer alarm
			HCFSMgmtUtils.startResetXferAlarm(mContext);
			
			// Create uid database if it not exists or update it
			Intent intentService = new Intent(mContext, HCFSMgmtService.class);
			intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_LAUNCH_UID_DATABASE);
			mContext.startService(intentService);
		} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			detectNetworkStatusAndSyncToCloud(mContext);
		} else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
			Intent intentService = new Intent(mContext, HCFSMgmtService.class);
			int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
			String packageName = intent.getData().getSchemeSpecificPart();
			intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_ADD_UID_TO_DATABASE);
			intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
			intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
			mContext.startService(intentService); 
		} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			Intent intentService = new Intent(mContext, HCFSMgmtService.class);
			int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
			String packageName = intent.getData().getSchemeSpecificPart();
			intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_REMOVE_UID_FROM_DATABASE);
			intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_UID, uid);
			intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME, packageName);
			mContext.startService(intentService);
		} else if (action.equals(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM)) {
			int operation = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, -1);
			Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtReceiver operation: " + operation);
			Intent intentService = new Intent(mContext, HCFSMgmtService.class);
			switch (operation) {
			case HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED:
				intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED);
				mContext.startService(intentService);
				break;
			case HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE:
				intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE);
				mContext.startService(intentService);
				break;
			case HCFSMgmtUtils.INTENT_VALUE_RESET_XFER:
				intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_RESET_XFER);
				mContext.startService(intentService);
				break;
			default:
				break;
			}
		}
	}

	private void startSyncToCloud(Context context, String logMsg) {
		Editor editor = sharedPreferences.edit();
		boolean is_first_network_connected_received = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED,
				true);
		if (is_first_network_connected_received) {
			Log.d(HCFSMgmtUtils.TAG, logMsg);
			notify_network_status(context, HCFSMgmtUtils.NOTIFY_ID_NETWORK_STATUS_CHANGED, context.getString(R.string.app_name),
					context.getString(R.string.notify_network_connected));
			HCFSMgmtUtils.startSyncToCloud();
			// is_first_network_connected_received = false;
			editor.putBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED, false);
		}
		// is_first_network_disconnected_received = true;
		editor.putBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED, true);
		editor.commit();
	}

	private void stopSyncToCloud(Context context, String logMsg) {
		Editor editor = sharedPreferences.edit();
		boolean is_first_network_disconnected_received = sharedPreferences
				.getBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED, true);
		if (is_first_network_disconnected_received) {
			Log.d(HCFSMgmtUtils.TAG, logMsg);
			notify_network_status(context, HCFSMgmtUtils.NOTIFY_ID_NETWORK_STATUS_CHANGED, context.getString(R.string.app_name),
					context.getString(R.string.notify_network_disconnected));
			HCFSMgmtUtils.stopSyncToCloud();
			// is_first_network_disconnected_received = false;
			editor.putBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED, false);
		}
		// is_first_network_connected_received = true;
		editor.putBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED, true);
		editor.commit();
	}

	private void notify_network_status(Context context, int notify_id, String notify_title, String notify_content) {
		boolean notifyConnFailedRecoveryPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_CONN_FAILED_RECOVERY, false);
		if (notifyConnFailedRecoveryPref) {
			HCFSMgmtUtils.notifyEvent(context, notify_id, notify_title, notify_content);
		}
	}

	private void detectNetworkStatusAndSyncToCloud(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
		boolean syncWifiOnlyPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_SYNC_WIFI_ONLY, true);
		if (syncWifiOnlyPref) {
			if (netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				String logMsg = "Wifi is connected";
				startSyncToCloud(context, logMsg);
			} else {
				String logMsg = "Wifi is not connected";
				stopSyncToCloud(context, logMsg);
			}
		} else {
			if (netInfo != null && netInfo.isConnected()) {
				String logMsg = "Wifi or Mobile network is connected";
				startSyncToCloud(context, logMsg);
			} else {
				String logMsg = "Wifi or Mobile network is not connected";
				stopSyncToCloud(context, logMsg);
			}
		}
	}

}
