package com.gaosiedu.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.godrick.ffmpeglib.NativeTest;
import com.godrick.ffmpeglib.util.TimeUtil;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    NativeTest test;

    TextView textView;

     Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.what == 1){

                int current = msg.arg1;
                int total = msg.arg2;

                textView.setText(TimeUtil.secdsToDateFormat(current,total) +"/"+ TimeUtil.secdsToDateFormat(total,total));


            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test = new NativeTest(this);
        test.setOnSourcePreparedListener(() -> {
            Log.e("ffmpeg", "prepared is cool");
            test.start();
        });

        test.setOnLoadListener(load->{
//            if(load)
//                Log.e("java","loading");
//            else
//                Log.e("java","playing");
        });

        test.setOnPauseResumeListener(pause->{
//            if(pause)
//                Log.e("java","pause");
//            else
//                Log.e("java","resume");
        });

        test.setOnProgressListener((current,total)->{

            Message message = handler.obtainMessage();
            message.what = 1;
            message.arg1 = current;
            message.arg2 = total;
            handler.sendMessage(message);
//            Log.e("java",String.format("current is %d, total is %d",current,total));
        });

        textView = findViewById(R.id.tv_progress);

    }


    public void begin(View view) {

        if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            test.setSource("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3");
//            test.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/1.mp3");
//            test.setSource("");
            test.prepared();
        }else{
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }


    }


    public void pause(View view) {
        test.pause();
    }

    public void resume(View view) {
        test.resume();
    }

    public void stop(View view) { test.stop();
    }
}
