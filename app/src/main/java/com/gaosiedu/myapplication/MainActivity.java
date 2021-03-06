package com.gaosiedu.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.godrick.ffmpeglib.NativeTest;
import com.godrick.ffmpeglib.listeners.OnCompleteListener;
import com.godrick.ffmpeglib.listeners.OnValueDbListener;
import com.godrick.ffmpeglib.opengl.CGLSurfaceView;
import com.godrick.ffmpeglib.util.TimeUtil;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    NativeTest test;

    TextView textView;

    SeekBar sbVolume;

    SeekBar sbProgress;

    private int progress;

    private boolean seekProgress = false;

    private int volume = 0;

    private CGLSurfaceView cglSurfaceView;

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
            sbProgress.setMax(test.getDuration());
        });

        test.setOnLoadListener(load->{
            if(load)
                Log.e("java","loading");
            else
                Log.e("java","playing");
        });

        test.setOnPauseResumeListener(pause->{
//            if(pause)
//                Log.e("java","pause");
//            else
//                Log.e("java","resume");
        });

        test.setOnProgressListener((current,total)->{






            if(!seekProgress) {

                Message message = handler.obtainMessage();
                message.what = 1;
                message.arg1 = current;
                message.arg2 = total;
                handler.sendMessage(message);

                sbProgress.setProgress(current);
            }
//            Log.e("java",String.format("current is %d, total is %d",current,total));
        });

        textView = findViewById(R.id.tv_progress);

        test.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                Log.e("java","play complete");
            }
        });

        sbVolume = findViewById(R.id.seekbar);

        sbVolume.setMax(100);

        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                test.setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        test.setOnValueDbListener(new OnValueDbListener() {
            @Override
            public void onValueDbCallback(int db) {
//                Log.e("java","db is " + db);
            }
        });

        cglSurfaceView = findViewById(R.id.gl_surface_view);

        test.setGlSurfaceView(cglSurfaceView);

        sbProgress = findViewById(R.id.sb_progress);

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                test.seek(progress);
                  MainActivity.this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekProgress = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                test.seek(MainActivity.this.progress);
                seekProgress = false;
            }
        });

    }


    public void begin(View view) {

        if(PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//            test.setSource("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3");
//            test.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/1.mp3");
            test.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4");
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

    public void stop(View view) {
        test.stop();
    }


    public void seek(View view) {

        test.seek(200);

    }

    public void next(View view) {

//        test.playNext("http://mpge.5nd.com/2015/2015-11-26/69708/1.mp3");
        test.playNext(Environment.getExternalStorageDirectory().getAbsolutePath() + "/redis.mp4");

    }

    public void left(View view) {
        test.setChannel(1);
    }

    public void right(View view) {
        test.setChannel(2);
    }

    public void center(View view) {
        test.setChannel(0);
    }

    public void startRecord(View view) {
        test.startRecord(new File(Environment.getExternalStorageDirectory(),"1.acc"));
    }

    public void stopRecord(View view) {
        test.stopRecord();
    }

    public void pauseRecord(View view) {
        test.pauseRecord();
    }

    public void resumeRecord(View view) {
        test.resumeRecord();
    }
}
