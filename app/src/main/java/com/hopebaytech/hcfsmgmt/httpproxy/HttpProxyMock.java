package com.hopebaytech.hcfsmgmt.httpproxy;

import android.content.ContentValues;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;
import com.hopebaytech.hcfsmgmt.utils.MgmtCluster;

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
import java.util.Map;

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

    public static final String CORRECT_AUTH_CODE = "0000000000";
    public static final String CORRECT_IMEI = "0000000001";
    public static final String CORRECT_OLD_AUTH_CODE = "0000000002";
    public static final String CORRECT_NEW_AUTH_CODE = "0000000003";

    public static final String CORRECT_USER_NAME= "aaron";
    public static final String CORRECT_USER_PASSWORD= "0000";

    private String mUrl;

    @Override
    public void setUrl(String url) {
        this.mUrl = url;
    }

    public void setHeaders(ContentValues cv) {

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
        }
        return 201;
    }

    public String getResponseContent() throws IOException {
        return "123";
    }

    public void disconnect() {

    }

    @Override
    public void setDoOutput(boolean flag) {

    }

}
