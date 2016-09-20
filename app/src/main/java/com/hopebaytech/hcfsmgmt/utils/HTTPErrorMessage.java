package com.hopebaytech.hcfsmgmt.utils;

import com.hopebaytech.hcfsmgmt.R;

import java.net.HttpURLConnection;

/**
 * @author Aaron
 *         Created by Aaron on 2016/9/20.
 */
public class HTTPErrorMessage {

    public static int getErrorMessageResId(int errorCode) {
        int errorMsgResId = R.string.http_unknown_error;
        switch (errorCode) {
            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                errorMsgResId = R.string.http_internal_error;
                break;
            case HttpURLConnection.HTTP_GATEWAY_TIMEOUT:
                errorMsgResId = R.string.http_gateway_timeout;
                break;
        }
        return errorMsgResId;
    }

}
