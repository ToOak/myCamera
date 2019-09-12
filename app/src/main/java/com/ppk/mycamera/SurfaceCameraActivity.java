package com.ppk.mycamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.ppk.mycamera.utils.CameraManage;
import com.ppk.mycamera.utils.CameraThreadPool;

import java.io.IOException;
import java.util.List;

public class SurfaceCameraActivity extends Activity implements SurfaceHolder.Callback {

    public static Intent getAction(Context context) {
        return new Intent(context, SurfaceCameraActivity.class);
    }

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private CameraManage cameraManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_surface_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        surfaceView = findViewById(R.id.surface_view);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        cameraManage = new CameraManage(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("oak", "surfaceCreated");
        if (cameraManage.getCamera() == null) {
            cameraManage.initCamera(holder);
        } else {
            cameraManage.resumeCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("oak", "surfaceChanged");
//        cameraManage.refreshCamera(this.holder);
        cameraManage.setCameraOrientationAndSize(surfaceView, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("oak", "surfaceDestroyed");
//        holder.removeCallback(this);
//        cameraManage.releaseCamera();
        cameraManage.stopCamera();
    }

    @Override
    protected void onDestroy() {
        cameraManage.releaseCamera();
        super.onDestroy();
    }
}
