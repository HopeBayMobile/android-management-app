package com.hopebaytech.hcfsmgmt.httpproxy;

import java.io.IOException;

/**
 * @author Aaron
 *         Created by Aaron on 2016/6/3.
 */
public class HttpProxy {

    public static final int MODE_REAL = 0;
    public static final int MODE_MOCK = 1;
    private static int mode = MODE_REAL;

    public static void setMode(int mode) {
        HttpProxy.mode = mode;
    }

    public static IHttpProxy newInstance() throws IOException {
        IHttpProxy httpProxy;
        if (mode == MODE_REAL) {
            httpProxy = new HttpProxyImpl();
        } else {
            httpProxy = new HttpProxyMock();
        }
        return httpProxy;
    }

}
