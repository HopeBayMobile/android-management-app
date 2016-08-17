package com.hopebaytech.hcfsmgmt.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.InputFilter;
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
import com.hopebaytech.hcfsmgmt.db.AccountDAO;
import com.hopebaytech.hcfsmgmt.db.TeraStatDAO;
import com.hopebaytech.hcfsmgmt.info.AccountInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;
import com.hopebaytech.hcfsmgmt.info.TeraStatInfo;
import com.hopebaytech.hcfsmgmt.utils.TeraAppConfig;
import com.hopebaytech.hcfsmgmt.utils.TeraCloudConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
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

        mHandlerThread = new HandlerThread(CLASSNAME);
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
        mActivateCode.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View currentFocusView = ((Activity) mContext).getCurrentFocus();
                if (currentFocusView != null) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
                }

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
                            final MgmtCluster.IAuthParam authParam;
                            if (authType == MgmtCluster.GOOGLE_AUTH) {
                                String authCode = getArguments().getString(ActivateWoCodeFragment.KEY_AUTH_CODE);
                                MgmtCluster.GoogleAuthParam googleAuthParam = new MgmtCluster.GoogleAuthParam();
                                googleAuthParam.setAuthCode(authCode);
                                googleAuthParam.setAuthBackend(MgmtCluster.GOOGLE_AUTH_BACKEND);
                                authParam = googleAuthParam;
                            } else {
                                MgmtCluster.UserAuthParam userAuthParam = new MgmtCluster.UserAuthParam();
                                userAuthParam.setUsername(username);
                                userAuthParam.setPassword(getArguments().getString(ActivateWoCodeFragment.KEY_PASSWORD));
                                authParam = userAuthParam;
                            }
                            authParam.setImei(imei);
                            authParam.setVendor(Build.BRAND);
                            authParam.setModel(Build.MODEL);
                            authParam.setAndroidVersion(Build.VERSION.RELEASE);
                            authParam.setHcfsVersion(getString(R.string.tera_version));
                            authParam.setActivateCode(activateCode);

                            String jwtToken = getArguments().getString(ActivateWoCodeFragment.KEY_JWT_TOKEN);
                            MgmtCluster.RegisterProxy registerProxy = new MgmtCluster.RegisterProxy(authParam, jwtToken);
                            registerProxy.setOnRegisterListener(new MgmtCluster.RegisterListener() {
                                @Override
                                public void onRegisterSuccessful(final RegisterResultInfo registerResultInfo) {
                                    mWorkHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            final boolean failed = TeraCloudConfig.storeHCFSConfig(registerResultInfo);
                                            if (failed) {
                                                TeraCloudConfig.resetHCFSConfig();
                                            } else {
                                                AccountInfo accountInfo = new AccountInfo();
                                                accountInfo.setName(username);

                                                AccountDAO accountDAO = AccountDAO.getInstance(mContext);
                                                accountDAO.clear();
                                                accountDAO.insert(accountInfo);
                                                accountDAO.close();

                                                TeraAppConfig.enableApp(mContext);
//                                                SharedPreferences sharedPreferences =
//                                                        PreferenceManager.getDefaultSharedPreferences(mContext);
//                                                SharedPreferences.Editor editor = sharedPreferences.edit();
//                                                editor.putBoolean(HCFSMgmtUtils.PREF_TERA_APP_LOGIN, true);
//                                                editor.apply();

                                                TeraCloudConfig.activateTeraCloud(mContext);
//                                                TeraStatDAO teraStatDAO = TeraStatDAO.getInstance(mContext);
//                                                TeraStatInfo teraStatInfo = new TeraStatInfo();
//                                                teraStatInfo.setEnabled(true);
//                                                if (teraStatDAO.getCount() == 0) {
//                                                    teraStatDAO.insert(teraStatInfo);
//                                                } else {
//                                                    teraStatDAO.update(teraStatInfo);
//                                                }

                                                String url = registerResultInfo.getBackendUrl();
                                                String token = registerResultInfo.getStorageAccessToken();
                                                HCFSMgmtUtils.setSwiftToken(url, token);
                                            }

                                            mUiHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    dismissProgressDialog();
                                                    if (failed) {
                                                        mErrorMessage.setText(R.string.activate_failed);
                                                    } else {
                                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                        ft.replace(R.id.fragment_container, MainFragment.newInstance(), MainFragment.TAG);
                                                        ft.commit();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }

                                @Override
                                public void onRegisterFailed(RegisterResultInfo registerResultInfo) {
                                    Logs.e(CLASSNAME, "onRegisterFailed", "registerResultInfo=" + registerResultInfo.toString());

                                    dismissProgressDialog();

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

                            });
                            registerProxy.register();

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

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
