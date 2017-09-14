package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.utils.AppAuthUtils;
import com.hopebaytech.hcfsmgmt.utils.GoogleDriveAPI;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.ProgressDialogUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

import net.openid.appauth.AuthState;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    protected static final String ACTION_AUTHORIZATION_RESPONSE = "com.hopebaytech.hcfsmgmt.HANDLE_AUTHORIZATION_RESPONSE";

    protected static final String KEY_GOOGLE_DRIVE_TOKEN = "key_google_drive_token";

    private static final int DISMISS_PROGRESS_DIALOG = -1;
    protected static final int ACTIVATE_FAILED = 0;
    protected static final int RESTORE_FAILED = 1;

    protected TextView mErrorMessage;

    protected Context mContext;
    protected ProgressDialogUtils mProgressDialogUtils;

    protected Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case ACTIVATE_FAILED:
                    mErrorMessage.setText(R.string.activate_failed);
                    mProgressDialogUtils.dismiss();
                    break;
                case RESTORE_FAILED:
                    mErrorMessage.setText(R.string.restore_failed);
                    mProgressDialogUtils.dismiss();
                    break;
                case DISMISS_PROGRESS_DIALOG:
                default:
                    mProgressDialogUtils.dismiss();
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialogUtils = new ProgressDialogUtils(mContext);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mErrorMessage = (TextView) view.findViewById(R.id.error_msg);
    }

    protected void doRestoreOrRegister(final Context context,
            final @NonNull AuthState authState, final @NonNull String token,
            final boolean checkRestoration) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String imei = HCFSMgmtUtils.getDeviceImei(context);
                    JSONArray items = GoogleDriveAPI.getTeraFolderItems(token, imei);

                    if (checkRestoration && GoogleDriveAPI.isCanRestore(items)) {
                        // Do Restore Here
                        Bundle args = new Bundle();
                        args.putParcelable(RestoreFragment.KEY_DEVICE_LIST, GoogleDriveAPI.buildDeviceListInfo(imei));
                        replaceWithRestoreFragment(args, authState);
                    } else {
                        GoogleDriveAPI.deleteFile(token, GoogleDriveAPI.getTeraFolderId(items));
                        registerTeraByGoogleDrive(authState, token);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected void registerTeraByGoogleDrive(AuthState authState, String token) {
        try {
            JSONObject userInfo = GoogleDriveAPI.getUserInfo(token);
            final String name = userInfo.optString("name", null);
            final String email = userInfo.optString("email", null);
            final String photoUrl = userInfo.optString("picture", null);
            final DeviceServiceInfo deviceServiceInfo = GoogleDriveAPI.buildDeviceServiceInfo(
                    null,
                    token,
                    "googledrive",
                    null,
                    email
            );

            if (setConfigAndActivate(deviceServiceInfo, buildAccountInfo(name, email, photoUrl))) {
                AppAuthUtils.saveAppAuthStatusToSharedPreference(mContext, authState);
            }
        } catch (Exception exception) {
            Logs.e(TAG, "registerTeraByGoogleDrive", Log.getStackTraceString(exception));
        }
    }

    protected boolean setConfigAndActivate(DeviceServiceInfo deviceServiceInfo, Bundle args) {
        boolean isSuccess = TeraCloudConfig.storeHCFSConfig(deviceServiceInfo, mContext);
        if (isSuccess) {
            saveAccountInfo(args);
            TeraAppConfig.enableApp(mContext);
            TeraCloudConfig.activateTeraCloud(mContext);
            HCFSMgmtUtils.setSwiftToken(deviceServiceInfo.getBackend().getUrl(),
                    deviceServiceInfo.getBackend().getToken());
            replaceWithMainFragment(args);
        } else {
            TeraCloudConfig.resetHCFSConfig();

            mUiHandler.sendEmptyMessage(ACTIVATE_FAILED);
        }

        return isSuccess;
    }

    protected Bundle buildAccountInfo(String name, String email, String photoUrl) {
        Bundle args = new Bundle();
        args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_DISPLAY_NAME, name);
        args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_EMAIL, email);
        args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_PHOTO_URI, photoUrl);
        return args;
    }

    private void saveAccountInfo(Bundle args) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(args.getString(TeraIntent.KEY_GOOGLE_SIGN_IN_DISPLAY_NAME));
        accountInfo.setEmail(args.getString(TeraIntent.KEY_GOOGLE_SIGN_IN_EMAIL));
        accountInfo.setImgUrl(args.getString(TeraIntent.KEY_GOOGLE_SIGN_IN_PHOTO_URI));

        AccountDAO accountDAO = AccountDAO.getInstance(mContext);
        accountDAO.clear();
        accountDAO.insert(accountInfo);
    }

    protected void replaceWithMainFragment(Bundle args) {
        Logs.d(TAG, "replaceWithMainFragment", "Replace with MainFragment");

        MainFragment mainFragment = MainFragment.newInstance();
        mainFragment.setArguments(args);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, mainFragment, MainFragment.TAG);
        ft.commit();

        mUiHandler.sendEmptyMessage(DISMISS_PROGRESS_DIALOG);
    }

    protected void replaceWithRestoreFragment(Bundle args, AuthState authState) {
        Logs.d(TAG, "replaceWithRestoreFragment", "Replace with RestoreFragment");

        RestoreFragment restoreFragment = RestoreFragment.newInstance();
        restoreFragment.setArguments(args);
        restoreFragment.setGoogleDriveAuthState(authState); //TODO: I don't like this, it makes this method google drive specific. Also, Rondou saved it to shared preference already.

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, restoreFragment, RestoreFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();

        mUiHandler.sendEmptyMessage(DISMISS_PROGRESS_DIALOG);
    }

    protected void replaceWithRestorePreparingFragment() {
        mProgressDialogUtils.dismiss();

        Logs.d(TAG, "replaceWithRestoreFragment", "Replace with RestorePreparingFragment");
        FragmentManager fm = getFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, RestorePreparingFragment.newInstance());
        ft.commitAllowingStateLoss();

        mUiHandler.sendEmptyMessage(DISMISS_PROGRESS_DIALOG);
    }

    protected boolean writeSwiftInfoToHCFSConfig(String url, String account, String key, String bucketName) {
        String storageAccount = account.split(":")[0];
        String accessKeyId = account.split(":")[1];

        Map<String, String> swiftConfigs = new HashMap<String, String>();
        swiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_CURRENT_BACKEND, "SWIFT");
        swiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_URL, url);
        swiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_ACCOUNT, storageAccount);
        swiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_USER, accessKeyId);
        swiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_PASS, key);
        swiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_CONTAINER, bucketName);

        return writeToHCFSConfig(swiftConfigs);
    }

    protected boolean writeToHCFSConfig(Map<String, String> configs) {
        boolean success = true;
        for (String key : configs.keySet()) {
            success |= TeraCloudConfig.setHCFSConfig(key, configs.get(key));
        }

        return success;
    }
}
