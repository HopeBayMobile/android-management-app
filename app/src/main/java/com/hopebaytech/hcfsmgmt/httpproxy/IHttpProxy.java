package com.hopebaytech.hcfsmgmt.httpproxy;

import android.content.ContentValues;

import java.io.IOException;

/**
 * @author Aaron
 *         Created by Aaron on 2016/6/3.
 */
public interface IHttpProxy {

    void setUrl(String url);

    void setHeaders(ContentValues cv);

    void connect() throws IOException;

    int post(ContentValues cv) throws IOException;

    int get() throws IOException;

    String getResponseContent() throws IOException;

    void disconnect();

    void setDoOutput(boolean allowPost);

}
