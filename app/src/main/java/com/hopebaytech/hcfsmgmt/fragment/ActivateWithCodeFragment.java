package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;
import com.hopebaytech.hcfsmgmt.main.LoadingActivity;
import com.hopebaytech.hcfsmgmt.main.MainActivity;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

import java.net.HttpURLConnection;
import java.util.Locale;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import java.net.HttpURLConnection;
import java.util.Locale;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/24.
 */
public class ActivateWithCodeFragment extends Fragment {

    public static final String TAG = ActivateWithCodeFragment.class.getSimpleName();
    private final String CLASSNAME = ActivateWithCodeFragment.class.getSimpleName();

    private Handler mWorkHandler;
    private Handler mUiHandler;
    private HandlerThread mHandlerThread;

    private Context mContext;
    private TextView mUsername;
    private EditText mActivateCode;
    private LinearLayout mActivateButton;
    private TextView mErrorMessage;
    private ProgressDialog mProgressDialog;

    public static ActivateWithCodeFragment newInstance() {
        return new ActivateWithCodeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandlerThread = new HandlerThread(LoadingActivity.class.getSimpleName());
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper());
        mUiHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activate_with_activation_code_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUsername = (TextView) view.findViewById(R.id.username);
        mActivateCode = (EditText) view.findViewById(R.id.activate_code);
        mActivateButton = (LinearLayout) view.findViewById(R.id.activate);
        mErrorMessage = (TextView) view.findViewById(R.id.error_msg);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String username = getArguments().getString(ActivateWoCodeFragment.KEY_USERNAME);
        mUsername.setText(username);
        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String activateCode = mActivateCode.getText().toString();
                if (activateCode.isEmpty()) {
                    mErrorMessage.setText(R.string.activate_require_activation_code);
                } else if (!MgmtCluster.verifyActivationCode(activateCode)) {
                    mErrorMessage.setText(R.string.activate_incorrect_activation_code);
                } else {
                    showProgressDialog();
                    mWorkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            final int authType = getArguments().getInt(ActivateWoCodeFragment.KEY_AUTH_TYPE);
                            String imei = HCFSMgmtUtils.getEncryptedDeviceImei(HCFSMgmtUtils.getDeviceImei(mContext));
                            MgmtCluster.IAuthParam authParam;
                            if (authType == MgmtCluster.GOOGLE_AUTH) {
                                String authCode = getArguments().getString(ActivateWoCodeFragment.KEY_AUTH_CODE);
                                MgmtCluster.GoogleAuthParam googleAuthParam = new MgmtCluster.GoogleAuthParam();
                                googleAuthParam.setAuthCode(authCode);
                                googleAuthParam.setImei(imei);
                                googleAuthParam.setVendor(Build.BRAND);
                                googleAuthParam.setModel(Build.MODEL);
                                authParam = googleAuthParam;
                            } else {
                                MgmtCluster.UserAuthParam userAuthParam = new MgmtCluster.UserAuthParam();
                                userAuthParam.setUsername(username);
                                userAuthParam.setPassword(getArguments().getString(ActivateWoCodeFragment.KEY_PASSWORD));
                                userAuthParam.setActivateCode(activateCode);
                                userAuthParam.setImei(imei);
                                userAuthParam.setVendor(Build.BRAND);
                                userAuthParam.setModel(Build.MODEL);
                                authParam = userAuthParam;
                            }

                            MgmtCluster.Register mgmtRegister = new MgmtCluster.Register(authParam);
                            mgmtRegister.setOnRegisterListener(new MgmtCluster.RegisterListener() {
                                @Override
                                public void onRegisterSuccessful(final RegisterResultInfo registerResultInfo) {
                                    mWorkHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // TODO Set arkflex token to hcfs
                                            // registerResultInfo.getStorageAccessToken();
                                            final boolean failed = HCFSConfig.storeHCFSConfig(registerResultInfo);
                                            if (failed) {
                                                HCFSConfig.resetHCFSConfig();
                                            }

                                            mUiHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    hideProgressDialog();
                                                    if (failed) {
                                                        mErrorMessage.setText(registerResultInfo.getMessage());
                                                    } else {
                                                        Intent intent = new Intent(mContext, MainActivity.class);
                                                        startActivity(intent);
                                                        ((Activity) mContext).finish();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }

                                @Override
                                public void onRegisterFailed(RegisterResultInfo registerResultInfo) {
                                    Logs.e(CLASSNAME, "onRegisterFailed", "registerResultInfo=" + registerResultInfo.toString());

                                    hideProgressDialog();

                                    int errorMsgResId = R.string.activate_incorrect_activation_code;
                                    if (registerResultInfo.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                                        if (registerResultInfo.getErrorCode().equals(MgmtCluster.INVALID_CODE_OR_MODEL)) {
                                            String hyperLink = "<a href=\"http://www.hopebaytech.com\">TeraClient</a>";
                                            Spanned errorMsg = Html.fromHtml(String.format(Locale.getDefault(), getString(R.string.activate_with_code_msg), hyperLink));
                                            mErrorMessage.setMovementMethod(LinkMovementMethod.getInstance());
                                            mErrorMessage.setText(errorMsg);
                                            return;
                                        } else if (registerResultInfo.getErrorCode().equals(MgmtCluster.INCORRECT_MODEL) ||
                                                registerResultInfo.getErrorCode().equals(MgmtCluster.INCORRECT_VENDOR)) {
                                            errorMsgResId = R.string.activate_failed_not_supported_device;
                                        } else if (registerResultInfo.getErrorCode().equals(MgmtCluster.DEVICE_EXPIRED)) {
                                            errorMsgResId = R.string.activate_failed_device_expired;
                                        }
                                    }
                                    mErrorMessage.setText(errorMsgResId);
                                }

                                @Override
                                public void onAuthFailed(AuthResultInfo authResultInfo) {
                                    Logs.e(CLASSNAME, "onRegisterFailed", "authResultInfo=" + authResultInfo.toString());

                                    hideProgressDialog();
                                    mErrorMessage.setText(R.string.activate_auth_failed);
                                }

                            });
                            mgmtRegister.register();
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(getString(R.string.activate_processing_msg));
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

}
