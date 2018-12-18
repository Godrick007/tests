package com.gaosiedu.myapplication;

import android.content.Context;
import android.util.AttributeSet;

import com.godrick.ffmpeglib.surface.EGLSurfaceView;

public class MutiSurfaceView extends EGLSurfaceView {



    public MutiSurfaceView(Context context) {
        this(context,null);
    }

    public MutiSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MutiSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);

    }

    private void init(Context context) {




    }
}
