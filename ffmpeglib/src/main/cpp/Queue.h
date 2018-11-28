//
// Created by Godrick Crown on 2018/11/28.
//

#ifndef TESTS_QUEUE_H
#define TESTS_QUEUE_H

#include "queue"
#include "pthread.h"
#include "androidLog.h"
#include "PlayStatus.h"

extern "C"
{
#include "libavcodec/avcodec.h"
};

class Queue {

public:
    std::queue<AVPacket *> queueAVPacket;
    pthread_mutex_t mutexPacket;
    pthread_cond_t condPacket;
    PlayStatus *playStatus = NULL;

public:

    Queue(PlayStatus *playStatus);

    ~Queue();

    int putAvPacket(AVPacket *packet);
    int getAvPacket(AVPacket *packet);

    int getQueueSize();

};


#endif //TESTS_QUEUE_H
