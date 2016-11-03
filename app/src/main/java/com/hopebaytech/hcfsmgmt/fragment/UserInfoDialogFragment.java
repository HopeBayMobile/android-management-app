package com.hopebaytech.hcfsmgmt.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.utils.BitmapBase64Factory;
import com.hopebaytech.hcfsmgmt.utils.GoogleSilentAuthProxy;
import com.hopebaytech.hcfsmgmt.utils.Interval;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

import java.io.BufferedInputStream;
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
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUserIcon();
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

        // User icon is not expired, reuse the prefetch base64 icon.
        if (System.currentTimeMillis() <= accountInfo.getImgExpiringTime()) {
            String imgBase64 = accountInfo.getImgBase64();
            if (imgBase64 != null) {
                mUserIcon.setImageBitmap(BitmapBase64Factory.decodeBase64(imgBase64));
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
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mUserIcon.setImageBitmap(iconBitmap);
                                }
                            });

                            String imgBase64 = BitmapBase64Factory.encodeToBase64(
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
            public void onAuthFailed() {
                Logs.e(CLASSNAME, "onAuthFailed", null);
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
