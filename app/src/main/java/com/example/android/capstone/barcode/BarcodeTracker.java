package com.example.android.capstone.barcode;

import android.content.Context;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

class BarcodeTracker extends Tracker<Barcode> {

    private BarcodeTrackerCallback mDetectionListener;

    /**
     * Interface to track barcode detection
     */
    public interface BarcodeTrackerCallback {
        void onDetectedQrCode(Barcode barcode);
    }

    /**
     * Default Constructor
     * @param detectionListener
     */
    BarcodeTracker(Context detectionListener) {
        mDetectionListener = (BarcodeTrackerCallback) detectionListener;
    }

    /**
     * Method invoked when new item detected
     * @param id
     * @param item
     */
    @Override
    public void onNewItem(int id, Barcode item) {
        if (item.displayValue != null) {
            mDetectionListener.onDetectedQrCode(item);
        }
    }
}
