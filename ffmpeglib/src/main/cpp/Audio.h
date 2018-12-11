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

#include "SoundTouch.h"

using namespace soundtouch;

extern "C" {
#include <libavcodec/avcodec.h>
#include <libswresample/swresample.h>
#include <libavutil/time.h>
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

    SLVolumeItf pcmPlayerVolume = NULL;

    SLMuteSoloItf pcmPlayerMute = NULL;

    CallJava *callJava;

    SoundTouch *soundTouch = NULL;
    SAMPLETYPE *sampleBuffer = NULL;

    bool finish = true;

    uint8_t *outBuffer = NULL;

    int nb = 0;

    int num = 0;


    float speed = 1.0f;
    float pitch = 1.0f;
    int channel = 0;

    bool  isRecord = false;

    pthread_mutex_t mutex_codec;

public:

    Audio(PlayStatus *playStatus,int sample_rate,CallJava *callJava);

    ~Audio();

    void play();

    int resampleAudio(void **pcmBuffer);

    void initSLES();

    int getCurrentSampleRateForOpenSLES(int sample_rate);

    void pause();

    void resume();

    void stop();

    void release();

    void setVolume(int percent);

    void switchChannel(int channel);

    int getSoundTouchData();

    void setPitch(float pitch);

    void setSpeed(float tempo);

    int getPCMDB(char *pcmcate,size_t pcmSize);

    void startStopRecord(bool state);

};


#endif //TESTS_AUDIO_H
