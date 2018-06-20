package com.example.android.capstone.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import com.example.android.capstone.R;

public class TimePreference extends DialogPreference {

    private int mTime;
    private int mDialogLayoutResId = R.layout.pref_dialog_time;
    //private long mTimeinMills;


    public TimePreference(Context context) {
        super(context);
    }

    /**
     * When you replace the 0 in the second constructor with R.attr.dialogPreferenceStyle (For a DialogPreference)
     * or R.attr.preferenceStyle (For any other preference) you won’t face any design issues later.
     */
    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int getTime() {
        return mTime;
    }

    public void setTime(int time) {
        mTime = time;

        // Save to Shared Preferences
        persistInt(time);
    }

    /**
     * Read default Pref value
     * @param a
     * @param index
     * @return
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getInt(index, 0);
    }

    /**
     * Reads stored preference value
     * @param restorePersistedValue
     * @param defaultValue
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        setTime(restorePersistedValue ? getPersistedInt(mTime) : (int) defaultValue);
    }

    /**
     * Set the layout resource for the dialog
     */
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }
}

