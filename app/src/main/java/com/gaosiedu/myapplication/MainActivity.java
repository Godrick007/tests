package com.gaosiedu.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.godrick.ffmpeglib.NativeTest;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    NativeTest test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        test = new NativeTest(this);
        test.setOnSourcePreparedListener(() -> {
            Log.e("ffmpeg", "prepared is cool");
            test.start();
        });
    }


    public void begin(View view) {

        if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            test.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/1.mp3");
            test.prepared();
        }else{
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }


    }


}
