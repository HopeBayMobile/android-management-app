package com.example.hcfsmgmt;

import com.example.hcfsmgmt.fragment.SettingsFragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class NetworkStateChangedReceiver extends BroadcastReceiver {

	private static boolean is_first_network_connected_received = true;
	private static boolean is_first_network_disconnected_received = true;
	private final int id_notify_network_connected = 0;
	private final int id_notify_network_disconnected = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean syncWifiOnlyPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_SYNC_WIFI_ONLY, true);
		final String action = intent.getAction();
		if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) || action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
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
					String logMsg = "Wifi or network is connected";
					startSyncToCloud(context, logMsg);
				} else {
					String logMsg = "Wifi or network is not connected";
					stopSyncToCloud(context, logMsg);
				}
			}
		}
	}

	private void startSyncToCloud(Context context, String logMsg) {
		if (is_first_network_connected_received) {
			Log.d(MainActivity.TAG, logMsg);
			
			notify_network_status(context, id_notify_network_connected, 
					context.getString(R.string.app_name),
					context.getString(R.string.notify_network_connected));
			
			// HCFSApiUtils.start_sync_to_cloud();
			is_first_network_connected_received = false;
		}
		is_first_network_disconnected_received = true;
	}

	private void stopSyncToCloud(Context context, String logMsg) {
		if (is_first_network_disconnected_received) {
			Log.d(MainActivity.TAG, logMsg);
			
			notify_network_status(context, id_notify_network_disconnected, 
					context.getString(R.string.app_name),
					context.getString(R.string.notify_network_disconnected));
			// HCFSApiUtils.stop_sync_to_cloud();
			is_first_network_disconnected_received = false;
		}
		is_first_network_connected_received = true;
	}
	
	private void notify_network_status(Context context, int id_notify, String notify_title, String notify_content) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		
		int defaults = 0;
		defaults |= Notification.DEFAULT_VIBRATE;
		builder.setWhen(System.currentTimeMillis())
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle(notify_title)
		.setContentText(notify_content)
		.setAutoCancel(true)
		.setDefaults(defaults);
		
        Notification notification = builder.build();
        notificationManager.notify(id_notify, notification);
	}

}
