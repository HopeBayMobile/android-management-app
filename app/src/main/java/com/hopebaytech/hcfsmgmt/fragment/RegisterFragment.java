/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.utils.GoogleDriveAPI;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.ProgressDialogUtils;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    protected static final int DISMISS_PROGRESS_DIALOG = -1;
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

    protected DeviceServiceInfo buildDeviceServiceInfo(String accessToken) {
        JSONObject userInfo;
        try {
            userInfo = GoogleDriveAPI.getUserInfo(accessToken);
        }catch (Exception exception) {
            Logs.e(TAG, "registerTeraByGoogleDrive", Log.getStackTraceString(exception));
            return null;
        }

        final String email = userInfo.optString("email", null);
        return GoogleDriveAPI.buildDeviceServiceInfo(accessToken, email);
    }

    protected Bundle buildAccountInfoBundle(String accessToken) {
        JSONObject userInfo;
        try {
            userInfo = GoogleDriveAPI.getUserInfo(accessToken);
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

    protected void replaceWithRestoreFragment(Bundle args) {
        Logs.d(TAG, "replaceWithRestoreFragment", "Replace with RestoreFragment");

        RestoreFragment restoreFragment = RestoreFragment.newInstance();
        restoreFragment.setArguments(args);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, restoreFragment, RestoreFragment.TAG);
        ft.addToBackStack(null);
        ft.commit();

        mUiHandler.sendEmptyMessage(DISMISS_PROGRESS_DIALOG);
    }

    protected void replaceWithRestorePreparingFragment() {
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
