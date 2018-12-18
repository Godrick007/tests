package com.gaosiedu.myapplication;

import android.content.Context;
import android.util.AttributeSet;

import com.godrick.ffmpeglib.surface.EGLSurfaceView;

public class MyEGLSurfaceView extends EGLSurfaceView {


    private MyRender render;


    public MyEGLSurfaceView(Context context) {
        this(context,null);
    }

    public MyEGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyEGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        render = new MyRender(context);

        setRender(render);

        setRenderMode(RenderMode.RENDER_MODE_WHEN_DIRTY);

    }

    public MyRender getRender() {
        return render;
    }
}
