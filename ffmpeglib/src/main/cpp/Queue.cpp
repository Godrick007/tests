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
    LOGE("release","Queue's release is called");
    clearAVPacket();
    pthread_mutex_destroy(&mutexPacket);
    pthread_cond_destroy(&condPacket);
}

int Queue::putAvPacket(AVPacket *packet) {

    pthread_mutex_lock(&mutexPacket);

    queueAVPacket.push(packet);


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

void Queue::clearAVPacket() {

   pthread_cond_signal(&condPacket);
   pthread_mutex_lock(&mutexPacket);

   while(queueAVPacket.empty())
   {
       AVPacket *packet = queueAVPacket.front();
       queueAVPacket.pop();
       av_packet_free(&packet);
       av_free(packet);
       packet = NULL;
   }

   pthread_mutex_unlock(&mutexPacket);


}
