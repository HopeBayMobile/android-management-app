package com.hopebaytech.hcfsmgmt.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.hopebaytech.hcfsmgmt.misc.Threshold;
import com.hopebaytech.hcfsmgmt.service.TeraMgmtService;

import java.util.Calendar;

/**
 * @author Aaron
 *         Created by Aaron on 2016/12/9.
 */
public class AlarmUtils {

    private static final String CLASSNAME = AlarmUtils.class.getSimpleName();

    /**
     * Start an alarm to monitor pin space, the time interval is {@link Interval#MONITOR_PINNED_SPACE}.
     * If the pin space is larger than {@link Threshold#PINNED_SPACE} of total pin space, a warning
     * notification will be sent to notify user.
     */
    public static void startMonitorPinnedSpace(Context context) {
        Logs.d(CLASSNAME, "startMonitorPinnedSpace", null);

        Intent intent = new Intent(context, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_MONITOR_PINNED_SPACE);

        int requestCode = RequestCode.MONITOR_PIN_SPACE;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime();
        long intervalMillis = Interval.MONITOR_PINNED_SPACE;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
    }

    public static void stopMonitorInsufficientPinSpace(Context context) {
        Logs.d(CLASSNAME, "stopMonitorInsufficientPinSpace", null);

        Intent intent = new Intent(context, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_MONITOR_PINNED_SPACE);

        int requestCode = RequestCode.MONITOR_PIN_SPACE;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    /**
     * Start an alarm to monitor local storage used space, the time interval is
     * {@link Interval#MONITOR_LOCAL_STORAGE_USED_SPACE}. If the pin space is larger than the
     * specified threshold by user of total pinned space, a warning notification will be sent to
     * notify user.
     */
    public static void startMonitorLocalStorageUsedSpace(Context context) {
        Logs.d(CLASSNAME, "startMonitorLocalStorageUsedSpace", null);

        Intent intent = new Intent(context, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_MONITOR_LOCAL_STORAGE_USED_SPACE);

        int requestCode = RequestCode.MONITOR_LOCAL_STORAGE_USED_SPACE;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime();
        long intervalMillis = Interval.MONITOR_LOCAL_STORAGE_USED_SPACE;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
    }

    public static void stopMonitorLocalStorageUsedSpace(Context context) {
        Logs.d(CLASSNAME, "stopMonitorLocalStorageUsedSpace", null);

        Intent intent = new Intent(context, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_MONITOR_LOCAL_STORAGE_USED_SPACE);

        int requestCode = RequestCode.MONITOR_LOCAL_STORAGE_USED_SPACE;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    /**
     * Start an alarm to reset data xfer, the time interval is {@link Interval#RESET_DATA_XFER}.
     */
    public static void startResetDataXferAlarm(Context context) {
        Logs.d(CLASSNAME, "startResetDataXferAlarm", null);

        Intent intent = new Intent(context, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_RESET_DATA_XFER);

        int requestCode = RequestCode.RESET_DATA_XFER;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, flags);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = Interval.RESET_DATA_XFER;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pi);
    }

    /**
     * Start an alarm to monitor the /sdcard/Android/* dir of apps, the time interval is
     * {@link Interval#MONITOR_EXTERNAL_APP_DIR}. If the external dir of an app is exist but not
     * recorded in uid.db, we need to update the external dir path of the app to database.
     */
    public static void startMonitorExternalAppDirAlarm(Context context) {
        Logs.d(CLASSNAME, "startMonitorExternalAppDirAlarm", null);

        Intent intent = new Intent(context, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_MONITOR_EXTERNAL_APP_DIR);

        int requestCode = RequestCode.MONITOR_EXTERNAL_APP_DIR;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, flags);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = Interval.MONITOR_EXTERNAL_APP_DIR;
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pi);
    }

    public static void stopResetXferAlarm(Context context) {
        Logs.d(CLASSNAME, "stopResetXferAlarm", null);

        Intent intent = new Intent(context, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_RESET_DATA_XFER);

        int requestCode = RequestCode.RESET_DATA_XFER;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, flags);
        pi.cancel();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    /**
     * Start an alarm to monitor the booster used space, the monitor time interval is
     * {@link Interval#MONITOR_BOOSTER_USED_SPACE}. If the booster used space is larger than
     * {@link Threshold#BOOSTER_USED_SPACE} of booster size, an alert dialog will pop up to notify user.
     */
    public static void startMonitorBoosterUsedSpace(Context context) {
        Logs.d(CLASSNAME, "startMonitorBoosterUsedSpace", null);

        Intent intent = new Intent(context, TeraMgmtService.class);
        intent.setAction(TeraIntent.ACTION_MONITOR_BOOSTER_USED_SPACE);

        int requestCode = RequestCode.MONITOR_BOOSTER_USED_SPACE;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getService(context, requestCode, intent, flags);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime();
        long intervalMillis = Interval.MONITOR_BOOSTER_USED_SPACE;
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtMillis, intervalMillis, pi);
    }

}
