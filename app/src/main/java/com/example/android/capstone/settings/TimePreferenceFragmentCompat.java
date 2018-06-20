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
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.android.capstone.R;
import com.example.android.capstone.alarm.AlarmReceiver;
import java.util.Calendar;

public class TimePreferenceFragmentCompat extends PreferenceDialogFragmentCompat {

    private TimePicker mTimePicker;
    private int time1;
    private long timeLong;


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
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

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
            throw new IllegalStateException("Dialog view must contain a TimePicker with id 'edit'");
        }

        // Get the time from the related Preference
        Integer minutesAfterMidnight = null;
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            minutesAfterMidnight = ((TimePreference) preference).getTime();
        }

        // Set the time to the TimePicker
        /*if (minutesAfterMidnight != null) {
            int hours = minutesAfterMidnight / 60;
            int minutes = minutesAfterMidnight % 60;
            boolean is24hour = DateFormat.is24HourFormat(getContext());

            mTimePicker.setIs24HourView(is24hour);
            mTimePicker.setCurrentHour(hours);
            mTimePicker.setCurrentMinute(minutes);
        }*/
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


            // Generate value to save
            int minutesAfterMidnight = (hours * 60) + minutes;
            Log.d("XXX", "minutesAfterMidnight = " + minutesAfterMidnight);

            // Save the value
            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = ((TimePreference) preference);
                // This allows the client to ignore the user value.
                if (timePreference.callChangeListener(minutesAfterMidnight)) {
                    // Save the value

                    //timePreference.setTime(minutesAfterMidnight);
                    timePreference.setTime(time1);
                    setAlarm(timeLong);
                }
            }
        }
    }

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

        Log.d("XXX", "Time in millsec = " + (int) calendar.getTimeInMillis());

        timeLong = calendar.getTimeInMillis();
        time1 = (int) calendar.getTimeInMillis();

        return timeLong;

    }

    public void setAlarm(long time) {

        // Get preference values
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean switchValue = sharedPrefs.getBoolean("pref_switch", false);

        if (switchValue) {

            Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, alarmIntent, 0);
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            //alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
            Toast.makeText(getContext(), "Alarm Set for " + time1 + " seconds", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "You need to switch on alarm switch", Toast.LENGTH_SHORT).show();
        }
    }
}
