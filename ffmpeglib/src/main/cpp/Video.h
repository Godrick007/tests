//
// Created by Godrick Crown on 2018/12/7.
//

#ifndef TESTS_VIDEO_H
#define TESTS_VIDEO_H

#include "Queue.h"
#include "CallJava.h"
#include "PlayStatus.h"
#include "Audio.h"

extern "C"{
#include <libavcodec/avcodec.h>
#include <libavutil/time.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
};

#define CODEC_YUV 0
#define CODEC_MEDIA_CODEC 1


class Video {

public:
    int streamIndex = -1;
    AVCodecContext *pCodecContext = NULL;
    AVCodecParameters *pCodecParameters = NULL;
    Queue *pQueue = NULL;
    PlayStatus *playStatus = NULL;
    CallJava *callJava = NULL;
    AVRational timeBase;
    pthread_t thread_play;

    Audio *audio;

    double clock;

    double delayTime = 0;

    double defaultDelayTime = 0.04;

    pthread_mutex_t mutex_codec;

    int codecType = CODEC_YUV;

    AVBSFContext *pBsCtx = NULL;

public:
    Video(PlayStatus *playStatus,CallJava *callJava);
    ~Video();

    void play();

    void release();

    double getFrameDiffTime(AVFrame *avFrame,AVPacket *avPacket);

    double getDelayTime(double diff);

};

#endif //TESTS_VIDEO_H
