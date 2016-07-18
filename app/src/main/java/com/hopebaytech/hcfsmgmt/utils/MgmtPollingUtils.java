package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Vince on 2016/7/14.
 */

public class MgmtPollingUtils {
    public static void startPollingService(Context context, int seconds, Class<?> cls) {
        Intent intentService = new Intent(context, cls);
        intentService.putExtra("interval", seconds);
        context.startService(intentService);
    }

    public static void stopPollingService(Context context, Class<?> cls) {
        Intent intentService = new Intent(context, cls);
        context.stopService(intentService);
    }
}
