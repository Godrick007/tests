#include "Ffmpeg.h"


Ffmpeg::Ffmpeg(CallJava *cj, const char *url) {
    this->callJava = cj;
    this->url = url;
}

void *decodeFfmpeg(void *data) {
    Ffmpeg *ffmpeg = (Ffmpeg *) (data);
    ffmpeg->decodeFfmpegThread();
    pthread_exit(&ffmpeg->threadDecode);
}

void Ffmpeg::prepared() {
    pthread_create(&this->threadDecode, NULL, decodeFfmpeg, this);
}

void Ffmpeg::decodeFfmpegThread() {

    av_register_all();
    avformat_network_init();

    if (avformat_open_input(&pFormatContext, this->url, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "open url error  -- %s", this->url);
        }
        return;
    }

    if (avformat_find_stream_info(pFormatContext, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not find stream");
        }
        return;
    }

    for (int i = 0; i < pFormatContext->nb_streams; i++) {
        if (pFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            if (audio == NULL) {
                audio = new Audio();
            }
            this->audio->streamIndex = i;
            this->audio->pCodecParameters = pFormatContext->streams[i]->codecpar;
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
            LOGE("ffmpeg", "can not alloc decoder context");
        }
        return;
    }

    if (avcodec_parameters_to_context(audio->pCodecContext, audio->pCodecParameters) < 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not fill decoder context");
        }
        return;
    }

    if (avcodec_open2(audio->pCodecContext, pCodec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not open audio stream");
        }
        return;
    }

    callJava->callJavaOnpreparedThread();
}

void Ffmpeg::setAudio(Audio *audio) {
    this->audio = audio;
}

void Ffmpeg::start() {

    if (audio == NULL) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "audio is null");
        }
        return;
    }

    int count = 0;

    while (1) {

        AVPacket *pPacket = av_packet_alloc();

        if (av_read_frame(this->pFormatContext, pPacket) == 0) {

            if (pPacket->stream_index == audio->streamIndex) {
                count++;
                if (LOG_DEBUG) {
                    LOGE("ffmpeg", "decode %d frame",count);
                }
            }else{
                av_packet_free(&pPacket);
                av_free(pPacket);
                pPacket = NULL;
            }


        } else {

            av_packet_free(&pPacket);
            av_free(pPacket);
            pPacket = NULL;
            break;
        }

    }

}
