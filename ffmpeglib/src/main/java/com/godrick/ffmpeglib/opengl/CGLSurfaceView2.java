package com.godrick.ffmpeglib.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.godrick.ffmpeglib.surface.EGLRender;

public class CGLSurfaceView2 extends GLSurfaceView {

    EGLRender render;

    public CGLSurfaceView2(Context context) {
        this(context,null);
    }

    public CGLSurfaceView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        render = new EGLRender();

        setRenderer(render);

    }
}
