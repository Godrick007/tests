package com.godrick.ffmpeglib.surface;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

public abstract class EGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public enum RenderMode {
        RENDER_MODE_WHEN_DIRTY,
        RENDER_MODE_CONTINUOUSLY
    }


    private Surface mSurface;
    private EGLContext mEglContext;
    private EGLThread eglThread;
    private EGLRender mRender;
    private RenderMode renderMode = RenderMode.RENDER_MODE_CONTINUOUSLY;

    public EGLSurfaceView(Context context) {
        this(context,null);
    }

    public EGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public EGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if(mSurface == null){
            mSurface = holder.getSurface();
        }

        eglThread = new EGLThread(new WeakReference<>(this));
        eglThread.isCreate = true;
        eglThread.start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        eglThread.width = width;
        eglThread.height = height;
        eglThread.isChange = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        eglThread.onDestroy();
        eglThread = null;
        mSurface = null;
        mEglContext = null;

    }


    public void setRenderMode(RenderMode renderMode) {

        if(mRender == null){
            throw new NullPointerException("render is null");
        }

        this.renderMode = renderMode;
    }

    public void setRender(EGLRender render){
        this.mRender = render;
    }

    public void setEGLVersion(int version){

    }

    public void setSurfaceAndEglSurface(Surface surface,EGLContext eglContext){
        this.mSurface = surface;
        this.mEglContext = eglContext;
    }

    public EGLContext getEglContext(){
        return eglThread != null ? eglThread.getEglContext() : null;
    }

    public void requestRender(){
        if(eglThread != null){
            eglThread.requestRender();
        }
    }


    public interface EGLRender{

        void onSurfaceCreated();

        void onSurfaceChanged(int width,int height);

        void onDrawFrame();

    }


    static class EGLThread extends Thread{

        private WeakReference<EGLSurfaceView> wrSurface;
        private EglHelper eglHelper;

        private Object lock;

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        private int width;
        private int height;

        public EGLThread(WeakReference<EGLSurfaceView> wrSurface){
            this.wrSurface = wrSurface;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            eglHelper = new EglHelper();
            lock = new Object();
            eglHelper.initEgl(
                    wrSurface.get().mSurface,
                    wrSurface.get().mEglContext
                    );



            while(true){

                if(isExit){
                    release();
                    break;
                }

                if(isStart){

                    if(wrSurface.get().renderMode == RenderMode.RENDER_MODE_WHEN_DIRTY){

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
                onChange(width,height);
                onDrawFrame();

                isStart = true;


            }



        }


        private void onCreate(){
            if(isCreate && wrSurface.get().mRender != null){
                wrSurface.get().mRender.onSurfaceCreated();
                isCreate = false;
            }
        }

        private void onChange(int width,int height){
            if(isChange && wrSurface.get().mRender != null){
                isChange = false;
                wrSurface.get().mRender.onSurfaceChanged(width,height);
            }
        }


        private void onDrawFrame(){

            if(wrSurface.get().mRender != null && eglHelper != null){
                wrSurface.get().mRender.onDrawFrame();

                if(!isStart){
                    wrSurface.get().mRender.onDrawFrame();
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
                wrSurface = null;
            }
        }

        private EGLContext getEglContext(){
            if(eglHelper != null){
                return eglHelper.getEglContext();
            }
            return null;
        }

    }
}
