package com.godrick.ffmpeglib.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.godrick.ffmpeglib.opengl.CRender;

import java.util.Arrays;

public class AudioRecordUtil {

    private AudioRecord audioRecord;

    private int bufferSizeInBytes;

    private boolean start = false;

    private int readSize = 0;

    private OnRecordListener onRecordListener;

    public AudioRecordUtil() {

        bufferSizeInBytes = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes
        );

    }

    public void startRecord() {


        new Thread(() -> {
            audioRecord.startRecording();
            start = true;

            byte[] audioData = new byte[bufferSizeInBytes];

            while (start) {

                readSize = audioRecord.read(
                        audioData,
                        0,
                        bufferSizeInBytes
                );

                if(onRecordListener != null){
                    onRecordListener.onRecord(audioData,readSize);
                }

                Arrays.fill(audioData, (byte) 0);
            }

            if(audioRecord != null ){
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }



        }).start();


    }

    public void stopRecord(){
        start = false;
    }


    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }

    public interface OnRecordListener {

        void onRecord(byte[] audioData,int size);

    }

}
