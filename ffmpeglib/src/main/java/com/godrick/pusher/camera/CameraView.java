package com.godrick.pusher.camera;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.godrick.ffmpeglib.surface.EGLSurfaceView;

public class CameraView extends EGLSurfaceView {

    private CameraRender cameraRender;
    private CCamera camera;

    private int cameraId = 0;

    private int textureId;

    public CameraView(Context context) {
        this(context,null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        cameraRender = new CameraRender(context);
        camera = new CCamera(context);

        previewAngle(context);

        cameraRender.setOnSurfaceCreatedListener((surfaceTexture ,textureId)-> {
            camera.initCamera(surfaceTexture,cameraId);
            this.textureId = textureId;
        });

        setRender(cameraRender);
        setRenderMode(RenderMode.RENDER_MODE_CONTINUOUSLY);
    }

    public void onDestory(){
        if(camera != null){
            camera.stopPreview();
        }
    }


    public void previewAngle(Context context){

        int angle = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        cameraRender.resetMatrix();

        switch (angle){



            case Surface.ROTATION_0:

                Log.e("camera","0");

                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    cameraRender.setAngle(90,0,0,1);
                    cameraRender.setAngle(180,1,0,0);
                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    cameraRender.setAngle(90,0,0,1);
                }



                break;

            case Surface.ROTATION_90:

                Log.e("camera","90");

                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
//                    cameraRender.setAngle(90,0,0,1);
                    cameraRender.setAngle(180,1,0,0);
                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
                    cameraRender.setAngle(180,0,0,1);
                }


                break;

            case Surface.ROTATION_180:

                Log.e("camera","180");

                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){



                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){



                }


                break;

            case Surface.ROTATION_270:

                Log.e("camera","270");

                if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    cameraRender.setAngle(180,0,1,0);
                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){

                }


                break;

        }

    }

    public int getTextureId(){
        return textureId;
    }

}
