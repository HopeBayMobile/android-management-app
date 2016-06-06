package com.hopebaytech.hcfsmgmt.httpproxy;

import android.content.ContentValues;

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

    public static final String CORRECT_USER_NAME = "aaron";
    public static final String CORRECT_USER_PASSWORD = "0000";
    public static final String CORRECT_CLIENT_ID = "795577377875-1tj6olgu34bqi7afnnmavvm5hj5vh1tr.apps.googleusercontent.com";

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
            String code = cv.getAsString("code");
            if (code.equals(CORRECT_AUTH_CODE)) {
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
            String username = cv.getAsString("username");
            String password = cv.getAsString("password");
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
        } else if (mUrl.startsWith(MgmtCluster.DEVICE_API)) {
            if (mUrl.contains("change_device_user")) {
                String jwtToken = headers.getAsString("Authorization").replace("JWT ", "");
                String urlImei = mUrl.replace(MgmtCluster.DEVICE_API, "").replace("/change_device_user/", "");
                if (jwtToken.equals(CORRECT_JWT_TOKEN) && urlImei.equals(CORRECT_IMEI)) {
                    String newAuthCode = cv.getAsString("new_auth_code");
                    if (newAuthCode.equals(CORRECT_NEW_AUTH_CODE)) {
                        return HttpsURLConnection.HTTP_OK;
                    } else {
                        return HttpsURLConnection.HTTP_BAD_REQUEST;
                    }
                } else {
                    return HttpsURLConnection.HTTP_BAD_REQUEST;
                }
            }
        }
        return HttpsURLConnection.HTTP_OK;
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
