package com.dailypromise.myapplication;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Stores the user's daily-notification preference (on/off + time) and schedules
 * a repeating daily alarm that fires {@link VerseAlarmReceiver}.
 *
 * <p>Uses an inexact repeating alarm: it doesn't need the exact-alarm permission
 * and is battery friendly, firing within a small window of the chosen time —
 * fine for a daily devotional.
 */
final class NotificationScheduler {

    static final String CHANNEL_ID = "daily_verse";
    static final int NOTIFICATION_ID = 2001;

    private static final String PREFS = "promises_prefs";
    private static final String KEY_ENABLED = "notif_enabled";
    private static final String KEY_HOUR = "notif_hour";
    private static final String KEY_MINUTE = "notif_minute";
    private static final int DEFAULT_HOUR = 8;
    private static final int DEFAULT_MINUTE = 0;
    private static final int REQUEST_CODE = 1001;

    private NotificationScheduler() {
    }

    // ---- Preferences -------------------------------------------------------

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(KEY_ENABLED, false);
    }

    static int getHour(Context context) {
        return prefs(context).getInt(KEY_HOUR, DEFAULT_HOUR);
    }

    static int getMinute(Context context) {
        return prefs(context).getInt(KEY_MINUTE, DEFAULT_MINUTE);
    }

    static void setTime(Context context, int hour, int minute) {
        prefs(context).edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply();
    }

    // ---- Enable / disable --------------------------------------------------

    /** Turns the daily notification on and schedules it. */
    static void enable(Context context) {
        prefs(context).edit().putBoolean(KEY_ENABLED, true).apply();
        createChannel(context);
        schedule(context);
    }

    /** Turns the daily notification off and cancels any pending alarm. */
    static void disable(Context context) {
        prefs(context).edit().putBoolean(KEY_ENABLED, false).apply();
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager != null) {
            alarmManager.cancel(buildPendingIntent(context));
        }
    }

    // ---- Scheduling --------------------------------------------------------

    /** (Re)schedules the repeating daily alarm at the stored time. */
    static void schedule(Context context) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, getHour(context));
        next.set(Calendar.MINUTE, getMinute(context));
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                next.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                buildPendingIntent(context));
    }

    private static PendingIntent buildPendingIntent(Context context) {
        Intent intent = new Intent(context, VerseAlarmReceiver.class);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void createChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.channel_description));
        manager.createNotificationChannel(channel);
    }
}
