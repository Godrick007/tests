package com.godrick.ffmpeglib.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CGLSurfaceView extends GLSurfaceView {

    private CRender render;


    public CGLSurfaceView(Context context) {
        this(context,null);
    }

    public CGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        setEGLContextClientVersion(2);
        render = new CRender(context);
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    public void setYuvData(int width,int height,byte[] y,byte[] u,byte[] v){

        if(render != null){
            render.setYUVRenderData(width,height,y,u,v);
            requestRender();
        }

    }

}
