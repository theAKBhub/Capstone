package com.example.android.capstone.alarm;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * BroadcastReceiver class that invokes Alarm Manager
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Start sound service to play sound for alarm
        context.startService(new Intent(context, AlarmSoundService.class));

        // Send a notification message and show notification in notification tray
        ComponentName comp = new ComponentName(context.getPackageName(),
                AlarmNotificationService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
    }
}
