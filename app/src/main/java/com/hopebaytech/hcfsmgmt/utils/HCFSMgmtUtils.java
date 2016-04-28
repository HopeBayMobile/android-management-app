package com.hopebaytech.hcfsmgmt.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.LocationStatus;
import com.hopebaytech.hcfsmgmt.info.ServiceAppInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.main.HCFSMgmtReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class HCFSMgmtUtils {

    public static final String TAG = "HopeBay";
    public static final String CLASSNAME = "HCFSMgmtUtils";
    public static final String ACTION_HCFS_MANAGEMENT_ALARM = "com.hopebaytech.hcfsmgmt.HCFSMgmtReceiver";

    public static final boolean ENABLE_AUTH = false;
    public static final boolean DEFAULT_PINNED_STATUS = false;
    public static final int LOGLEVEL = Log.DEBUG;

    public static final int INTERVAL_TEN_SECONDS = 10 * 1000;
    public static final int INTERVAL_MINUTE = 6 * INTERVAL_TEN_SECONDS;
    public static final int INTERVAL_HOUR = 60 * INTERVAL_MINUTE;
    public static final int INTERVAL_DAY = 24 * INTERVAL_HOUR;
    public static final int INTERVAL_NOTIFY_UPLOAD_COMPLETED = INTERVAL_HOUR;
    public static final int INTERVAL_PIN_DATA_TYPE_FILE = INTERVAL_HOUR;
    public static final int INTERVAL_RESET_XFER = 24 * INTERVAL_HOUR;
    public static final int INTERVAL_NOTIFY_LOCAL_STORAGE_USED_RATIO = INTERVAL_HOUR;

    public static final int NOTIFY_ID_NETWORK_STATUS_CHANGED = 0;
    public static final int NOTIFY_ID_UPLOAD_COMPLETED = 1;
    public static final int NOTIFY_ID_PIN_UNPIN_FAILURE = 2;
    public static final int NOTIFY_ID_ONGOING = 3;
    public static final int NOTIFY_ID_LOCAL_STORAGE_USED_RATIO = 4;
    public static final int NOTIFY_ID_FAILED_SILENT_SIGN_IN = 5;

    public static final int REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED = 100;
    public static final int REQUEST_CODE_PIN_DATA_TYPE_FILE = 101;
    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 102;
    public static final int REQUEST_CODE_RESET_XFER = 103;
    public static final int REQUEST_CODE_NOTIFY_LOCAL_STORAGE_USED_RATIO = 104;

    public static final String DATA_STATUS_CLOUD = "cloud";
    public static final String DATA_STATUS_HYBRID = "hybrid";
    public static final String DATA_STATUS_LOCAL = "local";

    public static final String INTENT_KEY_OPERATION = "intent_key_action";
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
    public static final String INTENT_KEY_ONGOING = "intent_key_ongoing";
    public static final String INTENT_KEY_SILENT_SIGN_IN = "intent_key_silent_sign_in";

    public static final String INTENT_VALUE_NONE = "intent_value_none";
    public static final String INTENT_VALUE_NOTIFY_UPLOAD_COMPLETED = "intent_value_notify_upload_complete";
    public static final String INTENT_VALUE_PIN_DATA_TYPE_FILE = "intent_value_pin_data_type_file";
    public static final String INTENT_VALUE_PIN_APP = "intent_value_pin_app";
    public static final String INTENT_VALUE_PIN_FILE_DIRECTORY = "intent_value_pin_file_directory";
    public static final String INTENT_VALUE_ADD_UID_AND_PIN_SYSTEM_APP_WHEN_BOOT_UP = "intent_value_add_uid_and_pin_system_app_when_boot_up";
    public static final String INTENT_VALUE_ADD_UID_TO_DATABASE_AND_UNPIN_USER_APP = "intent_value_add_uid_to_database_and_unpin_user_app";
    public static final String INTENT_VALUE_REMOVE_UID_FROM_DATABASE = "intent_value_remove_uid_from_database";
    public static final String INTENT_VALUE_RESET_XFER = "intent_value_reset_xfer";
    public static final String INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO = "intent_value_notify_local_storage_used_ratio";
    public static final String INTENT_VALUE_ONGOING_NOTIFICATION = "intent_value_ongoing_notification";
    public static final String INTENT_VALUE_PIN_UNPIN_UDPATE_APP = "intent_value_pin_unpin_update_app";
    public static final String INTENT_VALUE_SILENT_SIGN_IN = "intent_value_silent_sign_in";

    public static final String PREF_IS_SILENT_SIGN_IN = "pref_is_silent_sign_in";
    public static final String PREF_IS_HCFS_ACTIVATED = "pref_is_hcfs_activated";

//    public static final String HCFS_CONFIG_CURRENT_BACKEND = "current_backend";
//    public static final String HCFS_CONFIG_SWIFT_ACCOUNT = "swift_account";
//    public static final String HCFS_CONFIG_SWIFT_USER = "swift_user";
//    public static final String HCFS_CONFIG_SWIFT_PASS = "swift_pass";
//    public static final String HCFS_CONFIG_SWIFT_URL = "swift_url";
//    public static final String HCFS_CONFIG_SWIFT_CONTAINER = "swift_container";
//    public static final String HCFS_CONFIG_SWIFT_PROTOCOL = "swift_protocol";

    public static final String ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME = "google_sign_in_display_name";
    public static final String ITENT_GOOGLE_SIGN_IN_EMAIL = "google_sign_in_email";
    public static final String ITENT_GOOGLE_SIGN_IN_PHOTO_URI = "google_sign_in_photo_uri";

    // public static final String REPLACE_FILE_PATH_OLD = "/storage/emulated/0/";
    // public static final String REPLACE_FILE_PATH_NEW = "/mnt/shell/emulated/0/";

    public static final String EXTERNAL_STORAGE_SDCARD0_PREFIX = "/storage/emulated";

    public static boolean isAppPinned(Context context, AppInfo appInfo) {
        log(Log.DEBUG, CLASSNAME, "isAppPinned", appInfo.getItemName());
        UidDAO uidDAO = UidDAO.getInstance(context);
        UidInfo uidInfo = uidDAO.get(appInfo.getPackageName());
        return uidInfo != null && uidInfo.isPinned();
    }

    public static boolean isDataTypePinned(DataTypeDAO dataTypeDAO, String dataType) {
        boolean isPinned = DEFAULT_PINNED_STATUS;
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
        log(Log.DEBUG, CLASSNAME, "stopPinDataTypeFileAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(INTENT_KEY_OPERATION, INTENT_VALUE_PIN_DATA_TYPE_FILE);

        int requestCode = REQUEST_CODE_PIN_DATA_TYPE_FILE;
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static void startPinDataTypeFileAlarm(Context context) {
        log(Log.DEBUG, CLASSNAME, "startPinDataTypeFileAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(INTENT_KEY_OPERATION, INTENT_VALUE_PIN_DATA_TYPE_FILE);

        int requestCode = REQUEST_CODE_PIN_DATA_TYPE_FILE;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime();
        long intervalMillis = INTERVAL_PIN_DATA_TYPE_FILE;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, intervalMillis, pi);
    }

    public static void startResetXferAlarm(Context context) {
        log(Log.DEBUG, CLASSNAME, "startResetXferAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(INTENT_KEY_OPERATION, INTENT_VALUE_RESET_XFER);

        int requestCode = REQUEST_CODE_RESET_XFER;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = INTERVAL_RESET_XFER;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pi);
    }

    public static void stopResetXferAlarm(Context context) {
        log(Log.DEBUG, CLASSNAME, "stopResetXferAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(INTENT_KEY_OPERATION, INTENT_VALUE_RESET_XFER);

        int requestCode = REQUEST_CODE_RESET_XFER;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static void startNotifyUploadCompletedAlarm(Context context) {
        log(Log.DEBUG, CLASSNAME, "startNotifyUploadCompletedAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(INTENT_KEY_OPERATION, INTENT_VALUE_NOTIFY_UPLOAD_COMPLETED);

        int requestCode = REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime();
        long intervalMillis = INTERVAL_NOTIFY_UPLOAD_COMPLETED;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
    }

    public static void stopNotifyUploadCompletedAlarm(Context context) {
        log(Log.DEBUG, CLASSNAME, "stopNotifyUploadCompletedAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(INTENT_KEY_OPERATION, INTENT_VALUE_NOTIFY_UPLOAD_COMPLETED);

        int requestCode = REQUEST_CODE_NOTIFY_UPLAOD_COMPLETED;
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    @Nullable
    public static ArrayList<String> getAvailableFilesPaths(Context context) {
        ArrayList<String> nonMediaFilePaths = null;
        ContentResolver resolver = context.getContentResolver();
        String[] projection = null;
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
        /* External storage */
        Cursor cursor = resolver.query(MediaStore.Files.getContentUri("external"), projection, selection, null, null);
        if (cursor != null) {
            nonMediaFilePaths = new ArrayList<String>();
            cursor.moveToFirst();
            final int index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            for (int i = 0; i < cursor.getCount(); i++) {
                String path = cursor.getString(index);
                File file = new File(path);
                if (file.isDirectory()) {
                    log(Log.DEBUG, CLASSNAME, "getAvailableFilesPaths", "dir_path=" + path);
                } else {
                    log(Log.DEBUG, CLASSNAME, "getAvailableFilesPaths", "file_path=" + path);
                }
                nonMediaFilePaths.add(path);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return nonMediaFilePaths;
    }

    @Nullable
    public static ArrayList<String> getAvailableVideoPaths(Context context, long dateUpdated) {
        ArrayList<String> videoPaths = null;
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Audio.Media.DATE_ADDED};
        /* External storage */
        String selection = MediaStore.Audio.Media.DATE_ADDED + " > " + dateUpdated;
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, null, MediaStore.Video.Media._ID);
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
        // Log.d(TAG, "vedio path: " + path);
        // vedioPaths.add(path);
        // cursor.moveToNext();
        // }
        // cursor.close();
        return videoPaths;
    }

    @Nullable
    public static ArrayList<String> getAvailableAudioPaths(Context context, long dateUpdated) {
        ArrayList<String> audioPaths = null;
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DATE_ADDED};
        /* External storage */
        String selection = MediaStore.Audio.Media.DATE_ADDED + " > " + dateUpdated;
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, MediaStore.Audio.Media._ID);
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
        // Log.d(TAG, "audio path: " + path);
        // audioPaths.add(path);
        // cursor.moveToNext();
        // }
        // cursor.close();
        return audioPaths;
    }

    @Nullable
    public static ArrayList<String> getAvailableImagePaths(Context context, long dateUpdated) {
        /* External storage */
        ArrayList<String> imagePaths = null;
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Audio.Media.DATE_ADDED};
        String selection = MediaStore.Audio.Media.DATE_ADDED + " > " + dateUpdated;
        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, null, MediaStore.Images.Media._ID);
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

		/* Internal storage */
        // cursor = resolver.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
        // projection, null, null, MediaStore.Images.Media._ID);
        // cursor.moveToFirst();
        // for (int i = 0; i < cursor.getCount(); i++) {
        // String path = cursor.getString(index);
        // Log.d(TAG, "image path: " + path);
        // imagePaths.add(path);
        // cursor.moveToNext();
        // }
        // cursor.close();
        return imagePaths;
    }

    public static boolean getHCFSSyncStatus() {
        String jsonResult = HCFSApiUtils.getHCFSSyncStatus();
        JSONObject jObject;
        try {
            jObject = new JSONObject(jsonResult);
            JSONObject dataObj = jObject.getJSONObject("data");
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                log(Log.INFO, CLASSNAME, "getHCFSSyncStatus", "jsonResult=" + jsonResult);

                boolean enabled = dataObj.getBoolean("enabled");
                return enabled;
            } else {
                log(Log.ERROR, CLASSNAME, "getHCFSSyncStatus", "jsonResult=" + jsonResult);
            }
        } catch (JSONException e) {
            log(Log.ERROR, CLASSNAME, "getHCFSSyncStatus", Log.getStackTraceString(e));
        }
        return false;
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
                log(Log.INFO, CLASSNAME, "getHCFSStatInfo", "jsonResult=" + jsonResult);

                hcfsStatInfo = new HCFSStatInfo();
                hcfsStatInfo.setCloudTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_QUOTA));
                hcfsStatInfo.setCloudUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CLOUD_USED));
                hcfsStatInfo.setVolUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_VOL_USED));
                hcfsStatInfo.setCacheTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_TOTAL));
                hcfsStatInfo.setCacheUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_USED));
                hcfsStatInfo.setCacheDirtyUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_DIRTY));
                hcfsStatInfo.setPinTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_PIN_TOTAL));
                hcfsStatInfo.setPinMax(dataObj.getLong(HCFSStatInfo.STAT_DATA_PIN_MAX));
                hcfsStatInfo.setXferUpload(dataObj.getLong(HCFSStatInfo.STAT_DATA_XFER_UP));
                hcfsStatInfo.setXferDownload(dataObj.getLong(HCFSStatInfo.STAT_DATA_XFER_DOWN));
                hcfsStatInfo.setCloudConn(dataObj.getBoolean(HCFSStatInfo.STAT_DATA_CLOUD_CONN));
            } else {
                log(Log.ERROR, CLASSNAME, "getHCFSStatInfo", "jsonResult=" + jsonResult);
            }
        } catch (JSONException e) {
            log(Log.ERROR, CLASSNAME, "getHCFSStatInfo", Log.getStackTraceString(e));
        }
        return hcfsStatInfo;
    }

    public static boolean pinApp(ServiceAppInfo info) {
        HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "pinApp", "AppName=" + info.getAppName());
        String sourceDir = info.getSourceDir();
        String dataDir = info.getDataDir();
//        String externalDir = info.getExternalDir();
        ArrayList<String> externalDirList = info.getExternalDirList();
        boolean isSourceDirSuccess = true;
//        if (sourceDir != null) {
//            if (sourceDir.startsWith("/data/app")) {
//                isSourceDirSuccess = pinFileOrDirectory(sourceDir);
//            }
//        }
        boolean isDataDirSuccess = true;
        if (dataDir != null) {
            if (dataDir.startsWith("/data/data") || dataDir.startsWith("/data/user")) {
                isDataDirSuccess = pinFileOrDirectory(dataDir);
            }
        }
        boolean isExternalDirSuccess = true;
//        if (externalDir != null) {
//            isExternalDirSuccess = pinFileOrDirectory(externalDir);
//        }
        if (externalDirList != null) {
            for (String externalDir : externalDirList) {
                isExternalDirSuccess &= pinFileOrDirectory(externalDir);
            }
        }
        return isSourceDirSuccess & isDataDirSuccess & isExternalDirSuccess;
    }

    public static boolean unpinApp(ServiceAppInfo info) {
        HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "unpinApp", "appName=" + info.getAppName());
        String sourceDir = info.getSourceDir();
        String dataDir = info.getDataDir();
//        String externalDir = info.getExternalDir();
        ArrayList<String> externalDirList = info.getExternalDirList();

        boolean isSourceDirSuccess = true;
//        if (sourceDir != null) {
//            if (sourceDir.startsWith("/data/app")) {
//                isSourceDirSuccess = unpinFileOrDirectory(sourceDir);
//            }
//        }
        boolean isDataDirSuccess = true;
        if (dataDir != null) {
            if (dataDir.startsWith("/data/data") || dataDir.startsWith("/data/user")) {
                isDataDirSuccess = unpinFileOrDirectory(dataDir);
            }
        }
        boolean isExternalDirSuccess = true;
//        if (externalDir != null) {
//            isExternalDirSuccess = unpinFileOrDirectory(externalDir);
//        }
        if (externalDirList != null) {
            for (String externalDir : externalDirList) {
                isExternalDirSuccess &= unpinFileOrDirectory(externalDir);
            }
        }

        return isSourceDirSuccess & isDataDirSuccess & isExternalDirSuccess;
    }

    public static boolean pinFileOrDirectory(String filePath) {
        boolean isSuccess = DEFAULT_PINNED_STATUS;
        try {
            String jsonResult = HCFSApiUtils.pin(filePath);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            String logMsg = "operation=Pin, filePath=" + filePath + ", jsonResult=" + jsonResult;
            if (isSuccess) {
                log(Log.INFO, CLASSNAME, "pinFileOrDirectory", logMsg);
            } else {
                log(Log.ERROR, CLASSNAME, "pinFileOrDirectory", logMsg);
            }
        } catch (JSONException e) {
            log(Log.ERROR, CLASSNAME, "pinFileOrDirectory", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static boolean unpinFileOrDirectory(String filePath) {
        boolean isSuccess = DEFAULT_PINNED_STATUS;
        try {
            String jsonResult = HCFSApiUtils.unpin(filePath);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            String logMsg = "operation=Unpin, filePath=" + filePath + ", jsonResult=" + jsonResult;
            if (isSuccess) {
                log(Log.INFO, CLASSNAME, "unpinFileOrDirectory", logMsg);
            } else {
                log(Log.ERROR, CLASSNAME, "unpinFileOrDirectory", logMsg);
            }
        } catch (JSONException e) {
            log(Log.ERROR, CLASSNAME, "unpinFileOrDirectory", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static int getDirLocationStatus(String pathName) {
        int locationStatus = LocationStatus.LOCAL;
        try {
            // String logMsg = "pathName=" + pathName + ", startTime=" + System.currentTimeMillis();
            // HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "getDirLocationStatus", logMsg);
            String jsonResult = HCFSApiUtils.getDirStatus(pathName);
            // logMsg = "pathName=" + pathName + ", endTime=" + System.currentTimeMillis();
            // HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "getDirLocationStatus", logMsg);
            String logMsg = "pathName=" + pathName + ", jsonResult=" + jsonResult;
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
                        locationStatus = LocationStatus.LOCAL;
                    } else if (num_local != 0 && num_cloud == 0 && num_hybrid == 0) {
                        locationStatus = LocationStatus.LOCAL;
                    } else if (num_local == 0 && num_cloud != 0 && num_hybrid == 0) {
                        locationStatus = LocationStatus.CLOUD;
                    } else {
                        locationStatus = LocationStatus.HYBRID;
                    }
                    HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "getDirLocationStatus", logMsg);
                    // Log.i(TAG, "getDirLocationStatus[" + pathName + "]: " + jsonResult);
                } else {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirLocationStatus", logMsg);
                    // Log.e(TAG, "getDirLocationStatus[" + pathName + "]: " + jsonResult);
                }
            } else {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirLocationStatus", logMsg);
                // Log.e(TAG, "getDirLocationStatus[" + pathName + "]: " + jsonResult);
            }
        } catch (Exception e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getDirLocationStatus", Log.getStackTraceString(e));
            // Log.e(TAG, Log.getStackTraceString(e));
        }
        return locationStatus;
    }

    public static int getFileLocationStatus(String pathName) {
        int status = LocationStatus.LOCAL;
        try {
            String jsonResult = HCFSApiUtils.getFileStatus(pathName);
            String logMsg = "pathName=" + pathName + ", jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                int code = jObject.getInt("code");
                switch (code) {
                    case 0:
                        status = LocationStatus.LOCAL;
                        break;
                    case 1:
                        status = LocationStatus.CLOUD;
                        break;
                    case 2:
                        status = LocationStatus.HYBRID;
                        break;
                }
                HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "getFileLocationStatus", logMsg);
                // Log.i(TAG, "getFileLocationStatus[" + pathName + "]: " + jsonResult);
            } else {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getFileLocationStatus", logMsg);
                // Log.e(TAG, "getFileLocationStatus: " + jsonResult);
            }
        } catch (Exception e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getFileLocationStatus", Log.getStackTraceString(e));
            // Log.e(TAG, Log.getStackTraceString(e));
        }
        return status;
    }

    public static boolean isPathPinned(String pathName) {
        boolean isPinned = DEFAULT_PINNED_STATUS;
        try {
            String jsonResult = HCFSApiUtils.getPinStatus(pathName);
            String logMsg = "pathName=" + pathName + ", jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "isPathPinned", logMsg);
                // Log.i(TAG, "isPathPinned[" + pathName + "]: " + jsonResult);
                int code = jObject.getInt("code");
                if (code == 1) {
                    isPinned = true;
                } else {
                    isPinned = false;
                }
            } else {
                // Log.e(TAG, "isPathPinned[" + pathName + "]: " + jsonResult);
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "isPathPinned", logMsg);
            }
        } catch (JSONException e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "isPathPinned", Log.getStackTraceString(e));
            // Log.e(TAG, Log.getStackTraceString(e));
        }
        return isPinned;
    }

//    public static String getHCFSConfig(String key) {
//        String resultStr = "";
//        try {
//            String jsonResult = HCFSApiUtils.getHCFSConfig(key);
//            String logMsg = "jsonResult=" + jsonResult;
//            JSONObject jObject = new JSONObject(jsonResult);
//            boolean isSuccess = jObject.getBoolean("result");
//            if (isSuccess) {
//                JSONObject dataObj = jObject.getJSONObject("data");
//                resultStr = dataObj.getString(key);
//                // Log.i(TAG, "getHCFSConfig: " + jsonResult);
//                HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "getHCFSConfig", logMsg);
//            } else {
//                // Log.e(TAG, "getHCFSConfig: " + jsonResult);
//                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getHCFSConfig", logMsg);
//            }
//        } catch (JSONException e) {
//            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getHCFSConfig", Log.getStackTraceString(e));
//            // Log.e(TAG, Log.getStackTraceString(e));
//        }
//        return resultStr;
//    }

//    public static boolean setHCFSConfig(String key, String value) {
//        boolean isSuccess = false;
//        try {
//            String jsonResult = HCFSApiUtils.setHCFSConfig(key, value);
//            JSONObject jObject = new JSONObject(jsonResult);
//            isSuccess = jObject.getBoolean("result");
//            String logMsg = "key=" + key + ", value=" + value + ", jsonResult=" + jsonResult;
//            ;
//            if (isSuccess) {
//                HCFSMgmtUtils.log(Log.INFO, CLASSNAME, "setHCFSConfig", logMsg);
//            } else {
//                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "setHCFSConfig", logMsg);
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, Log.getStackTraceString(e));
//        }
//        return isSuccess;
//    }

//    public static boolean reloadConfig() {
//        boolean isSuccess = false;
//        try {
//            String jsonResult = HCFSApiUtils.reloadConfig();
//            JSONObject jObject = new JSONObject(jsonResult);
//            isSuccess = jObject.getBoolean("result");
//            if (isSuccess) {
//                log(Log.INFO, CLASSNAME, "reloadConfig", "jsonResult=" + jsonResult);
//            } else {
//                log(Log.ERROR, CLASSNAME, "reloadConfig", "jsonResult=" + jsonResult);
//            }
//        } catch (JSONException e) {
//            log(Log.ERROR, CLASSNAME, "reloadConfig", Log.getStackTraceString(e));
//        }
//        return isSuccess;
//    }

    public static boolean isDataUploadCompleted() {
        HCFSStatInfo hcfsStatInfo = getHCFSStatInfo();
        if (hcfsStatInfo != null) {
            return hcfsStatInfo.getCacheDirtyUsed().equals("0B");
        }
        return false;
    }

    public static boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    public static boolean resetXfer() {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.resetXfer();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Log.i(TAG, "resetXfer: " + jsonResult);
            } else {
                Log.e(TAG, "resetXfer: " + jsonResult);
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static void changeCloudSyncStatus(Context context) {
        log(Log.DEBUG, CLASSNAME, "changeCloudSyncStatus", null);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        boolean syncWifiOnlyPref = sharedPreferences.getBoolean(context.getString(R.string.pref_sync_wifi_only), true);
        if (syncWifiOnlyPref) {
            if (netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                String logMsg = "Wifi is connected";
                HCFSConfig.startSyncToCloud(context, logMsg);
            } else {
                String logMsg = "Wifi is not connected";
                HCFSConfig.stopSyncToCloud(context, logMsg);
            }
        } else {
            if (netInfo != null && netInfo.isConnected()) {
                String logMsg = "Wifi or Mobile network is connected";
                HCFSConfig.startSyncToCloud(context, logMsg);
            } else {
                String logMsg = "Wifi or Mobile network is not connected";
                HCFSConfig.stopSyncToCloud(context, logMsg);
            }
        }
    }

    public static void notify_network_status(Context context, int notify_id, String notify_title, String notify_content) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_notify_conn_failed_recovery);
        boolean notifyConnFailedRecoveryPref = sharedPreferences.getBoolean(key, false);
        if (notifyConnFailedRecoveryPref) {
            NotificationEvent.notify(context, notify_id, notify_title, notify_content, false);
        }
    }

    public static void startNotifyLocalStorageUsedRatioAlarm(Context context) {
        log(Log.DEBUG, CLASSNAME, "startNotifyLocalStorageUsedRatioAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(INTENT_KEY_OPERATION, INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);

        int requestCode = REQUEST_CODE_NOTIFY_LOCAL_STORAGE_USED_RATIO;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime();
        long intervalMillis = INTERVAL_NOTIFY_LOCAL_STORAGE_USED_RATIO;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
    }

    public static void stopNotifyLocalStorageUsedRatioAlarm(Context context) {
        log(Log.DEBUG, CLASSNAME, "stopNotifyLocalStorageUsedRatioAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
        intent.putExtra(INTENT_KEY_OPERATION, INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);

        int requestCode = REQUEST_CODE_NOTIFY_LOCAL_STORAGE_USED_RATIO;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static void log(int logLevel, String className, String funcName, String logMsg) {
        if (logLevel >= LOGLEVEL) {
            if (logMsg == null) {
                logMsg = "";
            }
            if (logLevel == Log.DEBUG) {
                Log.d(TAG, className + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.INFO) {
                Log.i(TAG, className + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.WARN) {
                Log.w(TAG, className + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.ERROR) {
                Log.e(TAG, className + "(" + funcName + "): " + logMsg);
            }
        }
    }

    public static void log(int logLevel, String className, String innerClassName, String funcName, String logMsg) {
        if (logLevel >= LOGLEVEL) {
            if (logMsg == null) {
                logMsg = "";
            }
            if (logLevel == Log.DEBUG) {
                Log.d(TAG, className + "->" + innerClassName + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.INFO) {
                Log.i(TAG, className + "->" + innerClassName + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.WARN) {
                Log.w(TAG, className + "->" + innerClassName + "(" + funcName + "): " + logMsg);
            } else if (logLevel == Log.ERROR) {
                Log.e(TAG, className + "->" + innerClassName + "(" + funcName + "): " + logMsg);
            }
        }
    }

    public static Drawable getPinUnpinImage(Context context, boolean isPinned) {
        return ContextCompat.getDrawable(context, isPinned ? R.drawable.icon_btn_app_pin : R.drawable.icon_btn_app_unpin);
    }

    @Nullable
    public static String getEncryptedDeviceIMEI() {
        String encryptedIMEI = new String(HCFSApiUtils.getEncryptedIMEI());
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "getEncryptedDeviceIMEI", "encryptedIMEI=" + encryptedIMEI);
        return encryptedIMEI;
    }

    public static String getDeviceIMEI(Context context) {
        String IMEI = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "getDeviceIMEI", "IMEI=" + IMEI);
        return IMEI == null ? "" : IMEI;
    }

}
