package com.example.android.capstone.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringDef;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.example.android.capstone.R;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import java.io.IOException;
import java.lang.Thread.State;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import timber.log.Timber;

@SuppressWarnings("deprecation")
public class CameraSource {

    private static final int MAX_WIDTH_HEIGHT = 1000000;
    private static final int DUMMY_TEXTURE_NAME = 100;
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;

    private static Context mContext;
    private Camera mCamera;
    private int mFacing = CameraInfo.CAMERA_FACING_BACK;
    private int mRotation;
    private Size mPreviewSize;
    private String mFocusMode = null;
    private String mFlashMode = null;
    private SurfaceTexture mDummySurfaceTexture;
    private Thread mProcessingThread;
    private FrameProcessingRunnable mFrameProcessor;
    private float mRequestedFps = 30.0f;
    private int mRequestedPreviewWidth = 1024;
    private int mRequestedPreviewHeight = 768;


    @StringDef({
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
            Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
            Camera.Parameters.FOCUS_MODE_AUTO,
            Camera.Parameters.FOCUS_MODE_EDOF,
            Camera.Parameters.FOCUS_MODE_FIXED,
            Camera.Parameters.FOCUS_MODE_INFINITY,
            Camera.Parameters.FOCUS_MODE_MACRO
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface FocusMode {

    }

    @StringDef({
            Camera.Parameters.FLASH_MODE_ON,
            Camera.Parameters.FLASH_MODE_OFF,
            Camera.Parameters.FLASH_MODE_AUTO,
            Camera.Parameters.FLASH_MODE_RED_EYE,
            Camera.Parameters.FLASH_MODE_TORCH
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface FlashMode {

    }

    private final Object mCameraLock = new Object();
    private Map<byte[], ByteBuffer> mBytesToByteBuffer = new HashMap<>();

    //==============================================================================================
    // Builder
    //==============================================================================================

    /**
     * Builder for configuring and creating an associated camera source.
     */
    public static class Builder {

        private final Detector<?> mDetector;
        private CameraSource mCameraSource = new CameraSource();

        /**
         * Creates a camera source builder with the supplied context and detector.  Camera preview
         * images will be streamed to the associated detector upon starting the camera source.
         */
        public Builder(Context context, Detector<?> detector) {
            if (context == null) {
                throw new IllegalArgumentException(mContext.getString(R.string.error_camera_context));
            }
            if (detector == null) {
                throw new IllegalArgumentException(mContext.getString(R.string.error_camera_detector));
            }

            mDetector = detector;
            mCameraSource.mContext = context;
        }

        /**
         * Sets the requested frame rate in frames per second. Default - 30
         */
        public Builder setRequestedFps(float fps) {
            if (fps <= 0) {
                throw new IllegalArgumentException(String.format(mContext.getString(R.string.error_camera_fps), fps));
            }
            mCameraSource.mRequestedFps = fps;
            return this;
        }

        public Builder setFocusMode(@FocusMode String mode) {
            mCameraSource.mFocusMode = mode;
            return this;
        }

        public Builder setFlashMode(@FlashMode String mode) {
            mCameraSource.mFlashMode = mode;
            return this;
        }

        /**
         * Sets the desired width and height of the camera frames in pixels. Default - 1024x768
         */
        public Builder setRequestedPreviewSize(int width, int height) {
            if ((width <= 0) || (width > MAX_WIDTH_HEIGHT) || (height <= 0) || (height > MAX_WIDTH_HEIGHT)) {
                throw new IllegalArgumentException(
                        String.format(mContext.getString(R.string.error_camera_invalid_dimen), width, height));
            }
            mCameraSource.mRequestedPreviewWidth = width;
            mCameraSource.mRequestedPreviewHeight = height;
            return this;
        }

        /**
         * Sets the camera to use. Default: back facing.
         */
        public Builder setFacing(int facing) {
            if ((facing != CameraInfo.CAMERA_FACING_BACK) && (facing != CameraInfo.CAMERA_FACING_FRONT)) {
                throw new IllegalArgumentException(
                        String.format(mContext.getString(R.string.error_camera_invalid_facing), facing));
            }
            mCameraSource.mFacing = facing;
            return this;
        }

        /**
         * Creates an instance of the camera source.
         */
        public CameraSource build() {
            mCameraSource.mFrameProcessor = mCameraSource.new FrameProcessingRunnable(mDetector);
            return mCameraSource;
        }
    }


    //==============================================================================================
    // Public methods
    //==============================================================================================

    /**
     * Stops the camera and releases the resources of the camera and underlying detector.
     */
    public void release() {
        synchronized (mCameraLock) {
            stop();
            mFrameProcessor.release();
        }
    }

    /**
     * Opens the camera and starts sending preview frames to the underlying detector.  The preview
     * frames are not displayed.
     *
     * @throws IOException if the camera's preview texture or display could not be initialized
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    public CameraSource start() throws IOException {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                return this;
            }

            mCamera = createCamera();

            mDummySurfaceTexture = new SurfaceTexture(DUMMY_TEXTURE_NAME);
            mCamera.setPreviewTexture(mDummySurfaceTexture);
            mCamera.startPreview();

            mProcessingThread = new Thread(mFrameProcessor);
            mFrameProcessor.setActive(true);
            mProcessingThread.start();
        }
        return this;
    }

    /**
     * Opens the camera and starts sending preview frames to the underlying detector.  The supplied
     * surface holder is used for the preview so frames can be displayed to the user.
     *
     * @param surfaceHolder the surface holder to use for the preview frames
     * @throws IOException if the supplied surface holder could not be used as the preview display
     */
    @RequiresPermission(Manifest.permission.CAMERA)
    public CameraSource start(SurfaceHolder surfaceHolder) throws IOException {
        synchronized (mCameraLock) {
            if (mCamera != null) {
                return this;
            }

            mCamera = createCamera();
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();

            mProcessingThread = new Thread(mFrameProcessor);
            mFrameProcessor.setActive(true);
            mProcessingThread.start();
        }
        return this;
    }

    /**
     * Closes the camera and stops sending frames to the underlying frame detector.
     *
     * This camera source may be restarted again by calling {@link #start()} or
     * {@link #start(SurfaceHolder)}.
     *
     * Call {@link #release()} instead to completely shut down this camera source and release the
     * resources of the underlying detector.
     */
    public void stop() {
        synchronized (mCameraLock) {
            mFrameProcessor.setActive(false);
            if (mProcessingThread != null) {
                try {
                    mProcessingThread.join();
                } catch (InterruptedException e) {
                    Timber.e(mContext.getString(R.string.error_camera_thread_paused));
                }
                mProcessingThread = null;
            }

            // clear the buffer to prevent oom exceptions
            mBytesToByteBuffer.clear();

            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);
                try {
                    mCamera.setPreviewTexture(null);
                } catch (Exception e) {
                    Timber.e(e.getMessage());
                }
                mCamera.release();
                mCamera = null;
            }
        }
    }

    /**
     * Returns the preview size that is currently in use by the underlying camera.
     */
    public Size getPreviewSize() {
        return mPreviewSize;
    }

    //==============================================================================================
    // Private Methods
    //==============================================================================================

    /**
     * Empty Constructor used only by builder class
     */
    private CameraSource() {
    }

    /**
     * Opens the camera and applies the user settings.
     *
     * @throws RuntimeException if the method fails
     */
    @SuppressLint("InlinedApi")
    private Camera createCamera() {
        int requestedCameraId = getIdForRequestedCamera(mFacing);
        if (requestedCameraId == -1) {
            throw new RuntimeException(mContext.getString(R.string.error_camera_not_found));
        }
        Camera camera = Camera.open(requestedCameraId);

        SizePair sizePair = selectSizePair(camera, mRequestedPreviewWidth, mRequestedPreviewHeight);
        if (sizePair == null) {
            throw new RuntimeException(mContext.getString(R.string.error_camera_no_preview));
        }
        Size pictureSize = sizePair.pictureSize();
        mPreviewSize = sizePair.previewSize();

        int[] previewFpsRange = selectPreviewFpsRange(camera, mRequestedFps);
        if (previewFpsRange == null) {
            throw new RuntimeException(mContext.getString(R.string.error_camera_no_fps));
        }

        Camera.Parameters parameters = camera.getParameters();

        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        }

        parameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        parameters.setPreviewFpsRange(
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        parameters.setPreviewFormat(ImageFormat.NV21);

        setRotation(camera, parameters, requestedCameraId);

        if (mFocusMode != null) {
            if (parameters.getSupportedFocusModes().contains(
                    mFocusMode)) {
                parameters.setFocusMode(mFocusMode);
            } else {
                Timber.i(mContext.getString(R.string.info_camera_focusmode));
            }
        }

        // setting mFocusMode to the one set in the params
        mFocusMode = parameters.getFocusMode();

        if (mFlashMode != null) {
            if (parameters.getSupportedFlashModes().contains(
                    mFlashMode)) {
                parameters.setFlashMode(mFlashMode);
            } else {
                Timber.i(mContext.getString(R.string.info_camera_flashmode));
            }
        }

        // setting mFlashMode to the one set in the params
        mFlashMode = parameters.getFlashMode();

        camera.setParameters(parameters);
        camera.setPreviewCallbackWithBuffer(new CameraPreviewCallback());
        camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));
        camera.addCallbackBuffer(createPreviewBuffer(mPreviewSize));

        return camera;
    }

    /**
     * Gets the id for the camera specified by the direction it is facing.  Returns -1 if no such
     * camera was found.
     *
     * @param facing the desired camera (front-facing or rear-facing)
     */
    private static int getIdForRequestedCamera(int facing) {
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Selects the most suitable preview and picture size, given the desired width and height.
     *
     * @param camera the camera to select a preview size from
     * @param desiredWidth the desired width of the camera preview frames
     * @param desiredHeight the desired height of the camera preview frames
     * @return the selected preview and picture size pair
     */
    private static SizePair selectSizePair(Camera camera, int desiredWidth, int desiredHeight) {
        List<SizePair> validPreviewSizes = generateValidPreviewSizeList(camera);

        // The method for selecting the best size is to minimize the sum of the differences between
        // the desired values and the actual values for width and height.  This is certainly not the
        // only way to select the best size, but it provides a decent tradeoff between using the
        // closest aspect ratio vs. using the closest pixel area.
        SizePair selectedPair = null;
        int minDiff = Integer.MAX_VALUE;
        for (SizePair sizePair : validPreviewSizes) {
            Size size = sizePair.previewSize();
            int diff = Math.abs(size.getWidth() - desiredWidth) +
                    Math.abs(size.getHeight() - desiredHeight);
            if (diff < minDiff) {
                selectedPair = sizePair;
                minDiff = diff;
            }
        }

        return selectedPair;
    }

    /**
     * Stores a preview size and a corresponding same-aspect-ratio picture size
     */
    private static class SizePair {

        private Size mPreview;
        private Size mPicture;

        public SizePair(Camera.Size previewSize,
                Camera.Size pictureSize) {
            mPreview = new Size(previewSize.width, previewSize.height);
            if (pictureSize != null) {
                mPicture = new Size(pictureSize.width, pictureSize.height);
            }
        }

        public Size previewSize() {
            return mPreview;
        }

        @SuppressWarnings("unused")
        public Size pictureSize() {
            return mPicture;
        }
    }

    /**
     * Generates a list of acceptable preview sizes
     */
    private static List<SizePair> generateValidPreviewSizeList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes =
                parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes =
                parameters.getSupportedPictureSizes();
        List<SizePair> validPreviewSizes = new ArrayList<>();
        for (Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;

            // By looping through the picture sizes in order, pick the higher resolutions.
            for (Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new SizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        // If there are no picture sizes with the same aspect ratio as any preview sizes, allow all
        // of the preview sizes and hope that the camera can handle it.
        if (validPreviewSizes.size() == 0) {
            Timber.w(mContext.getString(R.string.warning_camera_no_preview));
            for (Camera.Size previewSize : supportedPreviewSizes) {
                validPreviewSizes.add(new SizePair(previewSize, null));
            }
        }

        return validPreviewSizes;
    }

    /**
     * Selects the most suitable preview FPS range, given the desired frames per second.
     *
     * @param camera the camera to select a frames per second range from
     * @param desiredPreviewFps the desired frames per second for the camera preview frames
     * @return the selected preview frames per second range
     */
    private int[] selectPreviewFpsRange(Camera camera, float desiredPreviewFps) {
        // The camera API uses integers scaled by a factor of 1000 instead
        int desiredPreviewFpsScaled = (int) (desiredPreviewFps * 1000.0f);

        int[] selectedFpsRange = null;
        int minDiff = Integer.MAX_VALUE;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();
        for (int[] range : previewFpsRangeList) {
            int deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int diff = Math.abs(deltaMin) + Math.abs(deltaMax);
            if (diff < minDiff) {
                selectedFpsRange = range;
                minDiff = diff;
            }
        }
        return selectedFpsRange;
    }

    /**
     * Calculates the correct rotation for the given camera id and sets the rotation in the
     * parameters.  It also sets the camera's display orientation and rotation.
     *
     * @param parameters the camera parameters for which to set the rotation
     * @param cameraId the camera id to set rotation based on
     */
    private void setRotation(Camera camera, Camera.Parameters parameters, int cameraId) {
        WindowManager windowManager =
                (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int degrees = 0;
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                Timber.e(mContext.getString(R.string.error_camera_rotation));
        }

        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int angle;
        int displayAngle;
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            angle = (cameraInfo.orientation + degrees) % 360;
            displayAngle = (360 - angle); // compensate for it being mirrored
        } else {  // back-facing
            angle = (cameraInfo.orientation - degrees + 360) % 360;
            displayAngle = angle;
        }

        mRotation = angle / 90;

        camera.setDisplayOrientation(displayAngle);
        parameters.setRotation(angle);
    }

    /**
     * Creates one buffer for the camera preview callback.  The size of the buffer is based off of
     * the camera preview size and the format of the camera image.
     *
     * @return a new preview buffer of the appropriate size for the current camera settings
     */
    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            throw new IllegalStateException(mContext.getString(R.string.error_camera_failed_buffer));
        }

        mBytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }

    //==============================================================================================
    // Frame processing
    //==============================================================================================

    /**
     * Called when the camera has a new preview frame.
     */
    private class CameraPreviewCallback implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            mFrameProcessor.setNextFrame(data, camera);
        }
    }

    /**
     * This runnable controls access to the underlying receiver, calling it to process frames when
     * available from the camera.  This is designed to run detection on frames as fast as possible
     * (i.e., without unnecessary context switching or waiting on the next frame).
     *
     * While detection is running on a frame, new frames may be received from the camera.  As these
     * frames come in, the most recent frame is held onto as pending.  As soon as detection and its
     * associated processing are done for the previous frame, detection on the mostly recently
     * received frame will immediately start on the same thread.
     */
    private class FrameProcessingRunnable implements Runnable {

        private Detector<?> mDetector;
        private long mStartTimeMillis = SystemClock.elapsedRealtime();

        // This lock guards all of the member variables below.
        private final Object mLock = new Object();
        private boolean mActive = true;

        // These pending variables hold the state associated with the new frame awaiting processing.
        private long mPendingTimeMillis;
        private int mPendingFrameId = 0;
        private ByteBuffer mPendingFrameData;

        FrameProcessingRunnable(Detector<?> detector) {
            mDetector = detector;
        }

        /**
         * Releases the underlying receiver.  This is only safe to do after the associated thread
         * has completed, which is managed in camera source's release method above.
         */
        @SuppressLint("Assert")
        void release() {
            assert (mProcessingThread.getState() == State.TERMINATED);
            mDetector.release();
            mDetector = null;
        }

        /**
         * Marks the runnable as active/not active.  Signals any blocked threads to continue.
         */
        void setActive(boolean active) {
            synchronized (mLock) {
                mActive = active;
                mLock.notifyAll();
            }
        }

        /**
         * Sets the frame data received from the camera. This adds the previous unused frame buffer
         * (if present) back to the camera, and keeps a pending reference to the frame data for
         * future use.
         */
        void setNextFrame(byte[] data, Camera camera) {
            synchronized (mLock) {
                if (mPendingFrameData != null) {
                    camera.addCallbackBuffer(mPendingFrameData.array());
                    mPendingFrameData = null;
                }

                if (!mBytesToByteBuffer.containsKey(data)) {
                    Timber.d(mContext.getString(R.string.info_camera_skipping_frame));
                    return;
                }

                // Timestamp and frame ID are maintained here, which will give downstream code some
                // idea of the timing of frames received and when frames were dropped along the way.
                mPendingTimeMillis = SystemClock.elapsedRealtime() - mStartTimeMillis;
                mPendingFrameId++;
                mPendingFrameData = mBytesToByteBuffer.get(data);

                // Notify the processor thread if it is waiting on the next frame (see below).
                mLock.notifyAll();
            }
        }

        /**
         * As long as the processing thread is active, this executes detection on frames
         * continuously.
         */
        @Override
        public void run() {
            Frame outputFrame;
            ByteBuffer data;

            while (true) {
                synchronized (mLock) {
                    while (mActive && (mPendingFrameData == null)) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            Timber.e(e.getMessage());
                            return;
                        }
                    }

                    if (!mActive) {
                        // Exit the loop once this camera source is stopped or released
                        return;
                    }

                    outputFrame = new Frame.Builder()
                            .setImageData(mPendingFrameData, mPreviewSize.getWidth(),
                                    mPreviewSize.getHeight(), ImageFormat.NV21)
                            .setId(mPendingFrameId)
                            .setTimestampMillis(mPendingTimeMillis)
                            .setRotation(mRotation)
                            .build();

                    data = mPendingFrameData;
                    mPendingFrameData = null;
                }

                try {
                    mDetector.receiveFrame(outputFrame);
                } catch (Throwable t) {
                    Timber.e(t.getMessage());
                } finally {
                    mCamera.addCallbackBuffer(data.array());
                }
            }
        }
    }
}
