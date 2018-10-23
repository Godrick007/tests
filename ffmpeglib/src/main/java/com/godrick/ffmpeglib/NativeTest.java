package com.godrick.ffmpeglib;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.godrick.ffmpeglib.listeners.OnSourcePreparedListener;


public class NativeTest {

    private Context context;


    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("x264");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("postproc-54");
        System.loadLibrary("swresample-2");
        System.loadLibrary("swscale-4");

    }


    private String source;

    private OnSourcePreparedListener listener;


    public NativeTest(Context context) {
        this.context = context;
    }


    public void setSource(String url) {
        this.source = url;
    }

    public void setOnSourcePreparedListener(OnSourcePreparedListener listener) {
        this.listener = listener;
    }

    public void prepared() {

        if (TextUtils.isEmpty(source)) {
            Log.e("Ffmpeg", "source is empty");
            return;
        }

        new Thread(() -> native_prepared(source)).start();

    }

    public void start() {
        new Thread(this::native_start).start();
    }

    public native void native_prepared(String source);

    public native void native_start();


    public void onNativeCallPrepared() {
        if (listener != null) {
            listener.prepared();
        }
    }

}
