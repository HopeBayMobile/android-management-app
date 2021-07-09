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
package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;


/**
 * Wrapper the Google auth procedure to provide an user friendly API util
 *
 * @author Aaron
 *         Created by Aaron on 2016/7/7.
 */
public class GoogleSilentAuthProxy {

    private static final String CLASSNAME = GoogleSilentAuthProxy.class.getSimpleName();

    private Context mContext;
    private String mServerClientId;
    private GoogleApiClient mGoogleApiClient;

    /**
     * Flag for disconnecting Google Api client after connection
     */
    private boolean mAutoDisconnectEnabled = true;

    private OnAuthListener mOnAuthListener;

    public GoogleSilentAuthProxy(Context context, String serverClientId, @NonNull OnAuthListener listener) {
        this.mContext = context;
        this.mServerClientId = serverClientId;
        this.mOnAuthListener = listener;
    }

    public interface OnAuthListener {

        void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient);

        void onAuthFailed(@Nullable GoogleSignInResult result);

    }

    public void auth() {
        GoogleSignInApiClient signInApiClient = new GoogleSignInApiClient(mContext, mServerClientId,
                new GoogleSignInApiClient.OnConnectionListener() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle, GoogleApiClient googleApiClient) {
                        Logs.d(CLASSNAME, "onConnected", null);
                        mGoogleApiClient = googleApiClient;
                        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
                        if (opr.isDone()) {
                            Logs.d(CLASSNAME, "onConnected", "opr.isDone()");
                            GoogleSignInResult result = opr.get();
                            if (result != null && result.isSuccess()) {
                                mOnAuthListener.onAuthSuccessful(result, mGoogleApiClient);
                            } else {
                                mOnAuthListener.onAuthFailed(result);
                            }
                            if (mAutoDisconnectEnabled) {
                                mGoogleApiClient.disconnect();
                            }
                        } else {
                            Logs.d(CLASSNAME, "onConnected", "!opr.isDone()");
                            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                                @Override
                                public void onResult(@NonNull GoogleSignInResult result) {
                                    if (result.isSuccess()) {
                                        mOnAuthListener.onAuthSuccessful(result, mGoogleApiClient);
                                    } else {
                                        mOnAuthListener.onAuthFailed(result);
                                    }
                                    if (mAutoDisconnectEnabled) {
                                        mGoogleApiClient.disconnect();
                                    }
                                }
                            });
                        }

                    }

                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        Logs.d(CLASSNAME, "onConnectionFailed", "result=" + result);
                        mOnAuthListener.onAuthFailed(null);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Logs.d(CLASSNAME, "onConnectionSuspended", "cause=" + cause);
                    }
                });
        signInApiClient.connect();
    }

}
