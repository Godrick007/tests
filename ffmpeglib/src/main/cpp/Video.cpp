//
// Created by Godrick Crown on 2018/12/7.
//

#include "Video.h"

Video::Video(PlayStatus *playStatus, CallJava *callJava) {
    this->playStatus = playStatus;
    this->callJava = callJava;
    pQueue = new Queue(playStatus);
}

Video::~Video()
{
    
}




void *playVideo(void *data)
{
    Video *video = static_cast<Video *>(data);

    while (video->playStatus && !video->playStatus->exit)
    {

        if(video->playStatus->seek)
        {
            av_usleep(1000 * 100);
            continue;
        }


        if(video->pQueue->getQueueSize() == 0)
        {

            if(!video->playStatus->load)
            {
                video->playStatus->load = true;
                video->callJava->callJavaOnLoad(true);
            }
            av_usleep(1000 * 100);
            continue;
        }
        else
        {
            if(video->playStatus->load)
            {
                video->playStatus->load = false;
                video->callJava->callJavaOnLoad(false);
            }
        }

        AVPacket *pPacket = av_packet_alloc();

        if(video->pQueue->getAvPacket(pPacket) != 0)
        {
            av_usleep(1000 * 100);
            av_packet_free(&pPacket);
            av_free(pPacket);
            pPacket = NULL;
            continue;
        }

        if(avcodec_send_packet(video->pCodecContext,pPacket) != 0)
        {
            av_usleep(1000 * 100);
            av_packet_free(&pPacket);
            av_free(pPacket);
            pPacket = NULL;
            continue;
        }

        AVFrame *pFrame = av_frame_alloc();

        if(avcodec_receive_frame(video->pCodecContext,pFrame) != 0)
        {
            av_frame_free(&pFrame);
            av_free(pFrame);
            pFrame = NULL;
            av_usleep(1000 * 100);
            av_packet_free(&pPacket);
            av_free(pPacket);
            pPacket = NULL;

            continue;
        }


        //success get a packet





        av_frame_free(&pFrame);
        av_free(pFrame);
        pFrame = NULL;
        av_packet_free(&pPacket);
        av_free(pPacket);
        pPacket = NULL;
    }

    pthread_exit(&video->thread_play);

}


void Video::play() {
    pthread_create(&this->thread_play,NULL,playVideo,this);
}

void Video::release() {

    if(this->pQueue != NULL)
    {
        delete pQueue;
        pQueue = NULL;
    }


    if(this->pCodecContext != NULL)
    {
        avcodec_close(pCodecContext);
        avcodec_free_context(&pCodecContext);
        pCodecContext = NULL;
    }

    if(playStatus != NULL)
    {
        playStatus = NULL;
    }

    if(callJava != NULL)
    {
        callJava = NULL;
    }



}