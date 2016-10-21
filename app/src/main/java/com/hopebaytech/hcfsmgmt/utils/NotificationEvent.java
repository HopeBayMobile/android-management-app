package com.hopebaytech.hcfsmgmt.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.MainActivity;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/14.
 */
public class NotificationEvent {

    private static final String CLASSNAME = NotificationEvent.class.getSimpleName();

    private static final int FLAG_DEFAULT = 0;
    public static final int FLAG_ON_GOING = 1;
    public static final int FLAG_OPEN_APP = 1 << 1;
    public static final int FLAG_HEADS_UP = 1 << 2;
    public static final int FLAG_IN_PROGRESS = 1 << 3;

    public static void notify(Context context,
                              int notifyId,
                              String notifyTitle,
                              String notifyMessage) {
        notify(context, notifyId, notifyTitle, notifyMessage, R.drawable.icon_tera_logo_status_bar,
                null, FLAG_DEFAULT, null);
    }

    public static void notify(Context context,
                              int notifyId,
                              String notifyTitle,
                              String notifyMessage,
                              int flag) {
        notify(context, notifyId, notifyTitle, notifyMessage, R.drawable.icon_tera_logo_status_bar,
                null, flag, null);
    }

    public static void notify(Context context,
                              int notifyId,
                              String notifyTitle,
                              String notifyMessage,
                              NotificationCompat.Action action,
                              int flag) {
        notify(context, notifyId, notifyTitle, notifyMessage, R.drawable.icon_tera_logo_status_bar,
                action, flag, null);
    }

    public static void notify(Context context,
                              int notifyId,
                              String notifyTitle,
                              String notifyMessage,
                              Bundle extras) {
        notify(context, notifyId, notifyTitle, notifyMessage, R.drawable.icon_tera_logo_status_bar,
                null, FLAG_DEFAULT, extras);
    }

    public static void notify(Context context,
                              int notifyId,
                              String notifyTitle,
                              String notifyMessage,
                              int flag,
                              Bundle extras) {
        notify(context, notifyId, notifyTitle, notifyMessage, R.drawable.icon_tera_logo_status_bar,
                null, flag, extras);
    }

    public static void notify(Context context,
                              int notifyId,
                              String notifyTitle,
                              String notifyMessage,
                              int iconDrawableId,
                              int flag) {
        notify(context, notifyId, notifyTitle, notifyMessage, iconDrawableId, null, flag, null);
    }

    public static void notify(Context context,
                              int notifyId,
                              String notifyTitle,
                              String notifyMessage,
                              int iconDrawableId,
                              NotificationCompat.Action action,
                              int flag,
                              Bundle extras) {

        boolean onGoing = (flag & FLAG_ON_GOING) != 0;
        boolean openApp = (flag & FLAG_OPEN_APP) != 0;
        boolean headsUp = (flag & FLAG_HEADS_UP) != 0;
        boolean inProgress = (flag & FLAG_IN_PROGRESS) != 0;

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(notifyMessage);

        Intent intent = new Intent(context, MainActivity.class);
        if (extras != null) {
            String cause = extras.getString(HCFSMgmtUtils.PREF_AUTO_AUTH_FAILED_CAUSE);
            Logs.w(CLASSNAME, "notify", "cause=" + cause);
            intent.putExtras(extras);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_tera_app_default);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setWhen(System.currentTimeMillis())
                .setCategory(Notification.CATEGORY_EVENT)
                .setSmallIcon(iconDrawableId)
                .setLargeIcon(largeIcon)
                .setTicker(notifyTitle)
                .setContentTitle(notifyTitle)
                .setContentText(notifyMessage)
                .setTicker(notifyMessage)
                .setStyle(bigStyle);
        if (action != null) {
            builder.addAction(action);
        }
        if (openApp) {
            builder.setContentIntent(contentIntent);
        }
        if (inProgress) {
            builder.setProgress(0, 0, true)
                    .setCategory(Notification.CATEGORY_PROGRESS);
        }
        if (headsUp) {
            builder.setFullScreenIntent(contentIntent, true);
        }
        if (onGoing) {
            builder.setAutoCancel(false)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MAX);
        } else {
            int defaults = 0;
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
            builder.setAutoCancel(true)
                    .setOngoing(false)
                    .setDefaults(defaults);
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifyId, builder.build());
    }

    public static void cancel(Context context, int notifyId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notifyId);
    }

}
