package com.example.android.capstone.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import java.util.Calendar;

/**
 * Fragment to show the TimePicker dialog
 */
public class TimePickerFragment extends DialogFragment {

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(mContext, (TimePickerDialog.OnTimeSetListener) mContext,
                hour,
                min,
                DateFormat.is24HourFormat(mContext));
    }
}
