package com.example.android.capstone.barcode;

import android.content.Context;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Factory for creating a tracker and associated graphic to be associated with a new barcode
 */
class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private Context mContext;

    /**
     * Default constructor
     * @param context
     */
    BarcodeTrackerFactory(Context context) {
        mContext = context;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        return new BarcodeTracker(mContext);
    }
}