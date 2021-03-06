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
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

/**
 * A wrapper to wrap the Google sign-in procedure to provide a user friendly API util
 *
 * @author Aaron
 *         Created by Aaron on 2016/7/21.
 */
public class GoogleSignInApiClient {

    private static final String CLASSNAME = GoogleSignInApiClient.class.getSimpleName();

    private Context mContext;
    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleApiClient mGoogleApiClient;
    private String mServerClientId;

    private Handler mUiHandler;
    private OnConnectionListener mOnAuthListener;

    public GoogleSignInApiClient(Context context, String serverClientId, @NonNull OnConnectionListener listener) {
        this.mContext = context;
        this.mServerClientId = serverClientId;
        this.mUiHandler = new Handler(Looper.getMainLooper());
        this.mOnAuthListener = listener;
    }

    public interface OnConnectionListener {

        void onConnected(@Nullable Bundle bundle, GoogleApiClient googleApiClient);

        void onConnectionFailed(@NonNull ConnectionResult result);

        void onConnectionSuspended(int cause);

    }

    public void connect() {

        if (mServerClientId == null) {
            Logs.e(CLASSNAME, "auth", "mServerClientId == null");
            return;
        }

        if (mGoogleSignInOptions == null) {
            mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestServerAuthCode(mServerClientId, false)
                    //.requestScopes(new Scope("https://www.googleapis.com/auth/drive"), Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                    .requestScopes(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
                    .requestProfile()
                    .requestEmail()
                    .build();
        }

        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                                @Override
                                public void onConnectionFailed(@NonNull ConnectionResult result) {
                                    Logs.d(CLASSNAME, "onConnectionFailed", "result=" + result);
                                    mOnAuthListener.onConnectionFailed(result);
                                }
                            })
                            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(@Nullable Bundle bundle) {
                                    Logs.d(CLASSNAME, "onConnected", null);
                                    mOnAuthListener.onConnected(bundle, mGoogleApiClient);
                                }

                                @Override
                                public void onConnectionSuspended(int cause) {
                                    Logs.d(CLASSNAME, "onConnectionSuspended", "cause=" + cause);
                                    mOnAuthListener.onConnectionSuspended(cause);
                                }
                            })
                            .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                            .addApi(Drive.API)
                            .build();
                }
                if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.disconnect();
                }
                mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
            }
        });

    }

}
