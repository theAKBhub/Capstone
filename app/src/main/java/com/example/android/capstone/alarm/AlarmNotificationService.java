package com.example.android.capstone.alarm;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import com.example.android.capstone.R;
import com.example.android.capstone.helper.Constants;
import com.example.android.capstone.ui.TaskListActivity;

public class AlarmNotificationService extends IntentService {

    private NotificationManager mAlarmNotificationManager;

    // Notification ID for Alarm
    public static final int NOTIFICATION_ID = 1;

    private final static String NOTIFICATION_CHANNEL_ID = "task_notification_channel";


    public AlarmNotificationService() {
        super("AlarmNotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Send notification
        sendNotification("Time to wake up");
    }

    /**
     * Method to handle notification
     */
    private void sendNotification(String message) {
        mAlarmNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

        // get pending intent
        PendingIntent contentIntent = PendingIntent.getActivity(this, 1,
                new Intent(this, TaskListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // stop alarm from notification
        Intent stopIntent = new Intent(this, TaskListActivity.class);
        stopIntent.putExtra("action", "dismiss");
        stopIntent.putExtra(Constants.INTENT_KEY_TASK_FILTER, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        // Create notification
        NotificationCompat.Builder alamNotificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle(message)
                        .setContentText("Much longer text that cannot fit one line...")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Much longer text that cannot fit one line..."))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .addAction(R.mipmap.ic_launcher, "STOP", pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // notiy notification manager about new notification
        mAlarmNotificationManager.notify(NOTIFICATION_ID, alamNotificationBuilder.build());
    }

    /**
     * Before notification can be delivered on Android 8.0 and higher, the app's notification
     * channel must be registered with the system by passing an instance of NotificationChannel to
     * createNotificationChannel()
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            CharSequence name = "Channel";
            String description = "Notification Channel Description";
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            if (mAlarmNotificationManager != null) {
                mAlarmNotificationManager.createNotificationChannel(channel);
            }
        }
    }
}