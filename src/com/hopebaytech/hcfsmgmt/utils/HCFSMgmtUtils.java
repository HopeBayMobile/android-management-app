package com.hopebaytech.hcfsmgmt.utils;

import com.hopebaytech.hcfsmgmt.HCFSMgmtReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class HCFSMgmtUtils {

	public static final String TAG = "HopeBay";
	public static final int ID_NOTIFY_NETWORK_STATUS_CHANGED = 0;
	public static final int ID_NOTIFY_UPLOAD_COMPLETED = 1;
	public static final int REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED = 100;
	public static final String NOTIFY_UPLOAD_COMPLETED_ALARM_INTENT_ACTION = "com.hopebaytech.hcfsmgmt.services.CheckUploadCompletedService";
	public static final int INTERVAL_NOTIFY_UPLAOD_COMPLETED = 5; // minutes

	public static void login() {

	}

	public static void logout() {

	}

	public static void startNotifyUploadCompletedAlarm(Context context) {
		Log.d(HCFSMgmtUtils.TAG, "Start notify_upload_completed_alarm");
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.NOTIFY_UPLOAD_COMPLETED_ALARM_INTENT_ACTION);
		PendingIntent pi = PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		long triggerAtMillis = SystemClock.elapsedRealtime();
		long intervalMillis = HCFSMgmtUtils.INTERVAL_NOTIFY_UPLAOD_COMPLETED * 60000;
		am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
	}

	public static void stopNotifyUploadCompletedAlarm(Context context) {
		Log.d(HCFSMgmtUtils.TAG, "Stop notify_upload_completed_alarm");
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.NOTIFY_UPLOAD_COMPLETED_ALARM_INTENT_ACTION);
		PendingIntent pi = PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		pi.cancel();
		am.cancel(pi);
	}

}
