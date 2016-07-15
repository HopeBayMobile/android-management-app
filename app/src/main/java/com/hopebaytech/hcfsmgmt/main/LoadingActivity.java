package com.hopebaytech.hcfsmgmt.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Checkable;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.db.UidDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.AppInfo;
import com.hopebaytech.hcfsmgmt.info.FileDirInfo;
import com.hopebaytech.hcfsmgmt.info.GetDeviceInfo;
import com.hopebaytech.hcfsmgmt.info.ItemInfo;
import com.hopebaytech.hcfsmgmt.info.UidInfo;
import com.hopebaytech.hcfsmgmt.utils.DisplayTypeFactory;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.PinType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LoadingActivity extends AppCompatActivity {

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

//        String logMsg = "Build.BRAND=" + Build.BRAND +
//                ", Build.BOARD=" + Build.BOARD +
//                ", Build.BOOTLOADER=" + Build.BOOTLOADER +
//                ", Build.DEVICE=" + Build.DEVICE +
//                ", Build.HARDWARE=" + Build.HARDWARE +
//                ", Build.MANUFACTURER=" + Build.MANUFACTURER +
//                ", Build.MODEL=" + Build.MODEL +
//                ", Build.VERSION.RELEASE=" + Build.VERSION.RELEASE +
//                ", Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT +
//                ", Build.PRODUCT=" + Build.PRODUCT +
//                ", Build.SERIAL=" + Build.SERIAL;
//
//        Logs.w(CLASSNAME, "onCreate", logMsg);

        init();
    }

    public void init() {
        Logs.d(CLASSNAME, "init", null);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (HCFSConfig.isActivated(LoadingActivity.this)) {
                    Logs.d(CLASSNAME, "init", "Activated");
                    final String serverClientId = MgmtCluster.getServerClientId();
                    if (serverClientId != null) {
                        Thread getGoogleAccountInfoThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                                        .requestServerAuthCode(serverClientId, false)
                                        .requestEmail()
                                        .build();

                                // GoogleApiClient should be called from main thread of process
                                mGoogleApiClient = new GoogleApiClient.Builder(LoadingActivity.this)
                                        .enableAutoManage(LoadingActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                                            @Override
                                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                                // An unresolvable error has occurred and Google APIs (including Sign-In) will not be available.
                                                Logs.e(CLASSNAME, "onConnectionFailed", connectionResult.toString());

                                                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                                                Bundle bundle = getIntent().getExtras();
                                                if (bundle != null) {
                                                    intent.putExtras(bundle);
                                                }
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                        .build();

                                OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                                if (opr.isDone()) {
                                    GoogleSignInResult googleSignInResult = opr.get();
                                    handleSignInResult(googleSignInResult);
                                } else {
                                    opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                        @Override
                                        public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
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
                            Logs.d(CLASSNAME, "init", Log.getStackTraceString(e));
                        }
                    } else {
                        handleSignInResult(null);
                    }
                } else {
                    Logs.w(CLASSNAME, "init", "NOT Activated");
                    Intent intent = new Intent(LoadingActivity.this, ActivateActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void handleSignInResult(@Nullable GoogleSignInResult result) {
        Logs.d(CLASSNAME, "handleSignInResult", null);
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        if (result != null && result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                String name = acct.getDisplayName();
                String email = acct.getEmail();
                String photoUrl = null;
                if (acct.getPhotoUrl() != null) {
                    photoUrl = acct.getPhotoUrl().toString();
                }

                AccountDAO accountDAO = AccountDAO.getInstance(LoadingActivity.this);
                if (accountDAO.getCount() == 0) {
                    AccountInfo accountInfo = new AccountInfo();
                    accountInfo.setName(name);
                    accountInfo.setEmail(email);
                    accountInfo.setImgUrl(photoUrl);
                    accountDAO.insert(accountInfo);
                } else {
                    AccountInfo accountInfo = accountDAO.getAll().get(0);
                    if (System.currentTimeMillis() > accountInfo.getImgExpringTime()) {
                        accountInfo.setImgUrl(photoUrl);
                        accountInfo.setImgBase64(null);
                        accountInfo.setImgExpringTime(System.currentTimeMillis() + Interval.DAY);
                        accountDAO.update(accountInfo);
                    }
                }
                accountDAO.close();

                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME, acct.getDisplayName());
                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL, acct.getEmail());
                intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI, acct.getPhotoUrl());
            }
        }
        startActivity(intent);
        finish();
    }

}
