package com.godrick.ffmpeglib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class NativeTest {

    private Context context;

    Handler handler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                Toast.makeText(context, "haha", Toast.LENGTH_SHORT).show();
            }
        }
    };

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

    public NativeTest(Context context) {
        this.context = context;
    }

    public native void nativeCodeTest();

    public native String getString();

    public native void thread1();

    public void call() {
//        handler.post(() -> {
//            Toast.makeText(context, "haha", Toast.LENGTH_SHORT).show();
//        });

//        Message message = handler.obtainMessage();
//        handler.sendEmptyMessage(0);

        Log.e("java", "call is called");

    }

}
