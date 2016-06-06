package com.hopebaytech.hcfsmgmt.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
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
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.net.HttpURLConnection;

public class ActivateCloudStorageActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final String CLASSNAME = this.getClass().getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private Handler mWorkHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_cloud_storage_activity);

        init();
    }

    private void init() {
        HandlerThread handlerThread = new HandlerThread(LoadingActivity.class.getSimpleName());
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());

        LinearLayout activate = (LinearLayout) findViewById(R.id.activate);
        if (activate != null) {
            activate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    View view = ActivateCloudStorageActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    final String username = ((EditText) findViewById(R.id.username)).getText().toString();
                    final String password = ((EditText) findViewById(R.id.password)).getText().toString();
                    if (username.isEmpty() || password.isEmpty()) {
                        showAlertDialog(ActivateCloudStorageActivity.this,
                                getString(R.string.alert_dialog_title_warning),
                                getString(R.string.activate_cloud_storage_snackbar_require_username_password),
                                getString(R.string.alert_dialog_confirm));
                    } else {
                        mWorkHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (NetworkUtils.isNetworkConnected(ActivateCloudStorageActivity.this)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showProgressDialog();
                                        }
                                    });

                                    MgmtCluster.IAuthParam authParam = new MgmtCluster.NativeAuthParam(username, password);
                                    final AuthResultInfo authResultInfo = MgmtCluster.authWithMgmtCluster(authParam);
                                    if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                        boolean isFailed = HCFSConfig.storeHCFSConfig(authResultInfo);
                                        if (isFailed) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String failureMessage = authResultInfo.getMessage();
                                                    showAlertDialog(ActivateCloudStorageActivity.this,
                                                            getString(R.string.alert_dialog_title_warning),
                                                            failureMessage,
                                                            getString(R.string.alert_dialog_confirm));
                                                }
                                            });
                                            HCFSConfig.resetHCFSConfig();
                                        } else {
                                            Intent intent = new Intent(ActivateCloudStorageActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        String message = authResultInfo.getMessage();
                                        String dialogMessage = "responseCode=" + authResultInfo.getResponseCode();
                                        if (message != null) {
                                            dialogMessage += ", message=" + message;
                                        }
                                        final String finalDialogMessage = dialogMessage;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showAlertDialog(ActivateCloudStorageActivity.this,
                                                        getString(R.string.alert_dialog_title_warning),
                                                        finalDialogMessage,
                                                        getString(R.string.alert_dialog_confirm));
                                            }
                                        });

                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                        }
                                    });
                                } else {
                                    showAlertDialog(ActivateCloudStorageActivity.this,
                                            getString(R.string.alert_dialog_title_warning),
                                            getString(R.string.activate_cloud_alert_dialog_message),
                                            getString(R.string.alert_dialog_confirm));
                                }
                            }
                        });
                    }
                }
            });
        }

        TextView forgetPassword = (TextView) findViewById(R.id.forget_password);
        if (forgetPassword != null) {
            forgetPassword.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    View contentView = findViewById(android.R.id.content);
                    if (contentView != null) {
                        Snackbar.make(contentView, "忘記密碼", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
        }

        final TextView googleActivate = (TextView) findViewById(R.id.google_activate);
        if (googleActivate != null) {
            googleActivate.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtils.isNetworkConnected(ActivateCloudStorageActivity.this)) {
                        showProgressDialog();
                        mWorkHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                String serverClientId = MgmtCluster.getServerClientIdFromMgmtCluster();
                                if (serverClientId != null) {
                                    /**
                                     * Request only the user's ID token, which can be used to identify the
                                     * user securely to your backend. This will contain the user's basic
                                     * profile (name, profile picture URL, etc) so you should not need to
                                     * make an additional call to personalize your application.
                                     */
                                    final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                                            .requestServerAuthCode(serverClientId, false)
                                            .requestEmail()
                                            .build();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mGoogleApiClient == null) {
                                                mGoogleApiClient = new GoogleApiClient.Builder(ActivateCloudStorageActivity.this)
                                                        .enableAutoManage(ActivateCloudStorageActivity.this, ActivateCloudStorageActivity.this)
                                                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                                        .addApi(Plus.API)
                                                        .build();
                                            }

                                            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                                            startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
                                        }
                                    });
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                            showAlertDialog(ActivateCloudStorageActivity.this,
                                                    getString(R.string.alert_dialog_title_warning),
                                                    getString(R.string.failed_to_get_server_client_id),
                                                    getString(R.string.alert_dialog_confirm));
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        showAlertDialog(ActivateCloudStorageActivity.this,
                                getString(R.string.alert_dialog_title_warning),
                                getString(R.string.activate_cloud_alert_dialog_message),
                                getString(R.string.alert_dialog_confirm));
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /** An unresolvable error has occurred and Google APIs (including Sign-In) will not be available. */
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onConnectionFailed", "connectionResult=" + connectionResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                MgmtCluster.MgmtAuth mgmtAuth = new MgmtCluster.MgmtAuth(Looper.getMainLooper(), result);
                mgmtAuth.setOnAuthListener(new MgmtCluster.AuthListener() {
                    @Override
                    public void onAuthSuccessful(final GoogleSignInAccount acct, final AuthResultInfo authResultInfo) {
                        mWorkHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                boolean isFailed = HCFSConfig.storeHCFSConfig(authResultInfo);
                                if (isFailed) {
                                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                                    String failureMessage = authResultInfo.getMessage();
                                    showAlertDialog(ActivateCloudStorageActivity.this,
                                            getString(R.string.alert_dialog_title_warning),
                                            failureMessage,
                                            getString(R.string.alert_dialog_confirm));

                                    HCFSConfig.resetHCFSConfig();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
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

                                    AccountDAO accountDAO = AccountDAO.getInstance(ActivateCloudStorageActivity.this);
                                    accountDAO.clear();
                                    accountDAO.insert(accountInfo);
                                    accountDAO.close();

                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ActivateCloudStorageActivity.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, true);
                                    editor.apply();

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                        }
                                    });

                                    Intent intent = new Intent(ActivateCloudStorageActivity.this, MainActivity.class);
                                    intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME, name);
                                    intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL, email);
                                    intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI, photoUrl);
                                    startActivity(intent);
                                    finish();

                                }

                            }
                        });

                    }

                    @Override
                    public void onGoogleAuthFailed(String failedMsg) {
                        hideProgressDialog();
                        HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onGoogleAuthFailed", "failedMsg=" + failedMsg);

                        showAlertDialog(ActivateCloudStorageActivity.this,
                                getString(R.string.alert_dialog_title_warning),
                                getString(R.string.activate_cloud_storage_failed_to_signin_google_account),
                                getString(R.string.alert_dialog_confirm));

                        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                                .setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull Status status) {
                                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onGoogleAuthFailed", "status=" + status);
                                    }
                                });
                    }

                    @Override
                    public void onMmgtAuthFailed(AuthResultInfo authResultInfo) {
                        hideProgressDialog();
                        HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onMmgtAuthFailed", "authResultInfo=" + authResultInfo.toString());

                        String message = authResultInfo.getMessage();
                        String dialogMessage = "responseCode=" + authResultInfo.getResponseCode();
                        if (message != null) {
                            dialogMessage += ", message=" + message;
                        }
                        showAlertDialog(ActivateCloudStorageActivity.this,
                                getString(R.string.alert_dialog_title_warning),
                                dialogMessage,
                                getString(R.string.alert_dialog_confirm));

                        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                                .setResultCallback(new ResultCallback<Status>() {
                                    @Override
                                    public void onResult(@NonNull Status status) {
                                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onMmgtAuthFailed", "status=" + status);
                                    }
                                });
                    }
                });
                mgmtAuth.authenticate();
            }

        }

    }

    private void showProgressDialog() {
        hideProgressDialog();
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(getString(R.string.activate_cloud_storage_processing_msg));
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
//        if (mProgressDialog != null && mProgressDialog.isShowing()) {
//            mProgressDialog.hide();
//        }
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }

    public static void showAlertDialog(Context context, String title, String message, String positiveText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

}
