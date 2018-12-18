package com.gaosiedu.myapplication;

import android.Manifest;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.godrick.pusher.camera.CameraView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Activity4 extends AppCompatActivity {

    CameraView cameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        cameraView = findViewById(R.id.cv);

        requestPermissions(new String[]{Manifest.permission.CAMERA},1);

    }

    public void switchCamera(View view) {


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cameraView.previewAngle(this);
    }
}
