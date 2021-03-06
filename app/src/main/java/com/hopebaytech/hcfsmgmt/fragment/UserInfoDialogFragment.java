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
package com.hopebaytech.hcfsmgmt.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.utils.BitmapFactoryUtils;
import com.hopebaytech.hcfsmgmt.utils.GoogleSilentAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/9/29.
 */
public class UserInfoDialogFragment extends DialogFragment {

    public static final String TAG = UserInfoDialogFragment.class.getSimpleName();
    private final String CLASSNAME = TAG;

    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private Handler mUiHandler;

    private Context mContext;
    private ImageView mUserIcon;
    private TextView mUserName;
    private TextView mUserEmail;
    private TextView mChangeAccount;
    private LinearLayout mChangeAccountContainer;

    public static UserInfoDialogFragment newInstance() {
        return new UserInfoDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        mWorkThread = new HandlerThread(CLASSNAME);
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());
        mUiHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_info_dialog_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserIcon = (ImageView) view.findViewById(R.id.user_icon);
        mUserName = (TextView) view.findViewById(R.id.user_name);
        mUserEmail = (TextView) view.findViewById(R.id.user_email);
        mChangeAccount = (TextView) view.findViewById(R.id.change_account);
        mChangeAccountContainer = (LinearLayout) view.findViewById(R.id.change_account_container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUserIcon();
        mChangeAccountContainer.setVisibility(View.GONE);
        mChangeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeAccountDialogFragment dialogFragment = ChangeAccountDialogFragment.newInstance();
                dialogFragment.show(getFragmentManager(), ChangeAccountDialogFragment.TAG);
                dismiss();
            }
        });
    }

    private void setUserIcon() {
        final AccountDAO accountDAO = AccountDAO.getInstance(mContext);
        if (accountDAO.getCount() == 0) {
            return;
        }

        final AccountInfo accountInfo = accountDAO.getFirst();
        mUserName.setText(accountInfo.getName());
        mUserEmail.setText(accountInfo.getEmail());
        final String imgUrl = accountInfo.getImgUrl();

        new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... imgUrl) {
                final Bitmap iconBitmap = downloadUserIconWithRetry(imgUrl[0]);
                if (iconBitmap != null) {
                    int radius = iconBitmap.getWidth() / 2;
                    BitmapShader bitmapShader = new BitmapShader(
                            iconBitmap,
                            Shader.TileMode.CLAMP,
                            Shader.TileMode.CLAMP
                    );
                    final Bitmap circularIconBitmap = Bitmap.createBitmap(
                            iconBitmap.getWidth(),
                            iconBitmap.getHeight(),
                            Bitmap.Config.ARGB_8888
                    );
                    Canvas canvas = new Canvas(circularIconBitmap);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setShader(bitmapShader);
                    canvas.drawCircle(radius, radius, radius, paint);
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mUserIcon.setImageBitmap(circularIconBitmap);
                        }
                    });
                }
                return true;
            }
        }.execute(imgUrl);

        // User icon is not expired, reuse the prefetch base64 icon.
        if (System.currentTimeMillis() <= accountInfo.getImgExpiringTime()) {
            String imgBase64 = accountInfo.getImgBase64();
            if (imgBase64 != null) {
                mUserIcon.setImageBitmap(BitmapFactoryUtils.decodeBase64(imgBase64));
            }
            return;
        }

        // Download the latest Google user icon and set it to ImageView
        String serverClientId = MgmtCluster.getServerClientId();
        GoogleSilentAuthProxy silentAuthProxy = new GoogleSilentAuthProxy(mContext,
                serverClientId, new GoogleSilentAuthProxy.OnAuthListener() {
            @Override
            public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {
                if (result == null || !result.isSuccess()) {
                    return;
                }

                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct == null) {
                    Logs.w(CLASSNAME, "onAuthSuccessful", "GoogleSignInAccount is null");
                    return;
                }

                String iconUrl = null;
                if (acct.getPhotoUrl() != null) {
                    Uri iconUri = acct.getPhotoUrl();
                    if (iconUri == null) {
                        return;
                    } else {
                        iconUrl = iconUri.toString();
                    }
                }

                final String finalIconUrl = iconUrl;
                mWorkHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap iconBitmap = downloadUserIconWithRetry(finalIconUrl);
                        if (iconBitmap != null) {
                            int radius = iconBitmap.getWidth() / 2;
                            BitmapShader bitmapShader = new BitmapShader(
                                    iconBitmap,
                                    Shader.TileMode.CLAMP,
                                    Shader.TileMode.CLAMP
                            );
                            final Bitmap circularIconBitmap = Bitmap.createBitmap(
                                    iconBitmap.getWidth(),
                                    iconBitmap.getHeight(),
                                    Bitmap.Config.ARGB_8888
                            );
                            Canvas canvas = new Canvas(circularIconBitmap);
                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setShader(bitmapShader);
                            canvas.drawCircle(radius, radius, radius, paint);

                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mUserIcon.setImageBitmap(circularIconBitmap);
                                }
                            });

                            String imgBase64 = BitmapFactoryUtils.encodeToBase64(
                                    iconBitmap,
                                    Bitmap.CompressFormat.PNG,
                                    100);
                            accountInfo.setImgBase64(imgBase64);
                            accountInfo.setImgExpiringTime(System.currentTimeMillis() + Interval.DAY);

                            accountDAO.update(accountInfo);
                        }
                    }
                });
            }

            @Override
            public void onAuthFailed(@Nullable GoogleSignInResult result) {
                Logs.e(CLASSNAME, "onAuthFailed", "result=" + result);
                if (result != null) {
                    Logs.e(CLASSNAME, "onAuthFailed", "status=" + result.getStatus());
                }
            }
        });
        silentAuthProxy.auth();
    }

    private Bitmap downloadUserIconWithRetry(String iconUrl) {
        Bitmap bitmap = null;
        for (int i = 0; i < 3; i++) {
            bitmap = downloadUserIcon(iconUrl);
            if (bitmap != null) {
                break;
            }
        }
        return bitmap;
    }

    private Bitmap downloadUserIcon(String iconUrl) {
        Bitmap bitmap = null;
        if (iconUrl != null) {
            try {
                URL urlConnection = new URL(iconUrl);
                HttpsURLConnection conn = (HttpsURLConnection) urlConnection.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedInputStream bInputStream = new BufferedInputStream(conn.getInputStream());
                bitmap = BitmapFactory.decodeStream(bInputStream);
            } catch (Exception e) {
                Logs.e(CLASSNAME, "run", Log.getStackTraceString(e));
            }
        }
        return bitmap;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logs.d(CLASSNAME, "onDestroy", null);
        if (mWorkThread != null) {
            mWorkThread.quit();
            mWorkThread = null;
        }
    }

}
