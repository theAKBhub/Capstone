package com.example.android.capstone.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;
import com.example.android.capstone.R;

public class TimePreference extends DialogPreference {

    private int mTime;
    private int mDialogLayoutResId = R.layout.pref_dialog_time;

    /**
     * Timepreference Constructors
     */
    public TimePreference(Context context) {
        super(context);
    }

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

    /**
     * Getter and Setter methods
     */
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
     *
     * @return object
     */
    @Override
    protected Object onGetDefaultValue(TypedArray array, int index) {
        // Default value from attribute.
        return array.getInt(index, 0);
    }

    /**
     * Reads stored preference value
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setTime(restorePersistedValue ? getPersistedInt(mTime) : (int) defaultValue);
    }

    /**
     * Set the layout resource for the dialog
     *
     * @return dialog resource Id
     */
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }
}

