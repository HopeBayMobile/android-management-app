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
    public static final String REGISTER_AUTH_API = "https://" + DOMAIN_NAME + "/api/register/auth/";
    public static final String SOCIAL_AUTH_API = "https://" + DOMAIN_NAME + "/api/social-auth/";
    public static final String USER_AUTH_API = "https://" + DOMAIN_NAME + "/api/auth/";
    public static final String DEVICE_API = "https://" + DOMAIN_NAME + "/api/user/devices/";

    public static final String KEY_AUTH_CODE = "code";
    public static final String KEY_ERROR_CODE = "error_code";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_AUTHORIZATION = "Authorization";
    public static final String KEY_NEW_AUTH_CODE = "new_auth_code";
    public static final String KEY_IMEI = "imei_code";
    public static final String KEY_VENDOR = "vendor";
    public static final String KEY_MODEL = "model";
    public static final String KEY_ACTIVATION_CODE = "activation_code";
    public static final String KEY_BACKEND = "backend";
    public static final String KEY_ANDROID_VERSION = "android_version";
    public static final String KEY_HCFS_VERSION = "HCFS_version";

    public static final String GOOGLE_AUTH_BACKEND = "google-oauth2";

    /**
     * Unknown error.
     */
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

    /**
     * Input parameter relative error.
     */
    public static final String INPUT_ERROR = "INPUT_ERROR";

    /**
     * Device IMEI code is not registered.
     */
    public static final String IMEI_NOT_FOUND = "IMEI_NOT_FOUND";

    /**
     * Device IMEI code decrypt failed.
     */
    public static final String IMEI_DECRYPT_FAILED = "IMEI_DECRYPT_FAILED";

    /**
     * The model information is not matched with the device. It may not support tera service.
     */
    public static final String INCORRECT_MODEL = "INCORRECT_MODEL";

    /**
     * The vendor information is not matched with the device. It may not support tera service.
     */
    public static final String INCORRECT_VENDOR = "INCORRECT_VENDOR";

    /**
     * The device is expired. No support tera service anymore.
     */
    public static final String DEVICE_EXPIRED = "DEVICE_EXPIRED";

    /**
     * Invalid activation code or wrong model mapping. The device may not support tera service.
     */
    public static final String INVALID_CODE_OR_MODEL = "INVALID_CODE_OR_MODEL";

    /**
     * No service registered for the user and device.
     */
    public static final String MAPPING_NOT_FOUND = "MAPPING_NOT_FOUND";

    /**
     * The device is registered.
     */
    public static final String MAPPING_EXISTED = "MAPPING_EXISTED";

    /**
     * The device service is locked or expired.
     */
    public static final String SERVICE_BLOCK = "SERVICE_BLOCK";

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
                data.put(KEY_BACKEND, ((GoogleAuthParam) authParam).authBackend);
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
     * @param jwtToken          the token to access mgmt cluster
     * @param newServerAuthCode the auth code of google account to be switched
     * @param imei              the current device imei
     * @return true if the account is changed successfully; false otherwise.
     */
    public static RegisterResultInfo switchAccount(String jwtToken, String newServerAuthCode, String imei) {
        RegisterResultInfo registerResultInfo = null;
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
                String responseContent = httpProxyImpl.getResponseContent();
                Logs.e(CLASSNAME, "switchAccount", responseContent);
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    registerResultInfo = new RegisterResultInfo();
                    convertRegisterResult(registerResultInfo, responseContent);
                }
            } catch (Exception e) {
                Logs.e(CLASSNAME, "switchAccount", Log.getStackTraceString(e));
            } finally {
                if (httpProxyImpl != null) {
                    httpProxyImpl.disconnect();
                }
            }
        }

        Logs.w(CLASSNAME, "switchAccount", "registerResultInfo=" + registerResultInfo);
        return registerResultInfo;
    }

    public static String getServerClientId() {

        IHttpProxy httpProxyImpl = null;
        String serverClientId = null;
        try {
            httpProxyImpl = HttpProxy.newInstance();
            httpProxyImpl.setUrl(REGISTER_AUTH_API);
            httpProxyImpl.connect();

            int responseCode = httpProxyImpl.get();
            Logs.d(CLASSNAME, "getServerClientId", "responseCode=" + responseCode);
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

    public static abstract class IAuthParam {

        protected String activateCode;
        protected String model;
        protected String vendor;
        protected String imei;
        protected String android_version;
        protected String hcfs_version;

        abstract ContentValues createAuthParam();

        public void setActivateCode(String activateCode) {
            this.activateCode = activateCode;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public void setAndroidVersion(String android_version) {
            this.android_version = android_version;
        }

        public void setHcfsVersion(String hcfs_version) {
            this.hcfs_version = hcfs_version;
        }
    }

    public static class GoogleAuthParam extends IAuthParam {

        private String authCode;
        private String authBackend = GOOGLE_AUTH_BACKEND;

        @Override
        public ContentValues createAuthParam() {

            ContentValues cv = new ContentValues();
            if (authBackend != null) {
                cv.put(KEY_BACKEND, authBackend);
            }
            if (authCode != null) {
                cv.put(KEY_AUTH_CODE, authCode);
            }
            if (activateCode != null) {
                cv.put(KEY_ACTIVATION_CODE, activateCode);
            }
            if (imei != null) {
                cv.put(KEY_IMEI, imei);
            }
            if (vendor != null) {
                cv.put(KEY_VENDOR, vendor);
            }
            if (model != null) {
                cv.put(KEY_MODEL, model);
            }
            if (android_version != null) {
                cv.put(KEY_ANDROID_VERSION, android_version);
            }
            if (hcfs_version != null) {
                cv.put(KEY_HCFS_VERSION, hcfs_version);
            }

            Logs.d(CLASSNAME, "createAuthParam",
                    "authBackend=" + authBackend +
                            ", authCode=" + authCode +
                            ", activateCode=" + activateCode +
                            ", android_version=" + android_version +
                            ", hcfs_version=" + hcfs_version +
                            ", imei=" + imei +
                            ", vendor=" + vendor +
                            ", model=" + model);

            return cv;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }

        public void setAuthBackend(String authBackend) {
            this.authBackend = authBackend;
        }

    }

    public static class UserAuthParam extends IAuthParam {

        private String username;
        private String password;

        @Override
        public ContentValues createAuthParam() {

            ContentValues cv = new ContentValues();
            if (username != null) {
                cv.put(KEY_USERNAME, username);
            }
            if (password != null) {
                cv.put(KEY_PASSWORD, password);
            }
            if (imei != null) {
                cv.put(KEY_IMEI, imei);
            }
            if (activateCode != null) {
                cv.put(KEY_ACTIVATION_CODE, activateCode);
            }
            if (vendor != null) {
                cv.put(KEY_VENDOR, vendor);
            }
            if (model != null) {
                cv.put(KEY_MODEL, model);
            }
            if (android_version != null) {
                cv.put(KEY_ANDROID_VERSION, android_version);
            }
            if (hcfs_version != null) {
                cv.put(KEY_HCFS_VERSION, hcfs_version);
            }

            Logs.d(CLASSNAME, "UserAuthParam", "createAuthParam",
                    "username=" + username +
                            ", password=" + password +
                            ", imei=" + imei +
                            ", activateCode=" + activateCode +
                            ", android_version=" + android_version +
                            ", hcfs_version=" + hcfs_version +
                            ", vendor=" + vendor +
                            ", model=" + model
            );
            return cv;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

    private static RegisterResultInfo convertRegisterResult(RegisterResultInfo registerResultInfo,
                                                            String responseContent) throws JSONException {
        JSONObject jsonObj = new JSONObject(responseContent);
        registerResultInfo.setBackendType(jsonObj.getString("backend_type"));
        registerResultInfo.setAccount(jsonObj.getString("account").split(":")[0]);
        registerResultInfo.setUser(jsonObj.getString("account").split(":")[1]);
        registerResultInfo.setPassword(jsonObj.getString("password"));
        registerResultInfo.setBackendUrl(jsonObj.getString("domain") + ":" + jsonObj.getInt("port"));
        registerResultInfo.setBucket(jsonObj.getString("bucket"));
        registerResultInfo.setProtocol(jsonObj.getBoolean("TLS") ? "https" : "http");
        registerResultInfo.setStorageAccessToken(jsonObj.getString("token"));
        return registerResultInfo;
    }

    public static RegisterResultInfo register(IAuthParam authParam, String jwtToken) {
        RegisterResultInfo registerResultInfo = new RegisterResultInfo();
        IHttpProxy httpProxyImpl = null;
        try {
            httpProxyImpl = HttpProxy.newInstance();
            httpProxyImpl.setUrl(DEVICE_API);
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

                convertRegisterResult(registerResultInfo, responseContent);
//                JSONObject jsonObj = new JSONObject(responseContent);
//                registerResultInfo.setBackendType(jsonObj.getString("backend_type"));
//                registerResultInfo.setAccount(jsonObj.getString("account").split(":")[0]);
//                registerResultInfo.setUser(jsonObj.getString("account").split(":")[1]);
//                registerResultInfo.setPassword(jsonObj.getString("password"));
//                registerResultInfo.setBackendUrl(jsonObj.getString("domain") + ":" + jsonObj.getInt("port"));
//                registerResultInfo.setBucket(jsonObj.getString("bucket"));
//                registerResultInfo.setProtocol(jsonObj.getBoolean("TLS") ? "https" : "http");
//                registerResultInfo.setStorageAccessToken(jsonObj.getString("token"));

                Logs.d(CLASSNAME, "register", "backend_type=" + registerResultInfo.getBackendType());
                Logs.d(CLASSNAME, "register", "account=" + registerResultInfo.getAccount());
                Logs.d(CLASSNAME, "register", "user=" + registerResultInfo.getUser());
                Logs.d(CLASSNAME, "register", "password=" + registerResultInfo.getPassword());
                Logs.d(CLASSNAME, "register", "backend_url=" + registerResultInfo.getBackendUrl());
                Logs.d(CLASSNAME, "register", "protocol=" + registerResultInfo.getProtocol());
                Logs.d(CLASSNAME, "register", "storageAccessToken=" + registerResultInfo.getStorageAccessToken());
            } else {
                String responseContent = httpProxyImpl.getResponseContent();
                try {
                    JSONObject jsonObj = new JSONObject(responseContent);
                    if (responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                        String errorCode = jsonObj.getString("error_code");
                        registerResultInfo.setErrorCode(errorCode);
                    }
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
         * User authentication success, but registration to mgmt cluster failed.
         */
        void onRegisterFailed(RegisterResultInfo registerResultInfo);

    }

    public interface AuthListener {

        void onAuthSuccessful(AuthResultInfo authResultInfo);

        /**
         * User authentication failed.
         */
        void onAuthFailed(AuthResultInfo authResultInfo);

    }

    public static class AuthProxy {

        private AuthListener authListener;
        private MgmtCluster.IAuthParam authParam;

        public AuthProxy(IAuthParam authParam) {
            this.authParam = authParam;
        }

        public void auth() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final AuthResultInfo authResultInfo = MgmtCluster.auth(authParam);
                    if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                authListener.onAuthSuccessful(authResultInfo);
                            }
                        });
                    } else {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                authListener.onAuthFailed(authResultInfo);
                            }
                        });
                    }
                }
            }).start();
        }

        public void setOnAuthListener(AuthListener listener) {
            this.authListener = listener;
        }

    }

    public static class RegisterProxy {

        private RegisterListener registerListener;
        private MgmtCluster.IAuthParam authParam;
        private String jwtToken;

        public RegisterProxy(IAuthParam authParam, String jwtToken) {
            this.authParam = authParam;
            this.jwtToken = jwtToken;
        }

        public void register() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
//                    final AuthResultInfo authResultInfo = MgmtCluster.auth(authParam);
//                    if (authResultInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        final RegisterResultInfo registerResultInfo = MgmtCluster.register(authParam, jwtToken);
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
//                    } else {
//                        uiHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                registerListener.onAuthFailed(authResultInfo);
//                            }
//                        });
//                    }
                }
            }).start();
        }

        public void setOnRegisterListener(RegisterListener listener) {
            this.registerListener = listener;
        }

    }

    public static boolean verifyActivationCode(String activationCode) {
        boolean isVerified = false;
        if (activationCode.length() == 10) {
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
            Logs.d(CLASSNAME, "verifyActivationCode", "originalCode=" + originalCode);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(originalCode.getBytes(StandardCharsets.UTF_8));
                String hexHash = String.format("%064x", new BigInteger(1, hash));
                String base32 = Base32.encodeOriginal(hexHash.getBytes());
                String calcVerifyCode = base32.substring(0, 2);
                if (verifyCode.equals(calcVerifyCode)) {
                    isVerified = true;
                }
                Logs.d(CLASSNAME, "verifyActivationCode", "hexHash=" + hexHash);
                Logs.d(CLASSNAME, "verifyActivationCode", "base32=" + base32);
            } catch (Exception e) {
                Logs.e(CLASSNAME, "verifyActivationCode", Log.getStackTraceString(e));
            }
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

    public static int errorMessageResId(String errorCode) {
        int resId = 0;
        if (errorCode.equals(UNKNOWN_ERROR)) {

        } else if (errorCode.equals(UNKNOWN_ERROR)) {

        } else if (errorCode.equals(INPUT_ERROR)) {

        } else if (errorCode.equals(IMEI_NOT_FOUND)) {

        } else if (errorCode.equals(IMEI_DECRYPT_FAILED)) {

        } else if (errorCode.equals(INCORRECT_MODEL)) {

        } else if (errorCode.equals(INCORRECT_VENDOR)) {

        } else if (errorCode.equals(DEVICE_EXPIRED)) {

        } else if (errorCode.equals(INVALID_CODE_OR_MODEL)) {

        } else if (errorCode.equals(MAPPING_EXISTED)) {

        } else if (errorCode.equals(SERVICE_BLOCK)) {

        } else if (errorCode.equals(MAPPING_NOT_FOUND)) {

        }
        return resId;
    }


}
