package com.godrick.ffmpeglib;

public class NativeTest {

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


    public native void nativeCodeTest();

    public native String getString();

}
