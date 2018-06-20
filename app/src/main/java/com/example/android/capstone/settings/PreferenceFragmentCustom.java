package com.example.android.capstone.settings;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.example.android.capstone.R;
import com.example.android.capstone.alarm.AlarmNotificationService;
import com.example.android.capstone.alarm.AlarmReceiver;
import com.example.android.capstone.alarm.AlarmSoundService;

public class PreferenceFragmentCustom extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences preferences;
    SharedPreferences.Editor editor;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        onSharedPreferenceChanged(preferences, "pref_switch");

        Preference switchPref = findPreference("pref_switch");
        switchPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d("XXX", "New Switch Pref = " + newValue);
                preferences.edit().putBoolean("pref_switch", (Boolean) newValue).apply();
                String pref = String.valueOf(preferences.getBoolean("pref_switch", false));
                preference.setSummary(pref);

                if (!(Boolean) newValue) {  // cancel alarm if user turns off switch
                    stopAlarm();
                }

                return true;
            }
        });
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof TimePreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            Log.d("XXX", "Time Preference");
            dialogFragment = TimePreferenceFragmentCompat.newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            // The dialog was created (it was one of our custom Preferences), show the dialog for it
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference" +
                    ".PreferenceFragment.DIALOG");
        } else {
            // Dialog creation could not be handled here. Try with the super method.
            super.onDisplayPreferenceDialog(preference);
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("XXX", "onSharedPreferenceChanged / " + key);
    }

    @Override
    public void onResume() {
        super.onResume();
        //register the preferenceChange listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void stopAlarm() {
        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }

        // Stop the Media Player Service to stop sound
        getContext().stopService(new Intent(getContext(), AlarmSoundService.class));

        // remove the notification from notification tray
        NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(AlarmNotificationService.NOTIFICATION_ID);

        Toast.makeText(getContext(), "Alarm cancelled by User.", Toast.LENGTH_SHORT).show();
    }
}
