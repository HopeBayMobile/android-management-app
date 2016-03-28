package com.hopebaytech.hcfsmgmt.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.fragment.SettingsFragment;
import com.hopebaytech.hcfsmgmt.utils.HCFSApiUtils;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LoadingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final String CLASSNAME = getClass().getSimpleName();
    private Handler mHandler;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);

        HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName());
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        init();
    }

    public void init() {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", null);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isActivated()) {
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", "Activated");
                    final String serverClientId = HCFSMgmtUtils.getServerClientIdFromMgmtServer();
                    if (serverClientId != null) {
                        Thread getGoogleAccountInfoThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                /** GoogleApiClient should be called from main thread of process */
                                final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(serverClientId)
                                        .requestEmail()
                                        .build();

                                mGoogleApiClient = new GoogleApiClient.Builder(LoadingActivity.this)
                                        .enableAutoManage(LoadingActivity.this, LoadingActivity.this)
                                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                        .build();

                                OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                                if (opr.isDone()) {
                                    GoogleSignInResult googleSignInResult = opr.get();
                                    handleSignInResult(googleSignInResult);
                                } else {
                                    opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                        @Override
                                        public void onResult(GoogleSignInResult googleSignInResult) {
                                            handleSignInResult(googleSignInResult);
                                        }
                                    });
                                }
                            }
                        });
                        runOnUiThread(getGoogleAccountInfoThread);
                        try {
                            getGoogleAccountInfoThread.join();
                        } catch (InterruptedException e) {
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "init", Log.getStackTraceString(e));
                        }
                    } else {
                        handleSignInResult(null);
                    }
                } else {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "init", "NOT Activated");
                    Intent intent = new Intent(LoadingActivity.this, ActivateCloudStorageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private boolean isActivated() {
        return !HCFSMgmtUtils.getHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT).isEmpty();
    }

    private void handleSignInResult(@Nullable GoogleSignInResult result) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "handleSignInResult", null);
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        if (result != null && result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME, acct.getDisplayName());
                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL, acct.getEmail());
                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI, acct.getPhotoUrl());
            }
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /** An unresolvable error has occurred and Google APIs (including Sign-In) will not be available. */
        HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onConnectionFailed", "" + connectionResult);
//        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
//        startActivity(intent);
        finish();
    }

}
