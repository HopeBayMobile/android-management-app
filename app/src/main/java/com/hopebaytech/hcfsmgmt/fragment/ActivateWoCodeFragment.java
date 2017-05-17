package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.hopebaytech.hcfsmgmt.utils.AppAuthUtils;
import com.hopebaytech.hcfsmgmt.utils.GoogleSignInApiClient;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.ProgressDialogUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraIntent;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.TokenResponse;

import org.json.JSONObject;

import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class ActivateWoCodeFragment extends Fragment {

    public static final String TAG = ActivateWoCodeFragment.class.getSimpleName();
    private final String CLASSNAME = ActivateWoCodeFragment.class.getSimpleName();

    /**
     * Google auth and User auth
     */
    public static final String KEY_AUTH_TYPE = "auth_type";

    /**
     * Only for User authentication
     */
    public static final String KEY_PASSWORD = "password";

    /**
     * Only for User authentication
     */
    public static final String KEY_USERNAME = "username";

    /**
     * Only for Google authentication
     */
    public static final String KEY_AUTH_CODE = "auth_code";

    public static final String KEY_JWT_TOKEN = "jwt_token";

    public static final String KEY_GOOGLE_NAME = "key_google_name";

    public static final String KEY_GOOGLE_EMAIL = "key_google_email";

    public static final String KEY_GOOGLE_PHOTO_URL = "key_google_photo_url";

    public static final String GOOGLE_ENDPOINT_AUTH = "https://accounts.google.com/o/oauth2/v2/auth";

    public static final String GOOGLE_ENDPOINT_TOKEN = "https://www.googleapis.com/oauth2/v4/token";

    public static final String GOOGLE_CLIENT_ID = "795577377875-k5blp9vlffpe9s13sp6t4vqav0t6siss.apps.googleusercontent.com";

    private static final String USED_INTENT = "USED_INTENT";

    private GoogleApiClient mGoogleApiClient;
    private Handler mWorkHandler;
    private Handler mUiHandler;
    private HandlerThread mHandlerThread;

    private View mView;
    private Context mContext;
    private ProgressDialogUtils mProgressDialogUtils;
    private RelativeLayout mGoogleDriveActivate;
    private RelativeLayout mGoogleActivate;
    private TextView mErrorMessage;
    private TextView mTeraVersion;

    private AuthState mAuthState;

    public static ActivateWoCodeFragment newInstance() {
        return new ActivateWoCodeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialogUtils = new ProgressDialogUtils(mContext);
        mHandlerThread = new HandlerThread(CLASSNAME);
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper());
        mUiHandler = new Handler();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkIntent(getActivity().getIntent());
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
        mGoogleDriveActivate = (RelativeLayout) view.findViewById(R.id.google_drive_activate);
        mGoogleActivate = (RelativeLayout) view.findViewById(R.id.google_activate);
        mErrorMessage = (TextView) view.findViewById(R.id.error_msg);
        mTeraVersion = (TextView) view.findViewById(R.id.version);
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

        mGoogleDriveActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    appAuthorization(v);
                } else {
                    mErrorMessage.setText(R.string.activate_alert_dialog_message);
                }
            }
        });

        mGoogleActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // It needs to sign out first in order to show google account chooser as user
                // want to choose another Google account.
                if (mGoogleApiClient != null) {
                    signOut();
                }

                if (NetworkUtils.isNetworkConnected(mContext)) {
                    mProgressDialogUtils.show(getString(R.string.processing_msg));

                    mWorkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String serverClientId = MgmtCluster.getServerClientId();
                            if (serverClientId != null) {
                                GoogleSignInApiClient signInApiClient = new GoogleSignInApiClient(
                                        mContext, serverClientId, new GoogleSignInApiClient.OnConnectionListener() {

                                    @Override
                                    public void onConnected(@Nullable Bundle bundle, GoogleApiClient googleApiClient) {
                                        Logs.d(CLASSNAME, "onConnected", "bundle=" + bundle);
                                        mGoogleApiClient = googleApiClient;
                                        signIn();
                                    }

                                    @Override
                                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                                        Logs.d(CLASSNAME, "onConnectionFailed", "result=" + result);
                                        signOut();

                                        int errorCode = result.getErrorCode();
                                        if (errorCode == ConnectionResult.SERVICE_MISSING) {
                                            mErrorMessage.setText(R.string.activate_without_google_play_services);
                                        } else if (errorCode == ConnectionResult.SERVICE_INVALID) {
                                            mErrorMessage.setText(R.string.activate_without_google_play_services);
                                        } else if (errorCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                                            mErrorMessage.setText(R.string.activate_update_google_play_services_required);
                                            PlayServiceSnackbar.newInstance(mContext, mView).show();
                                        } else if (errorCode == ConnectionResult.SERVICE_MISSING_PERMISSION) {
                                            mErrorMessage.setText(R.string.activate_update_google_play_service_missing_permission);
                                        } else {
                                            mErrorMessage.setText(R.string.activate_signin_google_account_failed);
                                        }

                                        mProgressDialogUtils.dismiss();
                                    }

                                    @Override
                                    public void onConnectionSuspended(int cause) {
                                        Logs.d(CLASSNAME, "onConnectionSuspended", "cause=" + cause);
                                    }
                                });
                                signInApiClient.connect();
                            } else {
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressDialogUtils.dismiss();
                                        mErrorMessage.setText(R.string.activate_get_server_client_id_failed);
                                    }
                                });
                            }

                        }
                    });
                } else {
                    mErrorMessage.setText(R.string.activate_alert_dialog_message);
                }
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

    public static class PlayServiceSnackbar {

        private static Snackbar playServiceSnackbar;

        public static Snackbar newInstance(final Context context, View view) {
            playServiceSnackbar = Snackbar.make(view, R.string.activate_update_google_play_services_go, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.update, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String playServicesPackage = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE;
                            Intent intent;
                            try {
                                // Open app with Google Play app
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + playServicesPackage));
                                context.startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                // Open Google Play website
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + playServicesPackage));
                                context.startActivity(intent);
                            }
                        }
                    });
            return playServiceSnackbar;
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
                        String photoUrl = null;
                        if (acct.getPhotoUrl() != null) {
                            photoUrl = acct.getPhotoUrl().toString();
                        }
                        args.putString(KEY_GOOGLE_PHOTO_URL, photoUrl);

                        Fragment restorationFragment = RestoreFragment.newInstance();
                        restorationFragment.setArguments(args);

                        Logs.d(CLASSNAME, "onGetDeviceListSuccessful", "Replace with RestoreFragment");
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, restorationFragment, RestoreFragment.TAG);
                        ft.addToBackStack(null);
                        ft.commit();

                        mProgressDialogUtils.dismiss();
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

        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }

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

    private void registerTera(MgmtCluster.RegisterParam authParam, final String jwtToken, final GoogleSignInAccount acct) {
        MgmtCluster.RegisterProxy registerProxy = new MgmtCluster.RegisterProxy(authParam, jwtToken);
        registerProxy.setOnRegisterListener(new MgmtCluster.RegisterListener() {
            @Override
            public void onRegisterSuccessful(final DeviceServiceInfo deviceServiceInfo) {
                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean isSuccess = TeraCloudConfig.storeHCFSConfig(deviceServiceInfo, mContext);
                        if (!isSuccess) {
                            signOut();
                            TeraCloudConfig.resetHCFSConfig();
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialogUtils.dismiss();
                                    mErrorMessage.setText(R.string.activate_failed);
                                }
                            });
                        } else {
                            final String name = acct.getDisplayName();
                            final String email = acct.getEmail();
                            String photoUrl = null;
                            if (acct.getPhotoUrl() != null) {
                                photoUrl = acct.getPhotoUrl().toString();
                            }

                            AccountInfo accountInfo = new AccountInfo();
                            accountInfo.setName(name);
                            accountInfo.setEmail(email);
                            accountInfo.setImgUrl(photoUrl);

                            AccountDAO accountDAO = AccountDAO.getInstance(mContext);
                            accountDAO.clear();
                            accountDAO.insert(accountInfo);

                            TeraAppConfig.enableApp(mContext);
                            TeraCloudConfig.activateTeraCloud(mContext);

                            String url = deviceServiceInfo.getBackend().getUrl();
                            String token = deviceServiceInfo.getBackend().getToken();
                            HCFSMgmtUtils.setSwiftToken(url, token);

                            final String finalPhotoUrl = photoUrl;
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Bundle args = new Bundle();
                                    args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_DISPLAY_NAME, name);
                                    args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_EMAIL, email);
                                    args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_PHOTO_URI, finalPhotoUrl);

                                    MainFragment mainFragment = MainFragment.newInstance();
                                    mainFragment.setArguments(args);

                                    Logs.d(CLASSNAME, "onRegisterSuccessful", "Replace with MainFragment");
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.replace(R.id.fragment_container, mainFragment, MainFragment.TAG);
                                    ft.commit();

                                    mProgressDialogUtils.dismiss();
                                }
                            });

                        }
                    }
                });
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
                          "https://www.googleapis.com/auth/drive.appdata",
                          "https://www.googleapis.com/auth/presentations",
                          "https://www.googleapis.com/auth/spreadsheets");

        AuthorizationRequest request = builder.build();
        String action = "com.hopebaytech.hcfsmgmt.HANDLE_AUTHORIZATION_RESPONSE";
        Intent postAuthorizationIntent = new Intent(action);
        postAuthorizationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(v.getContext(), request.hashCode(), postAuthorizationIntent, 0);
        authorizationService.performAuthorizationRequest(request, pendingIntent);
    }

    private void checkIntent(@Nullable Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            Logs.d(CLASSNAME, "onStart checkIntent", "intentAction " + action);
            if (action != null) {
                switch (action) {
                    case "com.hopebaytech.hcfsmgmt.HANDLE_AUTHORIZATION_RESPONSE":
                        if (!intent.hasExtra(USED_INTENT)) {
                            mProgressDialogUtils.show(getString(R.string.processing_msg));
                            handleAuthorizationResponse(intent);
                            intent.putExtra(USED_INTENT, true);
                        }
                        break;
                    default:
                        // do nothing
                }
            }
        }
    }

    private void handleAuthorizationResponse(@NonNull Intent intent) {
        AuthorizationResponse response = AuthorizationResponse.fromIntent(intent);
        AuthorizationException error = AuthorizationException.fromIntent(intent);
        final AuthState authState = new AuthState(response, error);
        final AppAuthUtils appAuthUtils = new AppAuthUtils();

        Logs.d(CLASSNAME, "onHandleAuthResponse", "Response state " + response.state);
        if (response != null) {
            AuthorizationService service = new AuthorizationService(getActivity());
            service.performTokenRequest(response.createTokenExchangeRequest(), new AuthorizationService.TokenResponseCallback() {
                @Override
                public void onTokenRequestCompleted(@Nullable TokenResponse tokenResponse, @Nullable AuthorizationException exception) {
                    if (exception == null && tokenResponse != null) {
                        authState.update(tokenResponse, exception);
                        mAuthState = authState;
                        appAuthUtils.saveAppAuthStatusToSharedPreference(mContext, mAuthState);

                        Logs.d(CLASSNAME, "onTokenResponse", String.format(
                                "Token Response [ Access Token: %s, ID Token: %s ]",
                                tokenResponse.accessToken, tokenResponse.idToken));
                    }
                    registerTeraBattle(mAuthState);
                }
            });
        }
    }

    private void setDeviceServiceInfoBackend(JSONObject userinfo, String token) {
        try {
            DeviceServiceInfo deviceServiceInfo = new DeviceServiceInfo();
            DeviceServiceInfo.Backend _backend = new DeviceServiceInfo.Backend();
            _backend.setUrl("https://172.16.40.177");
            _backend.setToken(token);
            _backend.setBackendType("googledrive");
            _backend.setBucket("akdjlaksdjglksjadlk");
            _backend.setAccount(userinfo.get("email").toString());
            deviceServiceInfo.setBackend(_backend);
            boolean isSuccess = TeraCloudConfig.storeHCFSConfig(deviceServiceInfo, mContext);
        } catch (Exception exception) {
            Logs.e(CLASSNAME, "setDeviceServiceInfoBackend", Log.getStackTraceString(exception));
        }
    }

    private Bundle setAccountDAOandHCFStoken(JSONObject userinfo, String token){
        try {
            final String name = userinfo.optString("name", null);
            final String email = userinfo.optString("email", null);
            final String photoUrl = userinfo.optString("picture", null);
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setName(name);
            accountInfo.setEmail(email);
            accountInfo.setImgUrl(photoUrl);

            AccountDAO accountDAO = AccountDAO.getInstance(mContext);
            accountDAO.clear();
            accountDAO.insert(accountInfo);

            TeraAppConfig.enableApp(mContext);
            TeraCloudConfig.activateTeraCloud(mContext);

            HCFSMgmtUtils.setSwiftToken("https://172.16.40.117", token);

            Bundle args = new Bundle();
            args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_DISPLAY_NAME, name);
            args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_EMAIL, email);
            args.putString(TeraIntent.KEY_GOOGLE_SIGN_IN_PHOTO_URI, photoUrl);
            return args;

        } catch (Exception exception) {
            Logs.e(CLASSNAME, "setAccountDAOandHCFStoken", Log.getStackTraceString(exception));
        }
        return null;
    }

    private void registerTeraBattle(@NonNull AuthState authState) {

        authState.performActionWithFreshTokens(new AuthorizationService(mContext), new AuthState.AuthStateAction() {
            @Override
            public void execute(@Nullable String accessToken, @Nullable String idToken, @Nullable AuthorizationException exception) {
                new AsyncTask<String, Void, Bundle>() {
                    @Override
                    protected Bundle doInBackground(String... tokens) {
                        Logs.d(CLASSNAME, "onAppAuthRegister", "firstTimeToken " + tokens[0]);
                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("https://www.googleapis.com/oauth2/v3/userinfo")
                                .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                                .build();
                        final String token = tokens[0].toString();

                        try {
                            Response response = client.newCall(request).execute();
                            String jsonBody = response.body().string();
                            Logs.d(CLASSNAME, "onAppAuthRegister", String.format("User Info Response %s", jsonBody));

                            JSONObject userinfo = new JSONObject(jsonBody);
                            setDeviceServiceInfoBackend(userinfo, token);
                            Bundle args = setAccountDAOandHCFStoken(userinfo, token);
                            return args;
                        } catch (Exception exception) {
                            Logs.e(CLASSNAME, "appAuthRegisterTera", Log.getStackTraceString(exception));
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bundle args) {
                        MainFragment mainFragment = MainFragment.newInstance();
                        mainFragment.setArguments(args);
                        mProgressDialogUtils.dismiss();

                        Logs.d(CLASSNAME, "onRegisterSuccessful", "Replace with MainFragment");
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.fragment_container, mainFragment, MainFragment.TAG);
                        ft.commit();
                    }
                }.execute(accessToken);
            }
        });
    }

    private boolean checkPermission() {
        boolean readPhoneState = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;

        return readPhoneState;
    }

    private void grantPermission() {
        if (!checkPermission()){
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
}
