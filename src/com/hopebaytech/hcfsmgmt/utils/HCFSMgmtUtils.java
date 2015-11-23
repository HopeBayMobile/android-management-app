package com.hopebaytech.hcfsmgmt.utils;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceAppInfo;
import com.hopebaytech.hcfsmgmt.main.HCFSMgmtReceiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class HCFSMgmtUtils {

	public static final String TAG = "HopeBay";
	public static final String HCFS_MANAGEMENT_ALARM_INTENT_ACTION = "com.hopebaytech.hcfsmgmt.HCFSMgmtReceiver";

	public static final int NOTIFY_ID_NETWORK_STATUS_CHANGED = 0;
	public static final int NOTIFY_ID_UPLOAD_COMPLETED = 1;
	public static final int NOTIFY_ID_IMAGE_PIN_UNPIN_FAILURE = 2;
	public static final int NOTIFY_ID_VIDEO_PIN_UNPIN_FAILURE = 3;
	public static final int NOTIFY_ID_AUDIO_PIN_UNPIN_FAILURE = 4;

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
	public static final int INTENT_VALUE_PIN_APP = 202;
	public static final int INTENT_VALUE_PIN_FILE_DIRECTORY = 203;
	public static final String INTENT_KEY_PIN_FILE_DIR_FILEAPTH = "intent_key_pin_firdir_filepath";
	public static final String INTENT_KEY_PIN_FILE_DIR_PIN_STATUS = "intent_key_pin_firdir_pin_status";
	public static final String INTENT_KEY_PIN_APP_DATA_DIR = "intent_key_pin_app_data_dir";
	public static final String INTENT_KEY_PIN_APP_SOURCE_DIR = "intent_key_pin_app_source_dir";
	public static final String INTENT_KEY_PIN_APP_EXTERNAL_DIR = "intent_key_pin_app_external_dir";
	public static final String INTENT_KEY_PIN_APP_PIN_STATUS = "intent_key_pin_app_pin_status";
	public static final String INTENT_KEY_PIN_APP_NAME = "intent_key_pin_app_name";

	public static final int INTERVAL_NOTIFY_UPLAOD_COMPLETED = 60; // minutes
	public static final int INTERVAL_PIN_DATA_TYPE_FILE = 60; // minutes

	public static final boolean deafultPinnedStatus = false;

	public static final String REPLACE_FILE_PATH_OLD = "/storage/emulated/0/";
	public static final String REPLACE_FILE_PATH_NEW = "/storage/emulated/legacy/";

	public static void activate() {

	}

	public static boolean isAppPinned(String sourceDir, String dataDir) {
		// if (HCFSApiUtils.get_pin_status(sourceDir) & HCFSApiUtils.get_pin_status(sourceDir)) return true;
		return false;
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
				Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtUtils: stopPinDataTypeFileAlarm");
				PendingIntent pi = PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_PIN_DATA_TYPE_FILE, intent,
						PendingIntent.FLAG_CANCEL_CURRENT);
				pi.cancel();
				am.cancel(pi);
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

	public static void notifyEvent(Context context, int notify_id, String notify_title, String notify_message) {
		int defaults = 0;
		defaults |= NotificationCompat.DEFAULT_VIBRATE;

		NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
		bigStyle.bigText(notify_message);

		Notification notifcaition = new NotificationCompat.Builder(context)
				.setWhen(System.currentTimeMillis())
				 .setSmallIcon(android.R.drawable.sym_def_app_icon)
				 .setTicker(notify_title)
				 .setContentTitle(notify_title)
				 .setContentText(notify_message)
				 .setAutoCancel(true)
				 .setStyle(bigStyle)
				 .setDefaults(defaults)
				 .build();

		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		notificationManagerCompat.notify(notify_id, notifcaition);
	}

	public static void startSyncToCloud() {
//		HCFSApiUtils.setHCFSProperty("cloudsync", "on");
	}

	public static void stopSyncToCloud() {
//		HCFSApiUtils.setHCFSProperty("cloudsync", "off");
	}

	@Nullable
	public static HCFSStatInfo getHCFSStatInfo() {
		HCFSStatInfo hcfsStatInfo = null;
		try {
//			String jsonResult = HCFSApiUtils.getHCFSStat();
			String jsonResult = "{ \"data\": { \"cloud_total\": 107374182400, \"cloud_used\": 35433480192, \"cache_total\": 85899345920, \"cache_dirty\": 382730240, \"cache_clean\": 12884901888, \"pin_max\": 68719476736, \"pin_total\": 14431090114, \"xfer_up\": 104857600, \"xfer_down\": 31457280, \"cloud_conn\": true} }";
			JSONObject jObject = new JSONObject(jsonResult);
			JSONObject dataObj = jObject.getJSONObject("data");
			hcfsStatInfo = new HCFSStatInfo();
			hcfsStatInfo.setCloudTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_CLOUD_TOTAL));
			hcfsStatInfo.setCloudUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CLOUD_USED));
			hcfsStatInfo.setCacheTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_TOTAL));
			hcfsStatInfo.setCacheDirtyUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_DIRTY));
			hcfsStatInfo.setCacheCleanUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_CLEAN));
			hcfsStatInfo.setPinTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_PIN_TOTAL));
			hcfsStatInfo.setPinMax(dataObj.getLong(HCFSStatInfo.STAT_DATA_PIN_MAX));
			hcfsStatInfo.setXferUpload(dataObj.getLong(HCFSStatInfo.STAT_DATA_XFER_UP));
			hcfsStatInfo.setXferDownload(dataObj.getLong(HCFSStatInfo.STAT_DATA_XFER_DOWN));
			hcfsStatInfo.setCloudConn(dataObj.getBoolean(HCFSStatInfo.STAT_DATA_CLOUD_CONN));
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return hcfsStatInfo;
	}

	public static boolean pinApp(ServiceAppInfo info) {
		Log.i(HCFSMgmtUtils.TAG, "Pin App: " + info.getAppName());
		String sourceDir = info.getSourceDir();
		String dataDir = info.getDataDir();
		String externalDir = info.getExternalDir();
		if (externalDir == null) {
			return pinFileOrDirectory(sourceDir) & pinFileOrDirectory(dataDir);
		} else {
			return pinFileOrDirectory(sourceDir) & pinFileOrDirectory(dataDir) & pinFileOrDirectory(externalDir);
		}
	}

	public static boolean unpinApp(ServiceAppInfo info) {
		Log.i(HCFSMgmtUtils.TAG, "Unpin App: " + info.getAppName());
		String sourceDir = info.getSourceDir();
		String dataDir = info.getDataDir();
		String externalDir = info.getExternalDir();
		if (externalDir == null) {
			return unpinFileOrDirectory(sourceDir) & unpinFileOrDirectory(dataDir);
		} else {
			return unpinFileOrDirectory(sourceDir) & unpinFileOrDirectory(dataDir) & unpinFileOrDirectory(externalDir);
		}
	}

	public static boolean pinFileOrDirectory(String filePath) {
		boolean isSuccess = deafultPinnedStatus;
		// try {
		// String jsonResult = HCFSApiUtils.pin(filePath);
		// JSONObject jObject = new JSONObject(jsonResult);
		// isSuccess = jObject.getBoolean("result");
		// } catch (JSONException e) {
		// Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		// }

		if (isSuccess) {
			Log.i(HCFSMgmtUtils.TAG, "Pin " + filePath + ": " + isSuccess);
		} else {
			Log.e(HCFSMgmtUtils.TAG, "Pin " + filePath + ": " + isSuccess);
		}
		return isSuccess;
	}

	public static boolean unpinFileOrDirectory(String filePath) {
		boolean isSuccess = deafultPinnedStatus;
		// try {
		// String jsonResult = HCFSApiUtils.unpin(filePath);
		// JSONObject jObject = new JSONObject(jsonResult);
		// } catch (JSONException e) {
		// Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		// }
		if (isSuccess) {
			Log.i(HCFSMgmtUtils.TAG, "Unpin " + filePath + ": " + isSuccess);
		} else {
			Log.e(HCFSMgmtUtils.TAG, "Unpin " + filePath + ": " + isSuccess);
		}
		return isSuccess;
	}

	public static boolean isPathPinned(String pathName) {
		boolean isSuccess = deafultPinnedStatus;
		// try {
		// String jsonResult = HCFSApiUtils.getPinStatus(pathName);
		// JSONObject jObject = new JSONObject(jsonResult);
		// ?
		// } catch (JSONException e) {
		// Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		// }
		return isSuccess;
	}

}
