package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;

/**
 * Wrapper the Google auth procedure to provide an user friendly API util
 *
 * @author Aaron
 *         Created by Aaron on 2016/7/7.
 */
public class GoogleAuthProxy {

    private static final String CLASSNAME = GoogleAuthProxy.class.getSimpleName();

    private Context mContext;
    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleApiClient mGoogleApiClient;
    private String mServerClientId;

    private OnAuthListener mOnAuthListener;

    public GoogleAuthProxy(Context context, String serverClientId) {
        this.mContext = context;
        this.mServerClientId = serverClientId;
    }

    public interface OnAuthListener {
        void onAuthSuccessful(GoogleSignInResult result);
        void onAuthFailed();
    }

    public void setOnAuthListener(@NonNull OnAuthListener listener) {
        this.mOnAuthListener = listener;
    }

    public void auth() {

        if (mServerClientId == null) {
            mServerClientId = MgmtCluster.getServerClientId();
            if (mServerClientId == null) {
                Logs.e(CLASSNAME, "auth", "mServerClientId=" + mServerClientId);
                mOnAuthListener.onAuthFailed();
                return;
            }
        }

        if (mGoogleSignInOptions == null) {
            mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                    .requestServerAuthCode(mServerClientId)
                    .requestEmail()
                    .build();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult result) {
                            Logs.d(CLASSNAME, "onConnectionFailed", null);
                            mOnAuthListener.onAuthFailed();
                        }
                    })
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            Logs.d(CLASSNAME, "onConnected", null);
                            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                            if (opr.isDone()) {
                                Logs.d(CLASSNAME, "onConnected", "opr.isDone()");
                                GoogleSignInResult result = opr.get();
                                if (result != null && result.isSuccess()) {
                                    mOnAuthListener.onAuthSuccessful(result);
                                } else {
                                    mOnAuthListener.onAuthFailed();
                                }
                                mGoogleApiClient.disconnect();
                            } else {
                                Logs.d(CLASSNAME, "onConnected", "!opr.isDone()");
                                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                    @Override
                                    public void onResult(@NonNull GoogleSignInResult result) {
                                        Logs.d(CLASSNAME, "onResult", null);
                                        if (result.isSuccess()) {
                                            mOnAuthListener.onAuthSuccessful(result);
                                        } else {
                                            mOnAuthListener.onAuthFailed();
                                        }
                                        mGoogleApiClient.disconnect();
                                    }
                                });
                            }

                        }

                        @Override
                        public void onConnectionSuspended(int cause) {
                            Logs.d(CLASSNAME, "onConnectionSuspended", null);
                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                    .build();
        }
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }
        mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);

    }

}
