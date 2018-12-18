package com.gaosiedu.myapplication;

import android.content.Context;
import android.util.AttributeSet;

import com.godrick.ffmpeglib.surface.EGLSurfaceView;

public class EGLPreview extends EGLSurfaceView {


    public EGLPreview(Context context) {
        this(context,null);
    }

    public EGLPreview(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EGLPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {




    }
}
