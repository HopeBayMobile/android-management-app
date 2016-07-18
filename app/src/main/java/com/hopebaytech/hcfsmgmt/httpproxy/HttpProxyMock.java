package com.hopebaytech.hcfsmgmt.httpproxy;

import android.content.ContentValues;

import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/25.
 */
public class HttpProxyMock implements IHttpProxy {

    private static final String CLASSNAME = HttpProxyMock.class.getSimpleName();
    private HttpsURLConnection mConn;
    private int mResponseCode;
    private String mResponseContent;
    private ContentValues headers;

    public static final String CORRECT_AUTH_CODE = "0000000000";
    public static final String CORRECT_IMEI = "0000000001";
    public static final String CORRECT_NEW_AUTH_CODE = "0000000002";
    public static final String CORRECT_JWT_TOKEN = "0000000003";
    public static final String CORRECT_ACTIVATION_CODE = "000000004";
    public static final String CORRECT_USER_NAME = "aaron";
    public static final String CORRECT_USER_PASSWORD = "0000";
    public static final String CORRECT_CLIENT_ID = "795577377875-1tj6olgu34bqi7afnnmavvm5hj5vh1tr.apps.googleusercontent.com";

    public static final String INCORRECT_IMEI = "-";
    public static final String INCORRECT_AUTH_CODE = "-";
    public static final String INCORRECT_JWT_TOKEN = "-";
    public static final String INCORRECT_USER_NAME = "-";
    public static final String INCORRECT_USER_PASSWORD = "-";
    public static final String INCORRECT_ACTIVATION_CODE = "-";
    public static final String INCORRECT_CLIENT_ID = "-";

    private String mUrl;

    @Override
    public void setUrl(String url) {
        this.mUrl = url;
    }

    public void setHeaders(ContentValues cv) {
        this.headers = cv;
    }

    public void connect() throws IOException {

    }

    public int post(ContentValues cv) throws IOException {
        if (mUrl.equals(MgmtCluster.SOCIAL_AUTH_API)) {
            String authCode = cv.getAsString(MgmtCluster.KEY_AUTH_CODE);
            if (authCode.equals(CORRECT_AUTH_CODE)) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("token", "magic_token");
                    mResponseContent = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return HttpsURLConnection.HTTP_OK;
            } else {
                return HttpsURLConnection.HTTP_BAD_REQUEST;
            }
        } else if (mUrl.equals(MgmtCluster.USER_AUTH_API)) {
            String username = cv.getAsString(MgmtCluster.KEY_USERNAME);
            String password = cv.getAsString(MgmtCluster.KEY_PASSWORD);
            if (username.equals(CORRECT_USER_NAME) && password.equals(CORRECT_USER_PASSWORD)) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("token", "magic_token");
                    mResponseContent = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return HttpsURLConnection.HTTP_OK;
            } else {
                return HttpsURLConnection.HTTP_BAD_REQUEST;
            }
        } else if (mUrl.startsWith(MgmtCluster.DEVICE_API) && mUrl.contains("change_device_user")) {
            String jwtToken = headers.getAsString(MgmtCluster.KEY_AUTHORIZATION).replace("JWT ", "");
            String urlImei = mUrl.replace(MgmtCluster.DEVICE_API, "").replace("/change_device_user/", "");
            if (jwtToken.equals(CORRECT_JWT_TOKEN) && urlImei.equals(CORRECT_IMEI)) {
                String newAuthCode = cv.getAsString(MgmtCluster.KEY_NEW_AUTH_CODE);
                if (newAuthCode.equals(CORRECT_NEW_AUTH_CODE)) {
                    JSONObject jsonObj = new JSONObject();
                    try {
                        jsonObj.put("backend_type", "swift");
                        jsonObj.put("account", "aaron:aaron");
                        jsonObj.put("password", "0000");
                        jsonObj.put("domain", "www.hopebaytech.com");
                        jsonObj.put("port", "80");
                        jsonObj.put("bucket", "");
                        jsonObj.put("TLS", true);
                        jsonObj.put("token", "xxxxxxxx");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mResponseContent = jsonObj.toString();
                    return HttpsURLConnection.HTTP_OK;
                } else {
                    return HttpsURLConnection.HTTP_BAD_REQUEST;
                }
            } else {
                return HttpsURLConnection.HTTP_BAD_REQUEST;
            }
        } else if (mUrl.startsWith(MgmtCluster.DEVICE_API) && mUrl.contains("tx_ready")) {
            String jwtToken = headers.getAsString(MgmtCluster.KEY_AUTHORIZATION).replace("JWT ", "");
            String imei = mUrl.replace(MgmtCluster.DEVICE_API, "").replace("/tx_ready/", "");
            if (jwtToken.equals(CORRECT_JWT_TOKEN)) {
                if (imei.equals(CORRECT_IMEI)) {
                    return HttpsURLConnection.HTTP_OK;
                } else {
                    return HttpsURLConnection.HTTP_BAD_REQUEST;
                }
            } else {
                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("detail", "HTTP_FORBIDDEN");
                    mResponseContent = jsonObj.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return HttpsURLConnection.HTTP_FORBIDDEN;
            }
        } else if (mUrl.equals(MgmtCluster.DEVICE_API)) {
            String jwtToken = headers.getAsString(MgmtCluster.KEY_AUTHORIZATION).replace("JWT ", "");
            String imei = cv.getAsString(MgmtCluster.KEY_IMEI);
            String authCode = cv.getAsString(MgmtCluster.KEY_AUTH_CODE);
            String username = cv.getAsString(MgmtCluster.KEY_USERNAME);
            String password = cv.getAsString(MgmtCluster.KEY_PASSWORD);
            String activationCode = cv.getAsString(MgmtCluster.KEY_ACTIVATION_CODE);
            if (jwtToken.equals(CORRECT_JWT_TOKEN)) {
                int responseCode = HttpsURLConnection.HTTP_BAD_REQUEST;
                boolean correct = false;
                if (authCode != null) {
                    if (authCode.equals(CORRECT_AUTH_CODE)) {
                        if (imei.equals(CORRECT_IMEI)) {
                            correct = true;
                            responseCode = HttpsURLConnection.HTTP_OK;
                        } else {
                            responseCode = HttpsURLConnection.HTTP_NOT_FOUND;
                        }
                    }
                }

                if (username != null) {
                    if (username.equals(CORRECT_USER_NAME) && password.equals(CORRECT_USER_PASSWORD)) {
                        if (activationCode.equals(CORRECT_ACTIVATION_CODE)) {
                            if (imei.equals(CORRECT_IMEI)) {
                                correct = true;
                                responseCode = HttpsURLConnection.HTTP_OK;
                            } else {
                                responseCode = HttpsURLConnection.HTTP_NOT_FOUND;
                            }
                        }
                    }
                }

                if (correct) {
                    JSONObject jsonObj = new JSONObject();
                    try {
                        jsonObj.put("backend_type", "swift");
                        jsonObj.put("account", "aaron:aaron");
                        jsonObj.put("password", "0000");
                        jsonObj.put("domain", "www.hopebaytech.com");
                        jsonObj.put("port", "80");
                        jsonObj.put("bucket", "");
                        jsonObj.put("TLS", true);
                        jsonObj.put("token", "xxxxxxxx");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mResponseContent = jsonObj.toString();
                } else {
                    try {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("detail", "HTTP_NOT_FOUND");
                        jsonObj.put("error_code", MgmtCluster.INVALID_CODE_OR_MODEL);
                        mResponseContent = jsonObj.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return responseCode;
            } else {
                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("detail", "HTTP_BAD_REQUEST");
                    jsonObj.put("error_code", MgmtCluster.INVALID_CODE_OR_MODEL);
                    mResponseContent = jsonObj.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return HttpsURLConnection.HTTP_FORBIDDEN;
            }
        }
        return HttpsURLConnection.HTTP_INTERNAL_ERROR;
    }

    @Override
    public int get() throws IOException {
        if (mUrl.equals(MgmtCluster.REGISTER_AUTH_API)) {
            try {
                JSONObject jsonObject = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject auth = new JSONObject();
                auth.put("client_id", CORRECT_CLIENT_ID);
                data.put("google-oauth2", auth);
                jsonObject.put("data", data);
                mResponseContent = jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return HttpsURLConnection.HTTP_OK;
        }
        return HttpsURLConnection.HTTP_OK;
    }

    public String getResponseContent() throws IOException {
        return mResponseContent;
    }

    public void disconnect() {

    }

    @Override
    public void setDoOutput(boolean flag) {

    }

}
