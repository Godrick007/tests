//
// Created by Godrick Crown on 2018/12/7.
//



#include "Video.h"

Video::Video(PlayStatus *playStatus, CallJava *callJava) {
    this->playStatus = playStatus;
    this->callJava = callJava;
    pQueue = new Queue(playStatus);
    pthread_mutex_init(&mutex_codec,NULL);
}

Video::~Video()
{
    pthread_mutex_destroy(&mutex_codec);
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

        if(video->playStatus->pause)
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


        if(video->codecType == CODEC_MEDIA_CODEC)
        {
            av_packet_free(&pPacket);
            av_free(pPacket);
            pPacket = NULL;


        }
        else if(video->codecType == CODEC_YUV)
        {
            pthread_mutex_lock(&video->mutex_codec);
            if(avcodec_send_packet(video->pCodecContext,pPacket) != 0)
            {
                av_packet_free(&pPacket);
                av_free(pPacket);
                pPacket = NULL;
                pthread_mutex_unlock(&video->mutex_codec);
                continue;
            }

            AVFrame *pFrame = av_frame_alloc();

            if(avcodec_receive_frame(video->pCodecContext,pFrame) != 0)
            {
                av_frame_free(&pFrame);
                av_free(pFrame);
                pFrame = NULL;
                av_packet_free(&pPacket);
                av_free(pPacket);
                pPacket = NULL;
                pthread_mutex_unlock(&video->mutex_codec);
                continue;
            }


            //success get a packet

            if(pFrame->format == AV_PIX_FMT_YUV420P)
            {


                double diff = video->getFrameDiffTime(pFrame);

                if(LOG_DEBUG)
                {
//                LOGE("video","diff time is %f",diff);
                }

                av_usleep(video->getDelayTime(diff) * AV_TIME_BASE);



                // render
                video->callJava->callJavaYUVData(
                        video->pCodecContext->width,
                        video->pCodecContext->height,
                        pFrame->data[0],
                        pFrame->data[1],
                        pFrame->data[2]
                );
            }
            else
            {
                //use scale turn to yuv420p

                AVFrame *pFrameYUV420P = av_frame_alloc();

                int num = av_image_get_buffer_size(
                        AV_PIX_FMT_YUV420P,
                        video->pCodecContext->width,
                        video->pCodecContext->height,
                        1
                );

                uint8_t *buffer = static_cast<uint8_t *>(av_malloc(num * sizeof(uint8_t)));

                av_image_fill_arrays(
                        pFrameYUV420P->data,
                        pFrameYUV420P->linesize,
                        buffer,
                        AV_PIX_FMT_YUV420P,
                        video->pCodecContext->width,
                        video->pCodecContext->height,
                        1);

                SwsContext *sws_ctx = sws_getContext(
                        video->pCodecContext->width,
                        video->pCodecContext->height,
                        video->pCodecContext->pix_fmt,
                        video->pCodecContext->width,
                        video->pCodecContext->height,
                        AV_PIX_FMT_YUV420P,
                        SWS_BICUBIC,NULL,NULL,NULL
                );

                if(!sws_ctx)
                {

                    av_frame_free(&pFrameYUV420P);
                    av_free(pFrameYUV420P);
                    av_free(buffer);
                    pthread_mutex_unlock(&video->mutex_codec);
                    continue;
                }

                sws_scale(
                        sws_ctx,
                        pFrame->data,
                        pFrame->linesize,
                        0,
                        pFrame->height,
                        pFrameYUV420P->data,
                        pFrameYUV420P->linesize
                );

                //turn over and callback to application layer

                if(LOG_DEBUG)
                {
//                LOGE("video","this is NOT a yuv data");
                }

                double diff = video->getFrameDiffTime(pFrame);

                if(LOG_DEBUG)
                {
//                LOGE("video","diff time is %f",diff);
                }

                av_usleep(video->getDelayTime(diff) * AV_TIME_BASE);

                video->callJava->callJavaYUVData(
                        video->pCodecContext->width,
                        video->pCodecContext->height,
                        pFrameYUV420P->data[0],
                        pFrameYUV420P->data[1],
                        pFrameYUV420P->data[2]
                );



                av_frame_free(&pFrameYUV420P);
                av_free(pFrameYUV420P);
                av_free(buffer);
                sws_freeContext(sws_ctx);


            }



            av_frame_free(&pFrame);
            av_free(pFrame);
            pFrame = NULL;
            av_packet_free(&pPacket);
            av_free(pPacket);
            pPacket = NULL;
            pthread_mutex_unlock(&video->mutex_codec);
        }



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
        pthread_mutex_lock(&mutex_codec);
        avcodec_close(pCodecContext);
        avcodec_free_context(&pCodecContext);
        pCodecContext = NULL;
        pthread_mutex_unlock(&mutex_codec);
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

double Video::getFrameDiffTime(AVFrame *avFrame)
{


    double pts = av_frame_get_best_effort_timestamp(avFrame);

    if(pts == AV_NOPTS_VALUE)
    {
        pts = 0;
    }

    pts *= av_q2d(timeBase);

    if(pts > 0)
    {
        clock = pts;
    }

    double diff = audio->clock - clock;


    return diff;
}

double Video::getDelayTime(double diff) {

    if(diff > 0.003)
    {
        delayTime = delayTime * 2 / 3;

        if(delayTime < defaultDelayTime / 2)
        {
            delayTime = defaultDelayTime * 2 / 3;
        }
        else if(delayTime > defaultDelayTime * 2)
        {
            delayTime = defaultDelayTime * 2;
        }
    }
    else if(diff < -0.003)
    {
        delayTime = delayTime * 3 / 2;

        if(delayTime < defaultDelayTime / 2)
        {
            delayTime = defaultDelayTime * 2 / 3;
        }
        else if(delayTime > defaultDelayTime * 2)
        {
            delayTime = defaultDelayTime * 2;
        }

    }
    else if(diff == 0.003)
    {

    }

    if(diff >= 0.5)
    {
        delayTime = 0;
    }
    else if(diff <= -0.5)
    {
        delayTime = defaultDelayTime * 2;
    }

    if(fabs(diff) >= 10)
    {
        delayTime = defaultDelayTime;
    }


    return delayTime;
}
