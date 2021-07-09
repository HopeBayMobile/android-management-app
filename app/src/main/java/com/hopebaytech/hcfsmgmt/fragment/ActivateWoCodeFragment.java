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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;
import com.hopebaytech.hcfsmgmt.info.SwiftConfigInfo;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.AppAuthUtils;
import com.hopebaytech.hcfsmgmt.utils.BrowserUtils;
import com.hopebaytech.hcfsmgmt.utils.GoogleDriveAPI;
import com.hopebaytech.hcfsmgmt.utils.GoogleDriveSignInAPI;
import com.hopebaytech.hcfsmgmt.utils.GooglePlayServicesAPI;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.SwiftServerUtil;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.TokenResponse;

import java.util.List;
import java.util.Locale;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class ActivateWoCodeFragment extends RegisterFragment {

    public static final String TAG = ActivateWoCodeFragment.class.getSimpleName();
    private final String CLASSNAME = ActivateWoCodeFragment.class.getSimpleName();

    private static final String USED_INTENT = "USED_INTENT";

    private View mView;
    private ImageView teraLogo;
    private LinearLayout mActivationMethodLayout;
    private RelativeLayout mGoogleDriveActivationLayout;
    private RelativeLayout mSwiftActivationLayout;
    private TextView mTeraVersion;
    // swift connect layout
    private LinearLayout mSwiftAccountInfoLayout;
    private EditText mSwiftIpInputEditText;
    private EditText mSwiftPortInputEditText;
    private EditText mSwiftAccountInputEditText;
    private EditText mSwiftKeyInputEditText;
    private LinearLayout mSwiftActivateButton;
    private TextView mErrorMessage;

    private int tapsOnLogo = 0;
    private long startTime = 0L;

    private static final String TEST_SWIFT_INFO_IP = "192.168.1.112";
    private static final String TEST_SWIFT_INFO_PORT = "8010";
    private static final String TEST_SWIFT_INFO_ACCOUNT = "tera:YXTOBAGLOcfZO4P8zHEg";
    private static final String TEST_SWIFT_INFO_KEY = "f0uhszaCVkFdPilO9gR9CDj8zJBdNTf8";
    private String containerName = "";

    private AuthState mAuthState;

    public static ActivateWoCodeFragment newInstance() {
        return new ActivateWoCodeFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkAuthorizationResponse();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activate_wo_activation_code_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;
        teraLogo = (ImageView) view.findViewById(R.id.logo);

        mActivationMethodLayout = (LinearLayout) view.findViewById(R.id.activation_method_layout);
        mGoogleDriveActivationLayout = (RelativeLayout) view.findViewById(R.id.google_drive_activate);
        mSwiftActivationLayout = (RelativeLayout) view.findViewById(R.id.swift_activate);

        mSwiftAccountInfoLayout = (LinearLayout) view.findViewById(R.id.swift_account_info_layout);
        mSwiftIpInputEditText = (EditText) view.findViewById(R.id.swift_ip_input);
        mSwiftPortInputEditText = (EditText) view.findViewById(R.id.swift_port_input);
        mSwiftAccountInputEditText = (EditText) view.findViewById(R.id.swift_account_input);
        mSwiftKeyInputEditText = (EditText) view.findViewById(R.id.swift_key_input);
        mSwiftActivateButton = (LinearLayout) view.findViewById(R.id.swift_activate_button);

        mErrorMessage = (TextView) view.findViewById(R.id.error_msg);
        mTeraVersion = (TextView) view.findViewById(R.id.version);

        setOnTouchListenerForTeraLogo();
        setOnClickListenerForSwiftActivateButton();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTeraVersion.setText(
                String.format(
                        Locale.getDefault(),
                        getString(R.string.tera_version_info),
                        getString(R.string.tera_version)
                )
        );

        grantPermission();

        mGoogleDriveActivationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtils.isNetworkConnected(mContext)) {
                    mErrorMessage.setText(R.string.activate_alert_dialog_message);
                    return;
                }

                if (mContext instanceof MainActivity) {
                    if (((MainActivity)mContext).silentSignIn(mCallback)) {
                        mProgressDialogUtils.show(R.string.processing_msg);
                        return;
                    }
                }

                if (!BrowserUtils.isBrowserAvailable(mContext)) {
                    if (GooglePlayServicesAPI.hasGooglePlayServices(mContext)) {
                        BrowserUtils.toDownloadPage(mContext);
                    } else {
                        mErrorMessage.setText(R.string.activate_alert_dialog_message);
                    }
                    return;
                }

                AppAuthUtils.appAuthorization(getContext());
            }
        });

        if(mSwiftActivationLayout != null) {
            mSwiftActivationLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkUtils.isNetworkConnected(mContext)) {
                        mErrorMessage.setText(R.string.activate_alert_dialog_message);
                        return;
                    }

                    mProgressDialogUtils.show(R.string.processing_msg);
                    mActivationMethodLayout.setVisibility(View.GONE);
                    mSwiftAccountInfoLayout.setVisibility(View.VISIBLE);
                    mProgressDialogUtils.dismiss();
                }
            });
        }

        Bundle extras = getArguments();
        if (extras != null) {
            String cause = extras.getString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE);
            mErrorMessage.setText(cause);
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String cause = sharedPreferences.getString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE, null);
            if (cause != null) {
                mErrorMessage.setText(cause);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE);
                editor.apply();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Logs.w(CLASSNAME, "onRequestPermissionsResult", "requestCode=" + requestCode + ", grantResults.length=" + grantResults.length);
        for (int result : grantResults) {
            Logs.e(CLASSNAME, "onRequestPermissionsResult", "result=" + result);
        }
        switch (requestCode) {
            case RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                            Manifest.permission.READ_PHONE_STATE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(R.string.alert_dialog_title_warning);
                        builder.setMessage(R.string.activate_require_read_phone_state_permission);
                        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) mContext,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, null);
                        builder.show();
                    } else {
                        PermissionSnackbar.newInstance(mContext, mView).show();
                    }
                }
                break;
        }
    }

    private void setOnClickListenerForSwiftActivateButton() {
        mSwiftActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialogUtils.show(R.string.processing_msg);

                // collect user input
                String swiftIp = mSwiftIpInputEditText.getText().toString();
                String swiftPort = mSwiftPortInputEditText.getText().toString();
                String swiftAccount = mSwiftAccountInputEditText.getText().toString();
                String swiftKey = mSwiftKeyInputEditText.getText().toString();

                new SwiftActivationTask().execute(swiftIp, swiftPort, swiftAccount, swiftKey);
            }
        });

        mSwiftActivateButton.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    mProgressDialogUtils.show(R.string.processing_msg);

                    // collect user input
                    String swiftIp = mSwiftIpInputEditText.getText().toString();
                    String swiftPort = mSwiftPortInputEditText.getText().toString();
                    String swiftAccount = mSwiftAccountInputEditText.getText().toString();
                    String swiftKey = mSwiftKeyInputEditText.getText().toString();

                    new SwiftActivationTask().execute(swiftIp, swiftPort, swiftAccount, swiftKey);
                }
                return true;
            }
        });
    }

    private class GoogleDriveActivationTask extends AsyncTask<String, Void, Boolean> {
        String refreshToken;
        String accessToken;
        List<String> folders;

        @Override
        protected Boolean doInBackground(String... params) {
            refreshToken = params[0];
            accessToken = params[1];

            try {
                folders = GoogleDriveAPI.getTeraFolderItems(accessToken);
                return folders.size() != 0;
            } catch(Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onPostExecute(Boolean hasTeraFolderOnGoogleDrive) {
            mProgressDialogUtils.dismiss();

            if(!hasTeraFolderOnGoogleDrive) {
                setUpAsNewDevice();
            } else {
                showRestoreChoiceDialog();
            }
        }

        private AlertDialog showRestoreChoiceDialog() {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder
                    .setTitle(R.string.alert_dialog_title_google_drive_restore_choice)
                    .setItems(R.array.restore_choice, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int choiceIndex) {
                            switch(choiceIndex) {
                                case 0:
                                    mProgressDialogUtils.show(R.string.processing_msg);
                                    setUpAsNewDevice();
                                    break;
                                case 1:
                                    mProgressDialogUtils.show(R.string.processing_msg);
                                    handleRestoration();
                                    break;
                                default:
                                    throw new RuntimeException("Implementation Error");
                            }
                        }
                    });

            return dialogBuilder.show();
        }

        private void setUpAsNewDevice() {
            new ActivateAsNewDeviceViaGoogleDriveTask().execute(refreshToken, accessToken);
        }

        private void handleRestoration() {
            Bundle args = new Bundle();
            args.putParcelable(RestoreFragment.KEY_DEVICE_LIST, GoogleDriveAPI.buildDeviceListInfo(folders));
            args.putString(RestoreFragment.KEY_GOOGLE_DRIVE_ACCESS_TOKEN, accessToken);
            replaceWithRestoreFragment(args);
        }
    }

    private class ActivateAsNewDeviceViaGoogleDriveTask extends AsyncTask<String, Void, Boolean> {
        String refreshToken;
        String accessToken;
        Bundle accountInfo;

        @Override
        protected Boolean doInBackground(String... params) {
            refreshToken = params[0];
            accessToken = params[1];

            boolean isDeleted = false;
            while (!isDeleted) {
                isDeleted = GoogleDriveAPI.deleteTeraFolderOnGoogleDrive(mContext, accessToken);
                try {
                    Thread.sleep(Interval.DELETE_FOLDER_DELAY_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            DeviceServiceInfo deviceServiceInfo = buildDeviceServiceInfo(accessToken);
            if(deviceServiceInfo == null) {
                Logs.e(TAG, "doInBackground", "Failed to build device service info"); //TODO: extract
                return false;
            }

            boolean saveAuthStatusToSharedPreferenceSucceeded = AppAuthUtils.saveAppAuthStatusToSharedPreference(mContext, mAuthState);
            if (!saveAuthStatusToSharedPreferenceSucceeded) {
                Logs.e(TAG, "doInBackground", "Failed to save AuthStatus to shared preference"); //TODO: extract
                return false;
            }

            boolean writeToHCFSConfigSucceeded =
                    TeraCloudConfig.storeHCFSConfig(deviceServiceInfo, mContext);
            if (!writeToHCFSConfigSucceeded) {
                Logs.e(TAG, "doInBackground", "Failed to write google drive info to HCFS config"); //TODO: extract
                return false;
            }

            accountInfo = buildAccountInfoBundle(accessToken);
            if (accountInfo == null) {
                Logs.e(TAG, "doInBackground", "Failed to build account info"); //TODO: extract
                return false;
            }

            boolean saveAccountInfoSucceeded = saveAccountInfo(accountInfo);
            if (!saveAccountInfoSucceeded) {
                Logs.e(TAG, "doInBackground", "Failed to save account info"); //TODO: extract
                return false;
            }

            boolean setTokenSucceeded = HCFSMgmtUtils.setRefreshToken(refreshToken);
            if (!setTokenSucceeded) {
                Logs.e(TAG, "doInBackground", "Failed to set goolge drive token"); //TODO: extract
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean setUpSucceeded) {
            if(setUpSucceeded) {
                TeraAppConfig.enableApp(mContext);
                TeraCloudConfig.activateTeraCloud(mContext);
                replaceWithMainFragment(accountInfo);
            } else {
                TeraCloudConfig.resetHCFSConfig();
                mUiHandler.sendEmptyMessage(ACTIVATE_FAILED);
            }
        }
    }

    private class SwiftActivationTask extends AsyncTask<String, Void, Boolean> {
        public final String TAG = SwiftActivationTask.class.getSimpleName();
        private String swiftIp;
        private String swiftPort;
        private String swiftAccount;
        private String swiftKey;
        private String swiftUrl;
        private List<String> teraBuckets;

        @Override
        protected Boolean doInBackground(String... params) {
            swiftIp = params[0];
            swiftPort = params[1];
            swiftAccount = params[2];
            swiftKey = params[3];
            swiftUrl = String.format("%s:%s", swiftIp, swiftPort);

            teraBuckets = SwiftServerUtil.listTeraBuckets(swiftAccount, swiftKey, swiftUrl);
            if (teraBuckets == null) {
                Logs.e(TAG, "doInBackground", getString(R.string.activate_failed_list_bucket_error));
                return false;
            }

            return !teraBuckets.isEmpty();
        }

        protected void onPostExecute(Boolean hasTeraBucketOnSwiftServer) {
            if(!hasTeraBucketOnSwiftServer) {
                setUpAsNewDevice();
            } else {
                mProgressDialogUtils.dismiss();
                showRestoreChoiceDialog();
            }
        }

        private AlertDialog showRestoreChoiceDialog() {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            dialogBuilder
                    .setTitle(R.string.alert_dialog_title_swift_restore_choice)
                    .setItems(R.array.restore_choice, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int choiceIndex) {
                            switch(choiceIndex) {
                                case 0:
                                    mProgressDialogUtils.show(R.string.processing_msg);
                                    setUpAsNewDevice();
                                    break;
                                case 1:
                                    mProgressDialogUtils.show(R.string.processing_msg);
                                    handleRestoration();
                                    break;
                                default:
                                    throw new RuntimeException("Implementation Error");
                            }
                        }
                    });

            return dialogBuilder.show();
        }

        private void setUpAsNewDevice() {
            new ActivateAsNewDeviceViaSwiftTask().execute(swiftIp, swiftPort, swiftAccount, swiftKey);
        }

        private void handleRestoration() {
            // parse tera buckets into deviceListInfo
            DeviceListInfo deviceListInfo = new DeviceListInfo();
            for(String containerName : teraBuckets) {
                String deviceImei;
                String deviceModel;
                String containerIndex;
                try {
                    String[] bucketNameFragments = containerName.split("_");
                    deviceImei = bucketNameFragments[1];
                    deviceModel = bucketNameFragments[2];
                    containerIndex = "1";
                    if (bucketNameFragments.length == 4)
                        containerIndex = bucketNameFragments[3];

                    DeviceStatusInfo statusInfo = new DeviceStatusInfo();
                    statusInfo.setImei(deviceImei);
                    statusInfo.setModel(deviceModel);
                    statusInfo.setContainerIndex(containerIndex);
                    statusInfo.setServiceStatus(MgmtCluster.ServiceState.TX_READY); //Not sure if it will cause problem
                    deviceListInfo.addDeviceStatusInfo(statusInfo);
                } catch (IndexOutOfBoundsException e) {
                    Logs.w(TAG, "handleRestoration", String.format("Found swift bucket with tera prefix but incorrect format: %s", containerName));
                    continue;
                }
            }
            deviceListInfo.setType(DeviceListInfo.TYPE_RESTORE_FROM_SWIFT);

            if(deviceListInfo.isEmpty()) {
                Logs.e(TAG, "handleRestoration", getString(R.string.restore_failed_upon_retrieve_backup));
                TeraCloudConfig.resetHCFSConfig();
                mUiHandler.sendEmptyMessage(R.string.restore_failed_upon_retrieve_backup);
                mProgressDialogUtils.dismiss();
                return;
            }

            RestoreFragment restoreFragment = RestoreFragment.newInstance();
            Bundle bundle = new Bundle();

            // pass deviceListInfo into bundle for restore fragment.
            bundle.putParcelable(RestoreFragment.KEY_DEVICE_LIST, deviceListInfo);
            restoreFragment.setArguments(bundle);

            // pass swiftConfigInfo to bundle for restore fragment.
            SwiftConfigInfo swiftConfigInfo = new SwiftConfigInfo();
            swiftConfigInfo.setUrl(swiftUrl);
            swiftConfigInfo.setAccount(swiftAccount);
            swiftConfigInfo.setKey(swiftKey);
            bundle.putParcelable(SwiftConfigInfo.PARCEL_KEY, swiftConfigInfo);

            mProgressDialogUtils.dismiss();

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, restoreFragment, RestoreFragment.TAG);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    private class ActivateAsNewDeviceViaSwiftTask extends AsyncTask<String, Void, Boolean> {
        private String swiftIp;
        private String swiftPort;
        private String swiftAccount;
        private String swiftKey;
        private String swiftUrl;

        @Override
        protected Boolean doInBackground(String... params) {
            swiftIp = params[0];
            swiftPort = params[1];
            swiftAccount = params[2];
            swiftKey = params[3];
            swiftUrl = String.format("%s:%s", swiftIp, swiftPort);

            String deviceImei = HCFSMgmtUtils.getDeviceImei(mContext);
            String deviceModel = Build.MODEL == null? "UnknownModel" : Build.MODEL.replaceAll(" ", "");
            String newContainerPostfix = String.format("%s_%s", deviceImei, deviceModel);
            String containerName = String.format("%s%s", SwiftServerUtil.SWIFT_TERA_BUCKET_PREFIX, newContainerPostfix);

            // if container "tera_imei_model" exists, append number to name
            boolean containerExist = SwiftServerUtil.isContainerExist(swiftAccount, swiftKey, swiftUrl, containerName);
            if(containerExist) {
                Logs.d(TAG, "containerExist", String.format("[%s] container existed", containerName));
                int numberOfContainersByIMEI = SwiftServerUtil.getNumberOfContainersByThisIMEI(swiftAccount, swiftKey, swiftUrl, deviceImei);
                containerName = String.format("%s_%s", containerName, numberOfContainersByIMEI + 1);
                Logs.d(TAG, "containerExist", String.format("use [%s] as new name ", containerName));
            }

            // set up as new device
            boolean bucketCreationSucceeded = SwiftServerUtil.createBucket(swiftAccount, swiftKey, swiftUrl, containerName);
            if(!bucketCreationSucceeded) {
                Logs.e(TAG, "doInBackground", getString(R.string.activate_failed_cannot_create_bucket));
                return false;
            }

            boolean writeToHCFSConfigSucceeded = writeSwiftInfoToHCFSConfig(swiftUrl, swiftAccount, swiftKey,containerName);
            if (!writeToHCFSConfigSucceeded) {
                Logs.e(TAG, "doInBackground", getString(R.string.activate_failed_save_swift_config));
                return false;
            }

            // reload config to notify HCFS config change
            boolean reloadConfigSucceed = TeraCloudConfig.reloadConfig();
            if (!reloadConfigSucceed) {
                Logs.e(TAG, "doInBackground", getString(R.string.activate_failed_reload_config));
                return false;
            }

            return true;
        }

        protected void onPostExecute(Boolean setUpSucceeded) {
            if(setUpSucceeded) {
                // activate Tera
                TeraAppConfig.enableApp(mContext);
                TeraCloudConfig.activateTeraCloud(mContext);
                // show Tera main menu
                mProgressDialogUtils.dismiss();
                replaceWithMainFragment(new Bundle());
            } else {
                TeraCloudConfig.resetHCFSConfig();
                mUiHandler.sendEmptyMessage(ACTIVATE_FAILED);
            }
        }
    }

    public static class PermissionSnackbar {

        private static Snackbar permissionSnackbar;

        public static Snackbar newInstance(final Context context, View view) {
            permissionSnackbar = Snackbar.make(view, R.string.activate_require_read_phone_state_permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.go, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String packageName = context.getPackageName();
                            Intent teraPermissionSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
                            teraPermissionSettings.addCategory(Intent.CATEGORY_DEFAULT);
                            teraPermissionSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(teraPermissionSettings);
                        }
                    });
            return permissionSnackbar;
        }
    }

    public void setIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        String cause = extras.getString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE);
        mErrorMessage.setText(cause);
    }

    private void checkAuthorizationResponse() {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return;
        }

        if (GoogleDriveAPI.ACTION_AUTHORIZATION_RESPONSE.equals(intent.getAction()) &&
                !intent.hasExtra(USED_INTENT)) {
            mProgressDialogUtils.show(getString(R.string.processing_msg));
            handleAuthorizationResponse(intent);
            intent.putExtra(USED_INTENT, true);
        }
    }

    protected void handleAuthorizationResponse(@NonNull final Intent intent) {
        final AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        if (response == null) {
            return;
        }

        new AuthorizationService(mContext).performTokenRequest(
                response.createTokenExchangeRequest(),
                new AuthorizationService.TokenResponseCallback() {
                    @Override
                    public void onTokenRequestCompleted(final @Nullable TokenResponse tokenResponse,
                                                        @Nullable AuthorizationException exception) {
                        if (exception != null) {
                            exception.printStackTrace();
                            return;
                        }

                        if(tokenResponse == null) {
                            Logs.e(TAG, "onTokenRequestCompleted", "tokenResponse == null");
                            return;
                        }

                        mAuthState = new AuthState(response,
                                AuthorizationException.fromIntent(intent));
                        mAuthState.update(tokenResponse, exception);

                        Logs.d(TAG, "onTokenRequestCompleted", String.format(
                                "Token Response [ Access Token: %s, ID Token: %s ]",
                                tokenResponse.accessToken, tokenResponse.idToken));

                        new GoogleDriveActivationTask().execute(
                                tokenResponse.refreshToken, tokenResponse.accessToken);
                    }
                });
    }

    private boolean checkPermission() {
        boolean readPhoneState = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;

        return readPhoneState;
    }

    private void grantPermission() {
        if (!checkPermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_PHONE_STATE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.alert_dialog_title_warning));
                builder.setMessage(getString(R.string.activate_require_read_phone_state_permission));
                builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_PHONE_STATE}, RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    }
                });
                builder.setCancelable(false);
                builder.show();
            } else {
                PermissionSnackbar.newInstance(mContext, mView).show();
            }
        }
    }


    private void setOnTouchListenerForTeraLogo() {
        // fill test swift info when user taps on Tera logo 5 times within 3 seconds
        teraLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    Logs.d(TAG, "onTouch", "tapsOnLogo: " + tapsOnLogo);
                    long currentTime = System.currentTimeMillis();

                    if(startTime == 0 || currentTime - startTime > 3000) {
                        startTime = currentTime;
                        tapsOnLogo = 1;
                    } else {
                        tapsOnLogo++;
                    }

                    if(tapsOnLogo >= 5) {
                        mSwiftIpInputEditText.setText(TEST_SWIFT_INFO_IP);
                        mSwiftPortInputEditText.setText(TEST_SWIFT_INFO_PORT);
                        mSwiftAccountInputEditText.setText(TEST_SWIFT_INFO_ACCOUNT);
                        mSwiftKeyInputEditText.setText(TEST_SWIFT_INFO_KEY);
                    }
                }
                return true;
            }
        });
    }

    private GoogleDriveSignInAPI.Callback mCallback = new GoogleDriveSignInAPI.Callback() {
        @Override
        public void onSignIn(boolean isSuccess) {
            if (!isSuccess) {
                mErrorMessage.setText(R.string.activate_signin_google_account_failed);
                mProgressDialogUtils.dismiss();
            }
        }

        @Override
        public void onTokenResponse(String refreshToken, String accessToken) {
            Logs.d("refreshToken = " + refreshToken + " accessToken = " + accessToken);
            new GoogleDriveActivationTask().execute(refreshToken, accessToken);
        }
    };
}
