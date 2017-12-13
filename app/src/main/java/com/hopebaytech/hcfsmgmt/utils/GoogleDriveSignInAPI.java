package com.hopebaytech.hcfsmgmt.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

public class GoogleDriveSignInAPI implements ConnectionCallbacks, OnConnectionFailedListener {

    public static final String GOOGLE_API_CONSOLE_CLIENT_ID =
            "795577377875-1tj6olgu34bqi7afnnmavvm5hj5vh1tr.apps.googleusercontent.com";
    public static final String GOOGLE_API_CONSOLE_CLIENT_SRCRECT = "tiHzFJPeOmVHanDI6QaRxVqX";
    private static String GOOGLE_API_CONSOLE_REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    private static int SHARED_PREFERENCES_OPERATING_MODE = Context.MODE_PRIVATE;
    private static String KEY_PREFERENCES_NAME = "google_drive_sign_in";
    private static String KEY_REFRESH_TOKEN = "refresh_token";

    private static Scope SCOPE_DRIVE = new Scope("https://www.googleapis.com/auth/drive");

    private Context mContext;

    private GoogleSignInAccount mSignInAccount;
    private Callback mCallback;

    private GoogleTokenResponse mTokenResponse;
    private String mRefreshToken;
    private String mAccessToken;

    private final GoogleSignInOptions GOOGLE_SIGN_IN_OPTIONS =
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestServerAuthCode(GOOGLE_API_CONSOLE_CLIENT_ID, true)
                    .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER, SCOPE_DRIVE)
                    .requestProfile()
                    .requestEmail()
                    .build();

    private GoogleApiClient mGoogleApiClient;

    public GoogleDriveSignInAPI(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
        mRefreshToken = getRefreshToken(mContext);

        initClient();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    private void initClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Drive.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, GOOGLE_SIGN_IN_OPTIONS)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void silentSignIn() {
        if (!mGoogleApiClient.isConnected()) {
            Logs.d("client is not connected");
            if (mCallback != null) {
                signIn();
            }
            return;
        }

        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        Logs.d("pendingResult = " + pendingResult);
        if (pendingResult != null) {
            handleGooglePendingResult(pendingResult);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        ((Activity) mContext).startActivityForResult(signInIntent, RequestCode.GOOGLE_SIGN_IN);
    }

    private void signOut() {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Logs.d("status=" + status);
                    }
                });
    }

    private boolean requestToken() {
        try {
            mTokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    GOOGLE_API_CONSOLE_CLIENT_ID,
                    GOOGLE_API_CONSOLE_CLIENT_SRCRECT,
                    mSignInAccount.getServerAuthCode(),
                    GOOGLE_API_CONSOLE_REDIRECT_URI)
                    .execute();
            Logs.d("mTokenResponse = " + mTokenResponse);

            if (mTokenResponse != null) {
                String newRefreshToken = mTokenResponse.getRefreshToken();
                Logs.d("newRefreshToken = " + newRefreshToken);
                if (!TextUtils.isEmpty(newRefreshToken)) {
                    mRefreshToken = newRefreshToken;
                    setRefreshToken(mContext, mRefreshToken);
                }
                mAccessToken = mTokenResponse.getAccessToken();
                Logs.d("mAccessToken = " + mAccessToken);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void refreshToken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (requestToken()) {
                    Logs.d("mCallback = " + mCallback);
                    if (mCallback != null) {
                        mCallback.onTokenResponse(mRefreshToken, mAccessToken);
                    }
                }
            }
        }).start();
    }

    public void handleSignInResult(Intent data) {
        final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        Logs.d("result = " + result + " success = " + result.isSuccess());
        boolean isSuccess = false;
        if (result.isSuccess()) {
            mSignInAccount = result.getSignInAccount();
            if (mSignInAccount != null) {
                isSuccess = true;
                refreshToken();
            }
        }
        if (mCallback != null) {
            mCallback.onSignIn(isSuccess);
        }
    }

    private void onSilentSignInCompleted(GoogleSignInResult result) {
        Logs.d("result = " + result + " success = " + result.isSuccess());
        boolean isSuccess = false;
        if (result.isSuccess()) {
            mSignInAccount = result.getSignInAccount();
            if (mSignInAccount != null) {
                isSuccess = true;
                refreshToken();
            }
            if (mCallback != null) {
                mCallback.onSignIn(isSuccess);
            }
        } else {
            signIn();
        }
    }

    private void handleGooglePendingResult(OptionalPendingResult<GoogleSignInResult> pendingResult) {
        Logs.d("pendingResult.isDone() = " + pendingResult.isDone());
        if (pendingResult.isDone()) {
            GoogleSignInResult result = pendingResult.get();
            onSilentSignInCompleted(result);
        } else {
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    onSilentSignInCompleted(result);
                }
            });
        }
    }

    public static String getRefreshToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                KEY_PREFERENCES_NAME, SHARED_PREFERENCES_OPERATING_MODE);
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    private static void setRefreshToken(Context context, String refreshToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                KEY_PREFERENCES_NAME, SHARED_PREFERENCES_OPERATING_MODE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(KEY_REFRESH_TOKEN, refreshToken);
        sharedPreferencesEditor.commit();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Logs.d("bundle = " + bundle);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Logs.d("cause = " + cause);
    }


    int mRetryConnectCount;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logs.d("connectionResult = " + connectionResult);
        if (connectionResult.getErrorCode() == ConnectionResult.INTERNAL_ERROR) {
            if (connectionResult.hasResolution()) {
                if (mContext instanceof Activity) {
                    try {
                        connectionResult.startResolutionForResult(((Activity) mContext),
                                RequestCode.GOOGLE_SIGN_IN_RESOLVE_ERROR);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (mRetryConnectCount < 5) {
                    mRetryConnectCount++;
                    Logs.d("mRetryConnectCount = " + mRetryConnectCount);
                }
            }
        }
    }

    public static abstract class Callback {
        public void onSignIn(boolean isSuccess) { }
        public void onTokenResponse(String refreshToken, String accessToken) { }
    }
}
