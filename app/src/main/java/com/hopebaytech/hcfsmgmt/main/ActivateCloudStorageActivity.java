package com.hopebaytech.hcfsmgmt.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.utils.HCFSConfig;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;
import com.hopebaytech.hcfsmgmt.utils.NetworkUtils;

import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

public class ActivateCloudStorageActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private final String CLASSNAME = this.getClass().getSimpleName();
    //    private final String LOGIN_URL = "https://terafonnreg.hopebaytech.com/api/register/login/";
//    private final String AUTH_TYPE_GOOGLE = "auth_type_google";
//    private final String AUTH_TYPE_NORMAL = "auth_type_normal";
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private Handler mWorkHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_cloud_storage_activity);

        init();
    }

    private void init() {
        HandlerThread handlerThread = new HandlerThread(LoadingActivity.class.getSimpleName());
        handlerThread.start();
        mWorkHandler = new Handler(handlerThread.getLooper());

        LinearLayout activate = (LinearLayout) findViewById(R.id.activate);
        activate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = ActivateCloudStorageActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                final String username = ((EditText) findViewById(R.id.username)).getText().toString();
                final String password = ((EditText) findViewById(R.id.password)).getText().toString();
                if (username.isEmpty() || password.isEmpty()) {
                    showAlertDialog(ActivateCloudStorageActivity.this,
                            getString(R.string.activate_cloud_alert_dialog_title),
                            getString(R.string.activate_cloud_storage_snackbar_require_username_password),
                            getString(R.string.alert_dialog_confirm));
                } else {
                    if (HCFSMgmtUtils.ENABLE_AUTH) {
                        mWorkHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (NetworkUtils.isNetworkConnected(ActivateCloudStorageActivity.this)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showProgressDialog();
                                        }
                                    });

                                    MgmtCluster.IAuthParam authParam = new MgmtCluster.NativeAuthParam(username, password);
                                    final AuthResultInfo authResultInfo = MgmtCluster.authWithMgmtCluster(authParam);
                                    if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                        boolean isFailed = initHCFSConfig(authResultInfo);
                                        if (isFailed) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String failureMessage = authResultInfo.getMessage();
                                                    showAlertDialog(ActivateCloudStorageActivity.this,
                                                            getString(R.string.activate_cloud_alert_dialog_title),
                                                            failureMessage,
                                                            getString(R.string.alert_dialog_confirm));
                                                }
                                            });
                                            resetHCFSConfig();
                                        } else {
                                            Intent intent = new Intent(ActivateCloudStorageActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        String message = authResultInfo.getMessage();
                                        String dialogMessage = "responseCode=" + authResultInfo.getResponseCode();
                                        if (message != null) {
                                            dialogMessage += ", message=" + message;
                                        }
                                        final String finalDialogMessage = dialogMessage;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showAlertDialog(ActivateCloudStorageActivity.this,
                                                        getString(R.string.activate_cloud_alert_dialog_title),
                                                        finalDialogMessage,
                                                        getString(R.string.alert_dialog_confirm));
                                            }
                                        });

                                    }


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideProgressDialog();
                                        }
                                    });
                                } else {
                                    showAlertDialog(ActivateCloudStorageActivity.this,
                                            getString(R.string.activate_cloud_alert_dialog_title),
                                            getString(R.string.activate_cloud_alert_dialog_message),
                                            getString(R.string.alert_dialog_confirm));
                                }
                            }
                        });
                    } else {
                        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_CURRENT_BACKEND, "swift");
                        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_ACCOUNT, "test");
                        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_USER, "tester");
                        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PASS, "testing");
                        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_URL, "10.0.6.1:8080");
                        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_CONTAINER, "qa_terafonn_2");
                        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PROTOCOL, "http");
                        HCFSConfig.reloadConfig();

                        Intent intent = new Intent(ActivateCloudStorageActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

        TextView forgetPassword = (TextView) findViewById(R.id.forget_password);
        forgetPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(findViewById(android.R.id.content), "忘記密碼", Snackbar.LENGTH_SHORT).show();
            }
        });

        final SignInButton googleActivate = (SignInButton) findViewById(R.id.google_activate);
        googleActivate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(ActivateCloudStorageActivity.this)) {
                    showProgressDialog();
                    mWorkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String serverClientId = MgmtCluster.getServerClientIdFromMgmtCluster();
                            if (serverClientId != null) {
                                /**
                                 * Request only the user's ID token, which can be used to identify the
                                 * user securely to your backend. This will contain the user's basic
                                 * profile (name, profile picture URL, etc) so you should not need to
                                 * make an additional call to personalize your application.
                                 */
                                final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(serverClientId)
//                                        .requestServerAuthCode(serverClientId)
                                        .requestEmail()
                                        .build();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        googleActivate.setScopes(gso.getScopeArray());
                                        if (mGoogleApiClient == null) {
                                            mGoogleApiClient = new GoogleApiClient.Builder(ActivateCloudStorageActivity.this)
                                                    .enableAutoManage(ActivateCloudStorageActivity.this, ActivateCloudStorageActivity.this)
                                                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                                    .build();
                                        }

                                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                                        startActivityForResult(signInIntent, HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN);
                                        setGoogleSignInButtonText(googleActivate, getString(R.string.activate_cloud_storage_google_activate));
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showAlertDialog(ActivateCloudStorageActivity.this,
                                                getString(R.string.activate_cloud_alert_dialog_title),
                                                getString(R.string.failed_to_get_server_client_id),
                                                getString(R.string.alert_dialog_confirm));
                                    }
                                });
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressDialog();
                                }
                            });
                        }
                    });
                } else {
                    showAlertDialog(ActivateCloudStorageActivity.this,
                            getString(R.string.activate_cloud_alert_dialog_title),
                            getString(R.string.activate_cloud_alert_dialog_message),
                            getString(R.string.alert_dialog_confirm));
                }
            }
        });
        setGoogleSignInButtonText(googleActivate, getString(R.string.activate_cloud_storage_google_activate));
    }

    private void setGoogleSignInButtonText(SignInButton signInButton, String buttonText) {
        /** Find the TextView that is inside of the SignInButton and set its text */
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                tv.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                return;
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /** An unresolvable error has occurred and Google APIs (including Sign-In) will not be available. */
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onConnectionFailed", "connectionResult=" + connectionResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HCFSMgmtUtils.REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                final GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    showProgressDialog();
                    final String idToken = acct.getIdToken();
//                    final String serverAuthCode = acct.getServerAuthCode();
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "onActivityResult", "serverAuthCode=" + idToken);

                    mWorkHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MgmtCluster.IAuthParam authParam = new MgmtCluster.GoogleAuthParam(idToken);
                            final AuthResultInfo authResultInfo = MgmtCluster.authWithMgmtCluster(authParam);
                            if (authResultInfo.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                                boolean isFailed = initHCFSConfig(authResultInfo);
                                if (isFailed) {
                                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String failureMessage = authResultInfo.getMessage();
                                            showAlertDialog(ActivateCloudStorageActivity.this,
                                                    getString(R.string.activate_cloud_alert_dialog_title),
                                                    failureMessage,
                                                    getString(R.string.alert_dialog_confirm));
                                        }
                                    });
                                    resetHCFSConfig();
                                } else {
                                    Intent intent = new Intent(ActivateCloudStorageActivity.this, MainActivity.class);
                                    intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_DISPLAY_NAME, acct.getDisplayName());
                                    intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_EMAIL, acct.getEmail());
                                    intent.putExtra(HCFSMgmtUtils.ITENT_GOOGLE_SIGN_IN_PHOTO_URI, acct.getPhotoUrl());
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                String message = authResultInfo.getMessage();
                                String dialogMessage = "responseCode=" + authResultInfo.getResponseCode();
                                if (message != null) {
                                    dialogMessage += ", message=" + message;
                                }
                                final String finalDialogMessage = dialogMessage;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showAlertDialog(ActivateCloudStorageActivity.this,
                                                getString(R.string.activate_cloud_alert_dialog_title),
                                                finalDialogMessage,
                                                getString(R.string.alert_dialog_confirm));
                                    }
                                });
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressDialog();
                                }
                            });
                        }
                    });
                } else {
                    String failureMessage = getString(R.string.activate_cloud_storage_failed_to_signin_google_account);
                    Snackbar.make(findViewById(android.R.id.content), failureMessage, Snackbar.LENGTH_LONG).show();
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onActivityResult", "GoogleSignInAccount is null.");
                }
            } else {
                showAlertDialog(ActivateCloudStorageActivity.this,
                        getString(R.string.activate_cloud_alert_dialog_title),
                        getString(R.string.activate_cloud_storage_failed_to_signin_google_account),
                        getString(R.string.alert_dialog_confirm));
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "onActivityResult", "Failed to sign in Google account.");
            }
        }

    }

//    private String getQuery(List<NameValuePair> params) {
//        StringBuilder result = new StringBuilder();
//        boolean first = true;
//        for (NameValuePair pair : params) {
//            if (first) {
//                first = false;
//            } else {
//                result.append("&");
//            }
//
//            try {
//                if (pair.getName().equals("imei_code")) {
//                    result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
//                    result.append("=");
//                    result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
//                    Log.w(HCFSMgmtUtils.TAG, URLEncoder.encode(pair.getValue(), "UTF-8"));
//                } else {
//                    result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
//                    result.append("=");
//                    result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
//                }
//            } catch (UnsupportedEncodingException e) {
//                Log.e(HCFSMgmtUtils.TAG, Log.getStackTraceString(e));
//            }
//        }
//        return result.toString();
//    }


    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(getString(R.string.activate_cloud_storage_processing_msg));
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }


//    @Nullable
//    private AuthResultInfo authWithMgmtCluster(String authType, @Nullable String idToken) {
//        HttpsURLConnection conn = null;
//        AuthResultInfo authResultInfo = new AuthResultInfo();
//        try {
//            /*
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpPost httpPost = new HttpPost("https://yourbackend.example.com/tokensignin");
//
//            try {
//                List nameValuePairs = new ArrayList(1);
//                nameValuePairs.add(new BasicNameValuePair("idToken", idToken));
//                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//
//                HttpResponse response = httpClient.execute(httpPost);
//                int statusCode = response.getStatusLine().getStatusCode();
//                final String responseBody = EntityUtils.toString(response.getEntity());
//                Log.i(TAG, "Signed in as: " + responseBody);
//            } catch (ClientProtocolException e) {
//                Log.e(TAG, "Error sending ID token to backend.", e);
//            } catch (IOException e) {
//                Log.e(TAG, "Error sending ID token to backend.", e);
//            }
//            */
//
//            URL url = new URL(LOGIN_URL);
//            conn = (HttpsURLConnection) url.openConnection();
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.connect();
//
//            List<NameValuePair> params = new ArrayList<>();
//            String encryptedIMEI = HCFSMgmtUtils.getEncryptedDeviceIMEI(ActivateCloudStorageActivity.this);
//            if (authType.equals(AUTH_TYPE_GOOGLE)) {
//                /** Send token and IMEI to server and validate server-side */
//                params.add(new BasicNameValuePair("provider", "google-oauth2"));
//                params.add(new BasicNameValuePair("token", idToken));
//                params.add(new BasicNameValuePair("imei_code", encryptedIMEI));
//                HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "IMEI=" + encryptedIMEI);
//                HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "idToken=" + idToken);
//            } else {
//                final String username = ((EditText) findViewById(R.id.username)).getText().toString();
//                final String password = ((EditText) findViewById(R.id.password)).getText().toString();
//                params.add(new BasicNameValuePair("username", username));
//                params.add(new BasicNameValuePair("password", password));
//                params.add(new BasicNameValuePair("imei_code", encryptedIMEI));
//                HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "IMEI=" + encryptedIMEI);
//            }
//
//            OutputStream outputStream = conn.getOutputStream();
//            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
//            bufferedWriter.write(getQuery(params));
//            bufferedWriter.flush();
//            bufferedWriter.close();
//            outputStream.close();
//
//            int responseCode = conn.getResponseCode();
//            authResultInfo.setResponseCode(responseCode);
//            if (responseCode == HttpsURLConnection.HTTP_OK) {
//                String responseContent = getResponseContent(conn, responseCode);
//                JSONObject jsonObj = new JSONObject(responseContent);
//                boolean result = jsonObj.getBoolean("result");
//                String message = jsonObj.getString("msg");
//                authResultInfo.setMessage(message);
//                if (result) {
//                    JSONObject data = jsonObj.getJSONObject("data");
//                    authResultInfo.setBackendType(data.getString("backend_type"));
//                    authResultInfo.setAccount(data.getString("account").split(":")[0]);
//                    authResultInfo.setUser(data.getString("account").split(":")[1]);
//                    authResultInfo.setPassword(data.getString("password"));
//                    authResultInfo.setBackendUrl(data.getString("domain") + ":" + data.getInt("port"));
//                    authResultInfo.setBucket(data.getString("bucket"));
//                    authResultInfo.setProtocol(data.getBoolean("TLS") ? "https" : "http");
//
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "backend_type=" + authResultInfo.getBackendType());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "account=" + authResultInfo.getAccount());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "user=" + authResultInfo.getUser());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "password=" + authResultInfo.getPassword());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "backend_url=" + authResultInfo.getBackendUrl());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "protocol=" + authResultInfo.getProtocol());
//                }
//            } else {
//                try {
//                    String responseContent = getResponseContent(conn, responseCode);
//                    JSONObject jsonObj = new JSONObject(responseContent);
//                    String message = jsonObj.getString("msg");
//                    authResultInfo.setMessage(message);
//                } catch (JSONException e) {
//                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "authWithMgmtCluster", Log.getStackTraceString(e));
//                }
//            }
//        } catch (Exception e) {
//            authResultInfo.setMessage(Log.getStackTraceString(e));
//            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "authWithMgmtCluster", Log.getStackTraceString(e));
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//        return authResultInfo;
//    }

//    private String getResponseContent(HttpsURLConnection conn, int responseCode) throws IOException {
//        InputStream inputStream = null;
//        StringBuilder sb = new StringBuilder();
//        try {
//            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
//                inputStream = conn.getErrorStream();
//            } else {
//                inputStream = conn.getInputStream();
//            }
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                sb.append(line);
//            }
//        } catch (Exception e) {
//            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "authWithMgmtCluster", Log.getStackTraceString(e));
//        } finally {
//            if (inputStream != null) {
//                inputStream.close();
//            }
//        }
//        return sb.toString();
//    }

    private boolean initHCFSConfig(AuthResultInfo authResultInfo) {
        boolean isFailed = false;
        if (!HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_CURRENT_BACKEND, authResultInfo.getBackendType())) {
            isFailed = true;
        }
        if (!HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_ACCOUNT, authResultInfo.getAccount())) {
            isFailed = true;
        }
        if (!HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_USER, authResultInfo.getUser())) {
            isFailed = true;
        }
        if (!HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PASS, authResultInfo.getPassword())) {
            isFailed = true;
        }
        if (!HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_URL, authResultInfo.getBackendUrl())) {
            isFailed = true;
        }
        if (!HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_CONTAINER, authResultInfo.getBucket())) {
            isFailed = true;
        }
        if (!HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PROTOCOL, authResultInfo.getProtocol())) {
            isFailed = true;
        }
        if (!HCFSConfig.reloadConfig()) {
            isFailed = true;
        }
        return isFailed;
    }

    private void resetHCFSConfig() {
        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_CURRENT_BACKEND, "NONE");
        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_ACCOUNT, "");
        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_USER, "");
        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PASS, "");
        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_URL, "");
        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_CONTAINER, "");
        HCFSConfig.setHCFSConfig(HCFSConfig.HCFS_CONFIG_SWIFT_PROTOCOL, "");

//        HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_CURRENT_BACKEND, "NONE");
//        HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_ACCOUNT, "");
//        HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_USER, "");
//        HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PASS, "");
//        HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_URL, "");
//        HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_CONTAINER, "");
//        HCFSMgmtUtils.setHCFSConfig(HCFSMgmtUtils.HCFS_CONFIG_SWIFT_PROTOCOL, "");
    }

    public static void showAlertDialog(Context context, String title, String message, String positiveText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

}
