package com.hopebaytech.hcfsmgmt.services;

import java.util.ArrayList;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class HCFSMgmtService extends IntentService {

	private HandlerThread mThread;
	private Handler mThreadHandler;

	public HCFSMgmtService() {
		super(HCFSMgmtService.class.getSimpleName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		mThread = new HandlerThread(HCFSMgmtService.class.getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
		mThread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mThreadHandler = new Handler(mThread.getLooper());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final int operation = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED);
		mThreadHandler.post(new Runnable() {
			public void run() {
				switch (operation) {
				case HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED:
					notifyUploadCompleted(HCFSMgmtService.this);
					break;
				case HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE:
					pinDataTypeFile(HCFSMgmtService.this);
					break;
				default:
					break;
				}
			}
		});
	}

	private void notifyUploadCompleted(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLAOD_COMPLETED, true);
		if (notifyUploadCompletedPref) {
			boolean isUploadCompleted = true; // need to call HCFS API for checking upload status
			if (isUploadCompleted) {
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder builder = new NotificationCompat.Builder(HCFSMgmtService.this);

				int defaults = 0;
				defaults |= Notification.DEFAULT_VIBRATE;
				String notify_title = getString(R.string.app_name);
				String notify_content = getString(R.string.notify_upload_completed);
				builder.setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.ic_launcher).setContentTitle(notify_title)
						.setContentText(notify_content).setAutoCancel(true).setDefaults(defaults);

				Notification notification = builder.build();
				notificationManager.notify(HCFSMgmtUtils.ID_NOTIFY_UPLOAD_COMPLETED, notification);
			}
		}
	}

	private void pinDataTypeFile(Context context) {
		DataTypeDAO dataTypeDAO = new DataTypeDAO(context);
		boolean isImagePinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_IMAGE);
		ArrayList<String> imagePaths = HCFSMgmtUtils.getAvailableImagePaths(context);
		if (isImagePinned) {
			for (String path : imagePaths) {
				Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtService(Image): " + path);
				// HCFSApiUtils.pin(path);
			}
		} else {
			for (String path : imagePaths) {
				// HCFSApiUtils.unpin(path);
			}
		}

		boolean isVideoPinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_VIDEO);
		ArrayList<String> videoPaths = HCFSMgmtUtils.getAvailableVideoPaths(context);
		if (isVideoPinned) {
			for (String path : videoPaths) {
				Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtService(Video): " + path);
				// HCFSApiUtils.pin(path);
			}
		} else {
			for (String path : videoPaths) {
				// HCFSApiUtils.unpin(path);
			}
		}

		boolean isAudioPinned = HCFSMgmtUtils.isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_AUDIO);
		ArrayList<String> audioPaths = HCFSMgmtUtils.getAvailableAudioPaths(context);
		if (isAudioPinned) {
			for (String path : audioPaths) {
				Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtService(Audio): " + path);
				// HCFSApiUtils.pin(path);
			}
		} else {
			for (String path : audioPaths) {
				// HCFSApiUtils.unpin(path);
			}
		}
		dataTypeDAO.close();
	}

}
