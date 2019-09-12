package com.ppk.mycamera;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
