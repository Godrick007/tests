//
// Created by Godrick Crown on 2018/10/23.
//

#ifndef TESTS_FFMPEG_H
#define TESTS_FFMPEG_H

#include "pthread.h"
#include "CallJava.h"
#include "Audio.h"
#include "PlayStatus.h"

extern "C" {
#include <libavutil/time.h>
#include <libavformat/avformat.h>
};


class Ffmpeg {

public:
    CallJava *callJava = NULL;
    const char *url = NULL;
    pthread_t threadDecode;
    AVFormatContext *pFormatContext = NULL;
    Audio *audio = NULL;
    PlayStatus *playStatus;

    pthread_mutex_t mutexInit ;

    bool exit = false;

    int duration;

    pthread_mutex_t mutexSeek;

public:
    Ffmpeg(PlayStatus *playStatus,CallJava *cj, const char *url);

    ~Ffmpeg();


    void prepared();

    void decodeFfmpegThread();

    void setAudio(Audio *audio);

    void start();

    void pause();

    void resume();

    void stop();

    void release();

    void seek(int64_t second);

    void setVolume(int percent);

    void setChannel(int channel);

    void setSpeed(float speed);

    void setPitch(float pitch);

};


#endif //TESTS_FFMPEG_H
