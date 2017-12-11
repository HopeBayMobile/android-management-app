package com.hopebaytech.hcfsmgmt.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.utils.AppAuthUtils;
import com.hopebaytech.hcfsmgmt.utils.GoogleDriveAPI;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.HttpUtil;
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

    protected boolean deleteTeraFolderOnGoogleDrive(Context context, AuthState authState) {
        String accessToken = authState.getAccessToken();
        String imei = HCFSMgmtUtils.getDeviceImei(context);
        try {
            JSONArray items = GoogleDriveAPI.getTeraFolderItems(accessToken, imei);
            HttpUtil.HttpResponse response = GoogleDriveAPI.deleteFile(
                    accessToken, GoogleDriveAPI.getTeraFolderId(items));
            return response.getCode() == 204 || response.getCode() == 404;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    protected boolean setGoogleDriveInfoToHcfsConfig(AuthState authState) {
        JSONObject userInfo;
        try {
            userInfo = GoogleDriveAPI.getUserInfo(authState.getAccessToken());
        }catch (Exception exception) {
            Logs.e(TAG, "registerTeraByGoogleDrive", Log.getStackTraceString(exception));
            return false;
        }

        final String email = userInfo.optString("email", null);
        final DeviceServiceInfo deviceServiceInfo = GoogleDriveAPI.buildDeviceServiceInfo(null, authState.getAccessToken(), "googledrive", null, email);

        return TeraCloudConfig.storeHCFSConfig(deviceServiceInfo, mContext);
    }

    protected DeviceServiceInfo buildDeviceServiceInfo(AuthState authState) {
        JSONObject userInfo;
        try {
            userInfo = GoogleDriveAPI.getUserInfo(authState.getAccessToken());
        }catch (Exception exception) {
            Logs.e(TAG, "registerTeraByGoogleDrive", Log.getStackTraceString(exception));
            return null;
        }

        final String email = userInfo.optString("email", null);
        return GoogleDriveAPI.buildDeviceServiceInfo(null, authState.getAccessToken(), "googledrive", null, email);
    }

    protected Bundle buildAccountInfoBundle(AuthState authState) {
        JSONObject userInfo;
        try {
            userInfo = GoogleDriveAPI.getUserInfo(authState.getAccessToken());
        }catch (Exception exception) {
            Logs.e(TAG, "registerTeraByGoogleDrive", Log.getStackTraceString(exception));
            return null;
        }

        final String name = userInfo.optString("name", null);
        final String email = userInfo.optString("email", null);
        final String photoUrl = userInfo.optString("picture", null);

        Bundle bundle = new Bundle();
        bundle.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_DISPLAY_NAME, name);
        bundle.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_EMAIL, email);
        bundle.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_PHOTO_URI, photoUrl);
        return bundle;
    }

    protected boolean saveAccountInfo(Bundle args) {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setName(args.getString(TeraIntent.KEY_GOOGLE_SIGN_IN_DISPLAY_NAME));
        accountInfo.setEmail(args.getString(TeraIntent.KEY_GOOGLE_SIGN_IN_EMAIL));
        accountInfo.setImgUrl(args.getString(TeraIntent.KEY_GOOGLE_SIGN_IN_PHOTO_URI));

        AccountDAO accountDAO = AccountDAO.getInstance(mContext);
        accountDAO.clear();
        return accountDAO.insert(accountInfo);
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

    public static boolean writeSwiftInfoToHCFSConfig(String url, String account, String key, String bucketName) {
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

    public static boolean writeToHCFSConfig(Map<String, String> configs) {
        boolean success = true;
        for (String key : configs.keySet()) {
            success |= TeraCloudConfig.setHCFSConfig(key, configs.get(key));
        }

        return success;
    }
}
