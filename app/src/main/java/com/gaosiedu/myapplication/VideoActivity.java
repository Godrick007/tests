package com.gaosiedu.myapplication;

import android.Manifest;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.godrick.ffmpeglib.NativeTest;
import com.godrick.pusher.camera.CameraView;
import com.godrick.pusher.encodec.BaseMediaEncoder;
import com.godrick.pusher.encodec.MediaEncode;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class VideoActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnStart;
    private Button btnStop;
    private CameraView cameraView;

    private MediaEncode mediaEncode;

    private NativeTest test;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        test = new NativeTest(this);

        test.setOnSourcePreparedListener(()-> {
            test.startRecord(new File(getCacheDir(),"1.mp3"));
            test.start();
        });

        requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},100);

        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        cameraView = findViewById(R.id.camera_view);

        btnStop.setOnClickListener(this);
        btnStart.setOnClickListener(this);

        test.setOnPCMCallback((buffer, size) -> {

            mediaEncode.setPCMData(buffer,size);

        });

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_start:

                record();

                break;
            case R.id.btn_stop:

                stop();

                break;
        }

    }


    private void stop() {

        if(test != null){
            test.stop();
        }

       if(mediaEncode != null){
           mediaEncode.stopRecord();
           mediaEncode = null;
       }

    }

    private void record() {

        if(test != null){

            test.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4");
//            test.setSource("");
            test.prepared();

        }

        if(mediaEncode == null){

            Log.e("video", "texture id is " + cameraView.getTextureId());

            mediaEncode = new MediaEncode(this,cameraView.getTextureId());
//            mediaEncode.setOnMediaInfoListener(time -> Log.e("video",String.format("time is %d",time)));
            mediaEncode.initEncoder(cameraView.getEglContext(),
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/testaaa.mp4",
                    MediaFormat.MIMETYPE_VIDEO_AVC,1080,1920,44100,2);
            mediaEncode.startRecord();
        }

    }
}
