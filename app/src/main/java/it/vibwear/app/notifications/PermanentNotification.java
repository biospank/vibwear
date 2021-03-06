package it.vibwear.app.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import it.lampwireless.vibwear.app.R;
import it.vibwear.app.VibWearActivity;
import it.vibwear.app.handlers.StopNotificationHandler;

/**
 * Created by biospank on 23/10/15.
 */
public class PermanentNotification {
    public static final int VIBWEAR_PERSISTENT_NOTIFICATION_ID = 9571;

    private Context context;

    public PermanentNotification(Context context) {
        this.context = context;
    }

    public void show() {
        Notification.Builder builder = buildNotification();

        NotificationManager notificationManager =
                (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(VIBWEAR_PERSISTENT_NOTIFICATION_ID, builder.build());

    }

    public void cancel() {
        NotificationManager notificationManager =
                (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(VIBWEAR_PERSISTENT_NOTIFICATION_ID);

    }

    private Notification.Builder buildNotification() {
        Intent startIntent = new Intent(this.context, VibWearActivity.class);

        PendingIntent startPendingIntent =
                PendingIntent.getActivity(this.context,
                        VIBWEAR_PERSISTENT_NOTIFICATION_ID,
                        startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this.context)
                .setSmallIcon(R.drawable.ic_vibwear_notification)
                //.setTicker(this.context.getString(R.string.notification_ticker))
                // Set PendingIntent into Notification
                .setContentIntent(startPendingIntent);

        builder = builder.setContent(getNotificationView());

        builder.setOngoing(true);

        return builder;
    }

    private RemoteViews getNotificationView() {
        // Using RemoteViews to bind custom layouts into Notification
        RemoteViews notificationView = new RemoteViews(
                this.context.getPackageName(),
                R.layout.custom_notification
        );

        // Locate and set the Image into customnotificationtext.xml ImageViews
        notificationView.setImageViewResource(
                R.id.imagenotileft,
                R.drawable.ic_launcher);

        // Locate and set the Text into customnotificationtext.xml TextViews
        notificationView.setTextViewText(R.id.title, this.context.getString(R.string.app_name));
        notificationView.setTextViewText(R.id.text, this.context.getString(R.string.tap_to_show));

        return notificationView;
    }
}
