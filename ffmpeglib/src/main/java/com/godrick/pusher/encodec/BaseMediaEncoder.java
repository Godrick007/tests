package com.godrick.pusher.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.view.Surface;

import com.godrick.ffmpeglib.surface.EGLSurfaceView;
import com.godrick.ffmpeglib.surface.EglHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class BaseMediaEncoder {

    public enum RenderMode{
        RENDER_MODE_WHEN_DIRTY,
        RENDER_MODE_CONTINUOUSLY
    }






    private Surface mSurface;

    private EGLContext mEglContext;

    private RenderMode mRenderMode = RenderMode.RENDER_MODE_CONTINUOUSLY;

    private EGLSurfaceView.EGLRender mRender;

    private int width;
    private int height;

    private MediaCodec videoCodec;
    private MediaFormat videoFormat;
    private MediaCodec.BufferInfo videoBufferInfo;
    private MediaMuxer mediaMuxer;



    private VideoEncodeThread videoThread;
    private EGLMediaThread mediaThread;

    private Context context;

    private OnMediaInfoListener onMediaInfoListener;

    public BaseMediaEncoder(Context context) {
        this.context = context;
    }

    public void setRender(EGLSurfaceView.EGLRender eglRender){
        this.mRender = eglRender;
    }

    public void setRenderMode(RenderMode renderMode){

        if(mRender == null){
            throw new NullPointerException("render is null");
        }

        this.mRenderMode = renderMode;
    }


    public void startRecord(){
        if(mSurface != null && mEglContext != null){
            mediaThread = new EGLMediaThread(new WeakReference<>(this));
            videoThread = new VideoEncodeThread(new WeakReference<>(this));
            mediaThread.isCreate = true;
            mediaThread.isChange = true;
            mediaThread.start();
            videoThread.start();
        }
    }

    public void stopRecord(){

        if(mediaThread != null && videoThread != null){
            videoThread.exit();
            mediaThread.onDestroy();
            videoThread = null;
            mediaThread = null;
        }

    }

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public void initEncoder(EGLContext eglContext, String savePath, String mimeType, int width, int height){
        this.mEglContext = eglContext;
        this.width = width;
        this.height = height;
        initMediaEncoder(savePath,mimeType,width,height);
    }


    private void initVideoCodec(String mimeType, int width, int height){


        try {

            videoBufferInfo = new MediaCodec.BufferInfo();
            videoFormat = MediaFormat.createVideoFormat(mimeType,width,height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE,width * height * 4);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30); //max value
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1); // key frame interval / second


            videoCodec = MediaCodec.createEncoderByType(mimeType);

            videoCodec.configure(videoFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);

            mSurface = videoCodec.createInputSurface();

        } catch (IOException e) {
            e.printStackTrace();
            videoCodec = null;
            videoFormat = null;
            videoBufferInfo = null;
        }


    }


    private void initMediaEncoder(String savePath, String mimeType, int width, int height){

        try {
            mediaMuxer = new MediaMuxer(savePath,MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initVideoCodec(mimeType,width,height);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    static class EGLMediaThread extends Thread{

        private WeakReference<BaseMediaEncoder> encoder;
        private EglHelper eglHelper;
        private Object lock;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;



        public EGLMediaThread(WeakReference<BaseMediaEncoder> encoder) {
            this.encoder = encoder;
        }


        @Override
        public void run() {
            super.run();

            isExit = false;
            isStart = false;
            eglHelper = new EglHelper();
            lock = new Object();
            eglHelper.initEgl(
                    encoder.get().mSurface,
                    encoder.get().mEglContext
            );

            while(true){

                if(isExit){
                    release();
                    break;
                }

                if(isStart){

                    if(encoder.get().mRenderMode == RenderMode.RENDER_MODE_WHEN_DIRTY){

                        synchronized (lock){

                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }



                    }else{

                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }


                onCreate();
                onChange(encoder.get().width,encoder.get().height);
                onDrawFrame();

                isStart = true;


            }



        }


        private void onCreate(){
            if(isCreate && encoder.get().mRender != null){
                encoder.get().mRender.onSurfaceCreated();
                isCreate = false;
            }
        }

        private void onChange(int width,int height){
            if(isChange && encoder.get().mRender != null){
                isChange = false;
                encoder.get().mRender.onSurfaceChanged(width,height);
            }
        }


        private void onDrawFrame(){

            if(encoder.get().mRender != null && eglHelper != null){
                encoder.get().mRender.onDrawFrame();

                if(!isStart){
                    encoder.get().mRender.onDrawFrame();
                }

                eglHelper.swapBuffers();

            }
        }

        private void requestRender(){
            if(lock != null){
                synchronized (lock){
                    lock.notifyAll();
                }
            }
        }

        public void onDestroy(){

            isExit = true;
            requestRender();

        }

        public void release(){
            if(eglHelper != null){
                eglHelper.destoryEgl();
                eglHelper = null;
                lock = null;
                encoder = null;
            }
        }

        private EGLContext getEglContext(){
            if(eglHelper != null){
                return eglHelper.getEglContext();
            }
            return null;
        }
        
    }


    static class VideoEncodeThread extends Thread{

        private WeakReference<BaseMediaEncoder> encoder;

        private boolean isExit;
        private MediaCodec videoCodec;
        private MediaFormat videoFormat;
        private MediaCodec.BufferInfo videoBufferInfo;
        private MediaMuxer mediaMuxer;

        private int videoTrackIndex;

        private long pts;

        public VideoEncodeThread(WeakReference<BaseMediaEncoder> encoder) {
            this.encoder = encoder;
            this.isExit = false;
            this.videoCodec = encoder.get().videoCodec;
            this.videoFormat = encoder.get().videoFormat;
            this.videoBufferInfo = encoder.get().videoBufferInfo;
            this.mediaMuxer = encoder.get().mediaMuxer;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            this.videoCodec.start();
            videoTrackIndex = -1;
            pts = 0;

            while(true){

                if(isExit){

                    videoCodec.stop();
                    videoCodec.release();
                    videoCodec = null;

                    mediaMuxer.stop();
                    mediaMuxer.release();
                    mediaMuxer = null;


                    break;
                }


                int outputBufferIndex = videoCodec.dequeueOutputBuffer(videoBufferInfo,0);

                if(outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){

                    videoTrackIndex = mediaMuxer.addTrack(videoCodec.getOutputFormat());
                    mediaMuxer.start();

                }else{

                    while (outputBufferIndex >= 0){

                        ByteBuffer outputBuffer = videoCodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(videoBufferInfo.offset);
                        outputBuffer.limit(videoBufferInfo.offset + videoBufferInfo.size);

                        //encode
                        if(pts == 0){
                            pts = videoBufferInfo.presentationTimeUs;
                        }

                        videoBufferInfo.presentationTimeUs = videoBufferInfo.presentationTimeUs - pts;

                        mediaMuxer.writeSampleData(videoTrackIndex,outputBuffer,videoBufferInfo);

                        if(encoder.get().onMediaInfoListener != null){
                            encoder.get().onMediaInfoListener.onMediaTime((int) (videoBufferInfo.presentationTimeUs  / 1000000));
                        }

                        videoCodec.releaseOutputBuffer(outputBufferIndex,false);
                        outputBufferIndex = videoCodec.dequeueOutputBuffer(videoBufferInfo,0);

                    }

                }



            }

        }


        public void exit(){
            this.isExit = true;

        }
    }


    public interface OnMediaInfoListener{

        void onMediaTime(int time);

    }

}
