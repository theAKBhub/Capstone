package com.example.android.capstone.settings;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;
import com.example.android.capstone.R;
import com.example.android.capstone.alarm.AlarmReceiver;
import com.example.android.capstone.alarm.AlarmSoundService;
import com.example.android.capstone.helper.Constants;

public class PreferenceFragmentCustom extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

    private static final String DIALOG_PREFERENCE = "android.support.v7.preference.PreferenceFragment.DIALOG";
    private static final String SWITCH_TRUE_PREF = "true";

    public static SharedPreferences mPreferences;
    private Toast mToast;
    private Context mContext;


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.preferences);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Handle ListPreference
        Preference orderByPref = findPreference(getString(R.string.settings_order_by_key));
        bindPreferenceSummaryToValue(orderByPref);

        // Handle Switch Preference
        onSharedPreferenceChanged(mPreferences, getString(R.string.settings_switch_key));
        Preference switchPref = findPreference(getString(R.string.settings_switch_key));
        switchPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mPreferences.edit().putBoolean(getString(R.string.settings_switch_key), (Boolean) newValue).apply();
                String pref = String.valueOf(mPreferences.getBoolean(getString(R.string.settings_switch_key), false));

                if (SWITCH_TRUE_PREF.equals(pref)) {
                    preference.setSummary(getString(R.string.settings_alarm_on));
                } else {
                    preference.setSummary(getString(R.string.settings_alarm_off));
                }

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
            // Create a new instance of TimePreferenceDialogFragment with the key of the related Preference
            dialogFragment = TimePreferenceFragmentCompat.newInstance(preference.getKey());
        }

        if (dialogFragment != null) {
            // show the dialog
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), DIALOG_PREFERENCE);

        } else {
            // Dialog creation could not be handled here. Try with the super method.
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    public void onResume() {
        super.onResume();
        //register the preferenceChange listener
        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister the preferenceChange listener
        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                CharSequence[] labels = listPreference.getEntries();
                preference.setSummary(labels[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }

    /**
     * Method to bind preference value to summary
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        String preferenceString = mPreferences.getString(preference.getKey(), "");
        onPreferenceChange(preference, preferenceString);
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
        NotificationManager notificationManager = (NotificationManager)
                getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Constants.ALARM_NOTIFICATION_ID);
        Toast.makeText(getContext(), getString(R.string.msg_alarm_cancelled), Toast.LENGTH_SHORT).show();
    }
}
