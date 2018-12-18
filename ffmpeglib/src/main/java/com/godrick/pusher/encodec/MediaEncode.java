package com.godrick.pusher.encodec;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaEncode extends BaseMediaEncoder {

    private EncodeRender encodeRender;


    public MediaEncode(Context context, int textureId) {
        super(context);
        encodeRender = new EncodeRender(context,textureId);
        setRender(encodeRender);
        setRenderMode(RenderMode.RENDER_MODE_CONTINUOUSLY);
    }
}
