package com.hopebaytech.hcfsmgmt.main;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.RequestCode;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/18.
 */
public class SwitchAccountActivity extends AppCompatActivity {

    public static final String CLASSNAME = SwitchAccountActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private TextView mCurrentAccount;
    private LinearLayout mTargetAccountLayout;
    private TextView mTargetAccount;
    private TextView mErrorMsg;
    private Snackbar mSnackbar;
    private LinearLayout mSwitchAccountLayoutText;
    private TextView mSwitchAccount;
    private LinearLayout mSwitchAccountLayoutIcon;
    private String mServerClientId;
    private GoogleSignInOptions mGoogleSignInOptions;
    private ProgressDialog mProgressDialog;
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
     * Request code to use when launching the resolution activity
     */
    private static final int REQUEST_RESOLVE_ERROR = 1001;

    /**
     * Unique tag for the error dialog fragment
     */
    private static final String DIALOG_ERROR = "dialog_error";

    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String ORIGINAL_SERVER_AUTH_CODE = "original_server_auth_code";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swtich_account_activity);

        View contentView = findViewById(android.R.id.content);
        if (contentView != null) {
            mSnackbar = Snackbar.make(contentView, R.string.switch_account_require_read_phone_state_permission, Snackbar.LENGTH_INDEFINITE);
            mSnackbar.setAction(R.string.enable_permission, new View.OnClickListener() {
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
        mCurrentAccount = (TextView) findViewById(R.id.current_account);
        mTargetAccount = (TextView) findViewById(R.id.target_account);
        mErrorMsg = (TextView) findViewById(R.id.error_msg);
        mTargetAccountLayout = (LinearLayout) findViewById(R.id.target_account_layout);
        if (mTargetAccountLayout != null) {
            mTargetAccountLayout.setVisibility(View.INVISIBLE);
        }
        mSwitchAccountLayoutText = (LinearLayout) findViewById(R.id.switch_account_layout_text);
        if (mSwitchAccountLayoutText != null) {
            mSwitchAccountLayoutText.setVisibility(View.INVISIBLE);
        }
        mSwitchAccountLayoutIcon = (LinearLayout) findViewById(R.id.switch_account_layout_icon);
        if (mSwitchAccountLayoutIcon != null) {
            mSwitchAccountLayoutIcon.setVisibility(View.VISIBLE);
        }

        mSwitchAccount = (TextView) findViewById(R.id.switch_account);
        if (mSwitchAccount != null) {
            mSwitchAccount.setEnabled(false);
            mSwitchAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(SwitchAccountActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        showProgressDialog();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MgmtCluster.GoogleAuthParam authParam = new MgmtCluster.GoogleAuthParam();
                                authParam.setAuthCode(mOldServerAuthCode);
                                AuthResultInfo authResultInfo = MgmtCluster.auth(authParam);
                                if (authResultInfo.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                                    String imei = HCFSMgmtUtils.getEncryptedDeviceImei(HCFSMgmtUtils.getDeviceImei(SwitchAccountActivity.this));
                                    final boolean isSuccess = MgmtCluster.switchAccount(authResultInfo.getToken(), mNewServerAuthCode, imei);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                            if (isSuccess) {
                                                AccountInfo accountInfo = new AccountInfo();
                                                accountInfo.setName(mAccountName);
                                                accountInfo.setEmail(mAccountEmail);
                                                accountInfo.setImgUrl(mAccountPhotoUrl);

                                                AccountDAO accountDAO = AccountDAO.getInstance(SwitchAccountActivity.this);
                                                accountDAO.clear();
                                                accountDAO.insert(accountInfo);
                                                accountDAO.close();

                                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SwitchAccountActivity.this);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean(HCFSMgmtUtils.PREF_HCFS_ACTIVATED, true);
                                                editor.apply();

                                                Intent intent = new Intent(SwitchAccountActivity.this, LoadingActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                mErrorMsg.setText(R.string.switch_account_failed);
                                                signOut();
                                            }
                                        }
                                    });
                                } else {
                                    mErrorMsg.setText(R.string.switch_account_failed);
                                }
                            }
                        }).start();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SwitchAccountActivity.this);
                        builder.setTitle(R.string.alert_dialog_title_warning);
                        builder.setMessage(R.string.switch_account_require_read_phone_state_permission);
                        builder.setPositiveButton(R.string.alert_dialog_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(SwitchAccountActivity.this, Manifest.permission.READ_PHONE_STATE)) {
                                    ActivityCompat.requestPermissions(SwitchAccountActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, RequestCode.PERMISSIONS_REQUEST_READ_PHONE_STATE);
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
        }

        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {

                AccountDAO accountDAO = AccountDAO.getInstance(SwitchAccountActivity.this);
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
                accountDAO.close();

                mServerClientId = MgmtCluster.getServerClientId();
                if (mServerClientId != null) {
                    mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestServerAuthCode(mServerClientId, false)
                            .requestEmail()
                            .build();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGoogleApiClient = new GoogleApiClient.Builder(SwitchAccountActivity.this)
                                    .enableAutoManage(SwitchAccountActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                                        @Override
                                        public void onConnectionFailed(@NonNull ConnectionResult result) {
                                            Logs.d(CLASSNAME, "onConnectionFailed", "");
                                            if (!mResolvingError) {
                                                if (result.hasResolution()) {
                                                    try {
                                                        mResolvingError = true;
                                                        result.startResolutionForResult(SwitchAccountActivity.this, REQUEST_RESOLVE_ERROR);
                                                    } catch (IntentSender.SendIntentException e) {
                                                        // There was an error with the resolution intent. Try again.
                                                        mGoogleApiClient.connect();
                                                    }
                                                } else {
                                                    // Show dialog using GoogleApiAvailability.getErrorDialog()
                                                    showErrorDialog(result.getErrorCode());
                                                    mResolvingError = true;
                                                    hideProgressDialog();
                                                }
                                            }
                                        }
                                    })
                                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                        @Override
                                        public void onConnected(@Nullable Bundle bundle) {
                                            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                                            if (opr.isDone()) {
                                                GoogleSignInResult result = opr.get();
                                                String currentAuthCode = getServerAuthCode(result);
                                                if (mOldServerAuthCode == null) {
                                                    mOldServerAuthCode = currentAuthCode;
                                                }
                                                Logs.w(CLASSNAME, "onCreate", "serverAuthCode=" + mOldServerAuthCode);
                                                hideProgressDialog();
                                            } else {
                                                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                                    @Override
                                                    public void onResult(@NonNull GoogleSignInResult result) {
                                                        String currentAuthCode = getServerAuthCode(result);
                                                        if (mOldServerAuthCode == null) {
                                                            mOldServerAuthCode = currentAuthCode;
                                                        }
                                                        Logs.w(CLASSNAME, "onCreate", "serverAuthCode=" + mOldServerAuthCode);
                                                        hideProgressDialog();
                                                    }
                                                });
                                            }

                                            // Sign out previous account first
                                            signOut();
                                        }

                                        @Override
                                        public void onConnectionSuspended(int cause) {

                                        }
                                    })
                                    .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                                    .build();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                        }
                    });
                }
            }
        }).start();

        View.OnClickListener chooseAccountListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mServerClientId == null) {
                            mServerClientId = MgmtCluster.getServerClientId();
                            mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(mServerClientId)
                                    .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                                    .requestEmail()
                                    .build();
                        }

                        if (mServerClientId != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mGoogleApiClient == null) {
                                        mGoogleApiClient = new GoogleApiClient.Builder(SwitchAccountActivity.this)
                                                .enableAutoManage(SwitchAccountActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                                                    @Override
                                                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                                        Logs.e(CLASSNAME, "onConnectionFailed", "");
                                                    }
                                                })
                                                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                                                .addApi(Plus.API)
                                                .build();
                                    }

                                    if (mGoogleApiClient.isConnected()) {
                                        signOut();
                                        signIn();
                                    } else {
                                        Toast.makeText(SwitchAccountActivity.this, R.string.switch_account_google_connected_failed, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SwitchAccountActivity.this);
                                    builder.setTitle(R.string.alert_dialog_title_warning);
                                    builder.setMessage(R.string.activate_get_server_client_id_failed);
                                    builder.setPositiveButton(R.string.alert_dialog_confirm, null);
                                    builder.show();
                                }
                            });
                        }
                    }
                }).start();
            }
        };

        ImageView chooseAccountIcon = (ImageView) findViewById(R.id.choose_account_icon);
        if (chooseAccountIcon != null) {
            chooseAccountIcon.setOnClickListener(chooseAccountListener);
        }
        TextView chooseAccountText = (TextView) findViewById(R.id.choose_account_text);
        if (chooseAccountText != null) {
            chooseAccountText.setOnClickListener(chooseAccountListener);
        }

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
        startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void showProgressDialog() {
        hideProgressDialog();
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(getString(R.string.activate_processing_msg));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN) {
            hideProgressDialog();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.isSuccess()) {
                Logs.w(CLASSNAME, "onActivityResult", "result.isSuccess()=" + result.isSuccess());
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
                        ((FrameLayout) mSwitchAccount.getParent()).setBackgroundColor(ContextCompat.getColor(SwitchAccountActivity.this, android.R.color.darker_gray));
                        mSwitchAccount.setEnabled(false);
                        mErrorMsg.setText(R.string.switch_account_require_new_account);
                    } else {
                        ((FrameLayout) mSwitchAccount.getParent()).setBackgroundColor(ContextCompat.getColor(SwitchAccountActivity.this, R.color.colorAccent));
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
                    mGoogleApiClient.connect();
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

    private void showAlertConfirmDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SwitchAccountActivity.this);
        builder.setTitle(R.string.alert_dialog_title_warning);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.alert_dialog_confirm, null);
        builder.show();
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

        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((SwitchAccountActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        outState.putString(ORIGINAL_SERVER_AUTH_CODE, mOldServerAuthCode);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
            mOldServerAuthCode = savedInstanceState.getString(ORIGINAL_SERVER_AUTH_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSnackbar != null) {
            if (ContextCompat.checkSelfPermission(SwitchAccountActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                mSnackbar.dismiss();
            } else {
                mSnackbar.show();
            }
        }

    }
}
