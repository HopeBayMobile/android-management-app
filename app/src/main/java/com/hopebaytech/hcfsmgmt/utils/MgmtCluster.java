package com.hopebaytech.hcfsmgmt.utils;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 */
public class MgmtCluster {

    private static final String CLASSNAME = MgmtCluster.class.getSimpleName();
    private static final String MGMT_CLUSTER_LOGIN_URL = "https://terafonnreg.hopebaytech.com/api/register/login/";
    public static final String MGMT_CLUSTER_AUTH_URL = "https://terafonnreg.hopebaytech.com/api/register/auth";
    public static final String MGMT_CLUSTER_EXCHANGE_TOKEN = "https://terafonnreg.hopebaytech.com/api/social-auth/";
    public static final String MGMT_CLUSTER_DEVICE_API = "https://terafonnreg.hopebaytech.com/api/user/devices/";
    private static int retryCount = 0;

    /**
     * @param oldServerAuthCode the server auth code of old account used to change account
     * @return true if the account is changed successfully; false otherwise.
     */
    public static boolean switchAccount(String oldServerAuthCode, String newServerAuthCode, String imei) {

        boolean isSwitchSuccess = true;
        String jwtToken = null;
        HttpProxy httpProxy = null;
        try {
            httpProxy = new HttpProxy(MGMT_CLUSTER_EXCHANGE_TOKEN, true);
            httpProxy.connect();

            ContentValues data = new ContentValues();
            data.put("backend", "google-oauth2");
            data.put("access_token", "");
            data.put("code", oldServerAuthCode);

            int responseCode = httpProxy.post(data);
            Logs.w(CLASSNAME, "switchAccount", "responseCode=" + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String responseContent = httpProxy.getResponseContent();
                JSONObject jsonObj = new JSONObject(responseContent);
                String userId = jsonObj.getString("user_id");
                String userName = jsonObj.getString("username");
                jwtToken = jsonObj.getString("token");

                Logs.w(CLASSNAME, "switchAccount", "userId=" + userId + ", userName=" + userName + ", jwtToken=" + jwtToken);
            } else {
                isSwitchSuccess = false;
                String responseContent = httpProxy.getResponseContent();
                Logs.e(CLASSNAME, "switchAccount", responseContent);
            }
        } catch (Exception e) {
            isSwitchSuccess = false;
            Logs.e(CLASSNAME, "switchAccount", Log.getStackTraceString(e));
        } finally {
            if (httpProxy != null) {
                httpProxy.disconnect();
            }
        }

        if (jwtToken != null) {
            httpProxy = null;
            try {
                String url = MGMT_CLUSTER_DEVICE_API + imei + "/change_device_user/";
                httpProxy = new HttpProxy(url, true);

                ContentValues header = new ContentValues();
                header.put("Authorization", "JWT " + jwtToken);
                httpProxy.setHeaders(header);
                httpProxy.connect();

                ContentValues data = new ContentValues();
                data.put("new_auth_code", newServerAuthCode);
                int responseCode = httpProxy.post(data);
                Logs.w(CLASSNAME, "switchAccount", "switch responseCode=" + responseCode);
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    isSwitchSuccess = false;
                    String responseContent = httpProxy.getResponseContent();
                    Logs.e(CLASSNAME, "switchAccount", responseContent);
                }
            } catch (Exception e) {
                isSwitchSuccess = false;
                Logs.e(CLASSNAME, "switchAccount", Log.getStackTraceString(e));
            } finally {
                if (httpProxy != null) {
                    httpProxy.disconnect();
                }
            }
        }

        Logs.w(CLASSNAME, "switchAccount", "isSwitchSuccess=" + isSwitchSuccess);
        return isSwitchSuccess;
    }

    public static String getServerClientIdFromMgmtCluster() {
        String serverClientId = null;
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(MGMT_CLUSTER_AUTH_URL);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                String jsonResponse = sb.toString();
                HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "getServerClientIdFromMgmtCluster", "jsonResponse=" + jsonResponse);
                if (!jsonResponse.isEmpty()) {
                    JSONObject jObj = new JSONObject(jsonResponse);
                    JSONObject dataObj = jObj.getJSONObject("data");
                    JSONObject authObj = dataObj.getJSONObject("google-oauth2");
                    serverClientId = authObj.getString("client_id");
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "getServerClientIdFromMgmtCluster", "server_client_id=" + serverClientId);
                    bufferedReader.close();
                }
            }
        } catch (Exception e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getServerClientIdFromMgmtCluster", Log.getStackTraceString(e));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return serverClientId;
    }

    public interface IAuthParam {

        ContentValues createAuthParam();

    }

    public static class GoogleAuthParam implements IAuthParam {

        private String authCode;

        public GoogleAuthParam(String authCode) {
            this.authCode = authCode;
        }

        @Override
        public ContentValues createAuthParam() {
            String encryptedIMEI = HCFSMgmtUtils.getEncryptedDeviceIMEI();

            ContentValues cv = new ContentValues();
            cv.put("provider", "google-oauth2");
//            cv.put("token", idToken);
            cv.put("code", authCode);
            cv.put("imei_code", encryptedIMEI);

            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "GoogleAuthParam", "createAuthParam", "authCode=" + authCode + ", encryptedIMEI=" + encryptedIMEI);

            return cv;
        }

    }

    public static class NativeAuthParam implements IAuthParam {

        private String username;
        private String password;

        public NativeAuthParam(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public ContentValues createAuthParam() {
            String encryptedIMEI = HCFSMgmtUtils.getEncryptedDeviceIMEI();
            ContentValues cv = new ContentValues();
            cv.put("username", username);
            cv.put("password", password);
            cv.put("imei_code", encryptedIMEI);

            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "NativeAuthParam", "createAuthParam", "username=" + username + ", password=" + password + ", encryptedIMEI=" + encryptedIMEI);
            return cv;
        }

    }

    public static AuthResultInfo authWithMgmtCluster(IAuthParam authParam) {
        HttpProxy httpProxy = null;
        AuthResultInfo authResultInfo = new AuthResultInfo();
        try {
            httpProxy = new HttpProxy(MGMT_CLUSTER_LOGIN_URL, true);
            httpProxy.connect();

            int responseCode = httpProxy.post(authParam.createAuthParam());
            authResultInfo.setResponseCode(responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String responseContent = httpProxy.getResponseContent();
                Logs.d(CLASSNAME, "authWithMgmtCluster", "responseContent=" + responseContent);
                JSONObject jsonObj = new JSONObject(responseContent);
                boolean result = jsonObj.getBoolean("result");
                String message = jsonObj.getString("msg");
                authResultInfo.setMessage(message);
                if (result) {
                    JSONObject data = jsonObj.getJSONObject("data");
                    authResultInfo.setBackendType(data.getString("backend_type"));
                    authResultInfo.setAccount(data.getString("account").split(":")[0]);
                    authResultInfo.setUser(data.getString("account").split(":")[1]);
                    authResultInfo.setPassword(data.getString("password"));
                    authResultInfo.setBackendUrl(data.getString("domain") + ":" + data.getInt("port"));
                    authResultInfo.setBucket(data.getString("bucket"));
                    authResultInfo.setProtocol(data.getBoolean("TLS") ? "https" : "http");

                    Logs.d(CLASSNAME, "authWithMgmtCluster", "backend_type=" + authResultInfo.getBackendType());
                    Logs.d(CLASSNAME, "authWithMgmtCluster", "account=" + authResultInfo.getAccount());
                    Logs.d(CLASSNAME, "authWithMgmtCluster", "user=" + authResultInfo.getUser());
                    Logs.d(CLASSNAME, "authWithMgmtCluster", "password=" + authResultInfo.getPassword());
                    Logs.d(CLASSNAME, "authWithMgmtCluster", "backend_url=" + authResultInfo.getBackendUrl());
                    Logs.d(CLASSNAME, "authWithMgmtCluster", "protocol=" + authResultInfo.getProtocol());
                }
            } else {
                try {
                    String responseContent = httpProxy.getResponseContent();
                    JSONObject jsonObj = new JSONObject(responseContent);
                    String message = jsonObj.getString("msg");
                    authResultInfo.setMessage(message);
                } catch (JSONException e) {
                    Logs.e(CLASSNAME, "authWithMgmtCluster", Log.getStackTraceString(e));
                }
            }
        } catch (Exception e) {
            authResultInfo.setMessage(Log.getStackTraceString(e));
            Logs.e(CLASSNAME, "authWithMgmtCluster", Log.getStackTraceString(e));
        } finally {
            if (httpProxy != null) {
                httpProxy.disconnect();
            }
        }
        return authResultInfo;
    }

    public interface AuthListener {

        void onAuthSuccessful(GoogleSignInAccount acct, AuthResultInfo authResultInfo);

        void onGoogleAuthFailed(String failedMsg);

        void onMmgtAuthFailed(AuthResultInfo authResultInfo);

    }

    public static class MgmtAuth {

        private GoogleSignInResult googleSignInResult;
        private AuthListener authListener;
        private Looper looper;

        public MgmtAuth(Looper looper, GoogleSignInResult googleSignInResult) {
            this.looper = looper;
            this.googleSignInResult = googleSignInResult;
        }

        public void authenticate() {
            if (googleSignInResult != null && googleSignInResult.isSuccess()) {
                final GoogleSignInAccount acct = googleSignInResult.getSignInAccount();
                if (acct != null) {
                    final String idToken = acct.getIdToken();
                    final String serverAuthCode = acct.getServerAuthCode();
                    Logs.d(CLASSNAME, "authenticate", "idToken=" + idToken);
                    Logs.d(CLASSNAME, "authenticate", "serverAuthCode=" + serverAuthCode);
                    Logs.d(CLASSNAME, "authenticate", "displayName=" + acct.getDisplayName());
                    Logs.d(CLASSNAME, "authenticate", "email=" + acct.getEmail());
                    Logs.d(CLASSNAME, "authenticate", "photoUrl=" + acct.getPhotoUrl());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
//                            MgmtCluster.IAuthParam authParam = new MgmtCluster.GoogleAuthParam(idToken);
                            MgmtCluster.IAuthParam authParam = new MgmtCluster.GoogleAuthParam(serverAuthCode);
                            final AuthResultInfo authResultInfo = MgmtCluster.authWithMgmtCluster(authParam);
                            Logs.d(CLASSNAME, "authenticate", "authResultInfo=" + authResultInfo);
                            HCFSConfig.storeHCFSConfig(authResultInfo);
                            // TODO Set arkflex token to HCFS
                            Handler handler = new Handler(looper);
                            if (authResultInfo.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        authListener.onAuthSuccessful(acct, authResultInfo);
                                    }
                                });
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        authListener.onMmgtAuthFailed(authResultInfo);
                                    }
                                });
                            }
                        }
                    }).start();
                } else {
                    String failedMsg = "acct is null";
                    authListener.onGoogleAuthFailed(failedMsg);
                }
            } else {
                String failedMsg;
                if (googleSignInResult == null) {
                    failedMsg = "googleSignInResult == null";
                } else {
                    failedMsg = "googleSignInResult.isSuccess()=" + googleSignInResult.isSuccess();
                }
                authListener.onGoogleAuthFailed(failedMsg);
            }
        }

        public void setOnAuthListener(AuthListener listener) {
            this.authListener = listener;
        }

    }

    public static void resetRetryCount() {
        retryCount = 0;
    }

    public static boolean isNeedToRetryAgain() {
        return retryCount < 3;
    }

    public static void plusRetryCount() {
        retryCount = retryCount + 1;
    }
}
