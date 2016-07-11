package com.hopebaytech.hcfsmgmt.httpproxy;

import android.content.ContentValues;
import android.util.Log;

import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;
import com.hopebaytech.hcfsmgmt.utils.Logs;

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
public class HttpProxyImpl implements IHttpProxy {

    private static final String CLASSNAME = HttpProxyImpl.class.getSimpleName();
    private HttpsURLConnection mConn;
    private int mResponseCode;
    private String url;
    private ContentValues header;
    private boolean allowPost;

    @Override
    public void setDoOutput(boolean allowPost) {
        this.allowPost = allowPost;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    public void setHeaders(ContentValues cv) {
        this.header = cv;
    }

    public void connect() throws IOException {
        mConn = (HttpsURLConnection) new URL(url).openConnection();
        mConn.setDoInput(true);
        if (allowPost) {
            mConn.setDoOutput(true);
        }
        if (header != null) {
            for (String key: header.keySet()) {
                mConn.setRequestProperty(key, header.getAsString(key));
            }
        }
        mConn.connect();
    }

    public int post(ContentValues cv) throws IOException {
        OutputStream outputStream = mConn.getOutputStream();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        bufferedWriter.write(getQuery(cv));
        bufferedWriter.flush();
        bufferedWriter.close();
        outputStream.close();

        mResponseCode = mConn.getResponseCode();
        return mResponseCode;
    }

    @Override
    public int get() throws IOException {
        mResponseCode = mConn.getResponseCode();
        return mResponseCode;
    }

    public String getResponseContent() throws IOException {
        InputStream inputStream = null;
        StringBuilder sb = new StringBuilder();
        try {
            if (mResponseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = mConn.getErrorStream();
            } else {
                inputStream = mConn.getInputStream();
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            Logs.e(CLASSNAME, "getResponseContent", Log.getStackTraceString(e));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return sb.toString();
    }

    public void disconnect() {
        mConn.disconnect();
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
                String value = entry.getValue() == null ? "" : entry.getValue().toString();
                result.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Logs.e(CLASSNAME, "getQuery", Log.getStackTraceString(e));
            }
        }
        Logs.d(CLASSNAME, "getQuery", "query=" + result.toString());
        return result.toString();
    }


}
