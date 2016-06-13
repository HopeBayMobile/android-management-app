package com.hopebaytech.hcfsmgmt.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;
import com.hopebaytech.hcfsmgmt.main.LoadingActivity;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

import javax.net.ssl.HttpsURLConnection;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;
import com.hopebaytech.hcfsmgmt.main.LoadingActivity;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;

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

    private GoogleApiClient mGoogleApiClient;
    private Handler mWorkHandler;
    private Handler mUiHandler;
    private HandlerThread mHandlerThread;

    private View mView;
    private Context mContext;
    private LinearLayout mActivateButton;
    private ProgressDialog mProgressDialog;
    private EditText mUsername;
    private EditText mPassword;
    private TextView mForgotPassword;
    private TextView mGoogleActivate;
    private TextView mErrorMessage;
    private Snackbar mSnackbar;

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

        mHandlerThread = new HandlerThread(LoadingActivity.class.getSimpleName());
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper());
        mUiHandler = new Handler();
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

        mActivateButton = (LinearLayout) view.findViewById(R.id.activate);
        mUsername = ((EditText) view.findViewById(R.id.username));
        mPassword = ((EditText) view.findViewById(R.id.password));
        mForgotPassword = (TextView) view.findViewById(R.id.forget_password);
        mGoogleActivate = (TextView) view.findViewById(R.id.google_activate);
        mErrorMessage = (TextView) view.findViewById(R.id.error_msg);
        mSnackbar = Snackbar.make(view, R.string.activate_require_read_phone_state_permission, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.got_to_enable_permission, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String packageName = getContext().getPackageName();
                        Intent teraPermissionSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
                        teraPermissionSettings.addCategory(Intent.CATEGORY_DEFAULT);
                        teraPermissionSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(teraPermissionSettings);
                    }
                });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View currentFocusView = ((Activity) mContext).getCurrentFocus();
                if (currentFocusView != null) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
                }

                final String username = mUsername.getText().toString();
                final String password = mPassword.getText().toString();
                if (username.isEmpty() || password.isEmpty()) {
                    mErrorMessage.setText(R.string.activate_require_username_password);
                } else {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        if (NetworkUtils.isNetworkConnected(mContext)) {
                            showProgressDialog();

                            final MgmtCluster.UserAuthParam authParam = new MgmtCluster.UserAuthParam();
                            authParam.setUsername(username);
                            authParam.setPassword(password);
                            authParam.setImei(HCFSMgmtUtils.getEncryptedDeviceImei(HCFSMgmtUtils.getDeviceImei(mContext)));
                            authParam.setVendor(Build.BRAND);
                            authParam.setModel(Build.MODEL);
                            authParam.setAndroidVersion(Build.VERSION.RELEASE);
                            authParam.setHcfsVersion("1.0.1");

                            MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                            authProxy.setOnAuthListener(new MgmtCluster.AuthListener() {
                                @Override
                                public void onAuthSuccessful(final AuthResultInfo authResultInfo) {
                                    MgmtCluster.RegisterProxy registerProxy = new MgmtCluster.RegisterProxy(authParam, authResultInfo.getToken());
                                    registerProxy.setOnRegisterListener(new MgmtCluster.RegisterListener() {
                                        @Override
                                        public void onRegisterSuccessful(final RegisterResultInfo registerResultInfo) {
                                            mWorkHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // TODO Set arkflex token to hcfs
                                                    // registerResultInfo.getStorageAccessToken();
                                                    final boolean failed = HCFSConfig.storeHCFSConfig(registerResultInfo);
                                                    if (failed) {
                                                        HCFSConfig.resetHCFSConfig();
                                                    } else {
                                                        AccountInfo accountInfo = new AccountInfo();
                                                        accountInfo.setName(username);

                                                        AccountDAO accountDAO = AccountDAO.getInstance(mContext);
                                                        accountDAO.clear();
                                                        accountDAO.insert(accountInfo);
                                                        accountDAO.close();

                                                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, true);
                                                        editor.apply();
                                                    }

                                                    mUiHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            hideProgressDialog();
                                                            if (failed) {
//                                                                mErrorMessage.setText(registerResultInfo.getMessage());
                                                                mErrorMessage.setText(R.string.activate_failed);
                                                            } else {
                                                                Intent intent = new Intent(mContext, MainActivity.class);
                                                                startActivity(intent);
                                                                ((Activity) mContext).finish();
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }

                                        @Override
                                        public void onRegisterFailed(RegisterResultInfo registerResultInfo) {
                                            Logs.e(CLASSNAME, "onRegisterFailed", "registerResultInfo=" + registerResultInfo.toString());

                                            hideProgressDialog();

                                            int errorMsgResId = R.string.activate_failed;
                                            if (registerResultInfo.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                                                if (registerResultInfo.getErrorCode().equals(MgmtCluster.IMEI_NOT_FOUND)) {
                                                    Bundle bundle = new Bundle();
                                                    bundle.putInt(KEY_AUTH_TYPE, MgmtCluster.USER_AUTH);
                                                    bundle.putString(KEY_USERNAME, username);
                                                    bundle.putString(KEY_PASSWORD, password);
                                                    bundle.putString(KEY_JWT_TOKEN, authResultInfo.getToken());

                                                    ActivateWithCodeFragment fragment = ActivateWithCodeFragment.newInstance();
                                                    fragment.setArguments(bundle);

                                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                    ft.replace(R.id.fragment_container, fragment, ActivateWithCodeFragment.TAG);
                                                    ft.commit();
                                                } else if (registerResultInfo.getErrorCode().equals(MgmtCluster.INCORRECT_MODEL) ||
                                                        registerResultInfo.getErrorCode().equals(MgmtCluster.INCORRECT_VENDOR)) {
                                                    errorMsgResId = R.string.activate_failed_not_supported_device;
                                                } else if (registerResultInfo.getErrorCode().equals(MgmtCluster.DEVICE_EXPIRED)) {
                                                    errorMsgResId = R.string.activate_failed_device_expired;
                                                }
                                            }
                                            mErrorMessage.setText(errorMsgResId);
                                        }

                                    });
                                    registerProxy.register();
                                }

                                @Override
                                public void onAuthFailed(AuthResultInfo authResultInfo) {
                                    Logs.e(CLASSNAME, "onAuthFailed", "authResultInfo=" + authResultInfo.toString());

                                    hideProgressDialog();
                                    mErrorMessage.setText(R.string.activate_auth_failed);
                                }
                            });
                            authProxy.auth();
                        } else {
                            mErrorMessage.setText(R.string.activate_alert_dialog_message);
                        }
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_PHONE_STATE)) {
                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
                            builder.setTitle(getString(R.string.alert_dialog_title_warning));
                            builder.setMessage(getString(R.string.activate_require_read_phone_state_permission));
                            builder.setPositiveButton(getString(R.string.alert_dialog_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_PHONE_STATE}, RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                        } else {
                            mSnackbar.show();
                        }
                    }
                }
            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View contentView = ((Activity) mContext).findViewById(android.R.id.content);
                if (contentView != null) {
                    Snackbar.make(contentView, "忘記密碼", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        mGoogleActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
                if (googleAPI.isGooglePlayServicesAvailable(mContext) != ConnectionResult.SUCCESS) {
                    mErrorMessage.setText(R.string.activate_without_google_play_services);
                    return;
                }

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    if (NetworkUtils.isNetworkConnected(mContext)) {
                        showProgressDialog();
                        mWorkHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String serverClientId = MgmtCluster.getServerClientId();
                                if (serverClientId != null) {
                                    // Request only the user's ID token, which can be used to identify the
                                    // user securely to your backend. This will contain the user's basic
                                    // profile (name, profile picture URL, etc) so you should not need to
                                    // make an additional call to personalize your application.
                                    final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                                            .requestServerAuthCode(serverClientId, false)
                                            .requestEmail()
                                            .build();

                                    mUiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mGoogleApiClient == null) {
                                                mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                                                        .enableAutoManage((AppCompatActivity) mContext, new GoogleApiClient.OnConnectionFailedListener() {
                                                            @Override
                                                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                                                // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
                                                                Logs.d(CLASSNAME, "onConnectionFailed", "connectionResult=" + connectionResult);
                                                                hideProgressDialog();

                                                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                                                                boolean hcfsActivated = sharedPreferences.getBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, false);
                                                                if (hcfsActivated) {
                                                                    Intent intent = new Intent(mContext, MainActivity.class);
                                                                    mContext.startActivity(intent);
                                                                    ((Activity) mContext).finish();
                                                                }
                                                            }
                                                        })
                                                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                                        .addApi(Plus.API)
                                                        .build();
                                            }

                                            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                                            ((Activity) mContext).startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
                                        }
                                    });
                                } else {
                                    mUiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                            mErrorMessage.setText(R.string.activate_get_server_client_id_failed);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        mErrorMessage.setText(R.string.activate_alert_dialog_message);
                    }
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_PHONE_STATE)) {
                        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(mContext);
                        builder.setTitle(getString(R.string.alert_dialog_title_warning));
                        builder.setMessage(getString(R.string.activate_require_read_phone_state_permission));
                        builder.setPositiveButton(getString(R.string.alert_dialog_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_PHONE_STATE}, RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                    } else {
                        View view = getView();
                        if (view != null) {
                            mSnackbar.show();
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSnackbar != null) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                mSnackbar.dismiss();
            }
        }

    }

    private void googleAuthFailed(String failedMsg) {
        Logs.e(CLASSNAME, "googleAuthFailed", "failedMsg=" + failedMsg);

        hideProgressDialog();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onGoogleAuthFailed", "status=" + status);
                    }
                });

        mErrorMessage.setText(R.string.activate_signin_google_account_failed);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Logs.w(CLASSNAME, "onActivityResult", "requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                final GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    final String serverAuthCode = acct.getServerAuthCode();
                    final String email = acct.getEmail();
                    final MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam();
                    authParam.setAuthCode(serverAuthCode);
                    authParam.setAuthBackend(MgmtCluster.GOOGLE_AUTH_BACKEND);
                    authParam.setImei(HCFSMgmtUtils.getEncryptedDeviceImei(HCFSMgmtUtils.getDeviceImei(mContext)));
                    authParam.setVendor(Build.BRAND);
                    authParam.setModel(Build.MODEL);
                    authParam.setAndroidVersion(Build.VERSION.RELEASE);
                    authParam.setHcfsVersion("1.0.1");

                    MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                    authProxy.setOnAuthListener(new MgmtCluster.AuthListener() {
                        @Override
                        public void onAuthSuccessful(final AuthResultInfo authResultInfo) {
                            MgmtCluster.RegisterProxy registerProxy = new MgmtCluster.RegisterProxy(authParam, authResultInfo.getToken());
                            registerProxy.setOnRegisterListener(new MgmtCluster.RegisterListener() {
                                @Override
                                public void onRegisterSuccessful(final RegisterResultInfo registerResultInfo) {
                                    mWorkHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            boolean isFailed = HCFSConfig.storeHCFSConfig(registerResultInfo);
                                            if (isFailed) {
                                                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                                                HCFSConfig.resetHCFSConfig();

                                                mUiHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        hideProgressDialog();
                                                        mErrorMessage.setText(R.string.activate_failed);
                                                    }
                                                });
                                            } else {
                                                String name = acct.getDisplayName();
                                                String email = acct.getEmail();
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
                                                accountDAO.close();

                                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, true);
                                                editor.apply();

                                                mUiHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        hideProgressDialog();
                                                    }
                                                });

                                                Intent intent = new Intent(mContext, MainActivity.class);
                                                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME, name);
                                                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL, email);
                                                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI, photoUrl);
                                                startActivity(intent);

                                                ((Activity) mContext).finish();
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onRegisterFailed(RegisterResultInfo registerResultInfo) {
                                    Logs.e(CLASSNAME, "onRegisterFailed", "registerResultInfo=" + registerResultInfo.toString());

                                    hideProgressDialog();

                                    Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                                            .setResultCallback(new ResultCallback<Status>() {
                                                @Override
                                                public void onResult(@NonNull Status status) {
                                                    Logs.d(CLASSNAME, "onRegisterFailed", "status=" + status);
                                                }
                                            });

                                    int errorMsgResId = R.string.activate_failed;
                                    if (registerResultInfo.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                                        if (registerResultInfo.getErrorCode().equals(MgmtCluster.IMEI_NOT_FOUND)) {
                                            Bundle bundle = new Bundle();
                                            bundle.putInt(KEY_AUTH_TYPE, MgmtCluster.GOOGLE_AUTH);
                                            bundle.putString(KEY_USERNAME, email);
                                            bundle.putString(KEY_AUTH_CODE, serverAuthCode);
                                            bundle.putString(KEY_JWT_TOKEN, authResultInfo.getToken());

                                            ActivateWithCodeFragment fragment = ActivateWithCodeFragment.newInstance();
                                            fragment.setArguments(bundle);

                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                                            ft.replace(R.id.fragment_container, fragment);
                                            ft.commit();
                                        } else if (registerResultInfo.getErrorCode().equals(MgmtCluster.INCORRECT_MODEL) ||
                                                registerResultInfo.getErrorCode().equals(MgmtCluster.INCORRECT_VENDOR)) {
                                            errorMsgResId = R.string.activate_failed_not_supported_device;
                                        } else if (registerResultInfo.getErrorCode().equals(MgmtCluster.DEVICE_EXPIRED)) {
                                            errorMsgResId = R.string.activate_failed_device_expired;
                                        }
                                    }
                                    mErrorMessage.setText(errorMsgResId);

                                }

                            });
                            registerProxy.register();
                        }

                        @Override
                        public void onAuthFailed(AuthResultInfo authResultInfo) {
                            Logs.e(CLASSNAME, "onAuthFailed", "authResultInfo=" + authResultInfo.toString());

                            hideProgressDialog();

                            Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                                    .setResultCallback(new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(@NonNull Status status) {
                                            Logs.d(CLASSNAME, "onAuthFailed", "status=" + status);
                                        }
                                    });

                            mErrorMessage.setText(R.string.activate_auth_failed);
                        }
                    });
                    authProxy.auth();

                } else {
                    String failedMsg = "acct is null";
                    googleAuthFailed(failedMsg);
                }
            } else {
                hideProgressDialog();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }

    }

    private void showProgressDialog() {
        Logs.w(CLASSNAME, "showProgressDialog", null);
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(getString(R.string.activate_processing_msg));
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        Logs.w(CLASSNAME, "hideProgressDialog", null);
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

}
