package com.tunnellight.biblepromise;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;

/** Fired by the daily alarm; posts a notification with the verse for the day. */
public class VerseAlarmReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission") // guarded by areNotificationsEnabled()
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationScheduler.createChannel(context);

        VerseRepository repository = new VerseRepository();
        Verse verse = repository.get(repository.indexForDate(LocalDate.now()));

        Intent open = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String fullText = context.getString(R.string.quoted_verse_format, verse.text)
                + "  " + context.getString(R.string.verse_reference_format, verse.reference);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, NotificationScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.verse_of_the_day))
                .setContentText(verse.text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(fullText))
                .setColor(ContextCompat.getColor(context, R.color.accent_gold))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (manager.areNotificationsEnabled()) {
            manager.notify(NotificationScheduler.NOTIFICATION_ID, builder.build());
        }
    }
}
