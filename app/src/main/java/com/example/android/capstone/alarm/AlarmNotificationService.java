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

/**
 * This IntentService class is used to handle alarm notifications
 */
public class AlarmNotificationService extends IntentService {

    private NotificationManager mAlarmNotificationManager;
    private final static String NOTIFICATION_CHANNEL_ID = "task_notification_channel";

    public AlarmNotificationService() {
        super("AlarmNotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Send notification
        sendNotification(getString(R.string.label_alarm_title));
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
        stopIntent.putExtra(Constants.INTENT_KEY_ALARM_ACTION, Constants.ALARM_ACTION_DISMISS);
        stopIntent.putExtra(Constants.INTENT_KEY_TASK_FILTER, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // create notification
        NotificationCompat.Builder alamNotificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm)
                        .setContentTitle(message)
                        .setContentText(getString(R.string.label_alarm_message))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.label_alarm_message)))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .addAction(R.mipmap.ic_launcher, getString(R.string.action_stop), pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // notiy notification manager about new notification
        mAlarmNotificationManager.notify(Constants.ALARM_NOTIFICATION_ID, alamNotificationBuilder.build());
    }

    /**
     * Register app's notification channel using an instance of NotificationChannel to createNotificationChannel().
     * This is specifically required for API >= 26
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_name), importance);
            channel.setDescription(getString(R.string.notifiation_channel_desc));

            // register the channel
            if (mAlarmNotificationManager != null) {
                mAlarmNotificationManager.createNotificationChannel(channel);
            }
        }
    }
}