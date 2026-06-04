package com.dailypromise.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Re-establishes the daily alarm after a device reboot (alarms don't survive). */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && NotificationScheduler.isEnabled(context)) {
            NotificationScheduler.schedule(context);
        }
    }
}
