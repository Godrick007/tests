package com.godrick.ffmpeglib.surface;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EGLRender implements GLSurfaceView.Renderer {


    private final float[] vertex_data = {
        -1f,-1f,
        1f,-1f,
        -1f,1f,
        1f,1f
    };

    private final float[] texture_data = {
        1f,0f,
        1f,1f,
        0f,0f,
        0f,1f
    };




    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f,0f,0f,1f);
    }
}
