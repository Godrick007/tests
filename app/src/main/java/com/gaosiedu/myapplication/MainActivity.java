package com.gaosiedu.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import com.godrick.ffmpeglib.NativeTest;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NativeTest test = new NativeTest(this);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);

        tv.setText(test.getString());

//        new NativeTest().nativeCodeTest();

//        new Thread(() -> test.thread1()).start();

        test.thread2();
    }


}
