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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private TextView mErrorMsg;
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
        mErrorMsg = (TextView) view.findViewById(R.id.error_msg);
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
                    mErrorMsg.setText(R.string.activate_require_activation_code);
                } else if (!MgmtCluster.verifyActivationCode(activateCode)) {
                    mErrorMsg.setText(R.string.activate_incorrect_activation_code);
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
                                userAuthParam.setVender(Build.BRAND);
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
                                                        mErrorMsg.setText(registerResultInfo.getMessage());
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
                                    // TODO Error code is not defined
                                    hideProgressDialog();
                                    if (registerResultInfo.getResponseCode() == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
                                        String hyperLink = "<a href=\"http://www.hopebaytech.com\">TeraClient</a>";
                                        Spanned errorMsg = Html.fromHtml(String.format(Locale.getDefault(), getString(R.string.activate_with_code_msg), hyperLink));
                                        mErrorMsg.setMovementMethod(LinkMovementMethod.getInstance());
                                        mErrorMsg.setText(errorMsg);
                                    } else {
                                        mErrorMsg.setText(R.string.activate_incorrect_activation_code);
                                    }
                                }

                                @Override
                                public void onAuthFailed(AuthResultInfo authResultInfo) {
                                    hideProgressDialog();
                                    mErrorMsg.setText(R.string.activate_auth_failed);
                                }

                            });
                            mgmtRegister.register();
                        }
                    });
                }
            }
        });

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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
    }
}
