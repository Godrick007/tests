package com.godrick.pusher.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.godrick.ffmpeglib.util.DisplayUtil;

import java.io.IOException;
import java.util.List;

public class CCamera {


    private SurfaceTexture surfaceTexture;
    private Camera camera;

    private int width;
    private int height;


    public CCamera(Context context){
        this.width = DisplayUtil.getScreenWidth(context);
        this.height = DisplayUtil.getScreenHeight(context);
    }


    public void initCamera(SurfaceTexture surfaceTexture,int cameraId){

        this.surfaceTexture = surfaceTexture;
        setCameraParam(cameraId);


    }


    private void setCameraParam(int cameraId){



        try {
            camera = Camera.open(cameraId);
            camera.setPreviewTexture(surfaceTexture);

            Camera.Parameters parameters = camera.getParameters();

            parameters.setFlashMode("off");
            parameters.setPreviewFormat(ImageFormat.NV21);

            Camera.Size sizePicture = getFitSize(parameters.getSupportedPictureSizes());
            Camera.Size sizePreview = getFitSize(parameters.getSupportedPreviewSizes());

            parameters.setPreviewSize(
                    sizePreview.width,
                    sizePreview.height
            );


            parameters.setPictureSize(
                    sizePicture.width,
                    sizePicture.height
            );

            camera.setParameters(parameters);

            camera.startPreview();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopPreview(){
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void switchCamera(int cameraId){

        if(camera != null){
            stopPreview();
        }

        setCameraParam(cameraId);

    }


    private Camera.Size getFitSize(List<Camera.Size> sizes){

        if(width < height){

            int t = height;
            height = width;
            width = t;

        }

        for(Camera.Size size : sizes){

            if(1.0f * size.width / size.height == 1.0f * width / height){
                return size;
            }

        }

        return sizes.get(0);
    }

}
