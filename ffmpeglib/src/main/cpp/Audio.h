//
// Created by Godrick Crown on 2018/10/23.
//

#ifndef TESTS_AUDIO_H
#define TESTS_AUDIO_H

#include <pthread.h>
#include "PlayStatus.h"
#include "Queue.h"

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


public:
    Audio(PlayStatus *playStatus);

    ~Audio();

    void play();

    int resampleAudio();

};


#endif //TESTS_AUDIO_H
