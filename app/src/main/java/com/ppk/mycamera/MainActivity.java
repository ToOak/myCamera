package com.ppk.mycamera;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoSurface(View view) {
        startActivity(SurfaceCameraActivity.getAction(this));
    }

    public void gotoTexture(View view) {
        startActivity(TextureViewCameraActivity.getAction(this));
    }
}
