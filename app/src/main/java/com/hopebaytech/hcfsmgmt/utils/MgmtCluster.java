package com.hopebaytech.hcfsmgmt.utils;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxy;
import com.hopebaytech.hcfsmgmt.httpproxy.IHttpProxy;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.RegisterResultInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 */
public class MgmtCluster {

    private static final String CLASSNAME = MgmtCluster.class.getSimpleName();
    private static final String DOMAIN_NAME = "terafonnreg.hopebaytech.com";
    public static final String REGISTER_LOGIN_API = "https://" + DOMAIN_NAME + "/api/register/login/";
    public static final String REGISTER_AUTH_API = "https://" + DOMAIN_NAME + "/api/register/auth/";
    public static final String SOCIAL_AUTH_API = "https://" + DOMAIN_NAME + "/api/social-auth/";
    public static final String USER_AUTH_API = "https://" + DOMAIN_NAME + "/api/auth/";
    public static final String DEVICE_API = "https://" + DOMAIN_NAME + "/api/user/devices/";

    public static final String KEY_AUTH_CODE = "code";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_AUTHORIZATION = "Authorization";
    public static final String KEY_NEW_AUTH_CODE = "new_auth_code";
    public static final String KEY_IMEI = "imei_code";
    public static final String KEY_VENDOR = "vendor";
    public static final String KEY_MODEL = "model";
    public static final String KEY_ACTIVATION_CODE = "activation_code";


    private static int retryCount = 0;
    public static final int GOOGLE_AUTH = 0;
    public static final int USER_AUTH = 1;

    public static AuthResultInfo auth(IAuthParam authParam) {

        IHttpProxy httpProxyImpl = null;
        AuthResultInfo authResultInfo = new AuthResultInfo();
        try {
            String url;
            ContentValues data = new ContentValues();
            if (authParam instanceof GoogleAuthParam) {
                url = SOCIAL_AUTH_API;
                data.put(KEY_AUTH_CODE, ((GoogleAuthParam) authParam).authCode);
            } else {
                url = USER_AUTH_API;
                data.put(KEY_USERNAME, ((UserAuthParam) authParam).username);
                data.put(KEY_PASSWORD, ((UserAuthParam) authParam).password);
            }

            httpProxyImpl = HttpProxy.newInstance();
            httpProxyImpl.setUrl(url);
            httpProxyImpl.setDoOutput(true);
            httpProxyImpl.connect();

            int responseCode = httpProxyImpl.post(data);
            authResultInfo.setResponseCode(responseCode);
            String responseContent = httpProxyImpl.getResponseContent();
            Logs.d(CLASSNAME, "auth", "responseCode=" + responseCode + ", responseContent=" + responseContent);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                JSONObject jsonObj = new JSONObject(responseContent);
                String jwtToken = jsonObj.getString("token");
                Logs.d(CLASSNAME, "auth", "jwtToken=" + jwtToken);
                authResultInfo.setToken(jwtToken);
            } else {
                authResultInfo.setMessage(responseContent);
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "auth", Log.getStackTraceString(e));
        } finally {
            if (httpProxyImpl != null) {
                httpProxyImpl.disconnect();
            }
        }
        return authResultInfo;
    }

    /**
     * @param jwtToken the token to access mgmt cluster
     * @param newServerAuthCode the auth code of google account to be switched
     * @param imei the current device imei
     * @return true if the account is changed successfully; false otherwise.
     */
    public static boolean switchAccount(String jwtToken, String newServerAuthCode, String imei) {

        boolean isSwitchSuccess = true;
        if (jwtToken != null) {
            IHttpProxy httpProxyImpl = null;
            try {
                String url = DEVICE_API + imei + "/change_device_user/";
                httpProxyImpl = HttpProxy.newInstance();
                httpProxyImpl.setUrl(url);
                httpProxyImpl.setDoOutput(true);

                ContentValues header = new ContentValues();
                header.put(KEY_AUTHORIZATION, "JWT " + jwtToken);
                httpProxyImpl.setHeaders(header);
                httpProxyImpl.connect();

                ContentValues data = new ContentValues();
                data.put(KEY_NEW_AUTH_CODE, newServerAuthCode);
                int responseCode = httpProxyImpl.post(data);
                Logs.w(CLASSNAME, "switchAccount", "switch responseCode=" + responseCode);
                if (responseCode != HttpsURLConnection.HTTP_OK) {
                    isSwitchSuccess = false;
                    String responseContent = httpProxyImpl.getResponseContent();
                    Logs.e(CLASSNAME, "switchAccount", responseContent);
                }
            } catch (Exception e) {
                isSwitchSuccess = false;
                Logs.e(CLASSNAME, "switchAccount", Log.getStackTraceString(e));
            } finally {
                if (httpProxyImpl != null) {
                    httpProxyImpl.disconnect();
                }
            }
        }

        Logs.w(CLASSNAME, "switchAccount", "isSwitchSuccess=" + isSwitchSuccess);
        return isSwitchSuccess;
    }

    public static String getServerClientId() {

        IHttpProxy httpProxyImpl = null;
        String serverClientId = null;
        try {
            httpProxyImpl = HttpProxy.newInstance();
            httpProxyImpl.setUrl(REGISTER_AUTH_API);
            httpProxyImpl.setDoOutput(true);
            httpProxyImpl.connect();

            int responseCode = httpProxyImpl.get();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String jsonResponse = httpProxyImpl.getResponseContent();
                Logs.d(CLASSNAME, "getServerClientId", "jsonResponse=" + jsonResponse);
                if (!jsonResponse.isEmpty()) {
                    JSONObject jObj = new JSONObject(jsonResponse);
                    JSONObject dataObj = jObj.getJSONObject("data");
                    JSONObject authObj = dataObj.getJSONObject("google-oauth2");
                    serverClientId = authObj.getString("client_id");
                    Logs.d(CLASSNAME, "getServerClientId", "server_client_id=" + serverClientId);
                }
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getServerClientId", Log.getStackTraceString(e));
        } finally {
            if (httpProxyImpl != null) {
                httpProxyImpl.disconnect();
            }
        }
        return serverClientId;
    }

    public interface IAuthParam {

        ContentValues createAuthParam();

    }

    public static class GoogleAuthParam implements IAuthParam {

        private String authCode;
        private String imei;
        private String model;
        private String vendor;

        @Override
        public ContentValues createAuthParam() {

            ContentValues cv = new ContentValues();
            cv.put("provider", "google-oauth2");
            cv.put(KEY_AUTH_CODE, authCode);
            cv.put(KEY_IMEI, imei);
            cv.put(KEY_VENDOR, vendor);
            cv.put(KEY_MODEL, model);

            Logs.d(CLASSNAME, "GoogleAuthParam", "createAuthParam", "authCode=" + authCode + ", encryptedIMEI=" + imei);

            return cv;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }
    }

    public static class UserAuthParam implements IAuthParam {

        private String username;
        private String password;
        private String imei;
        private String activateCode;
        private String model;
        private String vender;

        @Override
        public ContentValues createAuthParam() {

            ContentValues cv = new ContentValues();
            cv.put(KEY_USERNAME, username);
            cv.put(KEY_PASSWORD, password);
            cv.put(KEY_IMEI, imei);
            cv.put(KEY_ACTIVATION_CODE, activateCode);
            cv.put(KEY_VENDOR, vender);
            cv.put(KEY_MODEL, model);

            Logs.d(CLASSNAME, "UserAuthParam", "createAuthParam", "username=" + username + ", password=" + password + ", encryptedIMEI=" + imei);
            return cv;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public void setActivateCode(String activateCode) {
            this.activateCode = activateCode;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public void setVender(String vender) {
            this.vender = vender;
        }
    }

    public static RegisterResultInfo register(IAuthParam authParam, String jwtToken) {
        RegisterResultInfo registerResultInfo = new RegisterResultInfo();
        IHttpProxy httpProxyImpl = null;
        try {
            httpProxyImpl = HttpProxy.newInstance();
            httpProxyImpl.setUrl(REGISTER_LOGIN_API);
            httpProxyImpl.setDoOutput(true);
            ContentValues header = new ContentValues();
            header.put(KEY_AUTHORIZATION, "JWT " + jwtToken);
            httpProxyImpl.setHeaders(header);
            httpProxyImpl.connect();

            int responseCode = httpProxyImpl.post(authParam.createAuthParam());
            registerResultInfo.setResponseCode(responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String responseContent = httpProxyImpl.getResponseContent();
                Logs.d(CLASSNAME, "register", "responseContent=" + responseContent);
                JSONObject jsonObj = new JSONObject(responseContent);
                JSONObject data = jsonObj.getJSONObject("data");
                registerResultInfo.setBackendType(data.getString("backend_type"));
                registerResultInfo.setAccount(data.getString("account").split(":")[0]);
                registerResultInfo.setUser(data.getString("account").split(":")[1]);
                registerResultInfo.setPassword(data.getString("password"));
                registerResultInfo.setBackendUrl(data.getString("domain") + ":" + data.getInt("port"));
                registerResultInfo.setBucket(data.getString("bucket"));
                registerResultInfo.setProtocol(data.getBoolean("TLS") ? "https" : "http");
                registerResultInfo.setStorageAccessToken(data.getString("token"));

                Logs.d(CLASSNAME, "register", "backend_type=" + registerResultInfo.getBackendType());
                Logs.d(CLASSNAME, "register", "account=" + registerResultInfo.getAccount());
                Logs.d(CLASSNAME, "register", "user=" + registerResultInfo.getUser());
                Logs.d(CLASSNAME, "register", "password=" + registerResultInfo.getPassword());
                Logs.d(CLASSNAME, "register", "backend_url=" + registerResultInfo.getBackendUrl());
                Logs.d(CLASSNAME, "register", "protocol=" + registerResultInfo.getProtocol());
                Logs.d(CLASSNAME, "register", "storageAccessToken=" + registerResultInfo.getStorageAccessToken());
            } else {
                try {
                    String responseContent = httpProxyImpl.getResponseContent();
                    JSONObject jsonObj = new JSONObject(responseContent);
                    String message = jsonObj.getString("detail");
                    registerResultInfo.setMessage(message);
                } catch (JSONException e) {
                    Logs.e(CLASSNAME, "register", Log.getStackTraceString(e));
                }
            }
        } catch (Exception e) {
            registerResultInfo.setMessage(Log.getStackTraceString(e));
            Logs.e(CLASSNAME, "register", Log.getStackTraceString(e));
        } finally {
            if (httpProxyImpl != null) {
                httpProxyImpl.disconnect();
            }
        }

        return registerResultInfo;
    }

    public interface RegisterListener {

        void onRegisterSuccessful(RegisterResultInfo registerResultInfo);

        /**
         * User authentication success, but registration to mgmt cluster failed. There are
         * three conditions: 400 Bad Request means login failed, 404 Not Found means device not
         * registered and 500 Internal Server Error means unknown error happened on server.
         * */
        void onRegisterFailed(RegisterResultInfo registerResultInfo);

        /**
         * User authentication failed. There are two conditions: 400 Bad Request means authentication
         * failed and 500 Internal Server Error means unknown error happened on server.
         * */
        void onAuthFailed(AuthResultInfo authResultInfo);

    }

    public static class Register {

        private RegisterListener registerListener;
        private MgmtCluster.IAuthParam authParam;

        public Register(IAuthParam authParam) {
            this.authParam = authParam;
        }

        public void register() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final AuthResultInfo authResultInfo = MgmtCluster.auth(authParam);
                    if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        final RegisterResultInfo registerResultInfo = MgmtCluster.register(authParam, authResultInfo.getToken());
                        Logs.d(CLASSNAME, "register", "authResultInfo=" + registerResultInfo);
                        if (registerResultInfo.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    registerListener.onRegisterSuccessful(registerResultInfo);
                                }
                            });
                        } else {
                            uiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    registerListener.onRegisterFailed(registerResultInfo);
                                }
                            });
                        }
                    } else {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                registerListener.onAuthFailed(authResultInfo);
                            }
                        });
                    }
                }
            }).start();
        }

        public void setOnRegisterListener(RegisterListener listener) {
            this.registerListener = listener;
        }

    }

    public static boolean verifyActivationCode(String activationCode) {
        boolean isVerified = false;
        char verifyCode1 = activationCode.charAt(3);
        char verifyCode2 = activationCode.charAt(7);
        String verifyCode = String.valueOf(verifyCode1) + String.valueOf(verifyCode2);

        Logs.d(CLASSNAME, "verifyActivationCode", "activationCode=" + activationCode);
        Logs.d(CLASSNAME, "verifyActivationCode", "verifyCode1=" + verifyCode1);
        Logs.d(CLASSNAME, "verifyActivationCode", "verifyCode2=" + verifyCode2);

        String part1 = activationCode.substring(0, 3);
        String part2 = activationCode.substring(4, 7);
        String part3 = activationCode.substring(8, activationCode.length());

        String originalCode = part1 + part2 + part3;
        Logs.w(CLASSNAME, "verifyActivationCode", "originalCode=" + originalCode);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(originalCode.getBytes(StandardCharsets.UTF_8));
            String hexHash = String.format("%064x", new BigInteger(1, hash));
            String base32 = Base32.encodeOriginal(hexHash.getBytes());
            String calcVerifyCode = base32.substring(0, 2);
            if (verifyCode.equals(calcVerifyCode)) {
                isVerified = true;
            }
            Logs.w(CLASSNAME, "verifyActivationCode", "hexHash=" + hexHash);
            Logs.w(CLASSNAME, "verifyActivationCode", "base32=" + base32);
        } catch (Exception e) {
            Logs.e(CLASSNAME, "verifyActivationCode", Log.getStackTraceString(e));
        }
        return isVerified;
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
