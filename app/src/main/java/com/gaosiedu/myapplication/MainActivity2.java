package com.gaosiedu.myapplication;

import android.os.Bundle;

import com.gaosiedu.gl.CGLSurfaceView;
import com.godrick.ffmpeglib.opengl.CGLSurfaceView2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {

    CGLSurfaceView2 cglSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        cglSurfaceView = findViewById(R.id.gl_surface_view);
    }



}
