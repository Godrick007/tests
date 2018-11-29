//
// Created by Godrick Crown on 2018/10/23.
//

#ifndef TESTS_AUDIO_H
#define TESTS_AUDIO_H

#include <pthread.h>
#include "PlayStatus.h"
#include "Queue.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include "CallJava.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libswresample/swresample.h>
};

class Audio {

public:

    int streamIndex = -1;
    AVCodecParameters *pCodecParameters = NULL;
    AVCodecContext *pCodecContext = NULL;
    PlayStatus *playStatus = NULL;
    Queue *queue = NULL;

    pthread_t thread_play;
    AVPacket *avPacket = NULL;
    AVFrame *avFrame = NULL;
    int ret = 0;
    uint8_t *buffer = NULL;
    int data_size = 0;

    int sample_rate = 0;

    int duration = 0;

    AVRational time_base;

    double now_time = 0;

    double clock = 0;

    long last_time = 0;

    //engine
    SLObjectItf engineObject = NULL;
    SLEngineItf engineEngine = NULL;

//mix
    SLObjectItf outputMixObject = NULL;
    SLEnvironmentalReverbItf outputMixEnvironmentReverb = NULL;
    SLEnvironmentalReverbSettings reverbSettings =
            SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

//pcm
    SLObjectItf pcmPlayerObject = NULL;
    SLPlayItf pcmPlayerPlay = NULL;
    SLAndroidSimpleBufferQueueItf  pcmBufferQueue = NULL;


    CallJava *callJava;

public:

    Audio(PlayStatus *playStatus,int sample_rate,CallJava *callJava);

    ~Audio();

    void play();

    int resampleAudio();

    void initSLES();

    int getCurrentSampleRateForOpenSLES(int sample_rate);

    void pause();

    void resume();

    void stop();

    void release();

};


#endif //TESTS_AUDIO_H
