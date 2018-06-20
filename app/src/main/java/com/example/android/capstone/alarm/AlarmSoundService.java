package com.example.android.capstone.alarm;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.example.android.capstone.R;

/**
 * This class will play the alarm sound in onCreate() and stop the sound in onDestroy() when user stops the Alarm.
 */

public class AlarmSoundService extends Service {

    private static final String TAG = AlarmSoundService.class.getSimpleName();
    private static final String URI_BASE = AlarmSoundService.class.getName() + ".";
    //public static final String ACTION_DISMISS = URI_BASE + "ACTION_DISMISS";

    private MediaPlayer mMediaPlayer;
    private Intent mIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (intent.getExtras() != null) {
            Log.d(TAG, intent.getStringExtra("action"));
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
