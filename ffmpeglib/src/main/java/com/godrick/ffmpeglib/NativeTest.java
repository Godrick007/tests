package com.godrick.ffmpeglib;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.godrick.ffmpeglib.listeners.OnCompleteListener;
import com.godrick.ffmpeglib.listeners.OnErrorListener;
import com.godrick.ffmpeglib.listeners.OnLoadListener;
import com.godrick.ffmpeglib.listeners.OnPauseResumeListener;
import com.godrick.ffmpeglib.listeners.OnProgressListener;
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

    private OnSourcePreparedListener onSourcePreparedListener;

    private OnLoadListener onLoadListener;

    private OnPauseResumeListener onPauseResumeListener;

    private OnProgressListener onProgressListener;

    private OnErrorListener onErrorListener;

    private OnCompleteListener onCompleteListener;

    private static boolean playNext = false;

    public NativeTest(Context context) {
        this.context = context;
    }


    public void setSource(String url) {
        this.source = url;
    }

    public void setOnSourcePreparedListener(OnSourcePreparedListener listener) {
        this.onSourcePreparedListener = listener;
    }

    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public void setOnPauseResumeListener(OnPauseResumeListener onPauseResumeListener) {
        this.onPauseResumeListener = onPauseResumeListener;
    }

    public void setOnProgressListener(OnProgressListener onProgressListener){
        this.onProgressListener = onProgressListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void prepared() {

        if (TextUtils.isEmpty(source)) {
            Log.e("Ffmpeg", "source is empty");
            return;
        }

        new Thread(() -> native_prepared(source)).start();

    }

    public void start() {
        new Thread(()->{
//            native_stop();
            native_start();
        }).start();
    }

    public void pause(){

        native_pause();

        if(onPauseResumeListener != null){
            onPauseResumeListener.onPause(true);
        }

    }

    public void stop(){

        new Thread(this::native_stop).start();
    }


    public void resume(){

        native_resume();

        if(onPauseResumeListener != null){
            onPauseResumeListener.onPause(false);
        }

    }

    public void seek(int second){

        native_seek(second);

    }


    public void playNext(String url){
        source = url;
        playNext = true;
        stop();
    }

    public int getDuration(){
        return native_getDuration();
    }

    public void setVolume(int percent){
        if(percent >=0 && percent <=100) {
            native_setVolume(percent);
        }
    }

    public void setChannel(int channel){
        native_setChannel(channel);
    }


    private native void native_prepared(String source);

    private native void native_start();

    private native void native_pause();

    private native void native_resume();

    private native void native_stop();

    private native void native_seek(int second);

    private native int native_getDuration();

    private native void native_setVolume(int percent);

    private native void native_setChannel(int channel);

    public void onNativeCallPrepared() {
        if (onSourcePreparedListener != null) {
            onSourcePreparedListener.prepared();
        }
    }

    public void onNativeCallLoad(boolean load){
        if(onLoadListener != null){
            onLoadListener.onLoad(load);
        }
    }


    public void onNativeCallProgress(int current,int total){
        if(onProgressListener != null){
            onProgressListener.onProgress(current,total);
        }
    }

    public void onNativeCallError(int code,String msg){
        native_stop();
        if(onErrorListener != null){
            onErrorListener.onError(code,msg);
        }
    }


    public void onNativeCallComplete(){
        native_stop();
        if(onCompleteListener != null){
            onCompleteListener.onComplete();
        }
    }

    public void onNativeCallNext(){

        if(playNext){
            playNext = false;
            prepared();
        }

    }

}
