package com.example.android.capstone.barcode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.capstone.R;
import com.example.android.capstone.camera.CameraSource;
import com.example.android.capstone.camera.CameraSourcePreview;
import com.example.android.capstone.helper.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import java.io.IOException;
import timber.log.Timber;

public class BarcodeCaptureActivity extends AppCompatActivity implements BarcodeTracker.BarcodeTrackerCallback {

    private CameraSource mCameraSource;

    @BindView(R.id.preview)
    CameraSourcePreview mPreview;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.barcode_capture);
        ButterKnife.bind(this);

        // Check for camera permission before accessing the camera, and request permission if not granted
        int requestCode = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * Method to request camera permission.
     */
    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            Timber.w(getString(R.string.warning_error_getting_permission));
            ActivityCompat.requestPermissions(this, permissions, Constants.CAMERA_REQUEST_CODE);
        }
    }

    /**
     * Method to starts the camera
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = getApplicationContext();

        // Create a barcode detector and barcode factory instance
        // Barcode format is specified to detect ALL_FORMATS
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(this);
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        // Check if the barcode native libraries are already there.
        // If not GMS will download the libraries to enable barcode detection
        if (!barcodeDetector.isOperational()) {
            Timber.w(getString(R.string.warning_camera_detector_dependencies));

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean isStorageLow = registerReceiver(null, lowstorageFilter) != null;

            // Check if enough storage is available to download the required libraries
            if (isStorageLow) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Timber.w(getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraInfo.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(metrics.widthPixels, metrics.heightPixels)
                .setRequestedFps(24.0f);

        // use auto focus, and no flash options
        builder = builder.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        mCameraSource = builder
                .setFlashMode(null) // use Camera.Parameters.FLASH_MODE_TORCH if flash needed
                .build();
    }

    @Override
    public void onDetectedQrCode(Barcode barcode) {
        if (barcode != null) {
            Intent intent = new Intent();
            intent.putExtra(Constants.INTENT_KEY_BARCODE, barcode);
            setResult(CommonStatusCodes.SUCCESS, intent);
            finish();
        }
    }

    // Restarts the camera when activity resumes
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    // Stops the camera when activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with camera source
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Bail out early if the permission requested is not for camera
        if (requestCode != Constants.CAMERA_REQUEST_CODE) {
            Timber.d(getString(R.string.info_camera_unexpected_permission));
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // right permission requested, so create the camerasource
            createCameraSource();
            return;
        }

        // If however right permission is not granted then display dialog to request permission
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_request_permission)
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.action_yes, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dialog =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, Constants.GMS_REQUEST_CODE);
            dialog.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Timber.e(getString(R.string.error_camera_failed_start));
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }
}