package com.hopebaytech.hcfsmgmt.utils;

import java.io.File;
import java.util.ArrayList;

import com.hopebaytech.hcfsmgmt.HCFSMgmtReceiver;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

public class HCFSMgmtUtils {

	public static final String TAG = "HopeBay";
	public static final String HCFS_MANAGEMENT_ALARM_INTENT_ACTION = "com.hopebaytech.hcfsmgmt.HCFSMgmtReceiver";

	public static final int ID_NOTIFY_NETWORK_STATUS_CHANGED = 0;
	public static final int ID_NOTIFY_UPLOAD_COMPLETED = 1;

	public static final int REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED = 100;
	public static final int REQUEST_CODE_PIN_DATA_TYPE_FILE = 101;

	public static final String DATA_STATUS_CLOUD = "cloud";
	public static final String DATA_STATUS_HYBRID = "hybrid";
	public static final String DATA_STATUS_LOCAL = "local";

	public static final String DATA_TYPE_IMAGE = "image";
	public static final String DATA_TYPE_VIDEO = "video";
	public static final String DATA_TYPE_AUDIO = "audio";

	public static final String INTENT_KEY_OPERATION = "intent_key_action";
	public static final int INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED = 200;
	public static final int INTENT_VALUE_PIN_DATA_TYPE_FILE = 201;

	public static final int INTERVAL_NOTIFY_UPLAOD_COMPLETED = 60; // minutes
	public static final int INTERVAL_PIN_DATA_TYPE_FILE = 60; // minutes

	public static final boolean deafultPinnedStatus = true;

	public static void activate() {

	}

	public static void logout() {

	}

	public static boolean isAppPinned(String sourceDir, String dataDir) {
		// if (HCFSApiUtils.get_pin_status(sourceDir) & HCFSApiUtils.get_pin_status(sourceDir)) return true;
		return false;
	}

	public static void pinApp(String sourceDir, String dataDir) {
		pinFileOrDirectory(sourceDir);
		pinFileOrDirectory(dataDir);
	}

	public static void unpinApp(String sourceDir, String dataDir) {
		unpinFileOrDirectory(sourceDir);
		unpinFileOrDirectory(dataDir);
	}

	public static void pinFileOrDirectory(String filePath) {
		HCFSApiUtils.pin(filePath);
	}

	public static void unpinFileOrDirectory(String filePath) {
		HCFSApiUtils.unpin(filePath);
	}
	
	public static void startPinDataTypeFileAlarm(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.HCFS_MANAGEMENT_ALARM_INTENT_ACTION);
		intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE);
		boolean isAlarmExist = (PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_PIN_DATA_TYPE_FILE, intent,
				PendingIntent.FLAG_NO_CREATE) != null);
		if (!isAlarmExist) {
			Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtUtils: startPinDataTypeFileAlarm");
			PendingIntent pi = PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_PIN_DATA_TYPE_FILE, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			long triggerAtMillis = SystemClock.elapsedRealtime();
			long intervalMillis = HCFSMgmtUtils.INTERVAL_PIN_DATA_TYPE_FILE * 60000;
			am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
		}
	}

	public static boolean isDataTypePinned(DataTypeDAO dataTypeDAO, String dataType) {
		boolean isPinned = HCFSMgmtUtils.deafultPinnedStatus;
		DataTypeInfo dataTypeInfo = dataTypeDAO.get(dataType);
		if (dataTypeInfo != null) {
			if (dataTypeInfo.isPinned()) {
				isPinned = true;
			} else {
				isPinned = false;
			}
		}
		return isPinned;
	}

	public static void stopPinDataTypeFileAlarm(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.HCFS_MANAGEMENT_ALARM_INTENT_ACTION);
		intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE);
		boolean isAlarmExist = (PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_PIN_DATA_TYPE_FILE, intent,
				PendingIntent.FLAG_NO_CREATE) != null);
		if (isAlarmExist) {
			DataTypeDAO dataTypeDAO = new DataTypeDAO(context);
			boolean isImageTypePinned = isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_IMAGE);
			boolean isVideoTypePinned = isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_VIDEO);
			boolean isAudioTypePinned = isDataTypePinned(dataTypeDAO, HCFSMgmtUtils.DATA_TYPE_AUDIO);
			dataTypeDAO.close();
			if (!isImageTypePinned && !isVideoTypePinned && !isAudioTypePinned) {
				PendingIntent pi = PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_PIN_DATA_TYPE_FILE, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);
				pi.cancel();
				am.cancel(pi);
				Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtUtils: stopPinDataTypeFileAlarm");
			}
		}
	}

	public static void startNotifyUploadCompletedAlarm(Context context) {
		Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtUtils: startNotifyUploadCompletedAlarm");
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.HCFS_MANAGEMENT_ALARM_INTENT_ACTION);
		intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED);
		PendingIntent pi = PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		long triggerAtMillis = SystemClock.elapsedRealtime();
		long intervalMillis = HCFSMgmtUtils.INTERVAL_NOTIFY_UPLAOD_COMPLETED * 60000;
		am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
	}

	public static void stopNotifyUploadCompletedAlarm(Context context) {
		Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtUtils: stopNotifyUploadCompletedAlarm");
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.HCFS_MANAGEMENT_ALARM_INTENT_ACTION);
		intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED);
		PendingIntent pi = PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		pi.cancel();
		am.cancel(pi);
	}

	@Nullable
	public static ArrayList<String> getAvailableFilesPaths(Context context) {
		ArrayList<String> nonMediaFilePaths = null;
		ContentResolver resolver = context.getContentResolver();
		String[] projection = null;
		String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
		// External storage
		Cursor cursor = resolver.query(MediaStore.Files.getContentUri("external"), projection, selection, null, null);
		if (cursor != null) {
			nonMediaFilePaths = new ArrayList<String>();
			cursor.moveToFirst();
			final int index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
			for (int i = 0; i < cursor.getCount(); i++) {
				String path = cursor.getString(index);
				File file = new File(path);
				if (file.isDirectory()) {
					Log.d(HCFSMgmtUtils.TAG, "directory path: " + path);
				} else {
					Log.d(HCFSMgmtUtils.TAG, "file path: " + path);
				}
				nonMediaFilePaths.add(path);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return nonMediaFilePaths;
	}

	@Nullable
	public static ArrayList<String> getAvailableVideoPaths(Context context) {
		ArrayList<String> videoPaths = null;
		ContentResolver resolver = context.getContentResolver();
		String[] projection = { MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA };
		// External storage
		Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Video.Media._ID);
		if (cursor != null) {
			videoPaths = new ArrayList<String>();
			cursor.moveToFirst();
			final int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
			for (int i = 0; i < cursor.getCount(); i++) {
				String path = cursor.getString(index);
				videoPaths.add(path);
				cursor.moveToNext();
			}
			cursor.close();
		}
		// Internal storage
		// cursor = resolver.query(MediaStore.Video.Media.INTERNAL_CONTENT_URI,
		// projection, null, null, MediaStore.Video.Media._ID);
		// cursor.moveToFirst();
		// for (int i = 0; i < cursor.getCount(); i++) {
		// String path = cursor.getString(index);
		// Log.d(HCFSMgmtUtils.TAG, "vedio path: " + path);
		// vedioPaths.add(path);
		// cursor.moveToNext();
		// }
		// cursor.close();
		return videoPaths;
	}

	@Nullable
	public static ArrayList<String> getAvailableAudioPaths(Context context) {
		ArrayList<String> audioPaths = null;
		ContentResolver resolver = context.getContentResolver();
		String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA };
		// External storage
		Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Audio.Media._ID);
		if (cursor != null) {
			audioPaths = new ArrayList<String>();
			cursor.moveToFirst();
			int index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
			for (int i = 0; i < cursor.getCount(); i++) {
				String path = cursor.getString(index);
				audioPaths.add(path);
				cursor.moveToNext();
			}
			cursor.close();
		}
		// Internal storage
		// cursor = resolver.query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
		// projection, null, null, MediaStore.Audio.Media._ID);
		// cursor.moveToFirst();
		// for (int i = 0; i < cursor.getCount(); i++) {
		// String path = cursor.getString(index);
		// Log.d(HCFSMgmtUtils.TAG, "audio path: " + path);
		// audioPaths.add(path);
		// cursor.moveToNext();
		// }
		// cursor.close();
		return audioPaths;
	}

	@Nullable
	public static ArrayList<String> getAvailableImagePaths(Context context) {
		ArrayList<String> imagePaths = null;
		ContentResolver resolver = context.getContentResolver();
		String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
		// External storage
		Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media._ID);
		if (cursor != null) {
			imagePaths = new ArrayList<String>();
			cursor.moveToFirst();
			final int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
			for (int i = 0; i < cursor.getCount(); i++) {
				String path = cursor.getString(index);
				imagePaths.add(path);
				cursor.moveToNext();
			}
			cursor.close();
		}
		// Internal storage
		// cursor = resolver.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
		// projection, null, null, MediaStore.Images.Media._ID);
		// cursor.moveToFirst();
		// for (int i = 0; i < cursor.getCount(); i++) {
		// String path = cursor.getString(index);
		// Log.d(HCFSMgmtUtils.TAG, "image path: " + path);
		// imagePaths.add(path);
		// cursor.moveToNext();
		// }
		// cursor.close();
		return imagePaths;
	}
}
