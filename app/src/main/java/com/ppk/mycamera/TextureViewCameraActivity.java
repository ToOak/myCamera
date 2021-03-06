package com.ppk.mycamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;

import com.ppk.mycamera.camera.CameraManage;
import com.ppk.mycamera.utils.LogUtil;

public class TextureViewCameraActivity extends Activity implements TextureView.SurfaceTextureListener {

    public static Intent getAction(Context context) {
        return new Intent(context, TextureViewCameraActivity.class);
    }

    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private CameraManage cameraManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_texture_view_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textureView = findViewById(R.id.texture);
        textureView.setSurfaceTextureListener(this);
        cameraManage = new CameraManage(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LogUtil.e("onSurfaceTextureAvailable");
        surfaceTexture = surface;
        cameraManage.initCamera(surfaceTexture);
        cameraManage.setCameraOrientationAndSize(textureView, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        LogUtil.e("onSurfaceTextureSizeChanged");
        cameraManage.setCameraOrientationAndSize(textureView, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LogUtil.e("onSurfaceTextureDestroyed");
        surface.release();
        cameraManage.releaseCamera();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        LogUtil.e("onSurfaceTextureUpdated");
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraManage.resumeCamera(surfaceTexture);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cameraManage.stopCamera();
    }

}
