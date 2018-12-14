package com.gaosiedu.myapplication;

import android.os.Bundle;

import com.godrick.ffmpeglib.opengl.CGLSurfaceView2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity {

    MyEGLSurfaceView surfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        surfaceView = findViewById(R.id.surface_view);
    }



}
