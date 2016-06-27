package com.hopebaytech.hcfsmgmt.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.LoadingActivity;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/14.
 */
public class NotificationEvent {

    private static final int FLAG_DEFAULT = 0;
    public static final int FLAG_ON_GOING = 1;
    public static final int FLAG_JUMP_TO_APP = 1 << 1;

    public static void notify(Context context, int notifyId, String notifyTitle, String notifyMessage) {
        notify(context, notifyId, notifyTitle, notifyMessage, FLAG_DEFAULT, null);
    }

    public static void notify(Context context, int notifyId, String notifyTitle, String notifyMessage, int flag) {
        notify(context, notifyId, notifyTitle, notifyMessage, flag, null);
    }

    public static void notify(Context context, int notifyId, String notifyTitle, String notifyMessage, Bundle extras) {
        notify(context, notifyId, notifyTitle, notifyMessage, FLAG_DEFAULT, extras);
    }

    public static void notify(Context context, int notifyId, String notifyTitle, String notifyMessage, int flag, Bundle extras) {
        boolean onGoing = (flag & FLAG_ON_GOING) != 0;
        boolean jumpToApp = (flag & FLAG_JUMP_TO_APP) != 0;

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(notifyMessage);

        Intent intent = new Intent(context, LoadingActivity.class);
        if (extras != null) {
            intent.putExtras(extras);
        }
        PendingIntent contentIntent = PendingIntent.getActivity(context, notifyId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_tera_app_default);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder = (NotificationCompat.Builder) builder
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_tera_logo_status_bar)
                .setLargeIcon(largeIcon)
                .setTicker(notifyTitle)
                .setContentTitle(notifyTitle)
                .setContentText(notifyMessage)
                .setStyle(bigStyle);
        if (jumpToApp) {
            builder.setContentIntent(contentIntent);
        }

        if (onGoing) {
            builder = (NotificationCompat.Builder) builder
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MAX);
        } else {
            int defaults = 0;
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
            builder = (NotificationCompat.Builder) builder
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setDefaults(defaults)
                    .setFullScreenIntent(contentIntent, true);
        }
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notifyId, notification);
    }

    public static void cancel(Context context, int notify_id) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(notify_id);
    }

}
