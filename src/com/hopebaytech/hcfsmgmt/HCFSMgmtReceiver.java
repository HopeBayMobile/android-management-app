package com.hopebaytech.hcfsmgmt;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.services.CheckUploadCompletedService;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class HCFSMgmtReceiver extends BroadcastReceiver {

	private SharedPreferences sharedPreferences;

	@Override
	public void onReceive(Context context, Intent intent) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		final String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			detectNetworkStatusAndSyncToCloud(context);
			boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLAOD_COMPLETED, true);
			if (notifyUploadCompletedPref) {
				HCFSMgmtUtils.startNotifyUploadCompletedAlarm(context);
			}
		} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			detectNetworkStatusAndSyncToCloud(context);
		} else if (action.equals(HCFSMgmtUtils.NOTIFY_UPLOAD_COMPLETED_ALARM_INTENT_ACTION)) {
			boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLAOD_COMPLETED, true);
			if (notifyUploadCompletedPref) {
				Intent intentService = new Intent(context, CheckUploadCompletedService.class);
				context.startService(intentService);
			}
		}
	}

	private void startSyncToCloud(Context context, String logMsg) {
		Editor editor = sharedPreferences.edit();
		boolean is_first_network_connected_received = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED, true);		
		if (is_first_network_connected_received) {
			Log.d(HCFSMgmtUtils.TAG, logMsg);
			notify_network_status(context, HCFSMgmtUtils.ID_NOTIFY_NETWORK_STATUS_CHANGED, context.getString(R.string.app_name),
					context.getString(R.string.notify_network_connected));
			// HCFSApiUtils.start_sync_to_cloud();
//			is_first_network_connected_received = false;
			editor.putBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED, false);
		}
//		is_first_network_disconnected_received = true;
		editor.putBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED, true);
		editor.commit();
	}

	private void stopSyncToCloud(Context context, String logMsg) {
		Editor editor = sharedPreferences.edit();
		boolean is_first_network_disconnected_received = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED, true);
		if (is_first_network_disconnected_received) {
			Log.d(HCFSMgmtUtils.TAG, logMsg);
			notify_network_status(context, HCFSMgmtUtils.ID_NOTIFY_NETWORK_STATUS_CHANGED, context.getString(R.string.app_name),
					context.getString(R.string.notify_network_disconnected));
			// HCFSApiUtils.stop_sync_to_cloud();
//			is_first_network_disconnected_received = false;
			editor.putBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_DISCONNECTED_RECEIVED, false);
		}
//		is_first_network_connected_received = true;
		editor.putBoolean(SettingsFragment.KEY_PREF_IS_FIRST_NETWORK_CONNECTED_RECEIVED, true);
		editor.commit();
	}

	private void notify_network_status(Context context, int id_notify, String notify_title, String notify_content) {
		boolean notifyConnFailedRecoveryPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_CONN_FAILED_RECOVERY, false);
		if (notifyConnFailedRecoveryPref) {
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

			int defaults = 0;
			defaults |= Notification.DEFAULT_VIBRATE;
			builder.setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher).setContentTitle(notify_title)
					.setContentText(notify_content).setAutoCancel(true).setDefaults(defaults);

			Notification notification = builder.build();
			notificationManager.notify(id_notify, notification);
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
