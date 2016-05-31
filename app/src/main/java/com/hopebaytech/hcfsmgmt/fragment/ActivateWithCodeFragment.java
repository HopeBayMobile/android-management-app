package com.hopebaytech.hcfsmgmt.fragment;

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
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.hopebaytech.hcfsmgmt.main.LoadingActivity;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class ActivateWithCodeFragment extends Fragment {

    private final String CLASSNAME = ActivateWithCodeFragment.class.getSimpleName();

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private Handler mWorkHandler;
    private Handler mUiHandler;
    private HandlerThread mHandlerThread;
    private LinearLayout mActivateButton;
    private EditText mUsername;
    private EditText mPassword;
    private TextView mForgotPassword;
    private TextView mGoogleActivate;

    public static ActivateWithCodeFragment newInstance() {
        return new ActivateWithCodeFragment();
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
        return inflater.inflate(R.layout.activate_with_activation_code_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mActivateButton = (LinearLayout) view.findViewById(R.id.activate);
        mUsername = ((EditText) view.findViewById(R.id.username));
        mPassword = ((EditText) view.findViewById(R.id.password));
        mForgotPassword = (TextView) view.findViewById(R.id.forget_password);
        mGoogleActivate = (TextView) view.findViewById(R.id.google_activate);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = ((Activity) mContext).getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                final String username = mUsername.getText().toString();
                final String password = mPassword.getText().toString();
                if (username.isEmpty() || password.isEmpty()) {
                    showAlertDialog(mContext,
                            getString(R.string.alert_dialog_title_warning),
                            getString(R.string.activate_snackbar_require_username_password),
                            getString(R.string.alert_dialog_confirm));
                } else {
                    mWorkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (NetworkUtils.isNetworkConnected(mContext)) {
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showProgressDialog();
                                    }
                                });

                                String imei = HCFSMgmtUtils.getDeviceImei(mContext);
                                MgmtCluster.IAuthParam authParam = new MgmtCluster.NativeAuthParam(username, password, imei);
                                final AuthResultInfo authResultInfo = MgmtCluster.authWithMgmtCluster(authParam);
                                if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    boolean isFailed = HCFSConfig.storeHCFSConfig(authResultInfo);
                                    if (isFailed) {
                                        mUiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                String failureMessage = authResultInfo.getMessage();
                                                showAlertDialog(mContext,
                                                        getString(R.string.alert_dialog_title_warning),
                                                        failureMessage,
                                                        getString(R.string.alert_dialog_confirm));
                                            }
                                        });
                                        HCFSConfig.resetHCFSConfig();
                                    } else {
                                        Intent intent = new Intent(mContext, MainActivity.class);
                                        startActivity(intent);
                                        ((Activity) mContext).finish();
                                    }
                                } else {
                                    String message = authResultInfo.getMessage();
                                    String dialogMessage = "responseCode=" + authResultInfo.getResponseCode();
                                    if (message != null) {
                                        dialogMessage += ", message=" + message;
                                    }
                                    final String finalDialogMessage = dialogMessage;
                                    mUiHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showAlertDialog(mContext,
                                                    getString(R.string.alert_dialog_title_warning),
                                                    finalDialogMessage,
                                                    getString(R.string.alert_dialog_confirm));
                                        }
                                    });

                                }

                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgressDialog();
                                    }
                                });
                            } else {
                                showAlertDialog(mContext,
                                        getString(R.string.alert_dialog_title_warning),
                                        getString(R.string.activate_alert_dialog_message),
                                        getString(R.string.alert_dialog_confirm));
                            }
                        }
                    });
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
                if (NetworkUtils.isNetworkConnected(mContext)) {
                    showProgressDialog();
                    mWorkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String serverClientId = MgmtCluster.getServerClientIdFromMgmtCluster();
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
                                                        }
                                                    })
                                                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                                    .addApi(Plus.API)
                                                    .build();
                                        }

                                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                                        startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
                                    }
                                });
                            } else {
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgressDialog();
                                        showAlertDialog(mContext,
                                                getString(R.string.alert_dialog_title_warning),
                                                getString(R.string.failed_to_get_server_client_id),
                                                getString(R.string.alert_dialog_confirm));
                                    }
                                });
                            }
                        }
                    });
                } else {
                    showAlertDialog(mContext,
                            getString(R.string.alert_dialog_title_warning),
                            getString(R.string.activate_alert_dialog_message),
                            getString(R.string.alert_dialog_confirm));
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                String imei = HCFSMgmtUtils.getDeviceImei(mContext);
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                MgmtCluster.MgmtAuth mgmtAuth = new MgmtCluster.MgmtAuth(result, imei);
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
                                    showAlertDialog(mContext,
                                            getString(R.string.alert_dialog_title_warning),
                                            failureMessage,
                                            getString(R.string.alert_dialog_confirm));

                                    HCFSConfig.resetHCFSConfig();

                                    mUiHandler.post(new Runnable() {
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
                    public void onGoogleAuthFailed(String failedMsg) {
                        hideProgressDialog();
                        HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onGoogleAuthFailed", "failedMsg=" + failedMsg);

                        showAlertDialog(mContext,
                                getString(R.string.alert_dialog_title_warning),
                                getString(R.string.activate_failed_to_signin_google_account),
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

                        if (authResultInfo.getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                            ActivateWoCodeFragment fragment = ActivateWoCodeFragment.newInstance();
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_container, fragment);
                            ft.commit();
                        } else {
                            String message = authResultInfo.getMessage();
                            String dialogMessage = "responseCode=" + authResultInfo.getResponseCode();
                            if (message != null) {
                                dialogMessage += ", message=" + message;
                            }
                            showAlertDialog(mContext,
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
                    }
                });
                mgmtAuth.authenticate();
            }

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
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(getString(R.string.activate_processing_msg));
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void showAlertDialog(Context context, String title, String message, String positiveText) {
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
