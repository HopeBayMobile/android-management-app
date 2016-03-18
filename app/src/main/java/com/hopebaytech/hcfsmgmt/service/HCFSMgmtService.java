package com.hopebaytech.hcfsmgmt.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.ServiceAppDAO;
import com.hopebaytech.hcfsmgmt.db.ServiceFileDirDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceAppInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceFileDirInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.DisplayType;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class HCFSMgmtService extends Service {

    private final String CLASSNAME = getClass().getSimpleName();
    private ExecutorService mCacheExecutor;
    private ServiceFileDirDAO mServiceFileDirDAO;
    private ServiceAppDAO mServiceAppDAO;
    private UidDAO mUidDAO;
    private Thread mOngoingThread;

    @Override
    public void onCreate() {
        super.onCreate();
        mCacheExecutor = Executors.newCachedThreadPool();
        mServiceFileDirDAO = new ServiceFileDirDAO(this);
        mServiceAppDAO = new ServiceAppDAO(this);
        mUidDAO = new UidDAO(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            final String operation = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION);
            final Context context = getApplicationContext();
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onStartCommand", "operation=" + operation);
            mCacheExecutor.execute(new Runnable() {
                public void run() {
                    if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_NOTIFY_UPLAOD_COMPLETED)) {
                        /** Send a notification to users when upload completed */
                        notifyUploadCompleted();
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE)) {
                        /** Pin data type files */
                        pinOrUnpinDataTypeFile();
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_PIN_APP)) {
                        /** Pin user app */
                        final boolean default_pinned_status = HCFSMgmtUtils.DEFAULT_PINNED_STATUS;
                        boolean isAppPinned = intent.getBooleanExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_PIN_STATUS, default_pinned_status);
                        String appName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_NAME);
                        String packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_PACKAGE_NAME);
                        String dataDir = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_DATA_DIR);
                        String sourceDir = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_SOURCE_DIR);
                        String externalDir = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_APP_EXTERNAL_DIR);

                        ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
                        serviceAppInfo.setPinned(isAppPinned);
                        serviceAppInfo.setAppName(appName);
                        serviceAppInfo.setPackageName(packageName);
//                        serviceAppInfo.setSourceDir(sourceDir); // TODO
                        serviceAppInfo.setSourceDir(null);
                        serviceAppInfo.setDataDir(dataDir);
                        serviceAppInfo.setExternalDir(externalDir);
                        mServiceAppDAO.insert(serviceAppInfo);
                        pinOrUnpinApp(serviceAppInfo);
                        mServiceAppDAO.delete(serviceAppInfo);
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_PIN_FILE_DIRECTORY)) {
                        /** Pin file or directory */
                        final boolean default_pinned_status = HCFSMgmtUtils.DEFAULT_PINNED_STATUS;
                        boolean isFileDirPinned = intent.getBooleanExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_PIN_STATUS, default_pinned_status);
                        String filePath = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PIN_FILE_DIR_FILEAPTH);

                        ServiceFileDirInfo serviceFileDirInfo = new ServiceFileDirInfo();
                        serviceFileDirInfo.setPinned(isFileDirPinned);
                        serviceFileDirInfo.setFilePath(filePath);
                        mServiceFileDirDAO.insert(serviceFileDirInfo);
                        pinOrUnpinFileOrDirectory(serviceFileDirInfo);
                        mServiceFileDirDAO.delete(serviceFileDirInfo.getFilePath());
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_ADD_UID_AND_PIN_SYSTEM_APP_WHEN_BOOT_UP)) {
                        /** From HCFSMgmtReceiver's ACTION_BOOT_COMPLETED */

                        /** Add NEW uid info to database when system boot up */
                        List<UidInfo> uidInfoList = mUidDAO.getAll();
                        Set<String> packageNameSet = new HashSet<>();
                        for (UidInfo uidInfo : uidInfoList) {
                            packageNameSet.add(uidInfo.getPackageName());
                        }
                        PackageManager pm = getPackageManager();
                        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                        for (ApplicationInfo packageInfo : packages) {
                            String packageName = packageInfo.packageName;
                            if (!packageNameSet.contains(packageName)) {
                                int uid = packageInfo.uid;
                                boolean isPinned = false;
                                boolean isSystemApp = false;
                                if (HCFSMgmtUtils.isSystemPackage(packageInfo)) {
                                    isPinned = true;
                                    isSystemApp = true;
                                }
                                mUidDAO.insert(new UidInfo(isPinned, isSystemApp, uid, packageName));
                            }
                        }

                        /** Pin system app when system boot up */
                        ArrayList<ItemInfo> itemInfoList = DisplayType.getListOfInstalledApps(context, DisplayType.APP_SYSTEM);
                        for (ItemInfo itemInfo : itemInfoList) {
                            AppInfo appInfo = (AppInfo) itemInfo;
                            ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
                            serviceAppInfo.setPinned(true);
                            serviceAppInfo.setAppName(appInfo.getItemName());
                            serviceAppInfo.setPackageName(appInfo.getPackageName());
                            serviceAppInfo.setSourceDir(appInfo.getSourceDir());
                            serviceAppInfo.setDataDir(appInfo.getDataDir());
                            serviceAppInfo.setExternalDir(appInfo.getExternalDir());
                            pinOrUnpinApp(serviceAppInfo);
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_ADD_UID_TO_DATABASE_AND_UNPIN_USER_APP)) {
                        /** From HCFSMgmtReceiver's ACTION_PACKAGE_ADDED */

                        /** Add uid info of new installed app to database */
                        int uid = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_UID, -1);
                        String packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME);
                        if (mUidDAO.get(packageName) == null) {
                            final boolean isPinned = false;
                            final boolean isSystemApp = false;
                            mUidDAO.insert(new UidInfo(isPinned, isSystemApp, uid, packageName));
                        }

                        /** Unpin user app on /data/data and /data/app */
                        try {
                            PackageManager pm = getPackageManager();
                            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                            String sourceDir = applicationInfo.sourceDir;
                            int lastIndex = sourceDir.lastIndexOf("/");
                            String sourceDirWithoutApkSuffix = sourceDir.substring(0, lastIndex);
                            boolean isSystemApp = HCFSMgmtUtils.isSystemPackage(applicationInfo);
                            if (!isSystemApp) {
                                ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
                                serviceAppInfo.setPinned(false);
                                serviceAppInfo.setAppName(applicationInfo.loadLabel(pm).toString());
                                serviceAppInfo.setPackageName(packageName);
//                                serviceAppInfo.setSourceDir(sourceDirWithoutApkSuffix); // TODO
                                serviceAppInfo.setSourceDir(null);
                                serviceAppInfo.setDataDir(applicationInfo.dataDir);
                                serviceAppInfo.setExternalDir(null);
                                pinOrUnpinApp(serviceAppInfo);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_PIN_UNPIN_UDPATE_APP)) {
                        /** From HCFSMgmtReceiver's ACTION_PACKAGE_REPLACED */

                        /** Pin or unpin an update app according to pin_status field in uid.db */
                        String packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME);
                        try {
                            PackageManager pm = getPackageManager();
                            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                            String sourceDir = applicationInfo.sourceDir;
                            int lastIndex = sourceDir.lastIndexOf("/");
                            String sourceDirWithoutApkSuffix = sourceDir.substring(0, lastIndex);
                            UidInfo uidInfo = mUidDAO.get(packageName);
                            if (uidInfo != null) {
                                boolean isPinned = uidInfo.isPinned();
                                ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
                                serviceAppInfo.setPinned(isPinned);
                                serviceAppInfo.setAppName(applicationInfo.loadLabel(pm).toString());
                                serviceAppInfo.setPackageName(packageName);
//                                serviceAppInfo.setSourceDir(sourceDirWithoutApkSuffix); // TODO
                                serviceAppInfo.setSourceDir(null);
                                serviceAppInfo.setDataDir(applicationInfo.dataDir);
                                serviceAppInfo.setExternalDir(null);
                                pinOrUnpinApp(serviceAppInfo);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_REMOVE_UID_FROM_DATABASE)) {
                        /** From HCFSMgmtReceiver's ACTION_PACKAGE_REMOVED */

                        /** Remove uid info of uninstalled app from database */
                        int uid = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_UID, -1);
                        String packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME);
                        if (mUidDAO.get(packageName) != null) {
                            String logMsg = "operation=" + operation + ", uid=" + uid + ", packageName=" + packageName;
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onStartCommand", logMsg);
                            mUidDAO.delete(packageName);
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_RESET_XFER)) {
                        /** Reset xfer at 23:59:59 everyday */
                        HCFSMgmtUtils.resetXfer();
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
                        /** Send a notification to user when storage used ratio is above the value set by user */
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
                        String key_pref = SettingsFragment.KEY_PREF_NOTIFY_LOCAL_STORAGE_USED_RATIO;
                        String storage_used_ratio = sharedPreferences.getString(key_pref, defaultValue);
                        HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
                        if (statInfo != null) {
                            long rawCacheDirtyUsed = statInfo.getRawCacheDirtyUsed();
                            long rawPinTotal = statInfo.getRawPinTotal();
                            long rawCacheTotal = statInfo.getRawCacheTotal();
                            long max = Math.max(rawCacheDirtyUsed, rawPinTotal);
                            if ((max / rawCacheTotal) > Integer.valueOf(storage_used_ratio)) {
                                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
                                String notify_title = getString(R.string.app_name);
                                String notify_message = String.format(getString(R.string.notify_exceed_local_storage_used_ratio), storage_used_ratio);
                                HCFSMgmtUtils.notifyEvent(context, notify_id, notify_title, notify_message, false);
                            }
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_ONGOING_NOTIFICATION)) {
                        /** Send an ongoing notification to show hcfs network status and storage usage */
                        boolean isOnGoing = intent.getBooleanExtra(HCFSMgmtUtils.INTENT_KEY_ONGOING, false);
                        if (isOnGoing) {
                            if (mOngoingThread == null) {
                                mOngoingThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final long FIVE_MINUTES_IN_MILLISECONDS = 5 * 60 * 1000;
                                        while (true) {
                                            try {
                                                // TODO hcfs connection status is not ready
                                                HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
                                                if (statInfo != null) {
                                                    boolean syncEnabled = HCFSMgmtUtils.getHCFSSyncStatus();
                                                    boolean cloudConnected = statInfo.isCloudConn();
                                                    if (syncEnabled && cloudConnected) {

                                                    } else if (syncEnabled && !cloudConnected) {

                                                    } else {

                                                    }
                                                    String notifyTitle = getString(R.string.home_page_network_status_connected);
                                                    String notifyMsg = getString(R.string.home_page_used_space) + ": " + statInfo.getVolUsed() + " / " + statInfo.getCloudTotal();
                                                    HCFSMgmtUtils.notifyEvent(HCFSMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING, notifyTitle, notifyMsg, true);
                                                }
                                                Thread.sleep(FIVE_MINUTES_IN_MILLISECONDS);
                                            } catch (InterruptedException e) {
                                                break;
                                            }
                                        }
                                    }
                                });
                                mOngoingThread.start();
                            }
                        } else {
                            if (mOngoingThread != null) {
                                mOngoingThread.interrupt();
                                mOngoingThread = null;
                            }
                            HCFSMgmtUtils.cancelEvent(HCFSMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
                        }
                    }
                }
            });
        } else {
            /**
             * Service is restarted and then execute the uncompleted pin/unpin operation when user manually close app and removes it from background.
             */
            mCacheExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mServiceFileDirDAO.getCount() > 0) {
                        List<ServiceFileDirInfo> infoList = mServiceFileDirDAO.getAll();
                        for (final ServiceFileDirInfo info : infoList) {
                            pinOrUnpinFileOrDirectory(info);
                            mServiceFileDirDAO.delete(info.getFilePath());
                        }
                    }
                }
            });
            mCacheExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    if (mServiceAppDAO.getCount() > 0) {
                        List<ServiceAppInfo> infoList = mServiceAppDAO.getAll();
                        for (final ServiceAppInfo serviceAppInfo : infoList) {
                            pinOrUnpinApp(serviceAppInfo);
                            mServiceAppDAO.delete(serviceAppInfo);
                        }
                    }
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
        // return START_REDELIVER_INTENT;
    }

    private void notifyUploadCompleted() {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "notifyUploadCompleted", null);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notifyUploadCompletedPref = sharedPreferences.getBoolean(SettingsFragment.KEY_PREF_NOTIFY_UPLAOD_COMPLETED, true);
        if (notifyUploadCompletedPref) {
            boolean isUploadCompleted = HCFSMgmtUtils.isDataUploadCompleted();
            if (isUploadCompleted) {
                int id_notify = HCFSMgmtUtils.NOTIFY_ID_UPLOAD_COMPLETED;
                String notify_title = getString(R.string.app_name);
                String notify_content = getString(R.string.notify_upload_completed);
                HCFSMgmtUtils.notifyEvent(this, id_notify, notify_title, notify_content, false);
            }
        }
    }

    private void pinOrUnpinApp(ServiceAppInfo info) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "pinOrUnpinApp", info.getAppName());
        final boolean isPinned = info.isPinned();
        if (isPinned) {
            if (!HCFSMgmtUtils.pinApp(info)) {
                handleAppFailureOfPinOrUnpin(isPinned, info, getString(R.string.notify_pin_app_failure));
            }
        } else {
            if (!HCFSMgmtUtils.unpinApp(info)) {
                handleAppFailureOfPinOrUnpin(isPinned, info, getString(R.string.notify_unpin_app_failure));
            }
        }
    }

    private void handleAppFailureOfPinOrUnpin(boolean isPinned, ServiceAppInfo info, String notifyMsg) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "pinOrUnpinFailure", info.getAppName());

        UidInfo uidInfo = new UidInfo();
        uidInfo.setPackageName(info.getPackageName());
        uidInfo.setIsPinned(!isPinned);
        mUidDAO.update(uidInfo, UidDAO.PIN_STATUS_COLUMN);

        int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
        String notify_title = getString(R.string.app_name);
        String notify_message = notifyMsg + ": " + info.getAppName();
        HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message, false);
    }

    private void pinOrUnpinDataTypeFile() {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "pinOrUnpinDataTypeFile", null);

        DataTypeDAO dataTypeDAO = new DataTypeDAO(this);
        ArrayList<String> notifyMessageList = new ArrayList<>();

        DataTypeInfo imageTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_IMAGE);
        if (imageTypeInfo != null) {
            String dataType = imageTypeInfo.getDataType();
            boolean isImagePinned = imageTypeInfo.isPinned();
            long dateUpdated = imageTypeInfo.getDateUpdated();
            ArrayList<String> imagePaths = HCFSMgmtUtils.getAvailableImagePaths(this, dateUpdated);
            long processTimeSeconds = System.currentTimeMillis() / 1000;
            if (imagePaths != null) {
                if (isImagePinned) {
                    int imgFailedToPinCount = 0;
                    for (String path : imagePaths) {
                        if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
                            imgFailedToPinCount++;
                        }
                    }
                    if (imgFailedToPinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_management_service_image_failed_to_pin) + ": " + imgFailedToPinCount);
                    }
                } else {
                    int imgFailedToUnpinCount = 0;
                    for (String path : imagePaths) {
                        if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
                            imgFailedToUnpinCount++;
                        }
                    }
                    if (imgFailedToUnpinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_management_service_image_failed_to_unpin) + ": " + imgFailedToUnpinCount);
                    }
                }
                imageTypeInfo.setDateUpdated(processTimeSeconds);
                boolean isSuccess = dataTypeDAO.update(dataType, imageTypeInfo, DataTypeDAO.DATE_UPDATED_COLUMN);
                if (!isSuccess) {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "pinOrUnpinFileOrDirectory", "msg=Failed to upate datatype table, dataType=" + dataType
                            + ", column=" + DataTypeDAO.DATE_UPDATED_COLUMN + ", dateUpdated=" + imageTypeInfo.getDateUpdated());
                }
            }
        }

        DataTypeInfo videoTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_VIDEO);
        if (videoTypeInfo != null) {
            String dataType = videoTypeInfo.getDataType();
            boolean isVideoPinned = videoTypeInfo.isPinned();
            long dateUpdated = videoTypeInfo.getDateUpdated();
            ArrayList<String> videoPaths = HCFSMgmtUtils.getAvailableVideoPaths(this, dateUpdated);
            long processTimeSeconds = System.currentTimeMillis() / 1000;
            if (videoPaths != null) {
                if (isVideoPinned) {
                    int videoFailedToPinCount = 0;
                    for (String path : videoPaths) {
                        if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
                            videoFailedToPinCount++;
                        }
                    }
                    if (videoFailedToPinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_management_service_video_failed_to_pin) + ": " + videoFailedToPinCount);
                    }
                } else {
                    int videoFailedToUnpinCount = 0;
                    for (String path : videoPaths) {
                        if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
                            videoFailedToUnpinCount++;
                        }
                    }
                    if (videoFailedToUnpinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_management_service_video_failed_to_unpin) + ": " + videoFailedToUnpinCount);
                    }
                }
                videoTypeInfo.setDateUpdated(processTimeSeconds);
                boolean isSuccess = dataTypeDAO.update(dataType, videoTypeInfo, DataTypeDAO.DATE_UPDATED_COLUMN);
                if (!isSuccess) {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "pinOrUnpinFileOrDirectory", "msg=Failed to upate datatype table, dataType=" + dataType
                            + ", column=" + DataTypeDAO.DATE_UPDATED_COLUMN + ", dateUpdated=" + videoTypeInfo.getDateUpdated());
                }
            }
        }

        DataTypeInfo audioTypeInfo = dataTypeDAO.get(DataTypeDAO.DATA_TYPE_AUDIO);
        if (audioTypeInfo != null) {
            String dataType = audioTypeInfo.getDataType();
            boolean isAudioPinned = audioTypeInfo.isPinned();
            long dateUpdated = audioTypeInfo.getDateUpdated();
            ArrayList<String> audioPaths = HCFSMgmtUtils.getAvailableAudioPaths(this, dateUpdated);
            long processTimeSeconds = System.currentTimeMillis() / 1000;
            if (audioPaths != null) {
                if (isAudioPinned) {
                    int audioFailedToPinCount = 0;
                    for (String path : audioPaths) {
                        if (!HCFSMgmtUtils.pinFileOrDirectory(path)) {
                            audioFailedToPinCount++;
                        }
                    }
                    if (audioFailedToPinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_management_service_audio_failed_to_pin) + ": " + audioFailedToPinCount);
                    }
                } else {
                    int audioFailedToUnpinCount = 0;
                    for (String path : audioPaths) {
                        if (!HCFSMgmtUtils.unpinFileOrDirectory(path)) {
                            audioFailedToUnpinCount++;
                        }
                    }
                    if (audioFailedToUnpinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_management_service_audio_failed_to_unpin) + ": " + audioFailedToUnpinCount);
                    }
                }
                audioTypeInfo.setDateUpdated(processTimeSeconds);
                boolean isSuccess = dataTypeDAO.update(dataType, audioTypeInfo, DataTypeDAO.DATE_UPDATED_COLUMN);
                if (!isSuccess) {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "pinOrUnpinFileOrDirectory", "msg=Failed to upate datatype table, dataType=" + dataType
                            + ", column=" + DataTypeDAO.DATE_UPDATED_COLUMN + ", dateUpdated=" + audioTypeInfo.getDateUpdated());
                }
            }
        }

        if (notifyMessageList.size() != 0) {
            String notify_title = getString(R.string.app_name);
            StringBuilder notify_message = new StringBuilder();
            for (int i = 0; i < notifyMessageList.size(); i++) {
                notify_message.append(notifyMessageList.get(i));
                if (i < notifyMessageList.size() - 1) {
                    notify_message.append("\n");
                }
            }
            int notify_id = HCFSMgmtUtils.NOTIFY_ID_PIN_UNPIN_FAILURE;
            HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message.toString(), false);
        }
    }

    private void pinOrUnpinFileOrDirectory(ServiceFileDirInfo info) {
        String filePath = info.getFilePath();
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "pinOrUnpinFileOrDirectory",
                "filePath=" + filePath + ", threadName=" + Thread.currentThread().getName());
        boolean isPinned = info.isPinned();
        if (isPinned) {
            boolean isSuccess = HCFSMgmtUtils.pinFileOrDirectory(filePath);
            if (!isSuccess) {
                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
                String notify_title = getString(R.string.app_name);
                String notify_message = getString(R.string.notify_pin_file_dir_failure) + "： " + filePath;
                HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message, false);
            }
        } else {
            boolean isSuccess = HCFSMgmtUtils.unpinFileOrDirectory(filePath);
            if (!isSuccess) {
                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
                String notify_title = getString(R.string.app_name);
                String notify_message = getString(R.string.notify_unpin_file_dir_failure) + "： " + filePath;
                HCFSMgmtUtils.notifyEvent(this, notify_id, notify_title, notify_message, false);
            }
        }
    }

}
