package com.hopebaytech.hcfsmgmt.services;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class CheckUploadCompletedService extends IntentService {
	
	public CheckUploadCompletedService() {
		super(CheckUploadCompletedService.class.getSimpleName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread mThread = new HandlerThread("CheckUploadCompletedService", Process.THREAD_PRIORITY_BACKGROUND);
		mThread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		Handler mThreadHandler = new Handler(mThread.getLooper());
		mThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				NotificationManager notificationManager = (NotificationManager) 
						getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder builder = new NotificationCompat.Builder(CheckUploadCompletedService.this);

				int defaults = 0;
				defaults |= Notification.DEFAULT_VIBRATE;
				String notify_title = getString(R.string.app_name);
				String notify_content = getString(R.string.notify_upload_completed);
				builder.setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle(notify_title).setContentText(notify_content).setAutoCancel(true)
						.setDefaults(defaults);

				Notification notification = builder.build();
				notificationManager.notify(HCFSMgmtUtils.ID_NOTIFY_UPLOAD_COMPLETED, notification);
			}
		});
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
	}

}
