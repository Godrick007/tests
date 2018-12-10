package com.gaosiedu.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CGLSurfaceView extends GLSurfaceView {




    public CGLSurfaceView(Context context) {
        this(context,null);
    }

    public CGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        setEGLContextClientVersion(2);
        setRenderer(new CRender(context));

    }
}
