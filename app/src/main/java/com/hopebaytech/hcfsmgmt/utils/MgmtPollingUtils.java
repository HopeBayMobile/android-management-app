package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.Intent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vince
 *         Created by Vince on 2016/7/14.
 */

public class MgmtPollingUtils {

    public static final String KEY_INTERVAL = "key_interval";

    // Not allowed to initialize this class
    private MgmtPollingUtils() {
    }

    /**
     * Start a polling service with given class
     *
     * @param context        A Context of the application package implementing this class.
     * @param intervalMillis interval in milliseconds between subsequent repeats of the service.
     * @param cls            The component class that is to be used for the service.
     */
    public static void startPollingService(Context context, int intervalMillis, Class<?> cls) {
        Intent intentService = new Intent(context, cls);
        intentService.putExtra(KEY_INTERVAL, intervalMillis);
        context.startService(intentService);
    }

    /**
     * Stop a polling service with given class
     *
     * @param context A Context of the application package implementing this class.
     * @param cls     The component class that is to be used for the service.
     */
    public static void stopPollingService(Context context, Class<?> cls) {
        Intent intentService = new Intent(context, cls);
        context.stopService(intentService);
    }

}
