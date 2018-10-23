//
// Created by Godrick Crown on 2018/10/23.
//


#include "Ffmpeg.h"


Ffmpeg::Ffmpeg(CallJava *cj, const char *url) {

    this->callJava = cj;
    this->url = url;


}

void *decodeFfmpeg(void *data) {

    Ffmpeg *ffmpeg = (Ffmpeg *) (data);

    ffmpeg->decodeFfmpegThread();


    pthread_exit(ffmpeg->threadDecode);

}


void Ffmpeg::prepared() {

    pthread_create(this->threadDecode, NULL, decodeFfmpeg, this);

}

void Ffmpeg::decodeFfmpegThread() {


    av_register_all();
    avformat_network_init();

    pFormatContext = avformat_alloc_context();

    if (avformat_open_input(&pFormatContext, url, NULL, NULL) != 0) {

        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not open url %s", url);
        }
        return;
    }

    if (avformat_find_stream_info(pFormatContext, NULL) < 0) {

        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not find stream info");
        }
        return;
    }

    for (int i = 0; i < pFormatContext->nb_streams; i++) {
        if (pFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {

            if (audio == NULL) {
                audio = new Audio();
                audio->streamIndex = i;
                audio->pCodecParameters = pFormatContext->streams[i]->codecpar;
            }

        }
    }


    AVCodec *pCodec = avcodec_find_decoder(audio->pCodecParameters->codec_id);

    if (!pCodec) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not find decoder");
        }
        return;
    }


    audio->pCodecContext = avcodec_alloc_context3(pCodec);

    if (!audio->pCodecContext) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not alloc codec decoder");
        }
        return;
    }


    if (avcodec_parameters_to_context(audio->pCodecContext, audio->pCodecParameters) < 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not fill decoder");
        }
        return;
    }


    if (avcodec_open2(audio->pCodecContext, pCodec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not open audio streams");
        }
        return;
    }


    callJava->callJavaOnpreparedThread();


}

void Ffmpeg::setAudio(Audio *audio) {
    this->audio = audio;
}
