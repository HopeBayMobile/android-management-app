package com.hopebaytech.hcfsmgmt.service;

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

import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.DataTypeDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.DataTypeInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.GetDeviceInfo;
import com.hopebaytech.hcfsmgmt.info.HCFSStatInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.ServiceFileDirInfo;
import com.hopebaytech.hcfsmgmt.info.TeraIntent;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.info.UnlockDeviceInfo;
import com.hopebaytech.hcfsmgmt.interfaces.IMgmtBinder;
import com.hopebaytech.hcfsmgmt.interfaces.IPinUnpinListener;
import com.hopebaytech.hcfsmgmt.utils.DisplayTypeFactory;
import com.hopebaytech.hcfsmgmt.utils.FactoryResetUtils;
import com.hopebaytech.hcfsmgmt.utils.GoogleSilentAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NotificationEvent;
import com.hopebaytech.hcfsmgmt.utils.PinType;

import org.json.JSONException;
import org.json.JSONObject;

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

    private Context mContext;
    private UidDAO mUidDAO;
    private DataTypeDAO mDataTypeDAO;
//    private ServiceFileDirDAO mServiceFileDirDAO;

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
        mCacheExecutor = Executors.newCachedThreadPool();
//        mServiceFileDirDAO = ServiceFileDirDAO.getInstance(this);
        mUidDAO = UidDAO.getInstance(this);
        mDataTypeDAO = DataTypeDAO.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        mContext = this;
        if (intent != null) {
//            final String operation = intent.getStringExtra(TeraIntent.KEY_OPERATION);
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
                    } else if (action.equals(TeraIntent.ACTION_RESET_XFER)) {
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
                    }

//                    if (operation.equals(TeraIntent.VALUE_ADD_UID_AND_PIN_SYSTEM_APP_WHEN_BOOT_UP)) {
//                        addUidAndPinSysApp();
//                    } else
//                    if (operation.equals(TeraIntent.VALUE_ADD_UID_TO_DATABASE_AND_UNPIN_USER_APP)) {
//                        addUidAndUnpinUserApp(intent);
//                    } else if (operation.equals(TeraIntent.VALUE_PIN_UNPIN_UDPATE_APP)) {
//                        pinUnpinAgainWhenAppUpdated(intent);
//                    } else if (operation.equals(TeraIntent.VALUE_REMOVE_UID_FROM_DATABASE)) {
//                        removeUidFromDatabase(intent, operation);
//                    } else if (operation.equals(TeraIntent.VALUE_RESET_XFER)) {
//                        HCFSMgmtUtils.resetXfer();
//                    } else if (operation.equals(TeraIntent.VALUE_NOTIFY_LOCAL_STORAGE_USED_RATIO)) {
//                        notifyLocalStorageUsedRatio();
//                    } else if (operation.equals(TeraIntent.VALUE_ONGOING_NOTIFICATION)) {
//                        startOngoingNotificationService(intent);
//                    } else if (operation.equals(TeraIntent.VALUE_CHECK_DEVICE_STATUS)) {
//                        checkDeviceStatus();
//                    } else if (operation.equals(TeraIntent.VALUE_INSUFFICIENT_PIN_SPACE)) {
//                        notifyInsufficientPinSpace();
//                    }  else if (operation.equals(TeraIntent.VALUE_UPDATE_APP_EXTERNAL_DIR)) {
//                        updateAppExternalDir();
//                    }
                }
            });
        } else {
            // Service is restarted and then execute the uncompleted pin/unpin operation when user
            // manually close app and removes it from background.
//            mCacheExecutor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    if (mServiceFileDirDAO.getCount() > 0) {
//                        List<ServiceFileDirInfo> infoList = mServiceFileDirDAO.getAll();
//                        for (final ServiceFileDirInfo info : infoList) {
//                            pinOrUnpinFileOrDirectory(info);
//                            mServiceFileDirDAO.delete(info.getFilePath());
//                        }
//                    }
//                }
//            });
        }
        return super.onStartCommand(intent, flags, startId);
        // return START_REDELIVER_INTENT;
    }

//    private void mgmtAuthOrRegisterFailed(GoogleApiClient googleApiClient, String failedMsg) {
//        Logs.e(CLASSNAME, "mgmtAuthOrRegisterFailed", null);
//
//        if (MgmtCluster.isNeedToRetryAgain()) {
//            Intent intentService = new Intent(TeraMgmtService.this, TeraMgmtService.class);
//            intentService.putExtra(TeraIntent.KEY_OPERATION, TeraIntent.VALUE_CHECK_DEVICE_STATUS);
//            startService(intentService);
//            Logs.e(CLASSNAME, "mgmtAuthOrRegisterFailed", "Authentication failed, retry again");
//        } else {
//            int flag = NotificationEvent.FLAG_OPEN_APP;
//            int id_notify = HCFSMgmtUtils.NOTIFY_ID_CHECK_DEVICE_STATUS;
//            String notify_title = getString(R.string.app_name);
//            String notify_content = failedMsg;
//            Bundle extras = new Bundle();
//            extras.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
//            NotificationEvent.notify(TeraMgmtService.this, id_notify, notify_title, notify_content, flag, extras);
//
//            Auth.GoogleSignInApi.signOut(googleApiClient)
//                    .setResultCallback(new ResultCallback<Status>() {
//                        @Override
//                        public void onResult(Status status) {
//                            Logs.e(CLASSNAME, "mgmtAuthOrRegisterFailed", "status=" + status);
//                        }
//                    });
//
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TeraMgmtService.this);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, false);
//            editor.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
//            editor.apply();
//        }
//        googleApiClient.disconnect();
//        HCFSConfig.stopSyncToCloud();
//
//        NotificationEvent.cancel(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
//    }

    private void pinOrUnpinApp(AppInfo info) {
        Logs.d(CLASSNAME, "pinOrUnpinApp", info.getName());
        pinOrUnpinApp(info, PinType.NORMAL);
    }

    private boolean pinOrUnpinApp(AppInfo info, int pinType) {
        Logs.d(CLASSNAME, "pinOrUnpinApp", info.getName());
        boolean isSuccess = true;
        final boolean isPinned = info.isPinned();
        if (isPinned) {
            if (!HCFSMgmtUtils.pinApp(info, pinType)) {
                isSuccess = false;
                if (pinType != PinType.PRIORITY) {
                    handleAppFailureOfPinOrUnpin(info, getString(R.string.notify_pin_app_failure));
                }
            }
        } else {
            if (!HCFSMgmtUtils.unpinApp(info)) {
                isSuccess = false;
                if (pinType != PinType.PRIORITY) {
                    handleAppFailureOfPinOrUnpin(info, getString(R.string.notify_unpin_app_failure));
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
                handleAppFailureOfPinOrUnpin(info, getString(R.string.notify_pin_app_failure));
                listener.onPinUnpinFailed(info);
            }
        } else {
            if (HCFSMgmtUtils.unpinApp(info)) {
                listener.onPinUnpinSuccessful(info);
            } else {
                handleAppFailureOfPinOrUnpin(info, getString(R.string.notify_unpin_app_failure));
                listener.onPinUnpinFailed(info);
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

//        // Notify user pin/unpin failed
//        int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
//        String notify_title = getString(R.string.app_name);
//        String notify_message = notifyMsg + ": " + info.getName();
//        NotificationEvent.notify(this, notify_id, notify_title, notify_message);
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
//        mServiceFileDirDAO.insert(serviceFileDirInfo);
        String filePath = info.getFilePath();
        Logs.d(CLASSNAME, "pinOrUnpinFileOrDirectory", "filePath=" + filePath);
        boolean isPinned = info.isPinned();
        if (isPinned) {
            int code = HCFSMgmtUtils.pinFileOrDirectory(filePath);
            boolean isSuccess = (code == 0);
            if (!isSuccess) {
//                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
//                String notify_title = getString(R.string.app_name);
//                String notify_message = getString(R.string.notify_pin_file_dir_failure) + "： " + filePath + " (errorCode=" + code + ")";
//                NotificationEvent.notify(this, notify_id, notify_title, notify_message);

                listener.onPinUnpinFailed(info);
            } else {
                listener.onPinUnpinSuccessful(info);
            }
        } else {
            boolean isSuccess = (HCFSMgmtUtils.unpinFileOrDirectory(filePath) == 0);
            if (!isSuccess) {
//                int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
//                String notify_title = getString(R.string.app_name);
//                String notify_message = getString(R.string.notify_unpin_file_dir_failure) + "： " + filePath;
//                NotificationEvent.notify(this, notify_id, notify_title, notify_message);

                listener.onPinUnpinFailed(info);
            } else {
                listener.onPinUnpinSuccessful(info);
            }
        }
//        mServiceFileDirDAO.delete(serviceFileDirInfo.getFilePath());
    }

    private void doActionAccordingToDeviceStatus(final String jwtToken) {
        final String imei = HCFSMgmtUtils.getDeviceImei(TeraMgmtService.this);
        MgmtCluster.GetDeviceInfoProxy getDeviceInfoProxy = new MgmtCluster.GetDeviceInfoProxy(jwtToken, imei);
        getDeviceInfoProxy.setOnGetDeviceInfoListener(new MgmtCluster.GetDeviceInfoProxy.OnGetDeviceInfoListener() {
            @Override
            public void onGetDeviceInfoSuccessful(final GetDeviceInfo getDeviceInfo) {
                try {
                    String responseContent = getDeviceInfo.getMessage();
                    JSONObject result = new JSONObject(responseContent);
                    String state = result.getString("state");
                    if (state.equals(GetDeviceInfo.State.ACTIVATED)) {
                        JSONObject backend = result.getJSONObject("backend");
                        String url = backend.getString("url");
                        String token = backend.getString("token");
                        HCFSMgmtUtils.setSwiftToken(url, token);
                    } else {
                        JSONObject piggyback = result.getJSONObject("piggyback");
                        String category = piggyback.getString("category");
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
                } catch (JSONException e) {
                    Logs.e(CLASSNAME, "checkDeviceStatus", Log.getStackTraceString(e));
                }
            }

            @Override
            public void onGetDeviceInfoFailed(GetDeviceInfo getDeviceInfo) {
                Logs.e(CLASSNAME, "onGetDeviceInfoFailed", null);
                int id_notify = HCFSMgmtUtils.NOTIFY_ID_CHECK_DEVICE_STATUS;
                String notify_title = getString(R.string.app_name);
                String notify_content = getString(R.string.auth_at_bootup_auth_get_device_info);
                NotificationEvent.notify(TeraMgmtService.this, id_notify, notify_title, notify_content);
            }
        });
        getDeviceInfoProxy.get();
    }

    private void checkDeviceStatus() {
        final String serverClientId = MgmtCluster.getServerClientId();
        GoogleSilentAuthProxy googleAuthProxy = new GoogleSilentAuthProxy(TeraMgmtService.this, serverClientId,
                new GoogleSilentAuthProxy.OnAuthListener() {
                    @Override
                    public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {
                        String serverAuthCode = result.getSignInAccount().getServerAuthCode();

                        final MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam();
                        authParam.setAuthCode(serverAuthCode);
                        authParam.setAuthBackend(MgmtCluster.GOOGLE_AUTH_BACKEND);
                        authParam.setImei(HCFSMgmtUtils.getEncryptedDeviceImei(HCFSMgmtUtils.getDeviceImei(TeraMgmtService.this)));
                        authParam.setVendor(Build.BRAND);
                        authParam.setModel(Build.MODEL);
                        authParam.setAndroidVersion(Build.VERSION.RELEASE);
                        authParam.setHcfsVersion(getString(R.string.tera_version));

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
                                    HCFSConfig.stopSyncToCloud();
                                    NotificationEvent.cancel(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);

                                    int flag = NotificationEvent.FLAG_OPEN_APP;
                                    int id_notify = HCFSMgmtUtils.NOTIFY_ID_CHECK_DEVICE_STATUS;
                                    String notify_title = getString(R.string.app_name);
                                    String notify_content = getString(R.string.auth_at_bootup_auth_failed_mgmt_auth);
                                    Bundle extras = new Bundle();
                                    extras.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
                                    NotificationEvent.notify(TeraMgmtService.this, id_notify, notify_title, notify_content, flag, extras);

                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TeraMgmtService.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, false);
                                    editor.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
                                    editor.apply();
                                }
                            }
                        });
                        authProxy.auth();
                    }

                    @Override
                    public void onAuthFailed() { // Google silent auth failed
                        HCFSConfig.stopSyncToCloud();
                        NotificationEvent.cancel(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);

                        int flag = NotificationEvent.FLAG_OPEN_APP;
                        int id_notify = HCFSMgmtUtils.NOTIFY_ID_CHECK_DEVICE_STATUS;
                        String notify_title = getString(R.string.app_name);
                        String notify_content = getString(R.string.auth_at_bootup_auth_failed_google_auth);
                        Bundle extras = new Bundle();
                        extras.putString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, notify_content);
                        NotificationEvent.notify(TeraMgmtService.this, id_notify, notify_title, notify_content, flag, extras);

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TeraMgmtService.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, false);
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
                    int idNotify = HCFSMgmtUtils.NOTIFY_ID_INSUFFICIENT_PIN_SPACE;
                    String notifyTitle = getString(R.string.app_name);
                    String notifyContent = String.format(getString(R.string.notify_exceed_pin_used_ratio), notifyRatio);
                    Bundle extras = new Bundle();
                    extras.putBoolean(HCFSMgmtUtils.BUNDLE_KEY_INSUFFICIENT_PIN_SPACE, true);
                    NotificationEvent.notify(TeraMgmtService.this, idNotify, notifyTitle, notifyContent, extras);

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
     * Send an ongoing notification to show HCFS network status and storage usage
     */
    private void startOngoingNotificationService(Intent intent) {
        boolean isOnGoing = intent.getBooleanExtra(TeraIntent.KEY_ONGOING, false);
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
                                    String notifyMsg = getString(R.string.overview_used_space) + ": " + statInfo.getFormatVolUsed() + " / " + statInfo.getFormatCloudTotal();
                                    int flag = NotificationEvent.FLAG_ON_GOING | NotificationEvent.FLAG_OPEN_APP;
                                    NotificationEvent.notify(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING, notifyTitle, notifyMsg, flag);
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
            NotificationEvent.cancel(TeraMgmtService.this, HCFSMgmtUtils.NOTIFY_ID_ONGOING);
        }
    }

    /**
     * Send a notification to user when storage used ratio exceeds the threshold set by user
     */
    private void notifyLocalStorageUsedRatio() {
        HCFSStatInfo statInfo = HCFSMgmtUtils.getHCFSStatInfo();
        if (statInfo != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
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
                pinOrUnpinApp(appInfo);
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
                pinOrUnpinApp(appInfo);
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
            int notify_id = (int) (Math.random() * Integer.MAX_VALUE);
            String notify_title = getString(R.string.app_name);
            String notify_message = getString(R.string.notify_pin_unpin_system_app_failed);
            NotificationEvent.notify(TeraMgmtService.this, notify_id, notify_title, notify_message);
        }
    }


}
