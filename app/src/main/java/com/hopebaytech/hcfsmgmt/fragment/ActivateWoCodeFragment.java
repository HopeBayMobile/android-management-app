package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.HttpUtil;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class ActivateWoCodeFragment extends RegisterFragment {

    public static final String TAG = ActivateWoCodeFragment.class.getSimpleName();
    private final String CLASSNAME = ActivateWoCodeFragment.class.getSimpleName();

    //Google auth and User auth
    public static final String KEY_AUTH_TYPE = "auth_type";

    //Only for User authentication
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_AUTH_CODE = "auth_code";

    public static final String KEY_JWT_TOKEN = "jwt_token";
    public static final String KEY_GOOGLE_NAME = "key_google_name";
    public static final String KEY_GOOGLE_EMAIL = "key_google_email";
    public static final String KEY_GOOGLE_PHOTO_URL = "key_google_photo_url";

    public static final String SWIFT_HEADER_KEY_USER = "X-Auth-User";
    public static final String SWIFT_HEADER_KEY_KEY = "X-Auth-Key";
    public static final String SWIFT_HEADER_KEY_TOKEN = "X-Auth-Token";
    public static final String SWIFT_HEADER_KEY_STORAGE_URL = "x-storage-url";

    public static final String GOOGLE_ENDPOINT_AUTH = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String GOOGLE_ENDPOINT_TOKEN = "https://www.googleapis.com/oauth2/v4/token";
    public static final String GOOGLE_CLIENT_ID = "795577377875-k5blp9vlffpe9s13sp6t4vqav0t6siss.apps.googleusercontent.com";

    private static final String USED_INTENT = "USED_INTENT";

    private GoogleApiClient mGoogleApiClient;
    private Handler mWorkHandler = new Handler(Looper.getMainLooper());

    private View mView;
    private ImageView teraLogo;
    // activation method layout
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
    private EditText mSwiftBucketNameInputEditText;
    private LinearLayout mSwiftActivateButton;
    private TextView mErrorMessage;

    private int tapsOnLogo = 0;
    private long startTime = 0L;

    private static final String TEST_SWIFT_INFO_IP = "172.16.11.69";
    private static final String TEST_SWIFT_INFO_PORT = "8010";
    private static final String TEST_SWIFT_INFO_ACCOUNT = "hopebay:EKGKe3W3zW9IEul6zVjr";
    private static final String TEST_SWIFT_INFO_KEY = "PZJeuN5xfIV2dQkq1MSKNQCztKgzkPpn";
    private static final String TEST_SWIFT_INFO_BUCKET_NAME = "test1";

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
        setOnTouchListenerForTeraLogo();

        mActivationMethodLayout = (LinearLayout) view.findViewById(R.id.activation_method_layout);
        mGoogleDriveActivationLayout = (RelativeLayout) view.findViewById(R.id.google_drive_activate);
        mSwiftActivationLayout = (RelativeLayout) view.findViewById(R.id.swift_activate);

        mSwiftAccountInfoLayout = (LinearLayout) view.findViewById(R.id.swift_account_info_layout);
        mSwiftIpInputEditText = (EditText) view.findViewById(R.id.swift_ip_input);
        mSwiftPortInputEditText = (EditText) view.findViewById(R.id.swift_port_input);
        mSwiftAccountInputEditText = (EditText) view.findViewById(R.id.swift_account_input);
        mSwiftKeyInputEditText = (EditText) view.findViewById(R.id.swift_key_input);
        mSwiftBucketNameInputEditText = (EditText) view.findViewById(R.id.swift_bucket_name_input);
        mSwiftActivateButton = (LinearLayout) view.findViewById(R.id.swift_activate_button);

        mErrorMessage = (TextView) view.findViewById(R.id.error_msg);
        mTeraVersion = (TextView) view.findViewById(R.id.version);

        setOnClickListenerForSwiftActivateButton();
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
                        mSwiftBucketNameInputEditText.setText(TEST_SWIFT_INFO_BUCKET_NAME);
                    }
                }
                return true;
            }
        });
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
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    if (isBrowserAvailable()) {
                        appAuthorization(v);
                    } else if (hasGooglePlayServices()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.android.chrome"));
                        mContext.startActivity(intent);
                    }
                } else {
                    mErrorMessage.setText(R.string.activate_alert_dialog_message);
                }
            }
        });

        mSwiftActivationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePrivateSwiftActivation();
            }
        });

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logs.d(CLASSNAME, "onActivityResult", "requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode != RequestCode.GOOGLE_SIGN_IN || resultCode != Activity.RESULT_OK) {
            mProgressDialogUtils.dismiss();
            return;
        }

        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        final GoogleSignInAccount acct = result.getSignInAccount();
        if (acct == null) {
            String failedMsg = "acct is null";
            googleAuthFailed(failedMsg);
            mProgressDialogUtils.dismiss();
            return;
        }

        final String serverAuthCode = acct.getServerAuthCode();
        MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam(serverAuthCode);
        MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
        authProxy.setOnAuthListener(new MgmtCluster.OnAuthListener() {
            @Override
            public void onAuthSuccessful(final AuthResultInfo authResultInfo) {
                if (TeraCloudConfig.isTeraCloudActivated(mContext)) {
                    boolean isAllowEnabled = false;
                    AccountDAO accountDAO = AccountDAO.getInstance(mContext);
                    if (accountDAO.getCount() != 0) {
                        AccountInfo accountInfo = accountDAO.getFirst();
                        if (accountInfo.getEmail().equals(acct.getEmail())) {
                            isAllowEnabled = true;
                        }
                    }

                    if (isAllowEnabled) {
                        TeraAppConfig.enableApp(mContext);

                        Logs.d(CLASSNAME, "onAuthSuccessful", "Replace with MainFragment");
                        MainFragment mainFragment = MainFragment.newInstance();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, mainFragment, MainFragment.TAG);
                        ft.commit();
                    } else {
                        mErrorMessage.setText(R.string.activate_failed_device_in_use);
                    }

                    mProgressDialogUtils.dismiss();
                    return;
                }

                final String jwtToken = authResultInfo.getToken();
                String imei = HCFSMgmtUtils.getDeviceImei(mContext);
                MgmtCluster.GetDeviceListProxy proxy = new MgmtCluster.GetDeviceListProxy(jwtToken, imei);
                proxy.setOnGetDeviceListListener(new MgmtCluster.GetDeviceListProxy.OnGetDeviceListListener() {
                    @Override
                    public void onGetDeviceListSuccessful(DeviceListInfo deviceListInfo) {
                        Logs.d(CLASSNAME, "onGetDeviceListSuccessful", "deviceListInfo=" + deviceListInfo);

                        // No any device backup can be restored, directly register to Tera
                        if (deviceListInfo.getDeviceStatusInfoList().size() == 0) {
                            MgmtCluster.RegisterParam registerParam = new MgmtCluster.RegisterParam(mContext);
                            registerTera(registerParam, authResultInfo.getToken(), acct);
                            return;
                        }

                        Bundle args = new Bundle();
                        args.putParcelable(RestoreFragment.KEY_DEVICE_LIST, deviceListInfo);
                        args.putString(KEY_JWT_TOKEN, jwtToken);
                        args.putString(KEY_GOOGLE_NAME, acct.getDisplayName());
                        args.putString(KEY_GOOGLE_EMAIL, acct.getEmail());
                        args.putString(KEY_GOOGLE_PHOTO_URL,
                                acct.getPhotoUrl() != null ? acct.getPhotoUrl().toString() : null);
                        replaceWithRestoreFragment(args, null);
                    }

                    @Override
                    public void onGetDeviceListFailed(DeviceListInfo deviceListInfo) {
                        Logs.e(CLASSNAME, "onGetDeviceListFailed", "deviceListInfo=" + deviceListInfo);
                        if (deviceListInfo.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                            mErrorMessage.setText(R.string.activate_failed_device_in_use);
                        } else {
                            mErrorMessage.setText(R.string.activate_auth_failed);
                        }
                        mProgressDialogUtils.dismiss();
                    }
                });
                proxy.get();
            }

            @Override
            public void onAuthFailed(AuthResultInfo authResultInfo) {
                Logs.e(CLASSNAME, "onAuthFailed", "authResultInfo=" + authResultInfo.toString());

                signOut();
                mErrorMessage.setText(R.string.activate_auth_failed);
                mProgressDialogUtils.dismiss();
            }
        });
        authProxy.auth();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
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
                String swiftBucket = mSwiftBucketNameInputEditText.getText().toString();

                new SwiftActivationTask().execute(swiftIp, swiftPort, swiftAccount, swiftKey, swiftBucket);
            }
        });
    }

    private class SwiftActivationTask extends AsyncTask<String, Void, Boolean> {
        public final String TAG = SwiftActivationTask.class.getSimpleName();

        @Override
        protected Boolean doInBackground(String... params) {
            String swiftIp = params[0];
            String swiftPort = params[1];
            String swiftAccount = params[2];
            String swiftKey = params[3];
            String swiftBucket = params[4];
            String swiftUrl = String.format("%s:%s", swiftIp, swiftPort);
            String swiftStorageAccount = swiftAccount.split(":")[0];
            String swiftAccessKeyId = swiftAccount.split(":")[1];

            // connect to swift server, check x-auth-token in response header
            Map<String, String> authHeaders = new HashMap<String, String>();
            authHeaders.put(SWIFT_HEADER_KEY_USER, swiftAccount);
            authHeaders.put(SWIFT_HEADER_KEY_KEY, swiftKey);
            String authPath = String.format("%s%s%s", "http://", swiftUrl, "/auth/v1.0");
            HttpUtil.HttpRequest  authRequest = HttpUtil.buildGetRequest(authHeaders, authPath);
            HttpUtil.HttpResponse authResponse = HttpUtil.executeSynchronousRequest(authRequest);
            if (authResponse == null || authResponse.getCode() != 200 || authResponse.getHeader(SWIFT_HEADER_KEY_TOKEN) == null) {
                Logs.e(TAG, "doInBackground", getString(R.string.activate_failed_swift_server_auth_error));
                return false;
            }

            // connect to storage api, check bucket returned contains the one user inputs
            Map<String, String> checkBucketHeaders = new HashMap<String, String>();
            checkBucketHeaders.put(SWIFT_HEADER_KEY_TOKEN, authResponse.getHeader(SWIFT_HEADER_KEY_TOKEN));
            String checkBucketPath = authResponse.getHeader(SWIFT_HEADER_KEY_STORAGE_URL);
            HttpUtil.HttpResponse checkBucketResponse = HttpUtil.executeSynchronousRequest(HttpUtil.buildGetRequest(checkBucketHeaders, checkBucketPath));
            String responseBody = checkBucketResponse.getBody();
            if (checkBucketResponse == null || checkBucketResponse.getCode() != 200 || !responseBody.contains(swiftBucket)) {
                Logs.e(TAG, "doInBackground", getString(R.string.activate_failed_bucket_error));
                return false;
            }

            // write swift info to HCFS config;
            Map<String, String> hcfsSwiftConfigs = new HashMap<String, String>();
            hcfsSwiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_CURRENT_BACKEND, "SWIFT");
            hcfsSwiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_URL, swiftUrl);
            hcfsSwiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_ACCOUNT, swiftStorageAccount);
            hcfsSwiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_USER, swiftAccessKeyId);
            hcfsSwiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_PASS, swiftKey);
            hcfsSwiftConfigs.put(TeraCloudConfig.HCFS_CONFIG_SWIFT_CONTAINER, swiftBucket);
            boolean writeToHCFSConfigSucceed = writeToHCFSConfig(hcfsSwiftConfigs);
            if (!writeToHCFSConfigSucceed) {
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

        protected void onPostExecute(Boolean activationSucceeded) {
            if(!activationSucceeded) {
                TeraCloudConfig.resetHCFSConfig();
                mUiHandler.sendEmptyMessage(ACTIVATE_FAILED);
                mProgressDialogUtils.dismiss();
            } else {
                // activate Tera
                TeraAppConfig.enableApp(mContext);
                TeraCloudConfig.activateTeraCloud(mContext);
                // show Tera main menu
                mProgressDialogUtils.dismiss();
                replaceWithMainFragment(new Bundle());
            }
        }
    }

    private void handlePrivateSwiftActivation() {
        mProgressDialogUtils.show(R.string.processing_msg);
        mActivationMethodLayout.setVisibility(View.GONE);
        mSwiftAccountInfoLayout.setVisibility(View.VISIBLE);
        mProgressDialogUtils.dismiss();
    }

    private boolean hasGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(mContext);
        if (result != ConnectionResult.SUCCESS) {
            // add tip for install Browser
            mErrorMessage.setText(R.string.activate_failed_browser_should_install);
            return false;
        }
        return true;
    }

    private boolean isBrowserAvailable() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.testTera.com.tw"));
        PackageManager pm = mContext.getPackageManager();

        List resolvedActivityList = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        Iterator var5 = resolvedActivityList.iterator();
        while (var5.hasNext()) {
            ResolveInfo info = (ResolveInfo) var5.next();

            if (isFullBrowser(info)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFullBrowser(ResolveInfo resolveInfo) {
        if (resolveInfo.filter.hasAction("android.intent.action.VIEW") &&
                resolveInfo.filter.hasCategory("android.intent.category.BROWSABLE") &&
                resolveInfo.filter.schemesIterator() != null) {
            if (resolveInfo.filter.authoritiesIterator() != null) {
                return false;
            } else {
                boolean supportsHttp = false;
                boolean supportsHttps = false;
                Iterator schemeIter = resolveInfo.filter.schemesIterator();

                do {
                    if (!schemeIter.hasNext()) {
                        return false;
                    }

                    String scheme = (String) schemeIter.next();
                    supportsHttp |= "http".equals(scheme);
                    supportsHttps |= "https".equals(scheme);
                } while (!supportsHttp || !supportsHttps);

                return true;
            }
        } else {
            return false;
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

    private void googleAuthFailed(String failedMsg) {
        Logs.e(CLASSNAME, "googleAuthFailed", "failedMsg=" + failedMsg);
        mProgressDialogUtils.dismiss();
        signOut();
        mErrorMessage.setText(R.string.activate_signin_google_account_failed);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        ((Activity) mContext).startActivityForResult(signInIntent, RequestCode.GOOGLE_SIGN_IN);
    }

    private void signOut() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Logs.d(CLASSNAME, "signOut", "status=" + status);
                        }
                    });
        }
    }

    private void registerTera(MgmtCluster.RegisterParam authParam, final String jwtToken, final GoogleSignInAccount acct) {
        MgmtCluster.RegisterProxy registerProxy = new MgmtCluster.RegisterProxy(authParam, jwtToken);
        registerProxy.setOnRegisterListener(new MgmtCluster.RegisterListener() {
            @Override
            public void onRegisterSuccessful(final DeviceServiceInfo deviceServiceInfo) {
                final String name = acct.getDisplayName();
                final String email = acct.getEmail();
                final String photoUrl = acct.getPhotoUrl() != null ?
                        acct.getPhotoUrl().toString() : null;

                if (!setConfigAndActivate(deviceServiceInfo,
                        buildAccountInfo(name, email, photoUrl))) {
                    signOut();
                }
            }

            @Override
            public void onRegisterFailed(DeviceServiceInfo deviceServiceInfo) {
                Logs.e(CLASSNAME, "onRegisterFailed", "deviceServiceInfo=" + deviceServiceInfo.toString());

                signOut();
                mProgressDialogUtils.dismiss();

                CharSequence errorMessage = mContext.getText(R.string.activate_failed);
                if (deviceServiceInfo.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                    if (deviceServiceInfo.getErrorCode().equals(MgmtCluster.ErrorCode.IMEI_NOT_FOUND)) {
                        Bundle bundle = new Bundle();
                        bundle.putInt(KEY_AUTH_TYPE, MgmtCluster.GOOGLE_AUTH);
                        bundle.putString(KEY_USERNAME, acct.getEmail());
                        bundle.putString(KEY_AUTH_CODE, acct.getServerAuthCode());
                        bundle.putString(KEY_JWT_TOKEN, jwtToken);

                        ActivateWithCodeFragment fragment = ActivateWithCodeFragment.newInstance();
                        fragment.setArguments(bundle);

                        Logs.d(CLASSNAME, "onRegisterFailed", "Replace with ActivateWithCodeFragment");
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, fragment);
                        ft.commit();
                        return;
                    }
                    errorMessage = MgmtCluster.ErrorCode.getErrorMessage(mContext, deviceServiceInfo.getErrorCode());
                }
                mErrorMessage.setText(errorMessage);
            }

        });
        registerProxy.register();
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

    private void appAuthorization(View v) {
        AuthorizationServiceConfiguration serviceConfiguration = new AuthorizationServiceConfiguration(
                Uri.parse(GOOGLE_ENDPOINT_AUTH), /* auth endpoint */
                Uri.parse(GOOGLE_ENDPOINT_TOKEN) /* token endpoint */
        );

        AuthorizationService authorizationService = new AuthorizationService(v.getContext());
        String clientId = GOOGLE_CLIENT_ID;
        Uri redirectUri = Uri.parse("com.hopebaytech.hcfsmgmt:/oauth2callback");

        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfiguration,
                clientId,
                AuthorizationRequest.RESPONSE_TYPE_CODE,
                redirectUri
        );
        builder.setScopes("profile",
                "email",
                "https://www.googleapis.com/auth/drive",
                "https://www.googleapis.com/auth/drive.file",
                "https://www.googleapis.com/auth/drive.appdata");

        AuthorizationRequest request = builder.build();
        Intent postAuthorizationIntent = new Intent(ACTION_AUTHORIZATION_RESPONSE);
        postAuthorizationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(v.getContext(), request.hashCode(), postAuthorizationIntent, 0);
        authorizationService.performAuthorizationRequest(request, pendingIntent);
    }

    private void checkAuthorizationResponse() {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return;
        }

        if (ACTION_AUTHORIZATION_RESPONSE.equals(intent.getAction()) &&
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
                        } else if (tokenResponse != null) {
                            AuthState authState = new AuthState(response,
                                    AuthorizationException.fromIntent(intent));
                            authState.update(tokenResponse, exception);

                            Logs.d(TAG, "onTokenResponse", String.format(
                                    "Token Response [ Access Token: %s, ID Token: %s ]",
                                    tokenResponse.accessToken, tokenResponse.idToken));

                            doRestoreOrRegister(mContext, authState, tokenResponse.accessToken,
                                    true/* check restoration */);
                        }
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

    private boolean writeToHCFSConfig(Map<String, String> configs) {
        boolean success = true;
        for (String key : configs.keySet()) {
            success |= TeraCloudConfig.setHCFSConfig(key, configs.get(key));
        }

        return success;
    }
}