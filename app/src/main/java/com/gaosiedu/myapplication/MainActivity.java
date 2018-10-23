package com.gaosiedu.myapplication;

import android.os.Bundle;
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
        });
    }


    public void begin(View view) {
        test.setSource("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3");
        test.prepared();
    }


}
