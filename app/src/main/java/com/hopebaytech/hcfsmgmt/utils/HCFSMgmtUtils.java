package com.hopebaytech.hcfsmgmt.utils;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.LocationStatus;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.info.TeraIntent;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.main.HCFSMgmtReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HCFSMgmtUtils {

    public static final String TAG = "HopeBay";
    public static final String CLASSNAME = "HCFSMgmtUtils";
//    public static final String ACTION_HCFS_MANAGEMENT_ALARM = "com.hopebaytech.hcfsmgmt.HCFSMgmtReceiver";

    public static final String NOTIFY_INSUFFICIENT_PIN_PACE_RATIO = "80";

    public static final boolean DEFAULT_PINNED_STATUS = false;

    public static final int NOTIFY_ID_NETWORK_STATUS_CHANGED = 0;
    public static final int NOTIFY_ID_PIN_UNPIN_FAILURE = 1;
    public static final int NOTIFY_ID_ONGOING = 2;
    public static final int NOTIFY_ID_LOCAL_STORAGE_USED_RATIO = 3;
    public static final int NOTIFY_ID_CHECK_DEVICE_STATUS = 4;
    public static final int NOTIFY_ID_INSUFFICIENT_PIN_SPACE = 5;

    public static final String BUNDLE_KEY_INSUFFICIENT_PIN_SPACE = "bundle_key_insufficient_pin_space";

    public static final String PREF_CHECK_DEVICE_STATUS = "pref_silent_sign_in";
    public static final String PREF_TERA_APP_LOGIN = "pref_tera_app_login";
    public static final String PREF_ANDROID_FOLDER_PINNED = "pref_android_folder_pinned";
    public static final String PREF_AUTO_AUTH_FAILED_CAUSE = "pref_auto_auth_failed_cause";
    public static final String PREF_APP_FILE_DISPLAY_LAYOUT = "pref_app_file_display_layout";

    // public static final String REPLACE_FILE_PATH_OLD = "/storage/emulated/0/";
    // public static final String REPLACE_FILE_PATH_NEW = "/mnt/shell/emulated/0/";

    public static final String EXTERNAL_STORAGE_SDCARD0_PREFIX = "/storage/emulated";

    public static boolean isAppPinned(Context context, AppInfo appInfo) {
        Logs.d(CLASSNAME, "isAppPinned", appInfo.getName());
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
        Logs.d(CLASSNAME, "stopPinDataTypeFileAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_PIN_DATA_TYPE_FILE);
//        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_PIN_DATA_TYPE_FILE);

        int requestCode = RequestCode.PIN_DATA_TYPE_FILE;
        int flags = PendingIntent.FLAG_CANCEL_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static void startResetXferAlarm(Context context) {
        Logs.d(CLASSNAME, "startResetXferAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_RESET_XFER);
//        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_RESET_XFER);

        int requestCode = RequestCode.UPDATE_EXTERNAL_APP_DIR;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = Interval.UPDATE_EXTERNAL_APP_DIR;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pi);
    }

    public static void startUpdateExternalAppDirAlarm(Context context) {
        Logs.d(CLASSNAME, "startUpdateExternalAppDirAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_UPDATE_EXTERNAL_APP_DIR);

        int requestCode = RequestCode.UPDATE_EXTERNAL_APP_DIR;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = Interval.UPDATE_EXTERNAL_APP_DIR;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pi);
    }

    public static void stopResetXferAlarm(Context context) {
        Logs.d(CLASSNAME, "stopResetXferAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_RESET_XFER);
//        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_RESET_XFER);

        int requestCode = RequestCode.RESET_XFER;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
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
                    Logs.d(CLASSNAME, "getAvailableFilesPaths", "dir_path=" + path);
                } else {
                    Logs.d(CLASSNAME, "getAvailableFilesPaths", "file_path=" + path);
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
            videoPaths = new ArrayList<>();
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
        // External storage
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

        // Internal storage
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
                Logs.i(CLASSNAME, "getHCFSSyncStatus", "jsonResult=" + jsonResult);

                boolean enabled = dataObj.getBoolean("enabled");
                return enabled;
            } else {
                Logs.e(CLASSNAME, "getHCFSSyncStatus", "jsonResult=" + jsonResult);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "getHCFSSyncStatus", Log.getStackTraceString(e));
        }
        return false;
    }

    @Nullable
    public static HCFSStatInfo getHCFSStatInfo() {
        HCFSStatInfo hcfsStatInfo = null;
        try {
            String jsonResult = HCFSApiUtils.getHCFSStat();
            JSONObject jObject = new JSONObject(jsonResult);
            JSONObject dataObj = jObject.getJSONObject("data");
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Logs.i(CLASSNAME, "getHCFSStatInfo", "jsonResult=" + jsonResult);

                hcfsStatInfo = new HCFSStatInfo();
                hcfsStatInfo.setCloudTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_QUOTA));
                hcfsStatInfo.setCloudUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CLOUD_USED));
                // Add the Tera storage scope.
                hcfsStatInfo.setVolUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_VOL_USED));
                hcfsStatInfo.setCacheTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_TOTAL));
                hcfsStatInfo.setCacheUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_USED));
                hcfsStatInfo.setCacheDirtyUsed(dataObj.getLong(HCFSStatInfo.STAT_DATA_CACHE_DIRTY));
                hcfsStatInfo.setPinTotal(dataObj.getLong(HCFSStatInfo.STAT_DATA_PIN_TOTAL));
                hcfsStatInfo.setPinMax(dataObj.getLong(HCFSStatInfo.STAT_DATA_PIN_MAX));
                hcfsStatInfo.setXferUpload(dataObj.getLong(HCFSStatInfo.STAT_DATA_XFER_UP));
                hcfsStatInfo.setXferDownload(dataObj.getLong(HCFSStatInfo.STAT_DATA_XFER_DOWN));
                hcfsStatInfo.setCloudConn(dataObj.getBoolean(HCFSStatInfo.STAT_DATA_CLOUD_CONN));
                hcfsStatInfo.setDataTransfer(dataObj.getInt(HCFSStatInfo.STAT_DATA_DATA_TRANSFER));
                long totalSpace = PhoneStorageUsage.getTotalSpace() + hcfsStatInfo.getCloudTotal();
                long freeSpace = PhoneStorageUsage.getFreeSpace() +
                        (hcfsStatInfo.getCloudTotal() - hcfsStatInfo.getCloudUsed());
                hcfsStatInfo.setTeraTotal(totalSpace);
                hcfsStatInfo.setTeraUsed(totalSpace - freeSpace);
            } else {
                Logs.e(CLASSNAME, "getHCFSStatInfo", "jsonResult=" + jsonResult);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "getHCFSStatInfo", Log.getStackTraceString(e));
        }
        return hcfsStatInfo;
    }

    public static boolean pinApp(AppInfo info) {
        Logs.i(CLASSNAME, "pinApp", "AppName=" + info.getName());
        return pinApp(info, PinType.NORMAL);
    }

    public static boolean pinApp(AppInfo info, int pinType) {
        Logs.d(CLASSNAME, "pinApp", "AppName=" + info.getName());
        String sourceDir = info.getSourceDir();
        String dataDir = info.getDataDir();
        List<String> externalDirList = info.getExternalDirList();
        boolean isSourceDirSuccess = true;
        if (sourceDir != null) {
            Logs.d(CLASSNAME, "pinApp", "sourceDir=" + sourceDir);
            if (sourceDir.startsWith("/data/app")) {
//                isSourceDirSuccess = pinFileOrDirectory(sourceDir, pinType);
                // Priority pin for /data/app no matter pin or unpin
                isSourceDirSuccess = (pinFileOrDirectory(sourceDir, PinType.PRIORITY) == 0);
            }
        }
        boolean isDataDirSuccess = true;
        if (dataDir != null) {
            if (dataDir.startsWith("/data/data") || dataDir.startsWith("/data/user")) {
                isDataDirSuccess = (pinFileOrDirectory(dataDir, pinType) == 0);
            }
        }
        boolean isExternalDirSuccess = true;
        if (externalDirList != null) {
            for (String externalDir : externalDirList) {
                isExternalDirSuccess &= (pinFileOrDirectory(externalDir, pinType) == 0);
            }
        }
        return isSourceDirSuccess & isDataDirSuccess & isExternalDirSuccess;
    }

    public static boolean unpinApp(AppInfo info) {
        Logs.i(CLASSNAME, "unpinApp", "appName=" + info.getName());
        String sourceDir = info.getSourceDir();
        String dataDir = info.getDataDir();
        List<String> externalDirList = info.getExternalDirList();

        boolean isSourceDirSuccess = true;
        if (sourceDir != null) {
            if (sourceDir.startsWith("/data/app")) {
                // Priority pin for /data/app no matter pin or unpin
                isSourceDirSuccess = (pinFileOrDirectory(sourceDir, PinType.PRIORITY) == 0);
            }
        }
        boolean isDataDirSuccess = true;
        if (dataDir != null) {
            if (dataDir.startsWith("/data/data") || dataDir.startsWith("/data/user")) {
                isDataDirSuccess = (unpinFileOrDirectory(dataDir) == 0);
            }
        }
        boolean isExternalDirSuccess = true;
        if (externalDirList != null) {
            for (String externalDir : externalDirList) {
                isExternalDirSuccess &= (unpinFileOrDirectory(externalDir) == 0);
            }
        }

        return isSourceDirSuccess & isDataDirSuccess & isExternalDirSuccess;
    }

    /**
     * @return 0 if pin file or directory is successful, error otherwise.
     */
    public static int pinFileOrDirectory(String filePath) {
        return pinFileOrDirectory(filePath, PinType.NORMAL);
    }

    /**
     * @return 0 if pin file or directory is successful, error otherwise.
     */
    public static int pinFileOrDirectory(String filePath, int pinType) {
        int code = DEFAULT_PINNED_STATUS ? 0 : -1;
        try {
            String jsonResult = HCFSApiUtils.pin(filePath, pinType);
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            String logMsg = "operation=Pin, filePath=" + filePath + ", jsonResult=" + jsonResult;
            if (isSuccess) {
                code = 0;
                Logs.i(CLASSNAME, "pinFileOrDirectory", logMsg);
            } else {
                code = jObject.getInt("code");
                Logs.e(CLASSNAME, "pinFileOrDirectory", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "pinFileOrDirectory", Log.getStackTraceString(e));
        }
        return code;
    }

    /**
     * @return 0 if unpin file or directory is successful, error otherwise.
     */
    public static int unpinFileOrDirectory(String filePath) {
        int code = DEFAULT_PINNED_STATUS ? 0 : -1;
        try {
            String jsonResult = HCFSApiUtils.unpin(filePath);
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            String logMsg = "operation=Unpin, filePath=" + filePath + ", jsonResult=" + jsonResult;
            if (isSuccess) {
                code = 0;
                logMsg += ", code=" + code;
                Logs.i(CLASSNAME, "unpinFileOrDirectory", logMsg);
            } else {
                code = jObject.getInt("code");
                logMsg += ", code=" + code;
                Logs.e(CLASSNAME, "unpinFileOrDirectory", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "unpinFileOrDirectory", Log.getStackTraceString(e));
        }
        return code;
    }

    public static int getDirLocationStatus(String pathName) {
        int locationStatus = LocationStatus.LOCAL;
        try {
            String jsonResult = HCFSApiUtils.getDirStatus(pathName);
            String logMsg = "pathName=" + pathName + ", jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                int code = jObject.getInt("code");
                if (code == 0) {
                    Logs.i(CLASSNAME, "getDirLocationStatus", logMsg);

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
                } else {
                    Logs.e(CLASSNAME, "getDirLocationStatus", logMsg);
                }
            } else {
                Logs.e(CLASSNAME, "getDirLocationStatus", logMsg);
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getDirLocationStatus", Log.getStackTraceString(e));
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
//                Logs.i(CLASSNAME, "getFileLocationStatus", logMsg);
            } else {
                Logs.e(CLASSNAME, "getFileLocationStatus", logMsg);
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getFileLocationStatus", Log.getStackTraceString(e));
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
//                Logs.i(CLASSNAME, "isPathPinned", logMsg);
                int code = jObject.getInt("code");
                if (code == 1) {
                    isPinned = true;
                } else {
                    isPinned = false;
                }
            } else {
                Logs.e(CLASSNAME, "isPathPinned", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "isPathPinned", Log.getStackTraceString(e));
        }
        return isPinned;
    }

    public static boolean isDataUploadCompleted() {
        HCFSStatInfo hcfsStatInfo = getHCFSStatInfo();
        if (hcfsStatInfo != null) {
            return hcfsStatInfo.getFormatCacheDirtyUsed().equals("0B");
        }
        return false;
    }

    public static boolean isSystemPackage(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    /**
     * Reset xfer at 23:59:59 everyday
     */
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

    public static void updateAppExternalDir(Context context) {
        Logs.d(CLASSNAME, "updateAppExternalDir", null);
        Map<String, ArrayList<String>> externalPkgNameMap = new HashMap<>();
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            String externalPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
            File externalAndroidFile = new File(externalPath);
            if (externalAndroidFile.exists()) {
                for (File type : externalAndroidFile.listFiles()) {
                    File[] fileList = type.listFiles();
                    for (File file : fileList) {
                        String path = file.getAbsolutePath();
                        String[] splitPath = path.split("/");
                        String pkgName = splitPath[splitPath.length - 1];

                        ArrayList<String> externalPathList = externalPkgNameMap.get(pkgName);
                        if (externalPathList == null) {
                            externalPathList = new ArrayList<>();
                        }
                        externalPathList.add(path);
                        externalPkgNameMap.put(pkgName, externalPathList);
                    }
                }

                UidDAO uidDAO = UidDAO.getInstance(context);
                for (String pkgName : externalPkgNameMap.keySet()) {
                    UidInfo uidInfo = uidDAO.get(pkgName);
                    ArrayList<String> externalPathList = externalPkgNameMap.get(pkgName);
                    uidInfo.setExternalDir(externalPathList);
                    uidDAO.update(uidInfo, UidDAO.EXTERNAL_DIR_COLUMN);
                }
            }
        }
    }


    public static void changeCloudSyncStatus(Context context, boolean syncWifiOnly) {
        Logs.d(CLASSNAME, "changeCloudSyncStatus", null);
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        if (syncWifiOnly) {
            if (netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                String logMsg = "Wifi is connected";
                TeraCloudConfig.startSyncToCloud(context, logMsg);
            } else {
                String logMsg = "Wifi is not connected";
                TeraCloudConfig.stopSyncToCloud(context, logMsg);
            }
        } else {
            if (netInfo != null && netInfo.isConnected()) {
                String logMsg = "Wifi or Mobile network is connected";
                TeraCloudConfig.startSyncToCloud(context, logMsg);
            } else {
                String logMsg = "Wifi or Mobile network is not connected";
                TeraCloudConfig.stopSyncToCloud(context, logMsg);
            }
        }
    }

    public static void notifyNetworkStatus(Context context, int notify_id, String notify_title, String notify_content) {
        SettingsDAO settingsDAO = SettingsDAO.getInstance(context);
        SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_NOTIFY_CONN_FAILED_RECOVERY);
        boolean notifyConnFailedRecoveryPref = Boolean.valueOf(settingsInfo.getValue());
        if (notifyConnFailedRecoveryPref) {
            NotificationEvent.notify(context, notify_id, notify_title, notify_content);
        }
    }

    public static void startNotifyInsufficientPinSpaceAlarm(Context context) {
        Logs.d(CLASSNAME, "startNotifyInsufficientPinSpaceAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE);
//        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_INSUFFICIENT_PIN_SPACE);

        int requestCode = RequestCode.NOTIFY_INSUFFICIENT_PIN_SPACE;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime();
        long intervalMillis = Interval.NOTIFY_INSUFFICIENT_PIN_SPACE;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
    }

    public static void stopNotifyInsufficientPinSpaceAlarm(Context context) {
        Logs.d(CLASSNAME, "stopNotifyInsufficientPinSpaceAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE);
//        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_INSUFFICIENT_PIN_SPACE);

        int requestCode = RequestCode.NOTIFY_INSUFFICIENT_PIN_SPACE;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static void startNotifyLocalStorageUsedRatioAlarm(Context context) {
        Logs.d(CLASSNAME, "startNotifyLocalStorageUsedRatioAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO);
//        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);

        int requestCode = RequestCode.NOTIFY_LOCAL_STORAGE_USED_RATIO;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime();
        long intervalMillis = Interval.NOTIFY_LOCAL_STORAGE_USED_RATIO;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
    }

    public static void stopNotifyLocalStorageUsedRatioAlarm(Context context) {
        Logs.d(CLASSNAME, "stopNotifyLocalStorageUsedRatioAlarm", null);

        Intent intent = new Intent(context, HCFSMgmtReceiver.class);
        intent.setAction(TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO);
//        intent.setAction(ACTION_HCFS_MANAGEMENT_ALARM);
//        intent.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO);

        int requestCode = RequestCode.NOTIFY_LOCAL_STORAGE_USED_RATIO;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static Drawable getPinUnpinImage(Context context, boolean isPinned) {
        return ContextCompat.getDrawable(context, isPinned ? R.drawable.icon_btn_app_pin : R.drawable.icon_btn_app_unpin);
    }

//    @Nullable
//    public static String getEncryptedDeviceImei() {
//        String encryptedIMEI = new String(HCFSApiUtils.getEncryptedIMEI());
//        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "getEncryptedDeviceImei", "encryptedIMEI=" + encryptedIMEI);
//        return encryptedIMEI;
//    }

    @Nullable
    public static String getEncryptedDeviceImei(String imei) {
        String encryptedIMEI = new String(HCFSApiUtils.getEncryptedIMEI(imei));
        Logs.d(CLASSNAME, "getEncryptedDeviceImei", "encryptedIMEI=" + encryptedIMEI);
        return encryptedIMEI;
    }

    public static String getDecryptedJsonString(String jsonString) {
        String decryptedJsonString = new String(HCFSApiUtils.getDecryptedJsonString(jsonString));
        Logs.d(CLASSNAME, "getDecryptedJsonString", "decryptedJsonString=" + decryptedJsonString);
        return decryptedJsonString;
    }

    public static String getDeviceImei(Context context) {
        String imei = "";
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager.getPhoneCount() != 0) {
            imei = manager.getDeviceId(0);
        }

        Logs.d(CLASSNAME, "getDeviceImei", "Imei=" + imei);
        return imei;
    }

    public static long getOccupiedSize() {
        // Unpin-but-dirty size + pin size
        long occupiedSize = 0;
        try {
            String jsonResult = HCFSApiUtils.getOccupiedSize();
            String logMsg = "jsonResult=" + jsonResult;
            JSONObject jObject = new JSONObject(jsonResult);
            JSONObject dataObj = jObject.getJSONObject("data");
            occupiedSize = dataObj.getLong("occupied");
            Logs.i(CLASSNAME, "getOccupiedSize", logMsg);
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "getOccupiedSize", Log.getStackTraceString(e));
        }
        return occupiedSize;
    }

    public static boolean setSwiftToken(String url, String token) {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.setSwiftToken(url, token);
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            String logMsg = "url=" + url + ", token=" + token + ", result=" + jsonResult;
            if (isSuccess) {
                Logs.i(CLASSNAME, "setSwiftToken", logMsg);
            } else {
                Logs.e(CLASSNAME, "setSwiftToken", logMsg);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "setSwiftToken", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    /***
     * @return <li>1 if system is clean now. That is, there is no dirty data.</li>
     * <li>0 when setting sync point completed.</li>
     * <li>Negative error code in case that error occurs</li>
     */
    public static int startUploadTeraData() {
        int code = -1;
        try {
            String jsonResult = HCFSApiUtils.startUploadTeraData();
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                code = jObject.getInt("code");
                Logs.i(CLASSNAME, "startUploadTeraData", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "startUploadTeraData", null);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "startUploadTeraData", Log.getStackTraceString(e));
        }
        return code;
    }

    /***
     * @return <li>1 if no sync point is set.</li>
     * <li>0 when canceling the setting completed.</li>
     * <li>Negative error code in case that error occurs</li>
     */
    public static int stopUploadTeraData() {
        int code = -1;
        try {
            String jsonResult = HCFSApiUtils.stopUploadTeraData();
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                code = jObject.getInt("code");
                Logs.i(CLASSNAME, "stopUploadTeraData", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "stopUploadTeraData", null);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "stopUploadTeraData", Log.getStackTraceString(e));
        }
        return code;
    }

    public static boolean collectSysLogs() {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.collectSysLogs();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "collectSysLogs", Log.getStackTraceString(e));
        }
        return isSuccess;
    }
}
