//
// Created by Godrick Crown on 2018/10/23.
//

#ifndef TESTS_FFMPEG_H
#define TESTS_FFMPEG_H

#include "pthread.h"
#include "CallJava.h"
#include "Audio.h"

extern "C" {
# include<libavformat/avformat.h>
};


class Ffmpeg {

public:
    CallJava *callJava = NULL;
    const char *url = NULL;
    pthread_t threadDecode;
    AVFormatContext *pFormatContext = NULL;
    Audio *audio = NULL;

public:
    Ffmpeg(CallJava *cj, const char *url);

    ~Ffmpeg();


    void prepared();

    void decodeFfmpegThread();

    void setAudio(Audio *audio);

    void start();

};


#endif //TESTS_FFMPEG_H
