package com.example.android.capstone.alarm;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.example.android.capstone.R;
import com.example.android.capstone.helper.Constants;

/**
 * This class will play the alarm sound in onCreate() and stop the sound in onDestroy() when user stops the Alarm.
 */

public class AlarmSoundService extends Service {

    private MediaPlayer mMediaPlayer;
    private String mAction;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (intent.getExtras() != null) {
            mAction = intent.getStringExtra(Constants.INTENT_KEY_ALARM_ACTION);
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Start media player
        mMediaPlayer = MediaPlayer.create(this, R.raw.bell);
        mMediaPlayer.start();
        mMediaPlayer.setLooping(true); //set looping true to run it infinitely
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // On destory stop and release the media player
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
    }
}
