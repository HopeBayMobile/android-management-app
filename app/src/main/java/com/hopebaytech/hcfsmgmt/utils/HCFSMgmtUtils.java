package com.hopebaytech.hcfsmgmt.utils;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.os.RemoteException;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
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

import javax.net.ssl.HttpsURLConnection;

import java.lang.reflect.Method;

public class HCFSMgmtUtils {

    public static final String TAG = "HopeBay";
    public static final String CLASSNAME = "HCFSMgmtUtils";
//    public static final String ACTION_HCFS_MANAGEMENT_ALARM = "com.hopebaytech.hcfsmgmt.HCFSMgmtReceiver";

    public static final boolean DEFAULT_PINNED_STATUS = false;

    public static final String PREF_CHECK_DEVICE_STATUS = "pref_silent_sign_in";
    public static final String PREF_TERA_APP_LOGIN = "pref_tera_app_login";
    public static final String PREF_AUTO_AUTH_FAILED_CAUSE = "pref_auto_auth_failed_cause";
    public static final String PREF_APP_FILE_DISPLAY_LAYOUT = "pref_app_file_display_layout";
    public static final String PREF_APP_DISPLAY_LAYOUT = "pref_app_display_layout";
    public static final String PREF_FILE_DISPLAY_LAYOUT = "pref_file_display_layout";
    public static final String PREF_RESTORE_STATUS = "pref_restore_status";

    // public static final String REPLACE_FILE_PATH_OLD = "/storage/emulated/0/";
    // public static final String REPLACE_FILE_PATH_NEW = "/mnt/shell/emulated/0/";

    public static final String EXTERNAL_STORAGE_SDCARD0_PREFIX = "/storage/emulated";

    private static final String DATA_APP_PATH = "/data/app";

    public static boolean isAppPinned(Context context, AppInfo appInfo) {
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
//                Logs.d(CLASSNAME, "getHCFSStatInfo", "jsonResult=" + jsonResult);

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

        boolean isMinApkSuccess = false;
        boolean isSourceDirSuccess = true;
        if (sourceDir != null) {
            if (sourceDir.startsWith("/data/app")) {
                isSourceDirSuccess = (unpinFileOrDirectory(sourceDir) == 0);

                // Priority pin /data/app/<pkg-folder>/.basemin
                isMinApkSuccess = (pinFileOrDirectory(sourceDir + "/.basemin", PinType.PRIORITY) == 0);
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

        return isSourceDirSuccess & isMinApkSuccess & isDataDirSuccess & isExternalDirSuccess;
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
                    JSONObject dataObj = jObject.getJSONObject("data");
                    int num_local = dataObj.getInt("num_local");
                    int num_hybrid = dataObj.getInt("num_hybrid");
                    int num_cloud = dataObj.getInt("num_cloud");

                    if (num_cloud == 0 && num_hybrid == 0) {
                        locationStatus = LocationStatus.LOCAL;
                    } else {
                        locationStatus = LocationStatus.NOT_LOCAL;
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
                    case 2:
                        status = LocationStatus.NOT_LOCAL;
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
    public static boolean resetDataXfer() {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.resetXfer();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Log.i(TAG, "resetDataXfer: " + jsonResult);
            } else {
                Log.e(TAG, "resetDataXfer: " + jsonResult);
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
                    if (uidInfo == null) {
                        continue;
                    }
                    ArrayList<String> externalPathList = externalPkgNameMap.get(pkgName);
                    if (uidInfo == null) {
                        continue;
                    }
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
            if (netInfo != null) {
                Logs.d(CLASSNAME, "changeCloudSyncStatus", "type=" + netInfo.getType()
                        + ", state=" + netInfo.getState()
                        + ", detailedState=" + netInfo.getDetailedState());

                if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    String logMsg = "Current wifi network is active";
                    TeraCloudConfig.startSyncToCloud(context, logMsg);
                } else {
                    String logMsg = "Current network is active but not wifi";
                    TeraCloudConfig.stopSyncToCloud(context, logMsg);
                }
            } else {
                String logMsg = "No default network or wifi network is current active";
                TeraCloudConfig.stopSyncToCloud(context, logMsg);
            }
        } else {
            if (netInfo != null) {
                Logs.d(CLASSNAME, "changeCloudSyncStatus", "type=" + netInfo.getType()
                        + ", state=" + netInfo.getState()
                        + ", detailedState=" + netInfo.getDetailedState());

                String logMsg = "Current default network is active";
                TeraCloudConfig.startSyncToCloud(context, logMsg);
            } else {
                String logMsg = "No default network is current active";
                TeraCloudConfig.stopSyncToCloud(context, logMsg);
            }
        }
    }

    public static void notifyNetworkStatus(Context context, int notify_id, String notify_title, String notify_content) {
        SettingsDAO settingsDAO = SettingsDAO.getInstance(context);
        SettingsInfo settingsInfo = settingsDAO.get(SettingsFragment.PREF_NOTIFY_CONN_FAILED_RECOVERY);
        boolean notifyConnFailedRecoveryPref = false;
        if (settingsInfo != null) {
            notifyConnFailedRecoveryPref = Boolean.valueOf(settingsInfo.getValue());
        }
        if (notifyConnFailedRecoveryPref) {
            NotificationEvent.notify(context, notify_id, notify_title, notify_content);
        }
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

    /**
     * @param jsonString the encrypted json string which is only encrypted when HTTP response code
     *                   is {@link HttpsURLConnection#HTTP_OK}.
     * @return the decrypted json string
     */
    public static String getDecryptedJsonString(String jsonString) {
        Logs.d(CLASSNAME, "getDecryptedJsonString", "jsonString=" + jsonString);
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
                Logs.d(CLASSNAME, "setSwiftToken", logMsg);
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
        Logs.i(CLASSNAME, "startUploadTeraData", null);
        int code = -1;
        try {
            String jsonResult = HCFSApiUtils.startUploadTeraData();
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                code = jObject.getInt("code");
                Logs.d(CLASSNAME, "startUploadTeraData", "jObject=" + jObject);
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
        Logs.i(CLASSNAME, "stopUploadTeraData", null);
        int code = -1;
        try {
            String jsonResult = HCFSApiUtils.stopUploadTeraData();
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                code = jObject.getInt("code");
                Logs.d(CLASSNAME, "stopUploadTeraData", "jObject=" + jObject);
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

    /**
     * @return
     */
    public static int triggerRestore() {
        int code = -1;
        try {
            String jsonResult = HCFSApiUtils.triggerRestore();
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                code = jObject.getInt("code");
                Logs.i(CLASSNAME, "triggerRestore", "jsonResult=" + jsonResult);
            } else {
                code = -(jObject.getInt("code"));
                Logs.e(CLASSNAME, "triggerRestore", "jsonResult=" + jsonResult);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "triggerRestore", Log.getStackTraceString(e));
        }
        return code;
    }

    /**
     * @return <li>-1 if error occurs</li>
     * <li>0 if not being restored</li>
     * <li>1 if in stage 1 of restoration process</li>
     * <li>2 if in stage 2 of restoration process</li>
     */
    public static int checkRestoreStatus() {
        int code = -1;
        try {
            String jsonResult = HCFSApiUtils.checkRestoreStatus();
            JSONObject jObject = new JSONObject(jsonResult);
            boolean isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                code = jObject.getInt("code");
                Logs.i(CLASSNAME, "checkRestoreStatus", "jObject=" + jObject);
            } else {
                code = -(jObject.getInt("code"));
                Logs.e(CLASSNAME, "checkRestoreStatus", "jObject=" + jObject);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "checkRestoreStatus", Log.getStackTraceString(e));
        }
        return code;
    }

    public static boolean notifyAppListChange() {
        boolean isSuccess = false;
        try {
            String jsonResult = HCFSApiUtils.notifyApplistChange();
            JSONObject jObject = new JSONObject(jsonResult);
            isSuccess = jObject.getBoolean("result");
            if (isSuccess) {
                Logs.d(CLASSNAME, "notifyAppListChange", "jObject=" + jObject);
            } else {
                Logs.e(CLASSNAME, "notifyAppListChange", "jObject=" + jObject);
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "notifyAppListChange", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    public static boolean createMinimalApk(Context context, String packageName, boolean blocking) {
        boolean isSuccess = false;
        try {
            String sourceDir = getSourceDir(context, packageName);
            if (sourceDir != null) {
                String jsonResult = HCFSApiUtils.createMinimalApk(sourceDir, blocking ? 1 : 0);
                JSONObject jObject = new JSONObject(jsonResult);
                isSuccess = jObject.getBoolean("result");
                if (isSuccess) {
                    Logs.d(CLASSNAME, "createMinimalApk", "jObject=" + jObject);
                } else {
                    Logs.e(CLASSNAME, "createMinimalApk", "jObject=" + jObject);
                }
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "createMinimalApk", Log.getStackTraceString(e));
        }
        return isSuccess;
    }

    /**
     * Check the minimal base apk is exist or not
     *
     * @param context
     * @param packageName the package name to be checked
     * @param blocking    the method is blocking or not
     * @return a json string contains: true if success, false otherwise.
     * <li>-1, error</li>
     * <li>0, not existed</li>
     * <li>1, existed</li>
     * <li>2, create minimal base apk in progress</li>
     */
    public static int checkMinimalApk(Context context, String packageName, boolean blocking) {
        int code = -1;
        try {
            String sourceDir = getSourceDir(context, packageName);
            if (sourceDir != null) {
                String jsonResult = HCFSApiUtils.checkMinimalApk(sourceDir, blocking ? 1 : 0);
                JSONObject jObject = new JSONObject(jsonResult);
                if (jObject.getBoolean("result")) {
                    code = jObject.getInt("code");
                    Logs.d(CLASSNAME, "checkMinimalApk", "jObject=" + jObject);
                } else {
                    Logs.e(CLASSNAME, "checkMinimalApk", "jObject=" + jObject);
                }
            }
        } catch (JSONException e) {
            Logs.e(CLASSNAME, "checkMinimalApk", Log.getStackTraceString(e));
        }
        return code;
    }

    private static String getSourceDir(Context context, String packageName) {
        String sourceDir = null;
        try {
            PackageInfo p = context.getPackageManager().getPackageInfo(packageName, 0);
            sourceDir = p.applicationInfo.sourceDir;
            sourceDir = sourceDir.substring(0, sourceDir.lastIndexOf("/"));
            if (sourceDir.startsWith(DATA_APP_PATH)) {
                sourceDir = sourceDir.substring(DATA_APP_PATH.length() + 1, sourceDir.length());
                Logs.d(CLASSNAME, "getSourceDir", "package name: " + packageName + " package path: " + sourceDir);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logs.e(CLASSNAME, "getSourceDir", e.toString());
        }
        return sourceDir;
    }
}
