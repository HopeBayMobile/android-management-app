package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.content.Intent;

/**
 * @author Vince
 *         Created by Vince on 2016/7/14.
 */

public class MgmtPollingUtils {

    // Not allowed to initialize this class
    private MgmtPollingUtils() {

    }

    /**
     * Start a polling service with given class
     *
     * @param context         A Context of the application package implementing this class.
     * @param intervalSeconds interval in seconds between subsequent repeats of the service.
     * @param cls             The component class that is to be used for the service.
     */
    public static void startPollingService(Context context, int intervalSeconds, Class<?> cls) {
        Intent intentService = new Intent(context, cls);
        intentService.putExtra("interval", intervalSeconds);
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
