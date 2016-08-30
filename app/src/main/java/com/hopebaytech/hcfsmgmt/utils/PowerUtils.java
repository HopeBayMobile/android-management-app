package com.hopebaytech.hcfsmgmt.utils;

import android.content.Context;
import android.os.PowerManager;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/30.
 */
public class PowerUtils {

    public static void rebootSystem(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot(null);
    }

}
