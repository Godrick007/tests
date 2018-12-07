
#include "Ffmpeg.h"


Ffmpeg::Ffmpeg(PlayStatus *playStatus,CallJava *cj, const char *url) {
    this->callJava = cj;
    this->url = url;
    this->playStatus = playStatus;
    pthread_mutex_init(&mutexInit,NULL);
    pthread_mutex_init(&mutexSeek,NULL);
}

void *decodeFfmpeg(void *data) {
    Ffmpeg *ffmpeg = (Ffmpeg *) (data);
    ffmpeg->decodeFfmpegThread();
    pthread_exit(&ffmpeg->threadDecode);
}

void Ffmpeg::prepared() {
    pthread_create(&this->threadDecode, NULL, decodeFfmpeg, this);
}


int AvContextInterruptCallback(void *context)
{
    Ffmpeg *instance = static_cast<Ffmpeg *>(context);
    if(instance->playStatus->exit)
    {
        return AVERROR_EOF;
    }
    return 0;
}


void Ffmpeg::decodeFfmpegThread() {

    pthread_mutex_lock(&mutexInit);

    av_register_all();
    avformat_network_init();

    if (avformat_open_input(&pFormatContext, this->url, NULL, NULL) != 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "open url error  -- %s", this->url);
        }
        callJava->callJavaOnError(100,"can not open url");
        exit = true;
        pthread_mutex_unlock(&mutexInit);
        return;
    }



    if (avformat_find_stream_info(pFormatContext, NULL) < 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not find stream");
        }
        callJava->callJavaOnError(101,"can find stream");
        exit = true;
        pthread_mutex_unlock(&mutexInit);
        return;
    }


    pFormatContext->interrupt_callback.callback = AvContextInterruptCallback;
    pFormatContext->interrupt_callback.opaque = this;


    for (int i = 0; i < pFormatContext->nb_streams; i++) {
        if (pFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO)
        {
            if (audio == NULL)
            {
                audio = new Audio(playStatus,pFormatContext->streams[i]->codecpar->sample_rate,callJava);
                this->audio->streamIndex = i;
                this->audio->pCodecParameters = pFormatContext->streams[i]->codecpar;
                this->audio->duration = pFormatContext->duration / AV_TIME_BASE;
                this->audio->time_base = pFormatContext->streams[i]->time_base;
                this->duration = audio->duration;
            }
        }
        else if(pFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO)
        {
            if(video == NULL)
            {
                video = new Video(playStatus,callJava);
                this->video->streamIndex = i;
                this->video->pCodecParameters = pFormatContext->streams[i]->codecpar;
                this->video->timeBase = pFormatContext->streams[i]->time_base;
            }

        }
    }

    int ret;

    if(audio)
    {
        ret = getCodecContext(audio->pCodecParameters, &audio->pCodecContext);

        if(ret != 0)
        {
//            return;
        }
    }


    if(video)
    {
        ret = getCodecContext(video->pCodecParameters, &video->pCodecContext);

        if(ret != 0)
        {
//            return;
        }
    }






    callJava->callJavaOnPreparedThread();
    pthread_mutex_unlock(&mutexInit);
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
    audio->play();


    while (playStatus != NULL && !playStatus->exit) {




        if(playStatus->seek)
        {
            av_usleep(1000 * 100);
            continue;
        }

        if(audio->queue->getQueueSize() > 100)
        {
            av_usleep(1000 * 100);
            continue;
        }


        AVPacket *pPacket = av_packet_alloc();

        pthread_mutex_lock(&mutexSeek);

        int ret = av_read_frame(this->pFormatContext, pPacket);

        pthread_mutex_unlock(&mutexSeek);

        if ( ret == 0)
        {

            if (pPacket->stream_index == audio->streamIndex)
            {
                //解码操作
                audio->queue->putAvPacket(pPacket);
            }
            else if(pPacket->stream_index == video->streamIndex)
            {
                video->pQueue->putAvPacket(pPacket);
                LOGE("ffmepg","av packet video");
            }
            else
            {
                av_packet_free(&pPacket);
                av_free(pPacket);
            }


        }
        else
        {

            av_packet_free(&pPacket);
            av_free(pPacket);
            pPacket = NULL;

            //这一步操作是 不可以直接break 缓存中可能还有数据,会造成缓存数据没有播放而提前退出的问题
            while (playStatus != NULL && !playStatus->exit)
            {
                if(audio->queue->getQueueSize() > 0)
                {
                    av_usleep(1000 * 100);
                    continue;
                } else{
                    playStatus->exit = true;
                    break;
                }
            }

            break;
        }

    }

    exit = true;

    if(callJava)
    {
        callJava->callJavaOnComplete();
    }


}

void Ffmpeg::pause() {

    if(audio)
        audio->pause();
}

void Ffmpeg::resume() {

    if(audio)
        audio->resume();

}

void Ffmpeg::stop() {

}

void Ffmpeg::release() {


    playStatus->exit = true;


    pthread_mutex_lock(&mutexInit);


    int sleepCount = 0;

    while(!exit)
    {
        if(sleepCount > 1000)
        {
            exit = true;
        }

        if(LOG_DEBUG)
        {
            LOGE("ffmpeg","wait ffmpeg exit %d",sleepCount);
        }

        sleepCount ++ ;

        av_usleep(1000 * 10);

    }

    if(audio)
    {
        audio->release();
        delete audio;
        audio = NULL;
    }

    if(pFormatContext)
    {
        avformat_close_input(&pFormatContext);
        avformat_free_context(pFormatContext);
        pFormatContext = NULL;
    }

    if(playStatus)
    {
        playStatus = NULL;
    }


    if(callJava)
    {
        callJava = NULL;
    }

    pthread_mutex_unlock(&mutexInit);

    pthread_mutex_destroy(&mutexInit);
}

Ffmpeg::~Ffmpeg() {
    LOGE("release","Ffmpeg's release is called");
    pthread_mutex_destroy(&mutexSeek);
    pthread_mutex_destroy(&mutexInit);
}

void Ffmpeg::seek(int64_t second) {

    if(duration <= 0)
    {
        return;
    }

    if(second <=0 || second >= duration)
    {
        return;
    }

    if(audio != NULL)
    {
        playStatus->seek = true;
        audio->queue->clearAVPacket();
        audio->clock = 0;
        audio->last_time = 0;

        pthread_mutex_lock(&mutexSeek);

        int64_t rel = second * AV_TIME_BASE;

        avcodec_flush_buffers(audio->pCodecContext);

        avformat_seek_file(pFormatContext,-1,INT64_MIN,rel,INT64_MAX,0);



        pthread_mutex_unlock(&mutexSeek);
        playStatus->seek = false;
    }

}

void Ffmpeg::setVolume(int percent) {

    if(audio)
    {
        audio->setVolume(percent);
    }

}

void Ffmpeg::setChannel(int channel) {

    if(audio)
    {
        audio->switchChannel(channel);
    }

}

void Ffmpeg::setSpeed(float speed) {
    if(audio)
    {
        audio->setSpeed(speed);
    }
}

void Ffmpeg::setPitch(float pitch) {

    if(audio)
    {
        audio->setPitch(pitch);
    }
}

int Ffmpeg::getSampleRate() {

    if(audio)
    {
        return audio->pCodecContext->sample_rate;
    }
    return 0;
}

void Ffmpeg::startStopRecord(bool state) {
    if(audio)
    {
        audio->startStopRecord(state);
    }
}

int Ffmpeg::getCodecContext(AVCodecParameters *codecParameters, AVCodecContext **codecContext) {

    AVCodec *pCodec = avcodec_find_decoder(codecParameters->codec_id);

    if (!pCodec) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not find decoder");
        }
        callJava->callJavaOnError(102,"can not find decoder");
        exit = true;
        pthread_mutex_unlock(&mutexInit);
        return -1;
    }

    *codecContext = avcodec_alloc_context3(pCodec);

    if (*codecContext)
    {
        if (LOG_DEBUG)
        {
            LOGE("ffmpeg", "can not alloc decoder context");
        }
        callJava->callJavaOnError(103,"can not alloc decoder context");
        exit = true;
        pthread_mutex_unlock(&mutexInit);
        return -1;
    }

    if (avcodec_parameters_to_context(*codecContext, codecParameters) < 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not fill decoder context");
        }
        callJava->callJavaOnError(104,"can not fill decoder context");
        exit = true;
        pthread_mutex_unlock(&mutexInit);
        return -1;
    }

    if (avcodec_open2(*codecContext, pCodec, 0) != 0) {
        if (LOG_DEBUG) {
            LOGE("ffmpeg", "can not open audio stream");
        }
        callJava->callJavaOnError(105,"can not open audio stream");
        exit = true;
        pthread_mutex_unlock(&mutexInit);
        return -1;
    }



    return 0;
}

