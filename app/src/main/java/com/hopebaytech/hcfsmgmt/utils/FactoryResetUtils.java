package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.Intent;

/**
 * @author Aaron
 *         Created by Aaron on 2016/7/18.
 */
public class FactoryResetUtils {

    public static void reset(Context context) {
        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("android.intent.extra.REASON", "MasterClearConfirm");
        context.sendBroadcast(intent);
    }

}
