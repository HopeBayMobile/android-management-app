package com.hopebaytech.hcfsmgmt.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

import java.util.List;

/**
 * Created by Aaron on 2016/4/18.
 */
public class ChangeAccountActivity extends AppCompatActivity {

    public static final String CLASSNAME = ChangeAccountActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private TextView mCurrentAccount;
    private LinearLayout mTargetAccountLayout;
    private TextView mTargetAccount;
    private LinearLayout mSwitchAccountLayoutText;
    private TextView mSwitchAccount;
    private LinearLayout mSwitchAccountLayoutIcon;
    private String mServerClientId;
    private GoogleSignInOptions mGoogleSignInOptions;
    private ProgressDialog mProgressDialog;
    private String mServerAuthCode;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_account_activity);

        mCurrentAccount = (TextView) findViewById(R.id.current_account);
        mTargetAccount = (TextView) findViewById(R.id.target_account);
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

        mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        mSwitchAccount = (TextView) findViewById(R.id.switch_account);
        if (mSwitchAccount != null) {
            mSwitchAccount.setEnabled(false);
            mSwitchAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!MgmtCluster.changeAccount(mServerAuthCode)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showAlertConfirmDialog(getString(R.string.change_account_failed_to_change));
                                        signOut();
                                    }
                                });
                            }
                        }
                    }).start();
                }
            });
        }

        showProgressDialog();
        new Thread(new Runnable() {
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
                accountDAO.close();

                mServerClientId = MgmtCluster.getServerClientIdFromMgmtCluster();
                if (mServerClientId != null) {
                    mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                            .requestIdToken(mServerClientId)
                            .requestServerAuthCode(mServerClientId, false)
                            .requestEmail()
                            .build();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGoogleApiClient = new GoogleApiClient.Builder(ChangeAccountActivity.this)
                                    .enableAutoManage(ChangeAccountActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                                        @Override
                                        public void onConnectionFailed(@NonNull ConnectionResult result) {
                                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onConnectionFailed", "");
                                            if (!mResolvingError) {
                                                if (result.hasResolution()) {
                                                    try {
                                                        mResolvingError = true;
                                                        result.startResolutionForResult(ChangeAccountActivity.this, REQUEST_RESOLVE_ERROR);
                                                    } catch (IntentSender.SendIntentException e) {
                                                        /** There was an error with the resolution intent. Try again. */
                                                        mGoogleApiClient.connect();
                                                    }
                                                } else {
                                                    /** Show dialog using GoogleApiAvailability.getErrorDialog() */
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
                                                mServerAuthCode = getServerAuthCode(result);
                                                HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "serverAuthCode=" + mServerAuthCode);
                                                hideProgressDialog();
                                            } else {
                                                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                                    @Override
                                                    public void onResult(@NonNull GoogleSignInResult result) {
                                                        mServerAuthCode = getServerAuthCode(result);
                                                        HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "serverAuthCode=" + mServerAuthCode);
                                                        hideProgressDialog();
                                                    }
                                                });
                                            }

                                            if (mServerAuthCode != null) {
                                                signOut();
                                            }
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
                            mServerClientId = MgmtCluster.getServerClientIdFromMgmtCluster();
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
//                                        chooseAccount.setScopes(mGoogleSignInOptions.getScopeArray());
                                    if (mGoogleApiClient == null) {
                                        mGoogleApiClient = new GoogleApiClient.Builder(ChangeAccountActivity.this)
                                                .enableAutoManage(ChangeAccountActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                                                    @Override
                                                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                                        HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onConnectionFailed", "");
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
                                        Toast.makeText(ChangeAccountActivity.this, "請重新嘗試一次", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ChangeAccountActivity.this);
                                    builder.setTitle(getString(R.string.alert_dialog_title_warning));
                                    builder.setMessage(getString(R.string.failed_to_get_server_client_id));
                                    builder.setPositiveButton(getString(R.string.alert_dialog_confirm), null);
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
                        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, this.getClass().getName(), "onGoogleAuthFailed", "status=" + status);
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
    }

//    private void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
//        signInButton.setSize(SignInButton.SIZE_ICON_ONLY);
//        /** Find the TextView that is inside of the SignInButton and set its text */
//        for (int i = 0; i < signInButton.getChildCount(); i++) {
//            View v = signInButton.getChildAt(i);
//            if (v instanceof TextView) {
//                TextView tv = (TextView) v;
//                tv.setText(buttonText);
//                tv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
//                return;
//            }
//        }
//    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(getString(R.string.activate_cloud_storage_processing_msg));
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
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
                HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onActivityResult", "result.isSuccess()=" + result.isSuccess());
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    mSwitchAccountLayoutIcon.setVisibility(View.INVISIBLE);
                    mSwitchAccountLayoutText.setVisibility(View.VISIBLE);
                    mTargetAccountLayout.setVisibility(View.VISIBLE);
                    String currentAccount = mCurrentAccount.getText().toString();
                    String targetAccount = acct.getEmail();
                    mTargetAccount.setText(targetAccount);
                    if (currentAccount.equals(targetAccount)) {
//                        mSwitchAccount.setBackgroundColor(ContextCompat.getColor(ChangeAccountActivity.this, android.R.color.darker_gray));
                        ((FrameLayout) mSwitchAccount.getParent()).setBackgroundColor(ContextCompat.getColor(ChangeAccountActivity.this, android.R.color.darker_gray));
                        mSwitchAccount.setEnabled(false);

                        showAlertConfirmDialog(getString(R.string.change_account_require_new_account));
                    } else {
//                        mSwitchAccount.setBackgroundColor(ContextCompat.getColor(ChangeAccountActivity.this, R.color.colorAccent));
                        ((FrameLayout) mSwitchAccount.getParent()).setBackgroundColor(ContextCompat.getColor(ChangeAccountActivity.this, R.color.colorAccent));
                        mSwitchAccount.setEnabled(true);
                    }
                } else {
                    HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "acct == null");
                }
            }
        } else if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                /** Make sure the app is not already connected or attempting to connect */
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangeAccountActivity.this);
        builder.setTitle(getString(R.string.alert_dialog_title_warning));
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.alert_dialog_confirm), null);
        builder.show();
    }

    /**
     * Creates a dialog for an error message
     */
    private void showErrorDialog(int errorCode) {
        /** Create a fragment for the error dialog */
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        /** Pass the error that should be displayed */
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
            /** Get the error code and retrieve the appropriate dialog */
            int errorCode = getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance()
                    .getErrorDialog(getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((ChangeAccountActivity) getActivity()).onDialogDismissed();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }
}
