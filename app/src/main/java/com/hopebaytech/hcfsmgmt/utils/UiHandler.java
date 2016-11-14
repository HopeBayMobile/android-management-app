package com.hopebaytech.hcfsmgmt.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/14.
 */

public class UiHandler {

    private static Handler sUiHandler;

    public static Handler getInstance() {
        if (sUiHandler == null) {
            synchronized (UiHandler.class) {
                if (sUiHandler == null) {
                    sUiHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return sUiHandler;
    }

}
