package com.hopebaytech.hcfsmgmt.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.SettingsDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.RestoreFailedFragment;
import com.hopebaytech.hcfsmgmt.fragment.RestoreReadyFragment;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.GetDeviceInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceFileDirInfo;
import com.hopebaytech.hcfsmgmt.info.SettingsInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.info.UnlockDeviceInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IMgmtBinder;
import com.hopebaytech.hcfsmgmt.interfaces.IPinUnpinListener;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.main.MainApplication;
import com.hopebaytech.hcfsmgmt.utils.DisplayTypeFactory;
import com.hopebaytech.hcfsmgmt.utils.FactoryResetUtils;
import com.hopebaytech.hcfsmgmt.utils.GoogleSilentAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSEvent;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.MgmtPollingUtils;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.PinType;
import com.hopebaytech.hcfsmgmt.utils.RestoreStatus;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeraMgmtService extends Service {

    private final String CLASSNAME = getClass().getSimpleName();

    private final IBinder mBinder = new MgmtServiceBinder();

    private ExecutorService mCacheExecutor;
    private Thread mOngoingThread;
    private UidDAO mUidDAO;
    private DataTypeDAO mDataTypeDAO;
    private SettingsDAO mSettingsDAO;

    private Context mContext;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MgmtServiceBinder extends Binder implements IMgmtBinder {
        @Override
        public TeraMgmtService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TeraMgmtService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mCacheExecutor = Executors.newCachedThreadPool();
        mUidDAO = UidDAO.getInstance(this);
        mDataTypeDAO = DataTypeDAO.getInstance(this);
        mSettingsDAO = SettingsDAO.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            Logs.d(CLASSNAME, "onStartCommand", "action=" + action);
            mCacheExecutor.execute(new Runnable() {
                public void run() {
                    if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                        addUidAndPinSysApp();
                        HCFSMgmtUtils.updateAppExternalDir(TeraMgmtService.this);
                    } else if (action.equals(TeraIntent.ACTION_ADD_UID_TO_DB_AND_UNPIN_USER_APP)) {
                        addUidAndUnpinUserApp(intent);
                    } else if (action.equals(TeraIntent.ACTION_PIN_UNPIN_UDPATED_APP)) {
                        pinUnpinAgainWhenAppUpdated(intent);
                    } else if (action.equals(TeraIntent.ACTION_REMOVE_UID_FROM_DB)) {
                        removeUidFromDatabase(intent, action);
                    } else if (action.equals(TeraIntent.ACTION_RESET_DATA_XFER)) {
                        HCFSMgmtUtils.resetXfer();
                    } else if (action.equals(TeraIntent.ACTION_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
                        notifyLocalStorageUsedRatio();
                    } else if (action.equals(TeraIntent.ACTION_ONGOING_NOTIFICATION)) {
                        startOngoingNotificationService(intent);
                    } else if (action.equals(TeraIntent.ACTION_CHECK_DEVICE_STATUS)) {
                        checkDeviceStatus();
                    } else if (action.equals(TeraIntent.ACTION_NOTIFY_INSUFFICIENT_PIN_SPACE)) {
                        notifyInsufficientPinSpace();
                    } else if (action.equals(TeraIntent.ACTION_UPDATE_EXTERNAL_APP_DIR)) {
                        HCFSMgmtUtils.updateAppExternalDir(TeraMgmtService.this);
                    } else if (action.equals(TeraIntent.ACTION_TOKEN_EXPIRED)) {
                        checkTokenExpiredCause();
                    } else if (action.equals(TeraIntent.ACTION_EXCEED_PIN_MAX)) {
                        notifyUserExceedPinMax();
                    } else if (action.equals(TeraIntent.ACTION_RESTORE_STAGE_1)) {
                        handleStage1RestoreEvent(intent);
                    } else if (action.equals(TeraIntent.ACTION_RESTORE_STAGE_2)) {
                        handleStage2RestoreEvent(intent);
                    } else if (action.equals(TeraIntent.ACTION_MINI_RESTORE_REBOOT_SYSTEM)) {
                        RestoreReadyFragment.rebootSystemForStage2(mContext);
                    } else if (action.equals(TeraIntent.ACTION_CHECK_RESTORE_STATUS)) {
                        checkRestoreStatus();
                    } else if (action.equals(TeraIntent.ACTION_FACTORY_RESET)) {
                        FactoryResetUtils.reset(mContext);
                    } else if (action.equals(TeraIntent.ACTION_RETRY_RESTORE_WHEN_CONN_FAILED)) {
                        openPreparingOrRestoringPage();
                    }

                }
            });
        }

        // return START_STICKY instead of START_REDELIVER_INTENT. If the service has died, the
        // restarted service won't receive intent such as BOOT_COMPLETED, CHECK_RESTORE_STATUS and
        // PIN_UNPIN_UPDATED_APP. It prevents from doing unexpected action again.
        return START_STICKY;
    }

    private boolean pinOrUnpinApp(AppInfo info) {
        Logs.d(CLASSNAME, "pinOrUnpinApp", info.getName());
        return pinOrUnpinApp(info, PinType.NORMAL);
    }

    private boolean pinOrUnpinApp(AppInfo info, int pinType) {
        Logs.d(CLASSNAME, "pinOrUnpinApp", info.getName());
        boolean isSuccess = true;
        final boolean isPinned = info.isPinned();
        if (isPinned) {
            if (!HCFSMgmtUtils.pinApp(info, pinType)) {
                isSuccess = false;
                if (pinType != PinType.PRIORITY) {
                    revertPinStatus(info, getString(R.string.notify_pin_app_failure));
                }
            }
        } else {
            if (!HCFSMgmtUtils.unpinApp(info)) {
                isSuccess = false;
                if (pinType != PinType.PRIORITY) {
                    revertPinStatus(info, getString(R.string.notify_unpin_app_failure));
                }
            }
        }
        return isSuccess;
    }

    public void pinOrUnpinApp(AppInfo info, @NonNull IPinUnpinListener listener) {
        if (info.isPinned()) {
            if (HCFSMgmtUtils.pinApp(info)) {
                listener.onPinUnpinSuccessful(info);
            } else {
                revertPinStatus(info, getString(R.string.notify_pin_app_failure));
                listener.onPinUnpinFailed(info);
            }
        } else {
            if (HCFSMgmtUtils.unpinApp(info)) {
                listener.onPinUnpinSuccessful(info);
            } else {
                revertPinStatus(info, getString(R.string.notify_unpin_app_failure));
                listener.onPinUnpinFailed(info);
            }
        }
    }

    private void revertPinStatus(AppInfo info, String notifyMsg) {
        Logs.d(CLASSNAME, "revertPinStatus", info.getName());

        // Pin/Unpin failed, reset to original status.
        if (info.isPinned()) {
            HCFSMgmtUtils.unpinApp(info);
        } else {
            HCFSMgmtUtils.pinApp(info);
        }
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

    public void pinOrUnpinFileDirectory(FileDirInfo info, IPinUnpinListener listener) {
        ServiceFileDirInfo serviceFileDirInfo = new ServiceFileDirInfo();
        serviceFileDirInfo.setPinned(info.isPinned());
        serviceFileDirInfo.setFilePath(info.getFilePath());
        String filePath = info.getFilePath();
        Logs.d(CLASSNAME, "pinOrUnpinFileOrDirectory", "filePath=" + filePath);
        boolean isPinned = info.isPinned();
        if (isPinned) {
            int code = HCFSMgmtUtils.pinFileOrDirectory(filePath);
            boolean isSuccess = (code == 0);
            if (!isSuccess) {
                listener.onPinUnpinFailed(info);
            } else {
                listener.onPinUnpinSuccessful(info);
            }
        } else {
            boolean isSuccess = (HCFSMgmtUtils.unpinFileOrDirectory(filePath) == 0);
            if (!isSuccess) {
                listener.onPinUnpinFailed(info);
            } else {
                listener.onPinUnpinSuccessful(info);
            }
        }
    }

    private void doActionAccordingToDeviceStatus(final String jwtToken) {
        final String imei = HCFSMgmtUtils.getDeviceImei(TeraMgmtService.this);
        MgmtCluster.GetDeviceServiceInfoProxy getDeviceServiceInfoProxy = new MgmtCluster.GetDeviceServiceInfoProxy(jwtToken, imei);
        getDeviceServiceInfoProxy.setOnGetDeviceServiceInfoListener(new MgmtCluster.GetDeviceServiceInfoProxy.OnGetDeviceServiceInfoListener() {
            @Override
            public void onGetDeviceServiceInfoSuccessful(final DeviceServiceInfo deviceServiceInfo) {
                String state = deviceServiceInfo.getState();
                DeviceServiceInfo.Backend backend = deviceServiceInfo.getBackend();
                DeviceServiceInfo.Piggyback piggyback = deviceServiceInfo.getPiggyback();
                if (state.equals(GetDeviceInfo.State.ACTIVATED)) {
                    String url = backend.getUrl();
                    String token = backend.getToken();
                    HCFSMgmtUtils.setSwiftToken(url, token);
                } else {
                    String category = piggyback.getCategory();
                    switch (category) {
                        // Device is not transferred completely, revert device status to "activated" status
                        case GetDeviceInfo.Category.TX_WAITING:
                            MgmtCluster.UnlockDeviceProxy unlockDeviceProxy = new MgmtCluster.UnlockDeviceProxy(jwtToken, imei);
                            unlockDeviceProxy.setOnUnlockDeviceListener(new MgmtCluster.UnlockDeviceProxy.OnUnlockDeviceListener() {
                                @Override
                                public void onUnlockDeviceSuccessful(UnlockDeviceInfo unlockDeviceInfo) {
                                    Logs.d(CLASSNAME, "onUnlockDeviceSuccessful", null);
                                }

                                @Override
                                public void onUnlockDeviceFailed(UnlockDeviceInfo unlockDeviceInfo) {
                                    Logs.e(CLASSNAME, "onUnlockDeviceFailed", null);
                                }
                            });
                            unlockDeviceProxy.unlock();
                            break;
                        // Device is already transferred, execute factory reset
                        case GetDeviceInfo.Category.UNREGISTERED:
                            FactoryResetUtils.reset(TeraMgmtService.this);
                            break;
                    }
                }
            }

            @Override
            public void onGetDeviceServiceInfoFailed(DeviceServiceInfo deviceServiceInfo) {
                Logs.e(CLASSNAME, "onGetDeviceServiceInfoFailed", null);
                int id_notify = HCFSMgmtUtils.NOTIFY_ID_CHECK_DEVICE_STATUS;
                String notify_title = getString(R.string.app_name);
                String notify_content = getString(R.string.auth_at_bootup_auth_get_device_info);
                NotificationEvent.notify(TeraMgmtService.this, id_notify, notify_title, notify_content);
            }
        });
        getDeviceServiceInfoProxy.get();
    }

    private void checkDeviceStatus() {
        final String serverClientId = MgmtCluster.getServerClientId();
        GoogleSilentAuthProxy googleAuthProxy = new GoogleSilentAuthProxy(TeraMgmtService.this, serverClientId,
                new GoogleSilentAuthProxy.OnAuthListener() {
                    @Override
                    public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {
                        String serverAuthCode = result.getSignInAccount().getServerAuthCode();
                        MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam(serverAuthCode);
                        MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                        authProxy.setOnAuthListener(new MgmtCluster.OnAuthListener() {
                            @Override
                            public void onAuthSuccessful(AuthResultInfo authResultInfo) {
                                String jwtToken = authResultInfo.getToken();
                                doActionAccordingToDeviceStatus(jwtToken);
                            }

                            @Override
                            public void onAuthFailed(AuthResultInfo authResultInfo) { // Mmgt auth failed
                                Logs.d(CLASSNAME, "GoogleSilentAuthProxy", "onAuthFailed", "authResultInfo=" + authResultInfo.toString());
                                if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                                    TeraCloudConfig.stopSyncToCloud();
                                    NotificationEvent.cancel(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);

                                    int flag = NotificationEvent.FLAG_OPEN_APP;
                                    int id_notify = HCFSMgmtUtils.NOTIFY_ID_CHECK_DEVICE_STATUS;
                                    String notify_title = getString(R.string.app_name);
                                    String notify_content = getString(R.string.auth_at_bootup_auth_failed_mgmt_auth);
                                    Bundle extras = new Bundle();
                                    extras.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
                                    NotificationEvent.notify(TeraMgmtService.this, id_notify, notify_title, notify_content, flag, extras);

                                    TeraAppConfig.disableApp(TeraMgmtService.this);

                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TeraMgmtService.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
                                    editor.apply();
                                }
                            }
                        });
                        authProxy.auth();
                    }

                    @Override
                    public void onAuthFailed() { // Google silent auth failed
                        TeraCloudConfig.stopSyncToCloud();
                        NotificationEvent.cancel(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);

                        int flag = NotificationEvent.FLAG_OPEN_APP;
                        int id_notify = HCFSMgmtUtils.NOTIFY_ID_CHECK_DEVICE_STATUS;
                        String notify_title = getString(R.string.app_name);
                        String notify_content = getString(R.string.auth_at_bootup_auth_failed_google_auth);
                        Bundle extras = new Bundle();
                        extras.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
                        NotificationEvent.notify(TeraMgmtService.this, id_notify, notify_title, notify_content, flag, extras);

                        TeraAppConfig.disableApp(TeraMgmtService.this);

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TeraMgmtService.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
                        editor.apply();
                    }

                });
        googleAuthProxy.auth();
    }

    private void notifyInsufficientPinSpace() {
        HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
        if (statInfo != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TeraMgmtService.this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            boolean isNotified = sharedPreferences.getBoolean(SettingsFragment.PREF_INSUFFICIENT_PIN_SPACE_NOTIFIED, false);

            long pinTotal = statInfo.getPinTotal();
            long pinMax = statInfo.getPinMax();
            String notifyRatio = HCFSMgmtUtils.NOTIFY_INSUFFICIENT_PIN_PACE_RATIO;
            double ratio = ((double) pinTotal / pinMax) * 100;
            Logs.d(CLASSNAME, "onStartCommand", "notifyRatio=" + notifyRatio + ", ratio=" + ratio);
            if (ratio >= Integer.valueOf(notifyRatio)) {
                if (!isNotified) {
                    int flag = NotificationEvent.FLAG_OPEN_APP;
                    int idNotify = HCFSMgmtUtils.NOTIFY_ID_INSUFFICIENT_PIN_SPACE;
                    String notifyTitle = getString(R.string.app_name);
                    String notifyContent = String.format(getString(R.string.notify_exceed_pin_used_ratio), notifyRatio);
                    Bundle extras = new Bundle();
                    extras.putInt(HCFSMgmtUtils.BUNDLE_KEY_VIEW_PAGER_INDEX, 1 /* APP/FILE page */);
                    NotificationEvent.notify(TeraMgmtService.this, idNotify, notifyTitle, notifyContent, flag, extras);

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

    /**
     * Send an ongoing notification to show Tera connection status and used space, then continually
     * update the notification information util Tera app is back to foreground.
     */
    private void startOngoingNotificationService(Intent intent) {
        boolean isOnGoing = intent.getBooleanExtra(TeraIntent.KEY_ONGOING, false);
        if (!isOnGoing) {
            if (mOngoingThread != null) {
                mOngoingThread.interrupt();
                mOngoingThread = null;
            }
            NotificationEvent.cancel(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
            return;
        }

        if (mOngoingThread != null) {
            return;
        }
        mOngoingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final long FIVE_MINUTES_IN_MILLISECONDS = Interval.MINUTE * 5;
                while (true) {
                    try {
                        HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
                        if (statInfo == null) {
                            Thread.sleep(FIVE_MINUTES_IN_MILLISECONDS);
                            continue;
                        }

                        int connStatus = HCFSConnStatus.getConnStatus(TeraMgmtService.this, statInfo);
                        String notifyTitle;
                        switch (connStatus) {
                            case HCFSConnStatus.TRANS_FAILED:
                                notifyTitle = getString(R.string.overview_hcfs_conn_status_failed);
                                break;
                            case HCFSConnStatus.TRANS_NOT_ALLOWED:
                                notifyTitle = getString(R.string.overview_hcfs_conn_status_not_allowed);
                                break;
                            case HCFSConnStatus.TRANS_NORMAL:
                                notifyTitle = getString(R.string.overview_hcfs_conn_status_normal);
                                break;
                            case HCFSConnStatus.TRANS_IN_PROGRESS:
                                notifyTitle = getString(R.string.overview_hcfs_conn_status_in_progress);
                                break;
                            case HCFSConnStatus.TRANS_SLOW:
                                notifyTitle = getString(R.string.overview_hcfs_conn_status_slow);
                                break;
                            default:
                                notifyTitle = getString(R.string.overview_hcfs_conn_status_normal);
                        }
                        String notifyMsg = getString(R.string.overview_used_space) + ": " + statInfo.getFormatTeraUsed() + " / " + statInfo.getFormatTeraTotal();
                        int flag = NotificationEvent.FLAG_ON_GOING | NotificationEvent.FLAG_OPEN_APP;
                        NotificationEvent.notify(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING, notifyTitle, notifyMsg, flag);

                        Thread.sleep(FIVE_MINUTES_IN_MILLISECONDS);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        mOngoingThread.start();
    }

    /**
     * Send a notification to user when storage used ratio exceeds the threshold set by user
     */
    private void notifyLocalStorageUsedRatio() {
        HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
        if (statInfo != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

            String defaultValue = getResources().getStringArray(R.array.pref_notify_local_storage_used_ratio_value)[0];
            String storageUsedRatio = defaultValue;

            SettingsInfo settingsInfo = mSettingsDAO.get(SettingsFragment.PREF_NOTIFY_LOCAL_STORAGE_USAGE_RATIO);
            if (settingsInfo != null) {
                storageUsedRatio = settingsInfo.getValue();
            }

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
                    NotificationEvent.notify(mContext, notify_id, notify_title, notify_message);

                    editor.putBoolean(SettingsFragment.PREF_LOCAL_STORAGE_USAGE_RATIO_NOTIFIED, true);
                }
            } else {
                if (isNotified) {
                    editor.putBoolean(SettingsFragment.PREF_LOCAL_STORAGE_USAGE_RATIO_NOTIFIED, false);
                }
            }
            editor.apply();
        }
    }

    /**
     * Remove uid info of uninstalled app from database, triggered by HCFSMgmtReceiver's ACTION_PACKAGE_REMOVED
     */
    private void removeUidFromDatabase(Intent intent, String action) {
        int uid = intent.getIntExtra(TeraIntent.KEY_UID, -1);
        String packageName = intent.getStringExtra(TeraIntent.KEY_PACKAGE_NAME);
        if (mUidDAO.get(packageName) != null) {
            String logMsg = "action=" + action + ", uid=" + uid + ", packageName=" + packageName;
            Logs.d(CLASSNAME, "onStartCommand", logMsg);
            mUidDAO.delete(packageName);
        }
    }

    /**
     * Pin/unpin an app again after app updated according to pin_status field in uid.db, triggered
     * by HCFSMgmtReceiver's ACTION_PACKAGE_REPLACED
     */
    private void pinUnpinAgainWhenAppUpdated(Intent intent) {
        String packageName = intent.getStringExtra(TeraIntent.KEY_PACKAGE_NAME);
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            UidInfo uidInfo = mUidDAO.get(packageName);
            if (uidInfo != null) {
                boolean isPinned = uidInfo.isPinned();

                AppInfo appInfo = new AppInfo(mContext);
                appInfo.setPinned(isPinned);
                appInfo.setUid(applicationInfo.uid);
                appInfo.setApplicationInfo(applicationInfo);
                appInfo.setName(applicationInfo.loadLabel(pm).toString());
                appInfo.setExternalDirList(null);
                if (HCFSMgmtUtils.isSystemPackage(applicationInfo)) {
                    pinOrUnpinApp(appInfo, PinType.PRIORITY);
                } else {
                    pinOrUnpinApp(appInfo);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logs.e(CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
        }
    }

    /**
     * Add uid info of new installed app to database and unpin user app on /data/data and /data/app,
     * triggered by HCFSMgmtReceiver's ACTION_PACKAGE_ADDED
     */
    private void addUidAndUnpinUserApp(Intent intent) {
        // Add uid info of new installed app to database
        int uid = intent.getIntExtra(TeraIntent.KEY_UID, -1);
        String packageName = intent.getStringExtra(TeraIntent.KEY_PACKAGE_NAME);
        if (mUidDAO.get(packageName) == null) {
            final boolean isPinned = false;
            final boolean isSystemApp = false;
            mUidDAO.insert(new UidInfo(isPinned, isSystemApp, uid, packageName));
        }

        // Unpin user app on /data/data and /data/app
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            boolean isSystemApp = HCFSMgmtUtils.isSystemPackage(applicationInfo);
            if (!isSystemApp) {
                AppInfo appInfo = new AppInfo(mContext);
                appInfo.setPinned(false);
                appInfo.setUid(applicationInfo.uid);
                appInfo.setSystemApp(isSystemApp);
                appInfo.setApplicationInfo(applicationInfo);
                appInfo.setName(applicationInfo.loadLabel(pm).toString());
                appInfo.setExternalDirList(null);
                if (HCFSMgmtUtils.isSystemPackage(applicationInfo)) {
                    pinOrUnpinApp(appInfo, PinType.PRIORITY);
                } else {
                    pinOrUnpinApp(appInfo);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logs.e(CLASSNAME, "onStartCommand", Log.getStackTraceString(e));
        }
    }

    /**
     * Add NEW uid info to database when system boot up and pin /storage/emulated/0/Android folder,
     * and then pin system app, triggered by HCFSMgmtReceiver's ACTION_BOOT_COMPLETED
     */
    private void addUidAndPinSysApp() {
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
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
        boolean isSuccess = true;
        ArrayList<ItemInfo> itemInfoList = DisplayTypeFactory.getListOfInstalledApps(mContext, DisplayTypeFactory.APP_SYSTEM);
        for (ItemInfo itemInfo : itemInfoList) {
            AppInfo appInfo = (AppInfo) itemInfo;
            appInfo.setPinned(true);
            boolean result = pinOrUnpinApp(appInfo, PinType.PRIORITY);
            if (!result) {
                isSuccess = false;
            }
        }

        if (!isSuccess) {
            // Notify user pin/unpin system app failed
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, R.string.notify_pin_unpin_system_app_failed, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void checkTokenExpiredCause() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            return;
        }

        MgmtCluster.getJwtToken(this, new MgmtCluster.OnFetchJwtTokenListener() {
            @Override
            public void onFetchSuccessful(String jwtToken) {
                String imei = HCFSMgmtUtils.getDeviceImei(TeraMgmtService.this);
                MgmtCluster.GetDeviceServiceInfoProxy proxy =
                        new MgmtCluster.GetDeviceServiceInfoProxy(jwtToken, imei);
                proxy.setOnGetDeviceServiceInfoListener(new MgmtCluster.GetDeviceServiceInfoProxy.
                        OnGetDeviceServiceInfoListener() {
                    @Override
                    public void onGetDeviceServiceInfoSuccessful(DeviceServiceInfo deviceServiceInfo) {
                        String state = deviceServiceInfo.getState();
                        DeviceServiceInfo.Backend backend = deviceServiceInfo.getBackend();
                        if (state.equals(GetDeviceInfo.State.ACTIVATED)) {
                            // Refresh backend token
                            String url = backend.getUrl();
                            String token = backend.getToken();
                            HCFSMgmtUtils.setSwiftToken(url, token);
                        } else { // Other situation is handled by MgmtPollingService.
                            // Stop the the running service if exists.
                            MgmtPollingUtils.stopPollingService(TeraMgmtService.this, MgmtPollingService.class);

                            // Start a new polling service
                            MgmtPollingUtils.startPollingService(TeraMgmtService.this,
                                    Interval.CHECK_DEVICE_SERVICE_WHEN_TOKEN_EXPIRED, MgmtPollingService.class);
                        }
                    }

                    @Override
                    public void onGetDeviceServiceInfoFailed(DeviceServiceInfo deviceServiceInfo) {
                        Logs.e(CLASSNAME, "onGetDeviceInfoFailed", null);
                    }
                });
                proxy.get();
            }

            @Override
            public void onFetchFailed() {
                Logs.e(CLASSNAME, "onFetchFailed", null);
            }
        });
    }

    private void handleStage1RestoreEvent(Intent intent) {
        Logs.d(CLASSNAME, "handleStage1RestoreEvent", null);

        if (MainApplication.Foreground.get().isForeground()) {
            Logs.d(CLASSNAME, "handleStage1RestoreEvent", "Application is not in foreground.");
            return;
        }

        int status = RestoreStatus.NONE;
        int errorCode = intent.getIntExtra(TeraIntent.KEY_RESTORE_ERROR_CODE, -1);
        Logs.d(CLASSNAME, "handleStage1RestoreEvent", "errorCode=" + errorCode);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (errorCode) {
            case 0:
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_COMPLETED);

                String rebootAction = getString(R.string.restore_system_reboot);
                Intent rebootIntent = new Intent(TeraMgmtService.this, TeraMgmtService.class);
                rebootIntent.setAction(TeraIntent.ACTION_MINI_RESTORE_REBOOT_SYSTEM);
                PendingIntent pendingIntent = PendingIntent.getService(TeraMgmtService.this, 0, rebootIntent, 0);
                NotificationCompat.Action action = new NotificationCompat.Action(0, rebootAction, pendingIntent);

                int notifyId = HCFSMgmtUtils.NOTIFY_ID_ONGOING;
                int flag = NotificationEvent.FLAG_ON_GOING
                        | NotificationEvent.FLAG_HEADS_UP
                        | NotificationEvent.FLAG_OPEN_APP;
                String title = getString(R.string.restore_ready_title);
                String message = getString(R.string.restore_ready_message);
                NotificationEvent.notify(mContext, notifyId, title, message, action, flag);
                break;
            case HCFSEvent.ErrorCode.ENOENT:
                status = RestoreStatus.Error.DAMAGED_BACKUP;
                break;
            case HCFSEvent.ErrorCode.ENOSPC:
                status = RestoreStatus.Error.OUT_OF_SPACE;
                break;
            case HCFSEvent.ErrorCode.ENETDOWN:
                status = RestoreStatus.Error.CONN_FAILED;
                break;
        }
        if (errorCode != 0) {
            editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, status);
            RestoreFailedFragment.startFailedNotification(mContext, status);
        }
        editor.apply();

    }

    private void handleStage2RestoreEvent(Intent intent) {
        Logs.d(CLASSNAME, "handleStage2RestoreEvent", null);

        if (MainApplication.Foreground.get().isForeground()) {
            Logs.d(CLASSNAME, "handleStage2RestoreEvent", "Application is not in foreground.");
            return;
        }

        int status = RestoreStatus.NONE;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int errorCode = intent.getIntExtra(TeraIntent.KEY_RESTORE_ERROR_CODE, -1);
        switch (errorCode) {
            case 0:
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.FULL_RESTORE_COMPLETED);

                int flag = NotificationEvent.FLAG_HEADS_UP;
                int notifyId = HCFSMgmtUtils.NOTIFY_ID_ONGOING;
                String title = getString(R.string.restore_done_title);
                String message = getString(R.string.restore_done_message);
                NotificationEvent.notify(mContext, notifyId, title, message, flag);
                break;
            case HCFSEvent.ErrorCode.ENOENT:
                status = RestoreStatus.Error.DAMAGED_BACKUP;
                break;
            case HCFSEvent.ErrorCode.ENOSPC:
                status = RestoreStatus.Error.OUT_OF_SPACE;
                break;
            case HCFSEvent.ErrorCode.ENETDOWN:
                status = RestoreStatus.Error.CONN_FAILED;
                break;
        }
        if (errorCode != 0) {
            editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, status);
            RestoreFailedFragment.startFailedNotification(mContext, status);
        }
        editor.apply();
    }

    private void notifyUserExceedPinMax() {
        int idNotify = HCFSMgmtUtils.NOTIFY_ID_INSUFFICIENT_PIN_SPACE;
        String notifyTitle = getString(R.string.app_name);
        String notifyContent = getString(R.string.notify_exceed_pin_max);


        int flag = NotificationEvent.FLAG_OPEN_APP;
        Bundle extras = new Bundle();
        extras.putInt(HCFSMgmtUtils.BUNDLE_KEY_VIEW_PAGER_INDEX, 1 /* APP/FILE page */);
        NotificationEvent.notify(this, idNotify, notifyTitle, notifyContent, flag, extras);
    }

    private void checkRestoreStatus() {
        boolean startActivity = false;
        int status = HCFSMgmtUtils.checkRestoreStatus();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (status) {
            case 0: // Not being restored
                break;
            case 1: // In stage 1 of restoration process
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.MINI_RESTORE_IN_PROGRESS);
                startActivity = true;
                break;
            case 2: // In stage 2 of restoration process
                editor.putInt(HCFSMgmtUtils.PREF_RESTORE_STATUS, RestoreStatus.FULL_RESTORE_IN_PROGRESS);
                startActivity = true;
                break;
            default:
        }
        editor.apply();

        if (startActivity) {
            Intent intent = new Intent(mContext, MainActivity.class);
            // Need to add Intent.FLAG_ACTIVITY_NEW_TASK flag if starting activity from service.
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void openPreparingOrRestoringPage() {
        checkRestoreStatus();
    }

}
