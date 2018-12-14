package com.gaosiedu.myapplication;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.godrick.ffmpeglib.surface.EGLSurfaceView;

public class MyRender implements EGLSurfaceView.EGLRender {

    private Context context;

    public MyRender(Context context){
        this.context = context;
    }


    @Override
    public void onSurfaceCreated() {
        Log.e("render","onSurfaceCreated");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.e("render","onSurfaceChanged");
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame() {
        Log.e("render","onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f,1f,0f,1f);
    }
}
