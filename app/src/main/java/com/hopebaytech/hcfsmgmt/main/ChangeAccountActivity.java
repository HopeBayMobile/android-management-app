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
package com.hopebaytech.hcfsmgmt.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.utils.GoogleSignInApiClient;
import com.hopebaytech.hcfsmgmt.utils.GoogleSilentAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.ProgressDialogUtils;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;
import com.hopebaytech.hcfsmgmt.utils.ThreadPool;

import java.net.HttpURLConnection;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/18.
 */
public class ChangeAccountActivity extends AppCompatActivity {

    public static final String CLASSNAME = ChangeAccountActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    private TextView mCurrentAccount;
    private LinearLayout mTargetAccountLayout;
    private TextView mTargetAccount;
    private TextView mErrorMsg;
    private Snackbar mSnackbar;
    private LinearLayout mSwitchAccountLayoutText;
    private TextView mSwitchAccount;
    private LinearLayout mSwitchAccountLayoutIcon;
    private GoogleSignInOptions mGoogleSignInOptions;
    private ProgressDialogUtils mProgressDialog;
    private ImageView mChooseAccountIcon;
    private TextView mChooseAccountText;

    private String mServerClientId;
    private String mOldServerAuthCode;
    private String mNewServerAuthCode;
    private String mAccountEmail;
    private String mAccountName;
    private String mAccountPhotoUrl;

    /**
     * Bool to track whether the app is already resolving an error
     */
    private boolean mResolvingError;

    /**
     * Account has been changed or not.
     */
    private boolean mAccountChanged;

    /**
     * Request code to use when launching the resolution activity
     */
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    /**
     * Unique tag for the error dialog fragment
     */
    private static final String DIALOG_ERROR = "dialog_error";

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_account_activity);

        init();
        setupListener();
    }

    private void findViews() {
        mCurrentAccount = (TextView) findViewById(R.id.current_account);
        mTargetAccount = (TextView) findViewById(R.id.target_account);
        mErrorMsg = (TextView) findViewById(R.id.error_msg);
        mTargetAccountLayout = (LinearLayout) findViewById(R.id.target_account_layout);
        mSwitchAccountLayoutText = (LinearLayout) findViewById(R.id.switch_account_layout_text);
        mSwitchAccountLayoutIcon = (LinearLayout) findViewById(R.id.swift_account_info_layout);
        mSwitchAccount = (TextView) findViewById(R.id.switch_account);
        mChooseAccountIcon = (ImageView) findViewById(R.id.choose_account_icon);
        mChooseAccountText = (TextView) findViewById(R.id.choose_account_text);
    }

    private void setupListener() {
        mSwitchAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ChangeAccountActivity.this,
                        Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    mProgressDialog.show();

                    MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam(mOldServerAuthCode);
                    MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                    authProxy.setOnAuthListener(new MgmtCluster.OnAuthListener() {
                        @Override
                        public void onAuthSuccessful(final AuthResultInfo authResultInfo) {
                            changeAccount(authResultInfo.getToken());
                        }

                        @Override
                        public void onAuthFailed(AuthResultInfo authResultInfo) {
                            Logs.e(CLASSNAME, "onAuthFailed",
                                    "responseCode=" + authResultInfo.getResponseCode() +
                                            ", responseContent=" + authResultInfo.getResponseContent());

                            if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                                // Auth code is expired so that authenticate with Google failed
                                mErrorMsg.setText(R.string.change_account_auth_code_expired);
                            } else if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                                mErrorMsg.setText(
                                        MgmtCluster.ErrorCode.getErrorMessage(
                                                ChangeAccountActivity.this,
                                                authResultInfo.getErrorCode()
                                        )
                                );
                            } else {
                                mErrorMsg.setText(R.string.change_account_failed);
                            }
                            signOut();
                            mProgressDialog.dismiss();
                        }
                    });
                    authProxy.auth();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeAccountActivity.this);
                    builder.setTitle(R.string.alert_dialog_title_warning);
                    builder.setMessage(R.string.change_account_require_read_phone_state_permission);
                    builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChangeAccountActivity.this, Manifest.permission.READ_PHONE_STATE)) {
                                ActivityCompat.requestPermissions(ChangeAccountActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
                            } else {
                                View contentView = findViewById(android.R.id.content);
                                if (contentView != null) {
                                    Snackbar.make(contentView, R.string.require_read_phone_state_permission, Snackbar.LENGTH_INDEFINITE).show();
                                }
                            }
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
            }
        });

        View.OnClickListener chooseAccountListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mServerClientId == null) {
                            mServerClientId = MgmtCluster.getServerClientId();
                        }
                        if (mGoogleSignInOptions == null) {
                            mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                                    .requestServerAuthCode(mServerClientId)
                                    .requestEmail()
                                    .build();
                        }

                        if (mServerClientId != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mGoogleApiClient = new GoogleApiClient.Builder(ChangeAccountActivity.this)
                                            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                                                @Override
                                                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                                    Logs.e(CLASSNAME, "onConnectionFailed", "");
                                                }
                                            })
                                            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                                @Override
                                                public void onConnected(@Nullable Bundle bundle) {
                                                    signOut();
                                                    signIn();
                                                }

                                                @Override
                                                public void onConnectionSuspended(int i) {

                                                }
                                            })
                                            .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                                            .addApi(Plus.API)
                                            .build();
                                    mGoogleApiClient.disconnect();
                                    mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeAccountActivity.this);
                                    builder.setTitle(R.string.alert_dialog_title_warning);
                                    builder.setMessage(R.string.activate_get_server_client_id_failed);
                                    builder.setPositiveButton(R.string.confirm, null);
                                    builder.show();
                                }
                            });
                        }
                    }
                }).start();
            }
        };
        mChooseAccountIcon.setOnClickListener(chooseAccountListener);
        mChooseAccountText.setOnClickListener(chooseAccountListener);
    }

    private void init() {
        findViews();

        mProgressDialog = new ProgressDialogUtils(this);
        mProgressDialog.setMessage(getString(R.string.processing_msg));

        View contentView = findViewById(android.R.id.content);
        if (contentView != null) {
            mSnackbar = Snackbar.make(contentView, R.string.change_account_require_read_phone_state_permission, Snackbar.LENGTH_INDEFINITE);
            mSnackbar.setAction(R.string.go, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String packageName = getPackageName();
                    Intent teraPermissionSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName));
                    teraPermissionSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    teraPermissionSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(teraPermissionSettings);
                }
            });
        }

        mTargetAccountLayout.setVisibility(View.INVISIBLE);
        mSwitchAccountLayoutText.setVisibility(View.INVISIBLE);
        mSwitchAccountLayoutIcon.setVisibility(View.VISIBLE);
        mSwitchAccount.setEnabled(false);

        mProgressDialog.show();
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                AccountDAO accountDAO = AccountDAO.getInstance(ChangeAccountActivity.this);
                List<AccountInfo> accountInfoList = accountDAO.getAll();
                if (accountInfoList.size() != 0) {
                    AccountInfo info = accountInfoList.get(0);
                    final String email = info.getEmail();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCurrentAccount.setText(email);
                        }
                    });
                }
            }
        });

        mServerClientId = MgmtCluster.getServerClientId();
        GoogleSignInApiClient signInApiClient = new GoogleSignInApiClient(
                ChangeAccountActivity.this,
                mServerClientId,
                new GoogleSignInApiClient.OnConnectionListener() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle, GoogleApiClient googleApiClient) {
                        mGoogleApiClient = googleApiClient;

                        OptionalPendingResult<GoogleSignInResult> opr =
                                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                        if (opr.isDone()) {
                            GoogleSignInResult result = opr.get();
                            final String currentAuthCode = getServerAuthCode(result);
                            Logs.d(CLASSNAME, "onConnected", "currentAuthCode=" + currentAuthCode);
                            mOldServerAuthCode = currentAuthCode;
                        } else {
                            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                @Override
                                public void onResult(@NonNull GoogleSignInResult result) {
                                    final String currentAuthCode = getServerAuthCode(result);
                                    Logs.d(CLASSNAME, "onResult", "currentAuthCode=" + currentAuthCode);
                                    mOldServerAuthCode = currentAuthCode;
                                }
                            });
                        }

                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        Logs.d(CLASSNAME, "onConnectionFailed", null);
                        if (!mResolvingError) {
                            if (result.hasResolution()) {
                                try {
                                    mResolvingError = true;
                                    result.startResolutionForResult(ChangeAccountActivity.this, REQUEST_RESOLVE_ERROR);
                                } catch (IntentSender.SendIntentException e) {
                                    // There was an error with the resolution intent. Try again.
                                    mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
                                }
                            } else {
                                // Show dialog using GoogleApiAvailability.getErrorDialog()
                                showErrorDialog(result.getErrorCode());
                                mResolvingError = true;
                                mProgressDialog.dismiss();
                            }
                        }

                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        mProgressDialog.dismiss();
                    }
                });
        signInApiClient.connect();
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Logs.d(CLASSNAME, "signOut", "onResult", "status=" + status);
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RequestCode.GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCode.GOOGLE_SIGN_IN) {
            mProgressDialog.dismiss();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    mAccountEmail = acct.getEmail();
                    mAccountName = acct.getDisplayName();
                    mNewServerAuthCode = acct.getServerAuthCode();
                    if (acct.getPhotoUrl() != null) {
                        mAccountPhotoUrl = acct.getPhotoUrl().toString();
                    }

                    mSwitchAccountLayoutIcon.setVisibility(View.INVISIBLE);
                    mSwitchAccountLayoutText.setVisibility(View.VISIBLE);
                    mTargetAccountLayout.setVisibility(View.VISIBLE);

                    String currentAccount = mCurrentAccount.getText().toString();
                    mTargetAccount.setText(mAccountEmail);
                    if (currentAccount.equals(mAccountEmail)) {
                        ((FrameLayout) mSwitchAccount.getParent()).setBackgroundColor(
                                ContextCompat.getColor(ChangeAccountActivity.this, android.R.color.darker_gray));
                        mSwitchAccount.setEnabled(false);
                        mErrorMsg.setText(R.string.change_account_require_new_account);
                    } else {
                        ((FrameLayout) mSwitchAccount.getParent()).setBackgroundColor(
                                ContextCompat.getColor(ChangeAccountActivity.this, R.color.colorAccent));
                        mSwitchAccount.setEnabled(true);
                        mErrorMsg.setText("");
                    }
                } else {
                    Logs.w(CLASSNAME, "onCreate", "acct == null");
                }
            }
        } else if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
                }
            }
        }

    }

    @Nullable
    private String getServerAuthCode(GoogleSignInResult result) {
        String serverAuthCode = null;
        if (result != null && result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                serverAuthCode = acct.getServerAuthCode();
            }
        }
        return serverAuthCode;
    }

    private void changeAccount(String jwtToken) {
        if (jwtToken != null) {
            changeAccountWithJwtToken(jwtToken);
        } else {
            changeAccountWithoutJwtToken();
        }
    }

    private void changeAccountWithJwtToken(String jwtToken) {
        Logs.d(CLASSNAME, "changeAccountWithJwtToken", "jwtToken=" + jwtToken);

        String imei = HCFSMgmtUtils.getDeviceImei(ChangeAccountActivity.this);
        MgmtCluster.ChangeAccountProxy proxy =
                new MgmtCluster.ChangeAccountProxy(jwtToken, imei, mNewServerAuthCode);
        proxy.setOnChangeAccountListener(new MgmtCluster.ChangeAccountProxy.OnChangeAccountListener() {
            @Override
            public void onChangeAccountSuccessful(DeviceServiceInfo deviceServiceInfo) {
                mAccountChanged = true;

                AccountInfo accountInfo = new AccountInfo();
                accountInfo.setName(mAccountName);
                accountInfo.setEmail(mAccountEmail);
                accountInfo.setImgUrl(mAccountPhotoUrl);

                AccountDAO accountDAO = AccountDAO.getInstance(ChangeAccountActivity.this);
                accountDAO.clear();
                accountDAO.insert(accountInfo);

                TeraCloudConfig.storeHCFSConfig(deviceServiceInfo, ChangeAccountActivity.this);
                TeraAppConfig.enableApp(ChangeAccountActivity.this);

                String url = deviceServiceInfo.getBackend().getUrl();
                String token = deviceServiceInfo.getBackend().getToken();
                HCFSMgmtUtils.setSwiftToken(url, token);

                Intent intent = new Intent(ChangeAccountActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onChangeAccountFailed(DeviceServiceInfo deviceServiceInfo) {
                Logs.e(CLASSNAME, "onChangeAccountFailed", "deviceServiceInfo=" + deviceServiceInfo);
                if (deviceServiceInfo.getResponseCode() == HttpsURLConnection.HTTP_FORBIDDEN) {
                    changeAccountWithoutJwtToken();
                } else if (deviceServiceInfo.getResponseCode() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                    mErrorMsg.setText(deviceServiceInfo.getErrorMessage(ChangeAccountActivity.this));
                    mProgressDialog.dismiss();
                } else {
                    signOut();
                    mErrorMsg.setText(R.string.change_account_failed);
                    mProgressDialog.dismiss();
                }
            }
        });
        proxy.change();
    }

    private void changeAccountWithoutJwtToken() {
        Logs.d(CLASSNAME, "changeAccountWithoutJwtToken", null);
        String serverClientId = MgmtCluster.getServerClientId();
        GoogleSilentAuthProxy googleAuthProxy = new GoogleSilentAuthProxy(ChangeAccountActivity.this,
                serverClientId, new GoogleSilentAuthProxy.OnAuthListener() {
            @Override
            public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct == null) {
                    changeAccountWithoutJwtToken();
                    return;
                }
                String serverAuthCode = acct.getServerAuthCode();
                MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam(serverAuthCode);
                MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                authProxy.setOnAuthListener(new MgmtCluster.OnAuthListener() {
                    @Override
                    public void onAuthSuccessful(AuthResultInfo authResultInfo) {
                        String jwtToken = authResultInfo.getToken();
                        changeAccountWithJwtToken(jwtToken);
                    }

                    @Override
                    public void onAuthFailed(AuthResultInfo authResultInfo) {
                        Logs.e(CLASSNAME, "changeAccountWithoutJwtToken", "onAuthFailed",
                                "authResultInfo=" + authResultInfo);
                        int responseCode = authResultInfo.getResponseCode();
                        if (responseCode == HttpsURLConnection.HTTP_FORBIDDEN) {
                            changeAccountWithoutJwtToken();
                        } else {
                            mErrorMsg.setText(R.string.change_account_failed);
                        }
                    }
                });
                authProxy.auth();
            }

            @Override
            public void onAuthFailed(GoogleSignInResult result) {
                Logs.e(CLASSNAME, "changeAccountWithoutJwtToken", "onAuthFailed", null);
                mProgressDialog.dismiss();
            }

        });
        googleAuthProxy.auth();
    }

    /**
     * Creates a dialog for an error message
     */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), ErrorDialogFragment.TAG);
    }

    /**
     * Called from ErrorDialogFragment when the dialog is dismissed.
     */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /**
     * A fragment to display an error dialog
     */
    public static class ErrorDialogFragment extends DialogFragment {

        public static final String TAG = ErrorDialogFragment.class.getSimpleName();

        private Context mContext;

        public ErrorDialogFragment() {
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = getActivity();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog((Activity) mContext, errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((ChangeAccountActivity) mContext).onDialogDismissed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSnackbar != null) {
            if (ContextCompat.checkSelfPermission(ChangeAccountActivity.this,
                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                mSnackbar.dismiss();
            } else {
                mSnackbar.show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If user don't complete the change account process then leave Tera app, we need to sign
        // out the current user so that the google account chooser dialog will appear when user
        // click the Google login icon in login page.
        if (!mAccountChanged) {
            signOut();
        }
    }
}
