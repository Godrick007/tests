//
// Created by Godrick Crown on 2018/11/28.
//

#include "Queue.h"

Queue::Queue(PlayStatus *playStatus) {
    this->playStatus = playStatus;
    pthread_mutex_init(&mutexPacket,NULL);
    pthread_cond_init(&condPacket,NULL);
}

Queue::~Queue() {

}

int Queue::putAvPacket(AVPacket *packet) {

    pthread_mutex_lock(&mutexPacket);

    queueAVPacket.push(packet);

    if(LOG_DEBUG)
    {
        LOGE("ffmpeg","put a avpacket in queue and queue's size is %d",queueAVPacket.size());
    }

    pthread_cond_signal(&condPacket);
    pthread_mutex_unlock(&mutexPacket);

    return 0;
}

int Queue::getAvPacket(AVPacket *packet) {

    pthread_mutex_lock(&mutexPacket);

    while(playStatus != NULL && !playStatus->exit)
    {

        if(queueAVPacket.size() > 0)
        {
            AVPacket *avPacket = queueAVPacket.front();
            if(av_packet_ref(packet,avPacket) == 0)
            {
                queueAVPacket.pop();
            }
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            if(LOG_DEBUG)
            {
                LOGE("ffmpeg","get a avpacket form queue and queue's size is %d",queueAVPacket.size());
            }
            break;
        }
        else
        {
            pthread_cond_wait(&condPacket,&mutexPacket);
        }

    }
    pthread_mutex_unlock(&mutexPacket);
    return 0;
}

int Queue::getQueueSize() {

    int size = 0;

    pthread_mutex_lock(&mutexPacket);
    size = queueAVPacket.size();
    pthread_mutex_unlock(&mutexPacket);

    return size;
}
