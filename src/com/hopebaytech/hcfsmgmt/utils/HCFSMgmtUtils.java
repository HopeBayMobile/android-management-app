package com.hopebaytech.hcfsmgmt.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileStatus;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceAppInfo;
import com.hopebaytech.hcfsmgmt.main.HCFSMgmtReceiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class HCFSMgmtUtils {

	public static final String TAG = "HopeBay";
	public static final String ACTION_HCFS_MANAGEMENT_ALARM = "com.hopebaytech.hcfsmgmt.HCFSMgmtReceiver";

	public static final boolean ENABLE_AUTH = false;
	public static final boolean DEFAULT_PINNED_STATUS = true;
	public static final int INTERVAL_NOTIFY_UPLAOD_COMPLETED = 60; // minutes
	public static final int INTERVAL_PIN_DATA_TYPE_FILE = 60; // minutes
	public static final int INTERVAL_RESET_XFER = 1440; // minutes

	public static final int NOTIFY_ID_NETWORK_STATUS_CHANGED = 0;
	public static final int NOTIFY_ID_UPLOAD_COMPLETED = 1;
	public static final int NOTIFY_ID_PIN_UNPIN_FAILURE = 2;

	public static final int REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED = 100;
	public static final int REQUEST_CODE_PIN_DATA_TYPE_FILE = 101;
	public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 102;
	public static final int REQUEST_CODE_RESET_XFER = 103;

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
	public static final int INTENT_VALUE_LAUNCH_UID_DATABASE = 204;
	public static final int INTENT_VALUE_ADD_UID_TO_DATABASE = 205;
	public static final int INTENT_VALUE_REMOVE_UID_FROM_DATABASE = 206;
	public static final int INTENT_VALUE_RESET_XFER = 207;
	public static final String INTENT_KEY_PIN_FILE_DIR_FILEAPTH = "intent_key_pin_firdir_filepath";
	public static final String INTENT_KEY_PIN_FILE_DIR_PIN_STATUS = "intent_key_pin_firdir_pin_status";
	public static final String INTENT_KEY_PIN_APP_DATA_DIR = "intent_key_pin_app_data_dir";
	public static final String INTENT_KEY_PIN_APP_SOURCE_DIR = "intent_key_pin_app_source_dir";
	public static final String INTENT_KEY_PIN_APP_EXTERNAL_DIR = "intent_key_pin_app_external_dir";
	public static final String INTENT_KEY_PIN_APP_PIN_STATUS = "intent_key_pin_app_pin_status";
	public static final String INTENT_KEY_PIN_APP_NAME = "intent_key_pin_app_name";
	public static final String INTENT_KEY_PIN_PACKAGE_NAME = "intent_key_pin_package_name";
	public static final String INTENT_KEY_SERVER_CLIENT_ID = "server_client_id";
	public static final String INTENT_KEY_UID = "intent_key_uid";
	public static final String INTENT_KEY_PACKAGE_NAME = "intent_key_package_name";

	public static final String REPLACE_FILE_PATH_OLD = "/storage/emulated/0/";
	public static final String REPLACE_FILE_PATH_NEW = "/mnt/shell/emulated/0/";

	public static final String HCFS_CONFIG_CURRENT_BACKEND = "current_backend";
	public static final String HCFS_CONFIG_SWIFT_ACCOUNT = "swift_account";
	public static final String HCFS_CONFIG_SWIFT_USER = "swift_user";
	public static final String HCFS_CONFIG_SWIFT_PASS = "swift_pass";
	public static final String HCFS_CONFIG_SWIFT_URL = "swift_url";
	public static final String HCFS_CONFIG_SWIFT_CONTAINER = "swift_container";
	public static final String HCFS_CONFIG_SWIFT_PROTOCOL = "swift_protocol";

	public static final String ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME = "google_sign_in_display_name";
	public static final String ITENT_GOOGLE_SIGN_IN_EMAIL = "google_sign_in_email";
	public static final String ITENT_GOOGLE_SIGN_IN_PHOTO_URI = "google_sign_in_photo_uri";

	public static final String EXTERNAL_STORAGE_SDCARD0_PREFIX = "/storage/emulated";

	public static boolean isAppPinned(AppInfo appInfo) {
		Log.d(TAG, "isAppPinned: " + appInfo.getItemName());
		String sourceDir = appInfo.getSourceDir();
		String dataDir = appInfo.getDataDir();
		String externalDir = appInfo.getExternalDir();
		if (externalDir == null) {
			return isPathPinned(sourceDir) & isPathPinned(dataDir);
		} else {
			return isPathPinned(sourceDir) & isPathPinned(dataDir) & isPathPinned(externalDir);
		}
	}

	public static void startPinDataTypeFileAlarm(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
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
		boolean isPinned = HCFSMgmtUtils.DEFAULT_PINNED_STATUS;
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
		intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
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

	public static void startResetXferAlarm(Context context) {
		Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtUtils: startResetXferAlarm");
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
		intent.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_RESET_XFER);
		PendingIntent pi = PendingIntent.getBroadcast(context, HCFSMgmtUtils.REQUEST_CODE_RESET_XFER, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		long intervalMillis = HCFSMgmtUtils.INTERVAL_RESET_XFER * 60000;
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pi);
	}

	public static void startNotifyUploadCompletedAlarm(Context context) {
		Log.d(HCFSMgmtUtils.TAG, "HCFSMgmtUtils: startNotifyUploadCompletedAlarm");
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, HCFSMgmtReceiver.class);
		intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
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
		intent.setAction(HCFSMgmtUtils.ACTION_HCFS_MANAGEMENT_ALARM);
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
				if (path.startsWith(EXTERNAL_STORAGE_SDCARD0_PREFIX)) {
					videoPaths.add(path);
				}
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
				if (path.startsWith(EXTERNAL_STORAGE_SDCARD0_PREFIX)) {
					audioPaths.add(path);
				}
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
				if (path.startsWith(EXTERNAL_STORAGE_SDCARD0_PREFIX)) {
					imagePaths.add(path);
				}
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

		Notification notifcaition = new NotificationCompat.Builder(context).setWhen(System.currentTimeMillis())
				.setSmallIcon(android.R.drawable.sym_def_app_icon).setTicker(notify_title).setContentTitle(notify_title)
				.setContentText(notify_message).setAutoCancel(true).setStyle(bigStyle).setDefaults(defaults).build();

		NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		notificationManagerCompat.notify(notify_id, notifcaition);
	}

	public static void startSyncToCloud() {
		String jsonResult = HCFSApiUtils.setHCFSSyncStatus(1);
		Log.d(TAG, "startSyncToCloud: " + jsonResult);
	}

	public static void stopSyncToCloud() {
		String jsonResult = HCFSApiUtils.setHCFSSyncStatus(0);
		Log.d(TAG, "stopSyncToCloud: " + jsonResult);
	}

	@Nullable
	public static HCFSStatInfo getHCFSStatInfo() {
		HCFSStatInfo hcfsStatInfo = null;
		try {
			String jsonResult = HCFSApiUtils.getHCFSStat();
			// String jsonResult = "{ \"data\": { \"cloud_total\": 107374182400, \"cloud_used\": 35433480192, \"cache_total\": 85899345920,
			// \"cache_dirty\": 382730240, \"cache_clean\": 12884901888, \"pin_max\": 68719476736, \"pin_total\": 14431090114, \"xfer_up\": 104857600,
			// \"xfer_down\": 31457280, \"cloud_conn\": true} }";
			JSONObject jObject = new JSONObject(jsonResult);
			JSONObject dataObj = jObject.getJSONObject("data");
			boolean isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				Log.i(HCFSMgmtUtils.TAG, "getHCFSStatInfo: " + jsonResult);
				hcfsStatInfo = new HCFSStatInfo();
				// hcfsStatInfo.setCloudTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_CLOUD_TOTAL)); TODO
				hcfsStatInfo.setCloudTotal(107374182400L);
				hcfsStatInfo.setCloudUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CLOUD_USED));
				hcfsStatInfo.setCacheTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_TOTAL));
				hcfsStatInfo.setCacheUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_USED));
				hcfsStatInfo.setCacheDirtyUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_DIRTY));
				hcfsStatInfo.setPinTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_PIN_TOTAL));
				hcfsStatInfo.setPinMax(dataObj.getLong(HCFSStatInfo.STAT_DATA_PIN_MAX));
				hcfsStatInfo.setXferUpload(dataObj.getLong(HCFSStatInfo.STAT_DATA_XFER_UP));
				hcfsStatInfo.setXferDownload(dataObj.getLong(HCFSStatInfo.STAT_DATA_XFER_DOWN));
				hcfsStatInfo.setCloudConn(dataObj.getBoolean(HCFSStatInfo.STAT_DATA_CLOUD_CONN));
			} else {
				Log.e(HCFSMgmtUtils.TAG, "getHCFSStatInfo: " + jsonResult);
			}
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
		boolean isSuccess = DEFAULT_PINNED_STATUS;
		try {
			String jsonResult = HCFSApiUtils.pin(filePath);
			JSONObject jObject = new JSONObject(jsonResult);
			isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				Log.i(HCFSMgmtUtils.TAG, "Pin " + filePath + ": " + jsonResult);
			} else {
				Log.e(HCFSMgmtUtils.TAG, "Pin " + filePath + ": " + jsonResult);
			}
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}

		return isSuccess;
	}

	public static boolean unpinFileOrDirectory(String filePath) {
		boolean isSuccess = DEFAULT_PINNED_STATUS;
		try {
			String jsonResult = HCFSApiUtils.unpin(filePath);
			JSONObject jObject = new JSONObject(jsonResult);
			isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				Log.i(HCFSMgmtUtils.TAG, "Unpin " + filePath + ": " + jsonResult);
			} else {
				Log.e(HCFSMgmtUtils.TAG, "Unpin " + filePath + ": " + jsonResult);
			}
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}

		return isSuccess;
	}

	public static int getDirStatus(String pathName) {
		int status = FileStatus.LOCAL;
		try {
			String jsonResult = HCFSApiUtils.getDirStatus(pathName);
			JSONObject jObject = new JSONObject(jsonResult);
			boolean isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				int code = jObject.getInt("code");
				if (code == 0) {
					JSONObject dataObj = jObject.getJSONObject("data");
					int num_local = dataObj.getInt("num_local");
					int num_hybrid = dataObj.getInt("num_hybrid");
					int num_cloud = dataObj.getInt("num_cloud");

					if (num_local == 0 && num_cloud == 0 && num_hybrid == 0) {
						status = FileStatus.LOCAL;
					} else if (num_local != 0 && num_cloud == 0 && num_hybrid == 0) {
						status = FileStatus.LOCAL;
					} else if (num_local == 0 && num_cloud != 0 && num_hybrid == 0) {
						status = FileStatus.CLOUD;
					} else {
						status = FileStatus.HYBRID;
					}
					Log.i(HCFSMgmtUtils.TAG, "getDirStatus[" + pathName + "]: " + jsonResult);
				} else {
					Log.e(HCFSMgmtUtils.TAG, "getDirStatus[" + pathName + "]: " + jsonResult);
				}
			} else {
				Log.e(HCFSMgmtUtils.TAG, "getDirStatus[" + pathName + "]: " + jsonResult);
			}

		} catch (Exception e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return status;
	}

	public static int getFileStatus(String pathName) {
		int status = FileStatus.LOCAL;
		try {
			String jsonResult = HCFSApiUtils.getFileStatus(pathName);
			JSONObject jObject = new JSONObject(jsonResult);
			boolean isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				int code = jObject.getInt("code");
				switch (code) {
				case 0:
					status = FileStatus.LOCAL;
					break;
				case 1:
					status = FileStatus.CLOUD;
					break;
				case 2:
					status = FileStatus.HYBRID;
					break;
				}
				Log.i(HCFSMgmtUtils.TAG, "getFileStatus[" + pathName + "]: " + jsonResult);
			} else {
				Log.e(HCFSMgmtUtils.TAG, "getFileStatus: " + jsonResult);
			}
		} catch (Exception e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return status;
	}

	public static boolean isPathPinned(String pathName) {
		boolean isPinned = DEFAULT_PINNED_STATUS;
		try {
			String jsonResult = HCFSApiUtils.getPinStatus(pathName);
			JSONObject jObject = new JSONObject(jsonResult);
			boolean isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				Log.i(HCFSMgmtUtils.TAG, "isPathPinned[" + pathName + "]: " + jsonResult);
				int code = jObject.getInt("code");
				if (code == 1) {
					isPinned = true;
				} else {
					isPinned = false;
				}
			} else {
				Log.i(HCFSMgmtUtils.TAG, "isPathPinned[" + pathName + "]: " + jsonResult);
			}
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return isPinned;
	}

	public static String getHCFSConfig(String key) {
		String resultStr = "";
		try {
			String jsonResult = HCFSApiUtils.getHCFSConfig(key);
			JSONObject jObject = new JSONObject(jsonResult);
			boolean isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				JSONObject dataObj = jObject.getJSONObject("data");
				resultStr = dataObj.getString(key);
				Log.i(HCFSMgmtUtils.TAG, "getHCFSConfig: " + jsonResult);
			} else {
				Log.e(HCFSMgmtUtils.TAG, "getHCFSConfig: " + jsonResult);
			}
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return resultStr;
	}

	public static boolean setHCFSConfig(String key, String value) {
		boolean isSuccess = false;
		try {
			String jsonResult = HCFSApiUtils.setHCFSConfig(key, value);
			JSONObject jObject = new JSONObject(jsonResult);
			isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				Log.i(HCFSMgmtUtils.TAG, "setHCFSConfig: " + jsonResult);
			} else {
				Log.e(HCFSMgmtUtils.TAG, "setHCFSConfig: " + jsonResult);
			}
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return isSuccess;
	}

	public static boolean reboot() {
		boolean isSuccess = false;
		try {
			String jsonResult = HCFSApiUtils.reboot();
			JSONObject jObject = new JSONObject(jsonResult);
			isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				Log.i(HCFSMgmtUtils.TAG, "reboot: " + jsonResult);
			} else {
				Log.e(HCFSMgmtUtils.TAG, "reboot: " + jsonResult);
			}
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return isSuccess;
	}

	public static boolean reloadConfig() {
		boolean isSuccess = false;
		try {
			String jsonResult = HCFSApiUtils.reloadConfig();
			JSONObject jObject = new JSONObject(jsonResult);
			isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				Log.i(HCFSMgmtUtils.TAG, "reloadConfig: " + jsonResult);
			} else {
				Log.e(HCFSMgmtUtils.TAG, "reloadConfig: " + jsonResult);
			}
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return isSuccess;
	}

	public static boolean isDataUploadCompleted() {
		HCFSStatInfo hcfsStatInfo = getHCFSStatInfo();
		if (hcfsStatInfo != null) {
			return hcfsStatInfo.getCacheDirtyUsed().equals("0B") ? true : false;
		}
		return false;
	}

	public static boolean isSystemPackage(ApplicationInfo packageInfo) {
		return ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) ? true : false;
	}

	public static boolean resetXfer() {
		boolean isSuccess = false;
		try {
			String jsonResult = HCFSApiUtils.resetXfer();
			JSONObject jObject = new JSONObject(jsonResult);
			isSuccess = jObject.getBoolean("result");
			if (isSuccess) {
				Log.i(HCFSMgmtUtils.TAG, "resetXfer: " + jsonResult);
			} else {
				Log.e(HCFSMgmtUtils.TAG, "resetXfer: " + jsonResult);
			}
		} catch (JSONException e) {
			Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
		}
		return isSuccess;
	}

}
