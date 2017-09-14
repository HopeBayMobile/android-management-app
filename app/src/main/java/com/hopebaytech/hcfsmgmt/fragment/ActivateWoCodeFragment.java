package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.DeviceListInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceStatusInfo;
import com.hopebaytech.hcfsmgmt.info.SwiftConfigInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.SwiftServerUtil;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;
import com.hopebaytech.hcfsmgmt.utils.LogServerUtils;

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

    public static final String GOOGLE_ENDPOINT_AUTH = "https://accounts.google.com/o/oauth2/v2/auth";
    public static final String GOOGLE_ENDPOINT_TOKEN = "https://www.googleapis.com/oauth2/v4/token";
    public static final String GOOGLE_CLIENT_ID = "795577377875-k5blp9vlffpe9s13sp6t4vqav0t6siss.apps.googleusercontent.com";

    private static final String USED_INTENT = "USED_INTENT";

    private static final String SEND_LOG_ALREADY = "send_log_already";

    private GoogleApiClient mGoogleApiClient;
    private Handler mWorkHandler = new Handler(Looper.getMainLooper());

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

    private static final String TEST_SWIFT_INFO_IP = "172.16.11.69";
    private static final String TEST_SWIFT_INFO_PORT = "8010";
    private static final String TEST_SWIFT_INFO_ACCOUNT = "hopebay:EKGKe3W3zW9IEul6zVjr";
    private static final String TEST_SWIFT_INFO_KEY = "PZJeuN5xfIV2dQkq1MSKNQCztKgzkPpn";

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

                if (!isBrowserAvailable()) {
                    if(hasGooglePlayServices()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.android.chrome"));
                        mContext.startActivity(intent);
                    } else {
                        mErrorMessage.setText(R.string.activate_alert_dialog_message);
                        return;
                    }
                }

                appAuthorization(v);
            }
        });

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

                new SwiftActivationTask().execute(swiftIp, swiftPort, swiftAccount, swiftKey);
            }
        });
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
                    .setTitle(R.string.alert_dialog_title_restore_choice)
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
            new ActivateAsNewDeviceTask().execute(swiftIp, swiftPort, swiftAccount, swiftKey);
        }

        private void handleRestoration() {
            // parse tera buckets into deviceListInfo
            DeviceListInfo deviceListInfo = new DeviceListInfo();
            for(String bucketName : teraBuckets) {
                String deviceImei;
                String deviceModel;
                try {
                    String[] bucketNameFragments = bucketName.split("_");
                    deviceImei = bucketNameFragments[1];
                    deviceModel = bucketNameFragments[2];

                    DeviceStatusInfo statusInfo = new DeviceStatusInfo();
                    statusInfo.setImei(deviceImei);
                    statusInfo.setModel(deviceModel);
                    statusInfo.setServiceStatus(MgmtCluster.ServiceState.TX_READY); //Not sure if it will cause problem
                    deviceListInfo.addDeviceStatusInfo(statusInfo);
                } catch (IndexOutOfBoundsException e) {
                    Logs.w(TAG, "handleRestoration", String.format("Found swift bucket with tera prefix but incorrect format: %s", bucketName));
                    continue;
                }
            }
            deviceListInfo.setType(DeviceListInfo.TYPE_RESTORE_FROM_BACKUP);

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

    private class ActivateAsNewDeviceTask extends AsyncTask<String, Void, Boolean> {
        private String swiftIp;
        private String swiftPort;
        private String swiftAccount;
        private String swiftKey;
        private String swiftUrl;
        //private String swiftStorageAccount;
        //private String swiftAccessKeyId;

        @Override
        protected Boolean doInBackground(String... params) {
            swiftIp = params[0];
            swiftPort = params[1];
            swiftAccount = params[2];
            swiftKey = params[3];
            swiftUrl = String.format("%s:%s", swiftIp, swiftPort);
            //swiftStorageAccount = swiftAccount.split(":")[0];
            //swiftAccessKeyId = swiftAccount.split(":")[1];

            String deviceImei = HCFSMgmtUtils.getDeviceImei(mContext);
            String deviceModel = Build.MODEL == null? "UnknownModel" : Build.MODEL.replaceAll(" ", "");
            String newBucketPostfix = String.format("%s_%s", deviceImei, deviceModel);
            String bucketName = String.format("%s%s", SwiftServerUtil.SWIFT_TERA_BUCKET_PREFIX, newBucketPostfix);

            //TODO: delete old bucket... but how?

            // set up as new device
            boolean bucketCreationSucceeded = SwiftServerUtil.createBucket(swiftAccount, swiftKey, swiftUrl, bucketName);
            if(!bucketCreationSucceeded) {
                Logs.e(TAG, "doInBackground", getString(R.string.activate_failed_cannot_create_bucket));
                return false;
            }

            boolean writeToHCFSConfigSucceeded = writeSwiftInfoToHCFSConfig(swiftUrl, swiftAccount, swiftKey,bucketName);
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
                mProgressDialogUtils.dismiss();
            }
        }
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

            if (hasUsableBrowser(info)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasUsableBrowser(ResolveInfo resolveInfo) {
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

    protected boolean writeToHCFSConfig(Map<String, String> configs) {
        boolean success = true;
        for (String key : configs.keySet()) {
            success |= TeraCloudConfig.setHCFSConfig(key, configs.get(key));
        }

        return success;
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
}
