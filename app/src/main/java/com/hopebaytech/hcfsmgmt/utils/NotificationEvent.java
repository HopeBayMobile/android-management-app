package com.hopebaytech.hcfsmgmt.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.main.LoadingActivity;

/**
 * Created by Aaron on 2016/4/14.
 */
public class NotificationEvent {

    public static void notify(Context context, int notify_id, String notify_title, String notify_message, boolean onGoing) {
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(notify_message);

        Intent intent = new Intent(context, LoadingActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notify_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_tera_app_default);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder = (NotificationCompat.Builder) builder
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon_tera_logo_status_bar)
                .setLargeIcon(largeIcon)
                .setTicker(notify_title)
                .setContentTitle(notify_title)
                .setContentText(notify_message)
                .setStyle(bigStyle);
//                .setContentIntent(contentIntent);
        if (onGoing) {
            builder = (NotificationCompat.Builder) builder
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(contentIntent);
        } else {
            int defaults = 0;
            defaults |= NotificationCompat.DEFAULT_VIBRATE;
            builder = (NotificationCompat.Builder) builder
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setDefaults(defaults)
                    .setContentIntent(contentIntent)
                    .setFullScreenIntent(contentIntent, true);
        }
        Notification notification = builder.build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notify_id, notification);
    }

    public static void cancel(Context context, int notify_id) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(notify_id);
    }

}
