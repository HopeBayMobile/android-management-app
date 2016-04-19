package com.hopebaytech.hcfsmgmt.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

import java.util.List;
import java.util.Locale;

/**
 * Created by Aaron on 2016/4/18.
 */
public class ChangeAccountActivity extends AppCompatActivity {

    public static final String CLASSNAME = ChangeAccountActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private TextView mCurrentAccount;
    private TextView mTargetAccount;
    private String mServerClientId;
    private GoogleSignInOptions mGoogleSignInOptions;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_account_activity);

        mCurrentAccount = (TextView) findViewById(R.id.current_account);
        mTargetAccount = (TextView) findViewById(R.id.target_account);

        Button changeAccount = (Button) findViewById(R.id.change_account);
        if (changeAccount != null) {
            changeAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ChangeAccountActivity.this, "轉換", Toast.LENGTH_SHORT).show();
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
                            .requestIdToken(mServerClientId)
                            .requestEmail()
                            .build();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mGoogleApiClient = new GoogleApiClient.Builder(ChangeAccountActivity.this)
                                    .enableAutoManage(ChangeAccountActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                                        @Override
                                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onConnectionFailed", "");
                                        }
                                    })
                                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                        @Override
                                        public void onConnected(@Nullable Bundle bundle) {
                                            hideProgressDialog();
                                        }

                                        @Override
                                        public void onConnectionSuspended(int i) {

                                        }
                                    })
                                    .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                                    .build();

//                            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
//                            if (opr.isDone()) {
//                                HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "opr.isDone()");
//                                GoogleSignInResult googleSignInResult = opr.get();
//                                handleSignInResult(googleSignInResult, mCurrentAccount);
//                            } else {
//                                HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "!opr.isDone()");
//                                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
//                                    @Override
//                                    public void onResult(GoogleSignInResult googleSignInResult) {
//                                        handleSignInResult(googleSignInResult, mCurrentAccount);
//                                    }
//                                });
//                            }
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

        final SignInButton chooseAccount = (SignInButton) findViewById(R.id.choose_account);
//        setGoogleSignInButtonText(chooseAccount, "Google");
        if (chooseAccount != null) {
            chooseAccount.setSize(SignInButton.SIZE_ICON_ONLY);
            chooseAccount.setOnClickListener(new View.OnClickListener() {
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
                                        .requestEmail()
                                        .build();
                            }

                            if (mServerClientId != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chooseAccount.setScopes(mGoogleSignInOptions.getScopeArray());
                                        if (mGoogleApiClient == null) {
                                            mGoogleApiClient = new GoogleApiClient.Builder(ChangeAccountActivity.this)
                                                    .enableAutoManage(ChangeAccountActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                                                        @Override
                                                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onConnectionFailed", "");
                                                        }
                                                    })
                                                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                                        @Override
                                                        public void onConnected(@Nullable Bundle bundle) {

                                                        }

                                                        @Override
                                                        public void onConnectionSuspended(int i) {

                                                        }
                                                    })
                                                    .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
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
            });
        }

    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, this.getClass().getName(), "onGoogleAuthFailed", "status=" + status);
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
        /** Find the TextView that is inside of the SignInButton and set its text */
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                tv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                return;
            }
        }
    }

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
                    mTargetAccount.setText(acct.getEmail());
                } else {
                    HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "acct == null");
                }
            }
        }

    }

    private void handleSignInResult(@Nullable GoogleSignInResult result, TextView account) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "handleSignInResult", null);
        HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "result=" + result);
        if (result != null && result.isSuccess()) {
            HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "result.isSuccess()=" + result.isSuccess());
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                account.setText(acct.getEmail());
            } else {
                HCFSMgmtUtils.log(Log.WARN, CLASSNAME, "onCreate", "acct == null");
            }
        }
    }

}
