package com.hopebaytech.hcfsmgmt.utils;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 */
public class MgmtCluster {

    private static final String CLASSNAME = MgmtCluster.class.getSimpleName();
    private static final String MGMT_CLUSTER_LOGIN_URL = "https://terafonnreg.hopebaytech.com/api/register/login/";
    public static final String MGMT_CLUSTER_AUTH_URL = "https://terafonnreg.hopebaytech.com/api/register/auth";
    public static final String MGMT_CLUSTER_CHANGE_ACCOUNT_URL = "https://terafonnreg.hopebaytech.com/api/user/devices/";
    private static int retryCount = 0;

    /**
     * @param oldServerAuthCode the server auth code of old account used to change account
     * @return true if the account is changed successfully; false otherwise.
     * */
    // TODO Not implemented completely
    public static boolean changeAccount(String oldServerAuthCode) {

        boolean isChangedSuccess = true;
        HttpsURLConnection conn = null;
//        AuthResultInfo authResultInfo = new AuthResultInfo();
        try {
            URL url = new URL(MGMT_CLUSTER_CHANGE_ACCOUNT_URL);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.connect();

            ContentValues cv = new ContentValues();
            cv.put("new_token", oldServerAuthCode);

            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(getQuery(cv));
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            int responseCode = conn.getResponseCode();
//            authResultInfo.setResponseCode(responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String responseContent = getResponseContent(conn, responseCode);
                JSONObject jsonObj = new JSONObject(responseContent);
                boolean result = jsonObj.getBoolean("result");
                String message = jsonObj.getString("msg");
//                authResultInfo.setMessage(message);
                if (result) {
                    JSONObject data = jsonObj.getJSONObject("data");
//                    authResultInfo.setBackendType(data.getString("backend_type"));
//                    authResultInfo.setAccount(data.getString("account").split(":")[0]);
//                    authResultInfo.setUser(data.getString("account").split(":")[1]);
//                    authResultInfo.setPassword(data.getString("password"));
//                    authResultInfo.setBackendUrl(data.getString("domain") + ":" + data.getInt("port"));
//                    authResultInfo.setBucket(data.getString("bucket"));
//                    authResultInfo.setProtocol(data.getBoolean("TLS") ? "https" : "http");

//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "backend_type=" + authResultInfo.getBackendType());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "account=" + authResultInfo.getAccount());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "user=" + authResultInfo.getUser());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "password=" + authResultInfo.getPassword());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "backend_url=" + authResultInfo.getBackendUrl());
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "protocol=" + authResultInfo.getProtocol());
                }
            } else {
                isChangedSuccess = false;
                try {
                    String responseContent = getResponseContent(conn, responseCode);
                    JSONObject jsonObj = new JSONObject(responseContent);
                    String message = jsonObj.getString("msg");
//                    authResultInfo.setMessage(message);
                } catch (JSONException e) {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "changeAccount", Log.getStackTraceString(e));
                }
            }
        } catch (Exception e) {
            isChangedSuccess = false;
//            authResultInfo.setMessage(Log.getStackTraceString(e));
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "authWithMgmtCluster", Log.getStackTraceString(e));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return isChangedSuccess;
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

        String createAuthParamQuery();

    }

    public static class GoogleAuthParam implements IAuthParam {

        private String idToken;
//        private String authCode; TODO

        public GoogleAuthParam(String idToken) {
            this.idToken = idToken;
        }

//        public GoogleAuthParam(String authCode) { TODO
//            this.authCode = authCode;
//        }

        @Override
        public String createAuthParamQuery() {
//            List<NameValuePair> params = new ArrayList<>();
            String encryptedIMEI = HCFSMgmtUtils.getEncryptedDeviceIMEI();

            ContentValues cv = new ContentValues();
            cv.put("provider", "google-oauth2");
            cv.put("token", idToken);
            cv.put("imei_code", encryptedIMEI);

//            params.add(new BasicNameValuePair("provider", "google-oauth2"));
//            params.add(new BasicNameValuePair("token", idToken));
//            params.add(new BasicNameValuePair("token", authCode)); // TODO
//            params.add(new BasicNameValuePair("imei_code", encryptedIMEI));

            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "GoogleAuthParam", "createAuthParamQuery", "idToken=" + idToken + ", encryptedIMEI=" + encryptedIMEI);

//            return getQuery(params);
            return getQuery(cv);
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
        public String createAuthParamQuery() {

//            List<NameValuePair> params = new ArrayList<>();
            String encryptedIMEI = HCFSMgmtUtils.getEncryptedDeviceIMEI();
//            params.add(new BasicNameValuePair("username", username));
//            params.add(new BasicNameValuePair("password", password));
//            params.add(new BasicNameValuePair("imei_code", encryptedIMEI));

            ContentValues cv = new ContentValues();
            cv.put("username", username);
            cv.put("password", password);
            cv.put("imei_code", encryptedIMEI);

            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "NativeAuthParam", "createAuthParamQuery", "username=" + username + ", password=" + password + ", encryptedIMEI=" + encryptedIMEI);

            return getQuery(cv);
//            return getQuery(params);
        }

    }

    public static AuthResultInfo authWithMgmtCluster(IAuthParam authParam) {
        HttpsURLConnection conn = null;
        AuthResultInfo authResultInfo = new AuthResultInfo();
        try {
            URL url = new URL(MGMT_CLUSTER_LOGIN_URL);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            OutputStream outputStream = conn.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            bufferedWriter.write(authParam.createAuthParamQuery());
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            int responseCode = conn.getResponseCode();
            authResultInfo.setResponseCode(responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String responseContent = getResponseContent(conn, responseCode);
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

                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "backend_type=" + authResultInfo.getBackendType());
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "account=" + authResultInfo.getAccount());
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "user=" + authResultInfo.getUser());
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "password=" + authResultInfo.getPassword());
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "backend_url=" + authResultInfo.getBackendUrl());
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authWithMgmtCluster", "protocol=" + authResultInfo.getProtocol());
                }
            } else {
                try {
                    String responseContent = getResponseContent(conn, responseCode);
                    JSONObject jsonObj = new JSONObject(responseContent);
                    String message = jsonObj.getString("msg");
                    authResultInfo.setMessage(message);
                } catch (JSONException e) {
                    HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "authWithMgmtCluster", Log.getStackTraceString(e));
                }
            }
        } catch (Exception e) {
            authResultInfo.setMessage(Log.getStackTraceString(e));
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "authWithMgmtCluster", Log.getStackTraceString(e));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return authResultInfo;
    }

    private static String getQuery(ContentValues cv) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry: cv.valueSet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            try {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getQuery", Log.getStackTraceString(e));
            }
        }
        return result.toString();
    }

    private static String getQuery(List<NameValuePair> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (NameValuePair pair : params) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            try {
//                if (pair.getName().equals("imei_code")) {
//                    result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
//                    result.append("=");
//                    result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
//                } else {
                    result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
//                }
            } catch (UnsupportedEncodingException e) {
                HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getQuery", Log.getStackTraceString(e));
            }
        }
        return result.toString();
    }

    private static String getResponseContent(HttpsURLConnection conn, int responseCode) throws IOException {
        InputStream inputStream = null;
        StringBuilder sb = new StringBuilder();
        try {
            if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = conn.getErrorStream();
            } else {
                inputStream = conn.getInputStream();
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            HCFSMgmtUtils.log(Log.ERROR, CLASSNAME, "getResponseContent", Log.getStackTraceString(e));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return sb.toString();
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
//                    final String serverAuthCode = acct.getServerAuthCode();
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authenticate", "idToken=" + idToken);
//                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authenticate", "serverAuthCode=" + serverAuthCode);
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authenticate", "displayName=" + acct.getDisplayName());
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authenticate", "email=" + acct.getEmail());
                    HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authenticate", "photoUrl=" + acct.getPhotoUrl());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MgmtCluster.IAuthParam authParam = new MgmtCluster.GoogleAuthParam(idToken);
                            final AuthResultInfo authResultInfo = MgmtCluster.authWithMgmtCluster(authParam);
                            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "authenticate", "authResultInfo=" + authResultInfo);
                            // TODO Store backend information and auth token
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
                String failedMsg = null;
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
