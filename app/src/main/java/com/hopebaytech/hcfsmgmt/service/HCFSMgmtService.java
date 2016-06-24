package com.hopebaytech.hcfsmgmt.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.ServiceFileDirDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceFileDirInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IMgmtBinder;
import com.hopebaytech.hcfsmgmt.interfaces.IPinUnpinListener;
import com.hopebaytech.hcfsmgmt.utils.DisplayTypeFactory;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.PinType;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

public class HCFSMgmtService extends Service {

    private final String CLASSNAME = getClass().getSimpleName();
    private ExecutorService mCacheExecutor;
    private ServiceFileDirDAO mServiceFileDirDAO;
    private Thread mOngoingThread;
    private GoogleApiClient mGoogleApiClient;
    private UidDAO mUidDAO;
    private DataTypeDAO mDataTypeDAO;
    private final IBinder mBinder = new MgmtServiceBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MgmtServiceBinder extends Binder implements IMgmtBinder {
        @Override
        public HCFSMgmtService getService() {
            /** Return this instance of LocalService so clients can call public methods */
            return HCFSMgmtService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mCacheExecutor = Executors.newCachedThreadPool();
        mServiceFileDirDAO = ServiceFileDirDAO.getInstance(this);
        mUidDAO = UidDAO.getInstance(this);
        mDataTypeDAO = DataTypeDAO.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            final String operation = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION);
            final Context context = getApplicationContext();
            Logs.d(CLASSNAME, "onStartCommand", "operation=" + operation);
            mCacheExecutor.execute(new Runnable() {
                public void run() {
//                    if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_PIN_DATA_TYPE_FILE)) {
//                        /** Pin data type files */
//                        pinOrUnpinDataTypeFile();
//                    }
                    if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_ADD_UID_AND_PIN_SYSTEM_APP_WHEN_BOOT_UP)) {
                        // From HCFSMgmtReceiver's ACTION_BOOT_COMPLETED */

                        // Add NEW uid info to database when system boot up
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

                        // Pin /storage/emulated/0/Android folder
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean isAndroidFolderPinned = sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_ANDROID_FOLDER_PINNED, false);
                        if (!isAndroidFolderPinned) {
                            String externalAndroidPath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/Android";
                            if (!HCFSMgmtUtils.isPathPinned(externalAndroidPath)) {
                                HCFSMgmtUtils.pinFileOrDirectory(externalAndroidPath);
                            }

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(HCFSMgmtUtils.PREF_ANDROID_FOLDER_PINNED, true);
                            editor.apply();
                        }

                        // Pin system app on system start up
                        ArrayList<ItemInfo> itemInfoList = DisplayTypeFactory.getListOfInstalledApps(context, DisplayTypeFactory.APP_SYSTEM);
                        for (ItemInfo itemInfo : itemInfoList) {
                            AppInfo appInfo = (AppInfo) itemInfo;
                            appInfo.setPinned(true);
                            pinOrUnpinApp(appInfo, PinType.PRIORITY);
//                            ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
//                            serviceAppInfo.setPinned(true);
//                            serviceAppInfo.setAppName(appInfo.getName());
//                            serviceAppInfo.setPackageName(appInfo.getPackageName());
//                            serviceAppInfo.setSourceDir(appInfo.getSourceDir());
//                            serviceAppInfo.setDataDir(appInfo.getDataDir());
//                            serviceAppInfo.setExternalDirList(appInfo.getExternalDirList());
//                            pinOrUnpinApp(serviceAppInfo);
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_ADD_UID_TO_DATABASE_AND_UNPIN_USER_APP)) {
                        // From HCFSMgmtReceiver's ACTION_PACKAGE_ADDED

                        // Add uid info of new installed app to database
                        int uid = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_UID, -1);
                        String packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME);
                        if (mUidDAO.get(packageName) == null) {
                            final boolean isPinned = false;
                            final boolean isSystemApp = false;
                            mUidDAO.insert(new UidInfo(isPinned, isSystemApp, uid, packageName));
                        }

                        // Unpin user app on /data/data and /data/app
                        try {
                            PackageManager pm = getPackageManager();
                            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//                            String sourceDir = applicationInfo.sourceDir;
//                            int lastIndex = sourceDir.lastIndexOf("/");
//                            String sourceDirWithoutApkSuffix = sourceDir.substring(0, lastIndex);
                            boolean isSystemApp = HCFSMgmtUtils.isSystemPackage(applicationInfo);
                            if (!isSystemApp) {
//                                ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
//                                serviceAppInfo.setPinned(false);
//                                serviceAppInfo.setAppName(applicationInfo.loadLabel(pm).toString());
//                                serviceAppInfo.setPackageName(packageName);
//                                serviceAppInfo.setSourceDir(null);
//                                serviceAppInfo.setDataDir(applicationInfo.dataDir);
//                                serviceAppInfo.setExternalDirList(null);
//                                pinOrUnpinApp(serviceAppInfo);

                                AppInfo appInfo = new AppInfo(context);
                                appInfo.setPinned(false);
                                appInfo.setUid(applicationInfo.uid);
                                appInfo.setSystemApp(isSystemApp);
                                appInfo.setApplicationInfo(applicationInfo);
                                appInfo.setName(applicationInfo.loadLabel(pm).toString());
                                appInfo.setExternalDirList(null);
                                pinOrUnpinApp(appInfo);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            Logs.e(CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_PIN_UNPIN_UDPATE_APP)) {
                        // From HCFSMgmtReceiver's ACTION_PACKAGE_REPLACED

                        // Pin or unpin an update app according to pin_status field in uid.db
                        String packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME);
                        try {
                            PackageManager pm = getPackageManager();
                            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
//                            String sourceDir = applicationInfo.sourceDir;
//                            int lastIndex = sourceDir.lastIndexOf("/");
//                            String sourceDirWithoutApkSuffix = sourceDir.substring(0, lastIndex);
                            UidInfo uidInfo = mUidDAO.get(packageName);
                            if (uidInfo != null) {
                                boolean isPinned = uidInfo.isPinned();
//                                ServiceAppInfo serviceAppInfo = new ServiceAppInfo();
//                                serviceAppInfo.setPinned(isPinned);
//                                serviceAppInfo.setAppName(applicationInfo.loadLabel(pm).toString());
//                                serviceAppInfo.setPackageName(packageName);
//                                serviceAppInfo.setSourceDir(null);
//                                serviceAppInfo.setDataDir(applicationInfo.dataDir);
//                                serviceAppInfo.setExternalDirList(null);
//                                pinOrUnpinApp(serviceAppInfo);

                                AppInfo appInfo = new AppInfo(context);
                                appInfo.setPinned(isPinned);
                                appInfo.setUid(applicationInfo.uid);
                                appInfo.setApplicationInfo(applicationInfo);
                                appInfo.setName(applicationInfo.loadLabel(pm).toString());
                                appInfo.setExternalDirList(null);
                                pinOrUnpinApp(appInfo);
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            Logs.e(CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_REMOVE_UID_FROM_DATABASE)) {
                        // From HCFSMgmtReceiver's ACTION_PACKAGE_REMOVED

                        // Remove uid info of uninstalled app from database
                        int uid = intent.getIntExtra(HCFSMgmtUtils.INTENT_KEY_UID, -1);
                        String packageName = intent.getStringExtra(HCFSMgmtUtils.INTENT_KEY_PACKAGE_NAME);
                        if (mUidDAO.get(packageName) != null) {
                            String logMsg = "operation=" + operation + ", uid=" + uid + ", packageName=" + packageName;
                            Logs.d(CLASSNAME, "onStartCommand", logMsg);
                            mUidDAO.delete(packageName);
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_RESET_XFER)) {
                        // Reset xfer at 23:59:59 everyday
                        HCFSMgmtUtils.resetXfer();
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
                        // Send a notification to user when storage used ratio is above the value set by user
                        HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
                        if (statInfo != null) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                            String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
                            String key = SettingsFragment.PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO;
                            String storageUsedRatio = sharedPreferences.getString(key, defaultValue);

                            long occupiedSize = HCFSMgmtUtils.getOccupiedSize();
                            long rawCacheTotal = statInfo.getRawCacheTotal();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            boolean isNotified = sharedPreferences.getBoolean(SettingsFragment.PREF_LOCAL_STORAGE_USAGE_RATIO_NOTIFIED, false);
                            double pinPlusUnpinButDirtyRatio = ((double) occupiedSize / rawCacheTotal) * 100;
                            Logs.d(CLASSNAME, "onStartCommand", "occupiedSize=" + occupiedSize +
                                            ", rawCacheTotal=" + rawCacheTotal +
                                            ", pinPlusUnpinButDirty=" + pinPlusUnpinButDirtyRatio);
                            if (pinPlusUnpinButDirtyRatio >= Double.valueOf(storageUsedRatio)) {
                                if (!isNotified) {
                                    int notify_id = HCFSMgmtUtils.NOTIFY_ID_LOCAL_STORAGE_USED_RATIO;
                                    String notify_title = getString(R.string.app_name);
                                    String notify_message = String.format(getString(R.string.notify_exceed_local_storage_used_ratio), storageUsedRatio);
                                    NotificationEvent.notify(context, notify_id, notify_title, notify_message);

                                    editor.putBoolean(SettingsFragment.PREF_LOCAL_STORAGE_USAGE_RATIO_NOTIFIED, true);
                                }
                            } else {
                                if (isNotified) {
                                    editor.putBoolean(SettingsFragment.PREF_LOCAL_STORAGE_USAGE_RATIO_NOTIFIED, false);
                                }
                            }
                            editor.apply();
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_ONGOING_NOTIFICATION)) {
                        // Send an ongoing notification to show HCFS network status and storage usage
                        boolean isOnGoing = intent.getBooleanExtra(HCFSMgmtUtils.INTENT_KEY_ONGOING, false);
                        if (isOnGoing) {
                            if (mOngoingThread == null) {
                                mOngoingThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final long FIVE_MINUTES_IN_MILLISECONDS = Interval.MINUTE * 5;
                                        while (true) {
                                            try {
                                                HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
                                                if (statInfo != null) {
                                                    int connStatus = HCFSConnStatus.getConnStatus(context, statInfo);
                                                    String notifyTitle;
                                                    switch (connStatus) {
                                                        case HCFSConnStatus.TRANS_FAILED:
                                                            notifyTitle = getString(R.string.dashboard_hcfs_conn_status_failed);
                                                            break;
                                                        case HCFSConnStatus.TRANS_NOT_ALLOWED:
                                                            notifyTitle = getString(R.string.dashboard_hcfs_conn_status_not_allowed);
                                                            break;
                                                        case HCFSConnStatus.TRANS_NORMAL:
                                                            notifyTitle = getString(R.string.dashboard_hcfs_conn_status_normal);
                                                            break;
                                                        case HCFSConnStatus.TRANS_IN_PROGRESS:
                                                            notifyTitle = getString(R.string.dashboard_hcfs_conn_status_in_progress);
                                                            break;
                                                        case HCFSConnStatus.TRANS_SLOW:
                                                            notifyTitle = getString(R.string.dashboard_hcfs_conn_status_slow);
                                                            break;
                                                        default:
                                                            notifyTitle = getString(R.string.dashboard_hcfs_conn_status_normal);
                                                    }
                                                    String notifyMsg = getString(R.string.overview_used_space) + ": " + statInfo.getFormatVolUsed() + " / " + statInfo.getFormatCloudTotal();
                                                    NotificationEvent.notify(HCFSMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING, notifyTitle, notifyMsg, true);
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
                            NotificationEvent.cancel(HCFSMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
                        }
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_SILENT_SIGN_IN)) {
                        mCacheExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                final String serverClientId = MgmtCluster.getServerClientId();
                                if (serverClientId != null) {
                                    final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                                            .requestServerAuthCode(serverClientId)
                                            .requestEmail()
                                            .build();

                                    mGoogleApiClient = new GoogleApiClient.Builder(context)
                                            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                                                @Override
                                                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                                    Logs.d(CLASSNAME, "onConnectionFailed", "");
                                                    int id_notify = HCFSMgmtUtils.NOTIFY_ID_FAILED_SILENT_SIGN_IN;
                                                    String notify_title = getString(R.string.app_name);
                                                    String notify_content = "Google sign-in is failed";
                                                    NotificationEvent.notify(context, id_notify, notify_title, notify_content);
                                                }
                                            })
                                            .build();
                                    mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);

                                    OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                                    if (opr.isDone()) {
                                        Logs.d(CLASSNAME, "onStartCommand", "opr.isDone()");
                                        GoogleSignInResult result = opr.get();
                                        if (result != null && result.isSuccess()) {
                                            final GoogleSignInAccount acct = result.getSignInAccount();
                                            if (acct != null) {
                                                String serverAuthCode = acct.getServerAuthCode();
                                                Logs.w(CLASSNAME, "onStartCommand", "serverAuthCode=" + serverAuthCode);
                                                registerToMgmtCluster(context, serverAuthCode);
                                            } else {
                                                String failedMsg = "acct is null";
                                                googleAuthFailed(failedMsg);
                                            }
                                        } else {
                                            String failedMsg;
                                            if (result == null) {
                                                failedMsg = "googleSignInResult == null";
                                            } else {
                                                failedMsg = "googleSignInResult.isSuccess()=" + result.isSuccess();
                                            }
                                            googleAuthFailed(failedMsg);
                                        }
                                    } else {
                                        Logs.d(CLASSNAME, "onStartCommand", "!opr.isDone()");
                                        opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                            @Override
                                            public void onResult(@NonNull GoogleSignInResult result) {
                                                if (result.isSuccess()) {
                                                    final GoogleSignInAccount acct = result.getSignInAccount();
                                                    if (acct != null) {
                                                        String serverAuthCode = acct.getServerAuthCode();
                                                        registerToMgmtCluster(context, serverAuthCode);
                                                    } else {
                                                        String failedMsg = "acct is null";
                                                        googleAuthFailed(failedMsg);
                                                    }
                                                } else {
                                                    String failedMsg;
                                                    failedMsg = "googleSignInResult.isSuccess()=" + result.isSuccess();
                                                    googleAuthFailed(failedMsg);
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Logs.e(CLASSNAME, "onStartCommand", "serverClientId == null");
                                }
                            }
                        });
                    } else if (operation.equals(HCFSMgmtUtils.INTENT_VALUE_INSUFFICIENT_PIN_SPACE)) {
                        HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
                        if (statInfo != null) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            boolean isNotified = sharedPreferences.getBoolean(SettingsFragment.PREF_INSUFFICIENT_PIN_SPACE_NOTIFIED, false);

                            long pinTotal = statInfo.getPinTotal();
                            long pinMax = statInfo.getPinMax();
                            String notifyRatio = HCFSMgmtUtils.NOTIFY_INSUFFICIENT_PIN_PACE_RATIO;
                            double ratio = ((double) pinTotal / pinMax) * 100;
                            Logs.d(CLASSNAME, "onStartCommand", "notifyRatio=" + notifyRatio + ", ratio=" + ratio);
                            if (ratio >= Integer.valueOf(notifyRatio)) {
                                if (!isNotified) {
                                    int idNotify = HCFSMgmtUtils.NOTIFY_ID_INSUFFICIENT_PIN_SPACE;
                                    String notifyTitle = getString(R.string.app_name);
                                    String notifyContent = String.format(getString(R.string.notify_exceed_pin_used_ratio), notifyRatio);
                                    Bundle extras = new Bundle();
                                    extras.putBoolean(HCFSMgmtUtils.BUNDLE_KEY_INSUFFICIENT_PIN_SPACE, true);
                                    NotificationEvent.notify(HCFSMgmtService.this, idNotify, notifyTitle, notifyContent, extras);

                                    editor.putBoolean(SettingsFragment.PREF_INSUFFICIENT_PIN_SPACE_NOTIFIED, true);
                                }
                            } else {
                                if (isNotified) {
                                    editor.putBoolean(SettingsFragment.PREF_INSUFFICIENT_PIN_SPACE_NOTIFIED, false);
                                }
                            }
                            editor.apply();
                        }
                    }
                }
            });
        } else {
            // Service is restarted and then execute the uncompleted pin/unpin operation when user manually close app and removes it from background.
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
//            mCacheExecutor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    if (mServiceAppDAO.getCount() > 0) {
//                        List<ServiceAppInfo> infoList = mServiceAppDAO.getAll();
//                        for (final ServiceAppInfo serviceAppInfo : infoList) {
//                            pinOrUnpinApp(serviceAppInfo);
//                            mServiceAppDAO.delete(serviceAppInfo);
//                        }
//                    }
//                }
//            });
        }
        return super.onStartCommand(intent, flags, startId);
        // return START_REDELIVER_INTENT;
    }

    private void mgmtAuthOrRegisterFailed() {
        Logs.e(CLASSNAME, "mgmtAuthOrRegisterFailed", null);

        if (MgmtCluster.isNeedToRetryAgain()) {
            Intent intentService = new Intent(HCFSMgmtService.this, HCFSMgmtService.class);
            intentService.putExtra(HCFSMgmtUtils.INTENT_KEY_OPERATION, HCFSMgmtUtils.INTENT_VALUE_SILENT_SIGN_IN);
            startService(intentService);
            Logs.e(CLASSNAME, "mgmtAuthOrRegisterFailed", "Authentication failed, retry again");
        } else {
            int id_notify = HCFSMgmtUtils.NOTIFY_ID_FAILED_SILENT_SIGN_IN;
            String notify_title = getString(R.string.app_name);
            String notify_content = getString(R.string.auth_at_bootup_auth_mgmt_failed);
            NotificationEvent.notify(HCFSMgmtService.this, id_notify, notify_title, notify_content);

            Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            Logs.e(CLASSNAME, "mgmtAuthOrRegisterFailed", "status=" + status);
                        }
                    });

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HCFSMgmtService.this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, false);
            editor.apply();
        }
        mGoogleApiClient.disconnect();
        HCFSConfig.stopSyncToCloud();

        NotificationEvent.cancel(HCFSMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
    }

    private void googleAuthFailed(String failedMsg) {
        Logs.e(CLASSNAME, "googleAuthFailed", failedMsg);

        int id_notify = HCFSMgmtUtils.NOTIFY_ID_FAILED_SILENT_SIGN_IN;
        String notify_title = getString(R.string.app_name);
        String notify_content = getString(R.string.auth_at_bootup_auth_google_failed);
        NotificationEvent.notify(HCFSMgmtService.this, id_notify, notify_title, notify_content);

        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Logs.e(CLASSNAME, "googleAuthFailed", "status=" + status);
                    }
                });
        mGoogleApiClient.disconnect();
        HCFSConfig.stopSyncToCloud();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(HCFSMgmtService.this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, false);
        editor.apply();

        NotificationEvent.cancel(HCFSMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
    }

    private void registerToMgmtCluster(final Context context, String serverAuthCode) {
        final MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam();
        authParam.setAuthCode(serverAuthCode);
        authParam.setAuthBackend(MgmtCluster.GOOGLE_AUTH_BACKEND);
        authParam.setImei(HCFSMgmtUtils.getEncryptedDeviceImei(HCFSMgmtUtils.getDeviceImei(HCFSMgmtService.this)));
        authParam.setVendor(Build.BRAND);
        authParam.setModel(Build.MODEL);
        authParam.setAndroidVersion(Build.VERSION.RELEASE);
        authParam.setHcfsVersion("1.0.1");

        MgmtCluster.plusRetryCount();
        MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
        authProxy.setOnAuthListener(new MgmtCluster.AuthListener() {
            @Override
            public void onAuthSuccessful(AuthResultInfo authResultInfo) {
                MgmtCluster.RegisterProxy mgmtRegister = new MgmtCluster.RegisterProxy(authParam, authResultInfo.getToken());
                mgmtRegister.setOnRegisterListener(new MgmtCluster.RegisterListener() {
                    @Override
                    public void onRegisterSuccessful(final RegisterResultInfo registerResultInfo) {
                        mCacheExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Set arkflex token to hcfs
                                // registerResultInfo.getStorageAccessToken();
                                boolean failed = HCFSConfig.storeHCFSConfig(registerResultInfo);
                                if (failed) {
                                    HCFSConfig.resetHCFSConfig();
                                }
                            }
                        });

                        mGoogleApiClient.disconnect();
                        MgmtCluster.resetRetryCount();
                        HCFSConfig.startSyncToCloud();

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, true);
                        editor.apply();
                    }

                    @Override
                    public void onRegisterFailed(RegisterResultInfo registerResultInfo) {
                        mgmtAuthOrRegisterFailed();
                    }

                });
                mgmtRegister.register();
            }

            @Override
            public void onAuthFailed(AuthResultInfo authResultInfo) {
                mgmtAuthOrRegisterFailed();
            }

        });
        authProxy.auth();

    }

    private void pinOrUnpinApp(AppInfo info) {
        Logs.d(CLASSNAME, "pinOrUnpinApp", info.getName());
        pinOrUnpinApp(info, PinType.NORMAL);
    }

    private void pinOrUnpinApp(AppInfo info, int pinType) {
        Logs.d(CLASSNAME, "pinOrUnpinApp", info.getName());
        final boolean isPinned = info.isPinned();
        if (isPinned) {
            if (!HCFSMgmtUtils.pinApp(info, pinType)) {
                handleAppFailureOfPinOrUnpin(info, getString(R.string.notify_pin_app_failure));
            }
        } else {
            if (!HCFSMgmtUtils.unpinApp(info)) {
                handleAppFailureOfPinOrUnpin(info, getString(R.string.notify_unpin_app_failure));
            }
        }
    }

    public void pinOrUnpinApp(AppInfo info, @NonNull IPinUnpinListener listener) {
        final boolean isPinned = info.isPinned();
        if (isPinned) {
            if (!HCFSMgmtUtils.pinApp(info)) {
                handleAppFailureOfPinOrUnpin(info, getString(R.string.notify_pin_app_failure));
                info.setPinned(!isPinned);
                listener.OnPinUnpinFailed(info);
            }
        } else {
            if (!HCFSMgmtUtils.unpinApp(info)) {
                handleAppFailureOfPinOrUnpin(info, getString(R.string.notify_unpin_app_failure));
                info.setPinned(!isPinned);
                listener.OnPinUnpinFailed(info);
            }
        }
    }

    private void handleAppFailureOfPinOrUnpin(AppInfo info, String notifyMsg) {
        Logs.d(CLASSNAME, "pinOrUnpinFailure", info.getName());

        // Pin/Unpin failed, reset to original status.
        if (info.isPinned()) {
            HCFSMgmtUtils.unpinApp(info);
        } else {
            HCFSMgmtUtils.pinApp(info);
        }

        // Notify user pin/unpin failed
        int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
        String notify_title = getString(R.string.app_name);
        String notify_message = notifyMsg + ": " + info.getName();
        NotificationEvent.notify(this, notify_id, notify_title, notify_message);
    }

    private void pinOrUnpinDataTypeFile() {
        Logs.d(CLASSNAME, "pinOrUnpinDataTypeFile", null);

        ArrayList<String> notifyMessageList = new ArrayList<>();

        DataTypeInfo imageTypeInfo = mDataTypeDAO.get(DataTypeDAO.DATA_TYPE_IMAGE);
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
                        if (HCFSMgmtUtils.pinFileOrDirectory(path) != 0) {
                            imgFailedToPinCount++;
                        }
                    }
                    if (imgFailedToPinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_mgmt_service_image_failed_to_pin) + ": " + imgFailedToPinCount);
                    }
                } else {
                    int imgFailedToUnpinCount = 0;
                    for (String path : imagePaths) {
                        if (HCFSMgmtUtils.unpinFileOrDirectory(path) != 0) {
                            imgFailedToUnpinCount++;
                        }
                    }
                    if (imgFailedToUnpinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_mgmt_service_image_failed_to_unpin) + ": " + imgFailedToUnpinCount);
                    }
                }
                imageTypeInfo.setDateUpdated(processTimeSeconds);
                boolean isSuccess = mDataTypeDAO.update(dataType, imageTypeInfo, DataTypeDAO.DATE_UPDATED_COLUMN);
                if (!isSuccess) {
                    Logs.e(CLASSNAME, "pinOrUnpinFileOrDirectory", "msg=Failed to upate datatype table, dataType=" + dataType
                            + ", column=" + DataTypeDAO.DATE_UPDATED_COLUMN + ", dateUpdated=" + imageTypeInfo.getDateUpdated());
                }
            }
        }

        DataTypeInfo videoTypeInfo = mDataTypeDAO.get(DataTypeDAO.DATA_TYPE_VIDEO);
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
                        if (HCFSMgmtUtils.pinFileOrDirectory(path) != 0) {
                            videoFailedToPinCount++;
                        }
                    }
                    if (videoFailedToPinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_mgmt_service_video_failed_to_pin) + ": " + videoFailedToPinCount);
                    }
                } else {
                    int videoFailedToUnpinCount = 0;
                    for (String path : videoPaths) {
                        if (HCFSMgmtUtils.unpinFileOrDirectory(path) != 0) {
                            videoFailedToUnpinCount++;
                        }
                    }
                    if (videoFailedToUnpinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_mgmt_service_video_failed_to_unpin) + ": " + videoFailedToUnpinCount);
                    }
                }
                videoTypeInfo.setDateUpdated(processTimeSeconds);
                boolean isSuccess = mDataTypeDAO.update(dataType, videoTypeInfo, DataTypeDAO.DATE_UPDATED_COLUMN);
                if (!isSuccess) {
                    Logs.e(CLASSNAME, "pinOrUnpinFileOrDirectory", "msg=Failed to upate datatype table, dataType=" + dataType
                            + ", column=" + DataTypeDAO.DATE_UPDATED_COLUMN + ", dateUpdated=" + videoTypeInfo.getDateUpdated());
                }
            }
        }

        DataTypeInfo audioTypeInfo = mDataTypeDAO.get(DataTypeDAO.DATA_TYPE_AUDIO);
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
                        if (HCFSMgmtUtils.pinFileOrDirectory(path) != 0) {
                            audioFailedToPinCount++;
                        }
                    }
                    if (audioFailedToPinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_mgmt_service_audio_failed_to_pin) + ": " + audioFailedToPinCount);
                    }
                } else {
                    int audioFailedToUnpinCount = 0;
                    for (String path : audioPaths) {
                        if (HCFSMgmtUtils.unpinFileOrDirectory(path) != 0) {
                            audioFailedToUnpinCount++;
                        }
                    }
                    if (audioFailedToUnpinCount != 0) {
                        notifyMessageList.add(getString(R.string.hcfs_mgmt_service_audio_failed_to_unpin) + ": " + audioFailedToUnpinCount);
                    }
                }
                audioTypeInfo.setDateUpdated(processTimeSeconds);
                boolean isSuccess = mDataTypeDAO.update(dataType, audioTypeInfo, DataTypeDAO.DATE_UPDATED_COLUMN);
                if (!isSuccess) {
                    Logs.e(CLASSNAME, "pinOrUnpinFileOrDirectory", "msg=Failed to upate datatype table, dataType=" + dataType
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
            NotificationEvent.notify(this, notify_id, notify_title, notify_message.toString());
        }
    }

    private void pinOrUnpinFileOrDirectory(ServiceFileDirInfo info) {
        String filePath = info.getFilePath();
        Logs.d(CLASSNAME, "pinOrUnpinFileOrDirectory", "filePath=" + filePath);
        boolean isPinned = info.isPinned();
        if (isPinned) {
            int code = HCFSMgmtUtils.pinFileOrDirectory(filePath);
            boolean isSuccess = (code == 0);
            if (!isSuccess) {
                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
                String notify_title = getString(R.string.app_name);
                String notify_message = getString(R.string.notify_pin_file_dir_failure) + "： " + filePath + " (errorCode=" + code + ")";
                NotificationEvent.notify(this, notify_id, notify_title, notify_message);
            }
        } else {
            boolean isSuccess = (HCFSMgmtUtils.unpinFileOrDirectory(filePath) == 0);
            if (!isSuccess) {
                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
                String notify_title = getString(R.string.app_name);
                String notify_message = getString(R.string.notify_unpin_file_dir_failure) + "： " + filePath;
                NotificationEvent.notify(this, notify_id, notify_title, notify_message);
            }
        }
    }

    public void pinOrUnpinFileDirectory(FileDirInfo info, IPinUnpinListener listener) {
        ServiceFileDirInfo serviceFileDirInfo = new ServiceFileDirInfo();
        serviceFileDirInfo.setPinned(info.isPinned());
        serviceFileDirInfo.setFilePath(info.getFilePath());
        mServiceFileDirDAO.insert(serviceFileDirInfo);
        String filePath = info.getFilePath();
        Logs.d(CLASSNAME, "pinOrUnpinFileOrDirectory",
                "filePath=" + filePath + ", threadName=" + Thread.currentThread().getName());
        boolean isPinned = info.isPinned();
        if (isPinned) {
            int code = HCFSMgmtUtils.pinFileOrDirectory(filePath);
            boolean isSuccess = (code == 0);
            if (!isSuccess) {
                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
                String notify_title = getString(R.string.app_name);
                String notify_message = getString(R.string.notify_pin_file_dir_failure) + "： " + filePath + " (errorCode=" + code + ")";
                NotificationEvent.notify(this, notify_id, notify_title, notify_message);

                info.setPinned(!info.isPinned());
                listener.OnPinUnpinFailed(info);
            }
        } else {
            boolean isSuccess = (HCFSMgmtUtils.unpinFileOrDirectory(filePath) == 0);
            if (!isSuccess) {
                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
                String notify_title = getString(R.string.app_name);
                String notify_message = getString(R.string.notify_unpin_file_dir_failure) + "： " + filePath;
                NotificationEvent.notify(this, notify_id, notify_title, notify_message);

                info.setPinned(!info.isPinned());
                listener.OnPinUnpinFailed(info);
            }
        }
        mServiceFileDirDAO.delete(serviceFileDirInfo.getFilePath());
    }

}
