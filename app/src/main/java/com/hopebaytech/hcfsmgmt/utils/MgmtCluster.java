package com.hopebaytech.hcfsmgmt.utils;

import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.httpproxy.HttpProxy;
import com.hopebaytech.hcfsmgmt.httpproxy.IHttpProxy;
import com.hopebaytech.hcfsmgmt.info.AuthResultInfo;
import com.hopebaytech.hcfsmgmt.info.DeviceServiceInfo;
import com.hopebaytech.hcfsmgmt.info.TransferContentInfo;
import com.hopebaytech.hcfsmgmt.info.UnlockDeviceInfo;

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
    public static final String KEY_FORCE_CLOSE = "force";
    public static final String KEY_SOURCE_IMEI = "source_imei";

    public static final String GOOGLE_AUTH_BACKEND = "google-oauth2";

    public static final String SERVER_CLIENT_ID = "795577377875-1tj6olgu34bqi7afnnmavvm5hj5vh1tr.apps.googleusercontent.com";

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

    public static class ServiceState {

        /**
         * The device service has been activated.
         */
        public static final String ACTIVATED = "activated";

        /**
         * The device service is disabled. Can not access storage now. (locked state)
         */
        public static final String DISABLED = "disabled";

        /**
         * The device service is disabled and need to do remote reset.
         */
        public static final String DISABLE_N_WIPE = "disabled_n_wipe";

        /**
         * The device service is expired.
         */
        public static final String EXPIRED = "expired";

        /**
         * The device service is ready to transfer to other device.
         */
        public static final String TX_READY = "TXReady";

    }

    public static class ChangeAccountProxy {

        private OnChangeAccountListener listener;
        private String jwtToken;
        private String imei;
        private String newServerAuthCode;

        /**
         * @param jwtToken          the token to access mgmt cluster
         * @param newServerAuthCode the auth code of google account to be switched
         * @param imei              the current device imei
         */
        public ChangeAccountProxy(String jwtToken, String imei, String newServerAuthCode) {
            this.jwtToken = jwtToken;
            this.imei = imei;
            this.newServerAuthCode = newServerAuthCode;
        }

        public void setOnChangeAccountListener(OnChangeAccountListener listener) {
            this.listener = listener;
        }

        public void change() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final DeviceServiceInfo deviceServiceInfo = changeAccount();
                    if (deviceServiceInfo.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onChangeAccountSuccessful(deviceServiceInfo);
                            }
                        });
                    } else {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onChangeAccountFailed(deviceServiceInfo);
                            }
                        });
                    }
                }
            }).start();
        }

        private DeviceServiceInfo changeAccount() {
            DeviceServiceInfo deviceServiceInfo = new DeviceServiceInfo();
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
                    String responseContent = httpProxyImpl.getResponseContent();
                    String decryptResponseContent = HCFSMgmtUtils.getDecryptedJsonString(responseContent);

                    deviceServiceInfo.setResponseCode(responseCode);
                    deviceServiceInfo.setResponseContent(decryptResponseContent);
                    Logs.d(CLASSNAME, "ChangeAccountProxy", "changeAccount",
                            "responseCode=" + responseCode + ", responseContent=" + decryptResponseContent);
                    parseDeviceServiceInfo(deviceServiceInfo, deviceServiceInfo.getResponseContent());
                } catch (Exception e) {
                    Logs.e(CLASSNAME, "ChangeAccountProxy", "change", Log.getStackTraceString(e));
                } finally {
                    if (httpProxyImpl != null) {
                        httpProxyImpl.disconnect();
                    }
                }
            }
            return deviceServiceInfo;
        }

        public interface OnChangeAccountListener {

            void onChangeAccountSuccessful(DeviceServiceInfo deviceServiceInfo);

            void onChangeAccountFailed(DeviceServiceInfo deviceServiceInfo);

        }

    }

    public static class GetDeviceServiceInfoProxy {

        private String jwtToken;
        private String imei;

        private OnGetDeviceServiceInfoListener listener;

        public GetDeviceServiceInfoProxy(String jwtToken, String imei) {
            this.jwtToken = jwtToken;
            this.imei = imei;
        }

        public void get() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final DeviceServiceInfo deviceServiceInfo = getDeviceServiceInfo(jwtToken, imei);
                    if (deviceServiceInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onGetDeviceServiceInfoSuccessful(deviceServiceInfo);
                            }
                        });
                    } else {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onGetDeviceServiceInfoFailed(deviceServiceInfo);
                            }
                        });
                    }
                }
            }).start();
        }

        public void setOnGetDeviceServiceInfoListener(OnGetDeviceServiceInfoListener listener) {
            this.listener = listener;
        }

        public interface OnGetDeviceServiceInfoListener {

            void onGetDeviceServiceInfoSuccessful(DeviceServiceInfo deviceServiceInfo);

            void onGetDeviceServiceInfoFailed(DeviceServiceInfo deviceServiceInfo);

        }

        private DeviceServiceInfo getDeviceServiceInfo(String jwtToken, String imei) {
            IHttpProxy httpProxyImpl = null;
            DeviceServiceInfo deviceServiceInfo = new DeviceServiceInfo();
            try {
                String url = DEVICE_API + imei + "/service/";

                httpProxyImpl = HttpProxy.newInstance();
                httpProxyImpl.setUrl(url);

                ContentValues header = new ContentValues();
                header.put(KEY_AUTHORIZATION, "JWT " + jwtToken);
                httpProxyImpl.setHeaders(header);
                httpProxyImpl.connect();

                int responseCode = httpProxyImpl.get();
                String responseContent = httpProxyImpl.getResponseContent();
                String decryptResponseContent = HCFSMgmtUtils.getDecryptedJsonString(responseContent);

                deviceServiceInfo.setResponseCode(responseCode);
                deviceServiceInfo.setMessage(decryptResponseContent);
                Logs.d(CLASSNAME, "getDeviceServiceInfo", "responseCode=" + responseCode + ", responseContent=" + decryptResponseContent);
            } catch (Exception e) {
                Logs.e(CLASSNAME, "getDeviceServiceInfo", Log.getStackTraceString(e));
            } finally {
                if (httpProxyImpl != null) {
                    httpProxyImpl.disconnect();
                }
            }
            return deviceServiceInfo;
        }

    }

    public static class UnlockDeviceProxy {

        private String jwtToken;
        private String imei;

        private OnUnlockDeviceListener listener;

        public UnlockDeviceProxy(String jwtToken, String imei) {
            this.jwtToken = jwtToken;
            this.imei = imei;
        }

        public void unlock() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final UnlockDeviceInfo unlockDeviceInfo = unlockDevice(jwtToken, imei);
                    if (unlockDeviceInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onUnlockDeviceSuccessful(unlockDeviceInfo);
                            }
                        });
                    } else {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onUnlockDeviceFailed(unlockDeviceInfo);
                            }
                        });
                    }
                }
            }).start();
        }

        public void setOnUnlockDeviceListener(OnUnlockDeviceListener listener) {
            this.listener = listener;
        }

        public interface OnUnlockDeviceListener {

            void onUnlockDeviceSuccessful(UnlockDeviceInfo unlockDeviceInfo);

            void onUnlockDeviceFailed(UnlockDeviceInfo unlockDeviceInfo);

        }

        private UnlockDeviceInfo unlockDevice(String jwtToken, String imei) {
            IHttpProxy httpProxyImpl = null;
            UnlockDeviceInfo unlockDeviceInfo = new UnlockDeviceInfo();
            try {
                String url = DEVICE_API + imei + "/unlock_device/";

                httpProxyImpl = HttpProxy.newInstance();
                httpProxyImpl.setUrl(url);
                httpProxyImpl.setDoOutput(true);

                ContentValues header = new ContentValues();
                header.put(KEY_AUTHORIZATION, "JWT " + jwtToken);
                httpProxyImpl.setHeaders(header);
                httpProxyImpl.connect();

                ContentValues data = new ContentValues();
                int responseCode = httpProxyImpl.post(data);
                String responseContent = httpProxyImpl.getResponseContent();
                unlockDeviceInfo.setResponseCode(responseCode);
                unlockDeviceInfo.setMessage(responseContent);
                Logs.d(CLASSNAME, "transferContents", "responseCode=" + responseCode + ", responseContent=" + responseContent);
            } catch (Exception e) {
                Logs.e(CLASSNAME, "transferContents", Log.getStackTraceString(e));
            } finally {
                if (httpProxyImpl != null) {
                    httpProxyImpl.disconnect();
                }
            }
            return unlockDeviceInfo;
        }

    }

    public static class TransferContentProxy {

        private String jwtToken;
        private String imei;

        private OnTransferContentListener listener;

        public TransferContentProxy(String jwtToken, String imei) {
            this.jwtToken = jwtToken;
            this.imei = imei;
        }

        public void transfer() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final TransferContentInfo transferContentInfo = transferContents(jwtToken, imei);
                    if (transferContentInfo.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onTransferSuccessful(transferContentInfo);
                            }
                        });
                    } else {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onTransferFailed(transferContentInfo);
                            }
                        });
                    }
                }
            }).start();
        }

        public void setOnTransferContentListener(OnTransferContentListener listener) {
            this.listener = listener;
        }

        public interface OnTransferContentListener {

            void onTransferSuccessful(TransferContentInfo transferContentInfo);

            void onTransferFailed(TransferContentInfo transferContentInfo);

        }

        public static TransferContentInfo transferContents(String jwtToken, String imei) {
            IHttpProxy httpProxyImpl = null;
            TransferContentInfo transferContentInfo = new TransferContentInfo();
            try {
                String url = DEVICE_API + imei + "/tx_ready/";

                httpProxyImpl = HttpProxy.newInstance();
                httpProxyImpl.setUrl(url);
                httpProxyImpl.setDoOutput(true);

                ContentValues header = new ContentValues();
                header.put(KEY_AUTHORIZATION, "JWT " + jwtToken);
                httpProxyImpl.setHeaders(header);
                httpProxyImpl.connect();

                ContentValues data = new ContentValues();
                int responseCode = httpProxyImpl.post(data);
                String responseContent = httpProxyImpl.getResponseContent();
                transferContentInfo.setResponseCode(responseCode);
                transferContentInfo.setMessage(responseContent);
                Logs.d(CLASSNAME, "transferContents", "responseCode=" + responseCode + ", responseContent=" + responseContent);
            } catch (Exception e) {
                Logs.e(CLASSNAME, "transferContents", Log.getStackTraceString(e));
            } finally {
                if (httpProxyImpl != null) {
                    httpProxyImpl.disconnect();
                }
            }
            return transferContentInfo;
        }
    }

    public static class SwitchDeviceBackendProxy {

        private OnSwitchDeviceBackendListener listener;
        private SwitchDeviceBackendParam params;
        private String jwtToken;

        public SwitchDeviceBackendProxy(SwitchDeviceBackendParam params, String jwtToken) {
            this.params = params;
            this.jwtToken = jwtToken;
        }

        public void setOnSwitchDeviceBackendListener(OnSwitchDeviceBackendListener listener) {
            this.listener = listener;
        }

        public interface OnSwitchDeviceBackendListener {

            void onSwitchSuccessful(DeviceServiceInfo info);

            void onSwitchFailed(DeviceServiceInfo info);

        }

        public void switchBackend() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final DeviceServiceInfo info = switchDeviceBackend(params);
                    if (info.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSwitchSuccessful(info);
                            }
                        });
                    } else {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSwitchFailed(info);
                            }
                        });
                    }
                }
            }).start();
        }

        private DeviceServiceInfo switchDeviceBackend(SwitchDeviceBackendParam params) {
            IHttpProxy httpProxyImpl = null;
            DeviceServiceInfo deviceServiceInfo = new DeviceServiceInfo();
            try {
                String imei = this.params.sourceImei;
                String url = DEVICE_API + imei + "/switch_device_backend/";

                httpProxyImpl = HttpProxy.newInstance();
                httpProxyImpl.setUrl(url);
                httpProxyImpl.setDoOutput(true);

                ContentValues header = new ContentValues();
                header.put(KEY_AUTHORIZATION, "JWT " + jwtToken);
                httpProxyImpl.setHeaders(header);
                httpProxyImpl.connect();

                int responseCode = httpProxyImpl.post(params.createParams());
                deviceServiceInfo.setResponseCode(responseCode);
                String responseContent = httpProxyImpl.getResponseContent();
                String decryptResponseContent = HCFSMgmtUtils.getDecryptedJsonString(responseContent);
                deviceServiceInfo.setMessage(decryptResponseContent);
                Logs.d(CLASSNAME, "switchDeviceBackend", "responseCode=" + responseCode + ", rsponseContent=" + decryptResponseContent);
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    parseDeviceServiceInfo(deviceServiceInfo, responseContent);
                }
            } catch (Exception e) {
                Logs.e(CLASSNAME, "switchDeviceBackend", Log.getStackTraceString(e));
            } finally {
                if (httpProxyImpl != null) {
                    httpProxyImpl.disconnect();
                }
            }
            return deviceServiceInfo;
        }

    }

    public static class SwitchDeviceBackendParam implements IParam {

        private String sourceImei;
        private String model;
        private String androidVersion;
        private String hcfsVersion;

        public SwitchDeviceBackendParam(Context context) {
            model = Build.MODEL;
            androidVersion = Build.VERSION.RELEASE;
            hcfsVersion = context.getString(R.string.tera_version);
        }

        public void setSourceImei(String sourceImei) {
            this.sourceImei = sourceImei;
        }

        @Override
        public ContentValues createParams() {
            ContentValues cv = new ContentValues();
            cv.put(KEY_SOURCE_IMEI, sourceImei);
            cv.put(KEY_MODEL, model);
            cv.put(KEY_ANDROID_VERSION, androidVersion);
            cv.put(KEY_HCFS_VERSION, hcfsVersion);
            return cv;
        }

    }

    public static String getServerClientId() {
//        IHttpProxy httpProxyImpl = null;
//        String serverClientId = null;
//        try {
//            httpProxyImpl = HttpProxy.newInstance();
//            httpProxyImpl.setUrl(REGISTER_AUTH_API);
//            httpProxyImpl.connect();
//
//            int responseCode = httpProxyImpl.get();
//            Logs.d(CLASSNAME, "getServerClientId", "responseCode=" + responseCode);
//            if (responseCode == HttpsURLConnection.HTTP_OK) {
//                String jsonResponse = httpProxyImpl.getResponseContent();
//                Logs.d(CLASSNAME, "getServerClientId", "jsonResponse=" + jsonResponse);
//                if (!jsonResponse.isEmpty()) {
//                    JSONObject jObj = new JSONObject(jsonResponse);
//                    JSONObject dataObj = jObj.getJSONObject("data");
//                    JSONObject authObj = dataObj.getJSONObject("google-oauth2");
//                    serverClientId = authObj.getString("client_id");
//                    Logs.d(CLASSNAME, "getServerClientId", "server_client_id=" + serverClientId);
//                }
//            }
//        } catch (Exception e) {
//            Logs.e(CLASSNAME, "getServerClientId", Log.getStackTraceString(e));
//        } finally {
//            if (httpProxyImpl != null) {
//                httpProxyImpl.disconnect();
//            }
//        }
//        return serverClientId;
        return SERVER_CLIENT_ID;
    }

    public interface IParam {

        ContentValues createParams();

    }

    public static class GoogleAuthParam implements IParam {

        private String authCode;
        private String authBackend;

        public GoogleAuthParam(String authCode) {
            this.authCode = authCode;
            this.authBackend = GOOGLE_AUTH_BACKEND;
        }

        @Override
        public ContentValues createParams() {
            ContentValues cv = new ContentValues();
            cv.put(KEY_AUTH_CODE, authCode);
            cv.put(KEY_BACKEND, authBackend);

            Logs.d(CLASSNAME, "createAuthParam", "authBackend=" + authBackend
                    + ", authCode=" + authCode);

            return cv;
        }
    }

    public static class RegisterParam implements IParam {

        /**
         * String for encrypted imei.
         */
        private String imei;

        /**
         * True if want to force re-register. Will close the old cloud space.
         */
        private boolean force;
        private String androidVersion;
        private String hcfsVersion;
        private String authBackend;
        private String activateCode;
        private String model;
        private String vendor;

        public RegisterParam(Context context) {
            force = false;
            imei = HCFSMgmtUtils.getEncryptedDeviceImei(HCFSMgmtUtils.getDeviceImei(context));
            vendor = Build.BRAND;
            model = Build.MODEL;
            androidVersion = Build.VERSION.RELEASE;
            hcfsVersion = context.getString(R.string.tera_version);
            authBackend = GOOGLE_AUTH_BACKEND;
        }

        public void setActivateCode(String activateCode) {
            this.activateCode = activateCode;
        }

        /**
         * Close old cloud space and create a new one.
         */
        public void closeOldCloudSpace() {
            this.force = true;
        }

        @Override
        public ContentValues createParams() {
            ContentValues cv = new ContentValues();
            if (authBackend != null) {
                cv.put(KEY_ACTIVATION_CODE, activateCode);
            }
            cv.put(KEY_BACKEND, authBackend);
            cv.put(KEY_IMEI, imei);
            cv.put(KEY_VENDOR, vendor);
            cv.put(KEY_MODEL, model);
            cv.put(KEY_ANDROID_VERSION, androidVersion);
            cv.put(KEY_HCFS_VERSION, hcfsVersion);
            cv.put(KEY_FORCE_CLOSE, force);

            Logs.d(CLASSNAME, "createAuthParam",
                    "authBackend=" + authBackend +
                            ", activateCode=" + activateCode +
                            ", imei=" + imei +
                            ", model=" + model +
                            ", vendor=" + vendor +
                            ", androidVersion=" + androidVersion +
                            ", force=" + force +
                            ", hcfsVersion=" + hcfsVersion);

            return cv;
        }

    }

    private static void parseDeviceServiceInfo(DeviceServiceInfo deviceServiceInfo,
                                               String responseContent) throws JSONException {

        JSONObject result = new JSONObject(responseContent);
        JSONObject piggyback = result.getJSONObject("piggyback");
        String category = piggyback.getString("category");
        String message = piggyback.getString("message");
        if (category != null || message != null) {
            DeviceServiceInfo.Piggyback _piggyback = new DeviceServiceInfo.Piggyback();
            _piggyback.setCategory(category);
            _piggyback.setMessage(message);
            deviceServiceInfo.setPiggyback(_piggyback);
        }

        String state = result.getString("state");
        deviceServiceInfo.setState(state);

        JSONObject backend = result.getJSONObject("backend");
        String url = backend.getString("url");
        String token = backend.getString("token");
        String backendType = backend.getString("backend_type");
        String bucket = backend.getString("bucket");
        String account = backend.getString("account");
        if (url != null || token != null || backendType != null || bucket != null || account != null) {
            DeviceServiceInfo.Backend _backend = new DeviceServiceInfo.Backend();
            _backend.setUrl(url);
            _backend.setToken(token);
            _backend.setBackendType(backendType);
            _backend.setBucket(bucket);
            _backend.setAccount(account);
            deviceServiceInfo.setBackend(_backend);
        }


        /*
        JSONObject jsonObj = new JSONObject(responseContent);

        String token = null;
        try {
            token = jsonObj.getString("token");
        } catch (JSONException e) {
            Logs.w(CLASSNAME, "parseDeviceServiceInfo", Log.getStackTraceString(e));
        }

        if (token != null) {
            registerResultInfo.setBackendType(jsonObj.getString("backend_type") + "token"); // swifttoken
            registerResultInfo.setStorageAccessToken(token);
        } else {
            registerResultInfo.setBackendType(jsonObj.getString("backend_type"));
        }

        registerResultInfo.setBackendUser(jsonObj.getString("account").split(":")[0]);
        registerResultInfo.setBackendUrl(jsonObj.getString("url"));
        registerResultInfo.setBucket(jsonObj.getString("bucket"));
        return registerResultInfo;
*/
    }

    /**
     * Listener for fetching available JWT token from MGMT server
     *
     * @author Aaron
     *         Created by Aaron on 2016/4/19.
     */
    public interface OnFetchJwtTokenListener {

        /**
         * Callback function when fetch successful
         */
        void onFetchSuccessful(String jwtToken);

        /**
         * Callback function when fetch failed
         */
        void onFetchFailed();

    }

    /**
     * Listener for fetching available JWT token from MGMT server
     *
     * @author Aaron
     *         Created by Aaron on 2016/7/11.
     */
    public interface FetchJwtTokenListener {

        /**
         * Callback function when fetch successful
         */
        void onFetchSuccessful(String jwtToken);

        /**
         * Callback function when fetch failed
         */
        void onFetchFailed();

    }

    public interface RegisterListener {

        void onRegisterSuccessful(DeviceServiceInfo deviceServiceInfo);

        /**
         * User authentication success, but registration to mgmt cluster failed.
         */
        void onRegisterFailed(DeviceServiceInfo deviceServiceInfo);

    }

    public interface OnAuthListener {

        void onAuthSuccessful(AuthResultInfo authResultInfo);

        /**
         * User authentication failed.
         */
        void onAuthFailed(AuthResultInfo authResultInfo);

    }

    public static class AuthProxy {

        private OnAuthListener authListener;
        private GoogleAuthParam authParam;

        public AuthProxy(GoogleAuthParam authParam) {
            this.authParam = authParam;
        }

        public void auth() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final AuthResultInfo authResultInfo = auth(authParam);
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

        public void setOnAuthListener(OnAuthListener listener) {
            this.authListener = listener;
        }

        public static AuthResultInfo auth(GoogleAuthParam authParam) {

            IHttpProxy httpProxyImpl = null;
            AuthResultInfo authResultInfo = new AuthResultInfo();
            try {
                String url = SOCIAL_AUTH_API;
                ContentValues data = new ContentValues();
                data.put(KEY_AUTH_CODE, authParam.authCode);
                data.put(KEY_BACKEND, authParam.authBackend);

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

    }

    public static class RegisterProxy {

        private RegisterListener registerListener;
        private MgmtCluster.RegisterParam registerParam;
        private String jwtToken;

        public RegisterProxy(RegisterParam registerParam, String jwtToken) {
            this.registerParam = registerParam;
            this.jwtToken = jwtToken;
        }

        public void register() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Handler uiHandler = new Handler(Looper.getMainLooper());
                    final DeviceServiceInfo deviceServiceInfo = register(registerParam, jwtToken);
                    Logs.d(CLASSNAME, "register", "deviceServiceInfo=" + deviceServiceInfo);
                    if (deviceServiceInfo.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                registerListener.onRegisterSuccessful(deviceServiceInfo);
                            }
                        });
                    } else {
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                registerListener.onRegisterFailed(deviceServiceInfo);
                            }
                        });
                    }
                }
            }).start();
        }

        public void setOnRegisterListener(RegisterListener listener) {
            this.registerListener = listener;
        }

        public static DeviceServiceInfo register(RegisterParam registerParam, String jwtToken) {
            DeviceServiceInfo deviceServiceInfo = new DeviceServiceInfo();
            IHttpProxy httpProxyImpl = null;
            try {
                httpProxyImpl = HttpProxy.newInstance();
                httpProxyImpl.setUrl(DEVICE_API);
                httpProxyImpl.setDoOutput(true);
                ContentValues header = new ContentValues();
                header.put(KEY_AUTHORIZATION, "JWT " + jwtToken);
                httpProxyImpl.setHeaders(header);
                httpProxyImpl.connect();

                int responseCode = httpProxyImpl.post(registerParam.createParams());
                deviceServiceInfo.setResponseCode(responseCode);
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String responseContent = httpProxyImpl.getResponseContent();
                    String decryptResponseContent = HCFSMgmtUtils.getDecryptedJsonString(responseContent);
                    deviceServiceInfo.setMessage(decryptResponseContent);
                    Logs.d(CLASSNAME, "register", "responseCode=" + responseCode + ", responseContent=" + decryptResponseContent);

                    parseDeviceServiceInfo(deviceServiceInfo, decryptResponseContent);

                    Logs.d(CLASSNAME, "register", "backend_type=" + deviceServiceInfo.getBackend().getBackendType());
                    Logs.d(CLASSNAME, "register", "backend_url=" + deviceServiceInfo.getBackend().getUrl());
                    Logs.d(CLASSNAME, "register", "backend_user=" + deviceServiceInfo.getBackend().getUser());
                    Logs.d(CLASSNAME, "register", "bucket=" + deviceServiceInfo.getBackend().getBucket());
                    Logs.d(CLASSNAME, "register", "token=" + deviceServiceInfo.getBackend().getToken());
                } else {
                    String responseContent = httpProxyImpl.getResponseContent();
                    String decryptResponseContent = HCFSMgmtUtils.getDecryptedJsonString(responseContent);
                    try {
                        JSONObject jsonObj = new JSONObject(decryptResponseContent);
                        if (responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                            String errorCode = jsonObj.getString("error_code");
                            deviceServiceInfo.setErrorCode(errorCode);
                        }
                        String message = jsonObj.getString("detail");
                        deviceServiceInfo.setMessage(message);
                    } catch (JSONException e) {
                        Logs.e(CLASSNAME, "register", Log.getStackTraceString(e));
                    }
                }
            } catch (Exception e) {
                deviceServiceInfo.setMessage(Log.getStackTraceString(e));
                Logs.e(CLASSNAME, "register", Log.getStackTraceString(e));
            } finally {
                if (httpProxyImpl != null) {
                    httpProxyImpl.disconnect();
                }
            }

            return deviceServiceInfo;
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

    /**
     * Get an available JWT token from MGMT server
     */
    public static void getJwtToken(final Context context, final OnFetchJwtTokenListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String serverClientId = MgmtCluster.getServerClientId();
                GoogleSilentAuthProxy googleAuthProxy = new GoogleSilentAuthProxy(context, serverClientId,
                        new GoogleSilentAuthProxy.OnAuthListener() {
                            @Override
                            public void onAuthSuccessful(GoogleSignInResult result, GoogleApiClient googleApiClient) {
                                String serverAuthCode = result.getSignInAccount().getServerAuthCode();
                                GoogleAuthParam authParam = new GoogleAuthParam(serverAuthCode);
                                MgmtCluster.AuthProxy authProxy = new MgmtCluster.AuthProxy(authParam);
                                authProxy.setOnAuthListener(new MgmtCluster.OnAuthListener() {
                                    @Override
                                    public void onAuthSuccessful(AuthResultInfo authResultInfo) {
                                        String jwtToken = authResultInfo.getToken();
                                        listener.onFetchSuccessful(jwtToken);
                                    }

                                    @Override
                                    public void onAuthFailed(AuthResultInfo authResultInfo) {
                                        listener.onFetchFailed();
                                    }
                                });
                                authProxy.auth();
                            }

                            @Override
                            public void onAuthFailed() {
                                listener.onFetchFailed();
                            }

                        });
                googleAuthProxy.auth();
            }
        }).start();
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
