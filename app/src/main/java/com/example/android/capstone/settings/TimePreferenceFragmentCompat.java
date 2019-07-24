package com.example.android.capstone.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.android.capstone.R;
import com.example.android.capstone.alarm.AlarmReceiver;
import com.example.android.capstone.helper.Utils;
import java.util.Calendar;
import java.util.Locale;

public class TimePreferenceFragmentCompat extends PreferenceDialogFragmentCompat {

    private TimePicker mTimePicker;
    private int mTimeInt;
    private long mTimeLong;
    private Toast mToast;

    /**
     * This static method creates a new instance of our TimePreferenceFragmentCompat.
     * To know to which preference this new dialog belongs, we add a String parameter
     * with the key of the preference to our method and pass it (inside a Bundle) to the dialog.
     * @param key
     * @return
     */
    public static TimePreferenceFragmentCompat newInstance(String key) {
        final TimePreferenceFragmentCompat
                fragment = new TimePreferenceFragmentCompat();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Every time we open the dialog, it now displays the time which is stored in the SharedPreferences
     * @param view
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mTimePicker = (TimePicker) view.findViewById(R.id.edit);

        // Exception: There is no TimePicker with the id 'edit' in the dialog.
        if (mTimePicker == null) {
            throw new IllegalStateException(getString(R.string.error_timepicker_edit_not_found));
        }

        // Get the time from the related Preference
        Integer minutesAfterMidnight = null;
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            minutesAfterMidnight = ((TimePreference) preference).getTime();
        }
    }

    /**
     * Called when the Dialog is closed.
     * it should save the selected time when we click the OK button (positive result).
     * @param positiveResult Whether the Dialog was accepted or canceled.
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Get the current values from the TimePicker
            int hours;
            int minutes;
            if (Build.VERSION.SDK_INT >= 23) {
                hours = mTimePicker.getHour();
                minutes = mTimePicker.getMinute();
            } else {
                hours = mTimePicker.getCurrentHour();
                minutes = mTimePicker.getCurrentMinute();
            }

            getTimeinMillsec();

            // Generate value to set alarm
            int minutesAfterMidnight = (hours * 60) + minutes;

            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = ((TimePreference) preference);

                if (timePreference.callChangeListener(minutesAfterMidnight)) {
                    timePreference.setTime(mTimeInt);
                    setAlarm(mTimeLong);
                }
            }
        }
    }

    /**
     * Method to get Time in milliseconds for setting the alarm
     */
    public long getTimeinMillsec() {
        Calendar calendar = Calendar.getInstance();
        if (Build.VERSION.SDK_INT >= 23) {
            calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    mTimePicker.getHour(),
                    mTimePicker.getMinute(), 0);
        } else {
            calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    mTimePicker.getCurrentHour(),
                    mTimePicker.getCurrentMinute(), 0);
        }

        mTimeLong = calendar.getTimeInMillis();
        mTimeInt = (int) calendar.getTimeInMillis();

        return mTimeLong;
    }

    /**
     * Method to set the alarm
     * @param time in milliseconds
     */
    public void setAlarm(long time) {

        // Get preference values
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean switchValue = sharedPrefs.getBoolean(getString(R.string.settings_switch_key), false);

        if (switchValue) {

            Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pendingIntent);

            Utils.showToastMessage(getContext(),
                    mToast,
                    String.format(Locale.ENGLISH, getString(R.string.msg_alarm_set), Utils.getDisplayTime(mTimeLong)))
                    .show();
        } else {
            Utils.showToastMessage(getContext(),
                    mToast, getString(R.string.msg_notification_off)).show();
        }
    }
}
