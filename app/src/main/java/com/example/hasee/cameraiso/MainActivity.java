package com.example.hasee.cameraiso;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    public static final boolean NEED_RECORD = true;
    public static final int DEFAULT_CAPTURE_W = 640;
    public static final int DEFAULT_CAPTURE_H = 480;

    public String mDateString;
    public String mStorageDir;

    // Camera
    private Camera mCamera;
    private Boolean mIsCapturing = false;
    private CamCallbacks.ShutterCallback mShutter;
    private CamCallbacks.PictureCallback mPicture;
    private CamPreview mPreview;

    public static int isoType;
    public static List<String> isoModesList;


    // Debug
    private final String TAG = "TAG/CameraISO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();
        setContentView(R.layout.activity_main);


        if (!isExternalStorageWritable() && NEED_RECORD) {
            Toast toast = Toast.makeText(context, R.string.failed_to_access_external_storage, Toast.LENGTH_SHORT);
            toast.show();
            finish();
            return;
        }

        if (!checkCamera()) {
            Toast toast = Toast.makeText(context, R.string.failed_to_access_camere, Toast.LENGTH_SHORT);
            toast.show();
            finish();
            return;
        }

        getCameraInstance();
        if (mCamera == null) {
            Toast toast = Toast.makeText(context, R.string.failed_to_access_camere, Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        if (NEED_RECORD) {
            mDateString = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)).format(Calendar.getInstance().getTime());
            mStorageDir = getResources().getString(R.string.app_name) + File.separator + mDateString;
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    mStorageDir + File.separator + "IMG");
            if (!file.mkdirs()) {
                Toast toast = Toast.makeText(context, R.string.failed_to_access_external_storage, Toast.LENGTH_SHORT);
                toast.show();
                finish();
                return;
            }
        }

        mPreview = new CamPreview(context, mCamera);
        ((FrameLayout) findViewById(R.id.cam_layout)).addView(mPreview);

        mShutter = new CamCallbacks.ShutterCallback();
        mPicture = new CamCallbacks.PictureCallback(this);

        isoType = 2;
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        getCameraInstance();
        setCamFeatures();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        releaseCamera();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    public Boolean isCapturing() {
        synchronized (mIsCapturing) {
            return mIsCapturing;
        }
    }

    public CamCallbacks.ShutterCallback getShutter() {
        return mShutter;
    }

    public CamCallbacks.PictureCallback getPicture() {
        return mPicture;
    }

    public void onCaptureBtnClick(View view) {
        if (!mIsCapturing) {
            synchronized (mIsCapturing) {
                mIsCapturing = true;
            }

            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.start_capturing_msg, Toast.LENGTH_SHORT);
            toast.show();
            mCamera.takePicture(mShutter, null, mPicture);
        } else {
            synchronized (mIsCapturing) {
                mIsCapturing = false;
            }

            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, R.string.stop_capturing_msg, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean checkCamera() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return true;
        else
            return false;
    }

    private void getCameraInstance() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                Log.e(TAG, "getCameraInstance: " + e.getMessage());
            }
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void setCamFeatures() {
        Camera.Parameters params = mCamera.getParameters();

        List<String> sceneModes = params.getSupportedSceneModes();
        if (sceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO))
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        List<String> flashModes = params.getSupportedFlashModes();
        if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF))
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//        List<String> whiteBalanceModes = params.getSupportedWhiteBalance();
//        if (whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_DAYLIGHT))
//            params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);

        params.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
        params.set("camera-mode", 1);
        params.set("qc-camera-features", 1);
        params.setAutoExposureLock(true);
        params.setAutoWhiteBalanceLock(true);
        String supportedISOValues = params.get("iso-values");
        Log.i(TAG, "setCamFeatures: 该手机支持ISO模式:" + supportedISOValues);

        String[] supportedISOArray = supportedISOValues.split(",");
        isoModesList = Arrays.asList(supportedISOArray);
        String isoMode = supportedISOArray[isoType];
        params.set("iso", isoMode);

        params.setPreviewSize(DEFAULT_CAPTURE_W, DEFAULT_CAPTURE_H);
        params.setPictureSize(DEFAULT_CAPTURE_W, DEFAULT_CAPTURE_H);

        mCamera.setParameters(params);
        if (!mCamera.getParameters().get("iso").equals(isoMode)) {
            Toast toast = Toast.makeText(MainActivity.this, "设置iso：" + isoMode + "失败", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(MainActivity.this, "设置iso：" + isoMode + "成功", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}

