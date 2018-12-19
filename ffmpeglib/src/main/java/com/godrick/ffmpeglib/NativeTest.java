package com.godrick.ffmpeglib;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.godrick.ffmpeglib.listeners.OnCompleteListener;
import com.godrick.ffmpeglib.listeners.OnErrorListener;
import com.godrick.ffmpeglib.listeners.OnLoadListener;
import com.godrick.ffmpeglib.listeners.OnPCMCallback;
import com.godrick.ffmpeglib.listeners.OnPauseResumeListener;
import com.godrick.ffmpeglib.listeners.OnProgressListener;
import com.godrick.ffmpeglib.listeners.OnRenderCallback;
import com.godrick.ffmpeglib.listeners.OnSourcePreparedListener;
import com.godrick.ffmpeglib.listeners.OnValueDbListener;
import com.godrick.ffmpeglib.opengl.CGLSurfaceView;
import com.godrick.ffmpeglib.opengl.CRender;
import com.godrick.ffmpeglib.util.VideoUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class NativeTest {

    private Context context;


    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("x264");
        System.loadLibrary("soundtouch");
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

    private OnValueDbListener onValueDbListener;

    private OnRenderCallback onRenderCallback;

    private CGLSurfaceView glSurfaceView;

    private static boolean playNext = false;

    private boolean isRecording = false;

    private OnPCMCallback onPCMCallback;

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

    public void setOnValueDbListener(OnValueDbListener onValueDbListener) {
        this.onValueDbListener = onValueDbListener;
    }

    public void setOnRenderCallback(OnRenderCallback onRenderCallback) {
        this.onRenderCallback = onRenderCallback;
    }

    public void setGlSurfaceView(CGLSurfaceView glSurfaceView){
        this.glSurfaceView = glSurfaceView;
        glSurfaceView.getRender().setOnSurfaceCreatedListener(surface -> {
            this.surface = surface;
        });
    }

    public void setOnPCMCallback(OnPCMCallback onPCMCallback) {
        this.onPCMCallback = onPCMCallback;
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

        new Thread(()-> {
            stopRecord();
            releaseCodec();
            releaseMediaCodec();
            native_stop();
        }).start();
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

    public void startMediaRecord(){
        new Thread(this::native_startMediaReocrd).start();
    }

    public void stopMediaRecord(){
        new Thread(this::native_stopMediaRecord).start();
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

    public void setSpeed(float speed){
        native_setSpeed(speed);
    }

    public void setPitch(float pitch){
        native_setPitch(pitch);
    }

    public void startRecord(File file){

        if(native_getSampleRate() <= 0 || isRecording) return;
        initMediaCodec(native_getSampleRate(),file);
        isRecording = true;
        native_startStopRecord(true);

    }

    public void stopRecord(){
        isRecording = false;
        native_startStopRecord(false);
        releaseMediaCodec();
    }

    public void pauseRecord(){
        isRecording = false;
        native_startStopRecord(false);
    }

    public void resumeRecord(){
        isRecording = true;
        native_startStopRecord(true);
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

    private native void native_setSpeed(float speed);

    private native void native_setPitch(float pitch);

    private native int native_getSampleRate();

    private native void native_startStopRecord(boolean state);

    private native void native_startMediaReocrd();

    private native void native_stopMediaRecord();

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

    public void onNativeCallVolumeDB(int db){
        if(onValueDbListener != null){
            onValueDbListener.onValueDbCallback(db);
        }
    }



    //media codec encode

    private MediaFormat encoderFormat;
    private MediaCodec encoder;
    private FileOutputStream fos;
    private MediaCodec.BufferInfo bufferInfo;
    private int pcmSize = 0;
    private byte[] outputBuffer;
    private int ACCSampleRate;
    private float recordSecond;
    private int audioSampleRate = 0;

    private void initMediaCodec(int sampleRate, File outputFile){


        try {

            ACCSampleRate = getACCSampleRate(sampleRate);
            recordSecond = 0;
            audioSampleRate = native_getSampleRate();
            encoderFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,sampleRate,2);
            encoderFormat.setInteger(MediaFormat.KEY_BIT_RATE,96000);
            encoderFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            encoderFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,4096 * 2);

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            encoder.configure(encoderFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

            bufferInfo = new MediaCodec.BufferInfo();


            if(encoder == null){
                Log.e("java","encoder create error");
                return;
            }

            fos = new FileOutputStream(outputFile);
            encoder.start();
            isRecording = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private void onNativeCallEncodePCM2AAC(int size , byte[] buffer){

            if(onPCMCallback != null){
                onPCMCallback.onPCMCallback(buffer,size);
            }

        if(buffer != null && encoder != null && isRecording)
        {
            int inputBufferIndex = encoder.dequeueInputBuffer(0);
            if(inputBufferIndex >= 0)
            {


                recordSecond += size * 1.0f / (audioSampleRate * 2 * (16 / 8));

                Log.e("codec","record second is " + recordSecond);

                ByteBuffer byteBuffer = encoder.getInputBuffers()[inputBufferIndex];
                byteBuffer.clear();

//                Log.e("codec",String.format("remaining is %d , length is %d",byteBuffer.remaining(),buffer.length));

                byteBuffer.put(buffer);
                encoder.queueInputBuffer(inputBufferIndex, 0, size, 0, 0);
            }

            int index = encoder.dequeueOutputBuffer(bufferInfo, 0);
            while(index >= 0)
            {
                try {
                    pcmSize = bufferInfo.size + 7;
                    outputBuffer = new byte[pcmSize];

                    ByteBuffer byteBuffer = encoder.getOutputBuffers()[index];
                    byteBuffer.position(bufferInfo.offset);
                    byteBuffer.limit(bufferInfo.offset + bufferInfo.size);

                    addDtsHeader(outputBuffer, pcmSize, ACCSampleRate);

                    byteBuffer.get(outputBuffer, 7, bufferInfo.size);
                    byteBuffer.position(bufferInfo.offset);
                    fos.write(outputBuffer, 0, pcmSize);
                    encoder.releaseOutputBuffer(index, false);
                    index = encoder.dequeueOutputBuffer(bufferInfo, 0);
                    outputBuffer = null;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        }

    }


    public void onNativeCallRenderYUV(int width,int height,byte[] y, byte[] u, byte[] v){

//        Log.e("java","call back data");

//        if(onRenderCallback != null){
//            onRenderCallback.onCallback(width,height,y,u,v);
//        }

        if(glSurfaceView != null){
            glSurfaceView.setYuvData(width,height,y,u,v);
        }

    }


    public boolean onNativeCallSupportMediaCodec(String codecName){
        return VideoUtil.isSupportCodec(codecName);
    }

    private void releaseMediaCodec(){

        if(fos != null){
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        fos = null;
        if(encoder != null) {
            encoder.stop();
            encoder.release();
            encoder = null;
        }

        if(encoderFormat != null){
            encoderFormat = null;
        }
        if(bufferInfo != null) {
            bufferInfo = null;
        }

    }

    private void addDtsHeader(byte[] packet,int packetLength,int sampleRate){

        int profile = 2; // AAC LC
        int freqIdx = sampleRate; // samplerate
        int chanCfg = 2; // CPE

        packet[0] = (byte) 0xFF; // 0xFFF(12bit) 这里只取了8位，所以还差4位放到下一个里面
        packet[1] = (byte) 0xF9; // 第一个t位放F
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLength >> 11));
        packet[4] = (byte) ((packetLength & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLength & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;

    }


    private int getACCSampleRate(int sampleRate){

        int rate = 4;
        switch (sampleRate)
        {
            case 96000:
                rate = 0;
                break;
            case 88200:
                rate = 1;
                break;
            case 64000:
                rate = 2;
                break;
            case 48000:
                rate = 3;
                break;
            case 44100:
                rate = 4;
                break;
            case 32000:
                rate = 5;
                break;
            case 24000:
                rate = 6;
                break;
            case 22050:
                rate = 7;
                break;
            case 16000:
                rate = 8;
                break;
            case 12000:
                rate = 9;
                break;
            case 11025:
                rate = 10;
                break;
            case 8000:
                rate = 11;
                break;
            case 7350:
                rate = 12;
                break;
        }
        return rate;
    }


    private MediaFormat mediaFormat;
    private MediaCodec mediaCodec;

    private Surface surface;

    private MediaCodec.BufferInfo outputBufferInfo;

    public void initMediaCodec(String codecName,int width,int height,byte[] csd_0,byte[] csd_1){


        if(surface != null){
            try {

                glSurfaceView.getRender().setRender_type(CRender.RENDER_TYPE.CODEC);

                String mime = VideoUtil.findVideoCodecName(codecName);

                mediaFormat = MediaFormat.createVideoFormat(mime,width,height);
                mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,width * height);
                mediaFormat.setByteBuffer("csd-0",ByteBuffer.wrap(csd_0));
                mediaFormat.setByteBuffer("csd-1",ByteBuffer.wrap(csd_1));

                mediaCodec = MediaCodec.createDecoderByType(mime);

                outputBufferInfo = new MediaCodec.BufferInfo();

                mediaCodec.configure(
                        mediaFormat,
                        surface,
                        null,
                        0
                );



                mediaCodec.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{

        }

    }



    public void decodeVideo(byte[] data,int size){

        if(surface != null && size > 0 && data != null && mediaCodec != null){

            try{
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(10);

                if(inputBufferIndex >= 0){

                    ByteBuffer byteBuffer = mediaCodec.getInputBuffers()[inputBufferIndex];

                    byteBuffer.clear();

                    byteBuffer.put(data);

                    mediaCodec.queueInputBuffer(inputBufferIndex,0,size,0,0);

                }

                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(outputBufferInfo,10);

                while(outputBufferIndex >= 0){

                    mediaCodec.releaseOutputBuffer(outputBufferIndex,true);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(outputBufferInfo,10);

                }
            }catch (Exception e){
                e.printStackTrace();
            }



        }

    }


    private void releaseCodec(){

        try {
            if(mediaCodec != null){
                mediaCodec.flush();
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }

            if(mediaFormat != null){
                mediaFormat = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }




    }

}
