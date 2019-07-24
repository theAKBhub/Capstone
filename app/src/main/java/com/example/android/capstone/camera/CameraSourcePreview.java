package com.example.android.capstone.camera;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.RequiresPermission;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import com.example.android.capstone.R;
import com.google.android.gms.common.images.Size;
import java.io.IOException;
import timber.log.Timber;

public class CameraSourcePreview extends ViewGroup {

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource) throws IOException, SecurityException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startIfReady() throws IOException, SecurityException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            mStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (SecurityException se) {
                Timber.e(mContext.getString(R.string.error_camera_no_permission));
            } catch (IOException e) {
                Timber.e(mContext.getString(R.string.error_camera_failed_start));
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int previewWidth = 320;
        int previewHeight = 240;
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                previewWidth = size.getWidth();
                previewHeight = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int temp = previewWidth;
            previewWidth = previewHeight;
            previewHeight = temp;
        }

        final int viewWidth = right - left;
        final int viewHeight = bottom - top;

        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;
        float widthRatio = (float) viewWidth / (float) previewWidth;
        float heightRatio = (float) viewHeight / (float) previewHeight;

        // fill the view with the camera preview, preserving the correct aspect ratio
        if (widthRatio > heightRatio) {
            childWidth = viewWidth;
            childHeight = (int) ((float) previewHeight * widthRatio);
            childYOffset = (childHeight - viewHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * heightRatio);
            childHeight = viewHeight;
            childXOffset = (childWidth - viewWidth) / 2;
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(
                    -1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Timber.e(mContext.getString(R.string.error_camera_failed_start));
        } catch (SecurityException se) {
            Timber.e(mContext.getString(R.string.error_camera_not_found));
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Timber.d(mContext.getString(R.string.info_camera_portrait_default));
        return false;
    }
}
