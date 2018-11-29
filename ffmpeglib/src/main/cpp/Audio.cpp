//
// Created by Godrick Crown on 2018/10/23.
//


#include <cassert>
#include "Audio.h"

Audio::Audio(PlayStatus *playStatus,int sample_rate,CallJava *callJava) {
    this->playStatus = playStatus;
    this->sample_rate = sample_rate;
    this->callJava = callJava;
    queue = new Queue(playStatus);
    buffer = (uint8_t *)(av_malloc(sample_rate * 2 * 2));
}

Audio::~Audio() {
    LOGE("release","Audio's release is called");
    release();
}

void *decodePlay(void * data)
{
    Audio *instance = (Audio*)data;

//    instance->resampleAudio();
    instance->initSLES();

    pthread_exit(&instance->thread_play);
}


void Audio::play() {

    pthread_create(&this->thread_play,NULL,decodePlay,this);

}


int Audio::resampleAudio() {

    while (playStatus != NULL && !playStatus->exit)
    {

        //判断当前队列状态,如果是0 就说明没有数据可以播放
        if(queue->getQueueSize() == 0)
        {
            if(!playStatus->load)
            {
                playStatus->load = true;
                callJava->callJavaOnLoad(true);
            }
            continue;
        }
        else
        {
            if(playStatus->load)
            {
                playStatus->load = false;
                callJava->callJavaOnLoad(false);
            }
        }


        //为avpacket 分配内存空间
        avPacket = av_packet_alloc();

        //从队列中获取
        if(queue->getAvPacket(avPacket) != 0)
        {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            continue;
        }

        //解码流程
        //解码对象 将数据发送到解码器
        ret = avcodec_send_packet(pCodecContext,avPacket);
        if(ret != 0)
        {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            continue;
        }

        //接受对象 从解码器中获取数据
        avFrame = av_frame_alloc();
        ret = avcodec_receive_frame(pCodecContext,avFrame);
        if(ret != 0)
        {
            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            continue;
        }
        else
        {
            //重采样

            //channel 是声道数                  声道布局
            if(avFrame->channels > 0 && avFrame->channel_layout == 0)
            {
                //通过声道数来设置声道布局
                avFrame->channel_layout = av_get_default_channel_layout(avFrame->channels);
            }
            else
            {
                //根据声道布局获取声道数(异常情况)
                avFrame->channels = av_get_channel_layout_nb_channels(avFrame->channel_layout);
            }

            //重采样 上下文对象
            SwrContext *swr_ctx;

            swr_ctx = swr_alloc_set_opts(
                    NULL,                             //本身
                    AV_CH_LAYOUT_STEREO,              //输出声道布局  (立体声)
                    AV_SAMPLE_FMT_S16,                //输出重采样位数
                    avFrame->sample_rate,             //输出采样率
                    avFrame->channel_layout,          //输入声道布局
                    (AVSampleFormat)(avFrame->format),//输入重采样位数
                    avFrame->sample_rate,             //输入重采样采样率
                    NULL,                             //
                    NULL                              //
                    );

            if(!swr_ctx || swr_init(swr_ctx) < 0)
            {
                av_packet_free(&avPacket);
                av_free(avPacket);
                avPacket = NULL;
                av_frame_free(&avFrame);
                av_free(avFrame);
                avFrame = NULL;
                if(swr_ctx != NULL)
                {
                    swr_free(&swr_ctx);
                }
                continue;
            }

            //获取到转换之后的buffer
            int nb = swr_convert(
                    swr_ctx,
                    &buffer, //转码后的输出pcm数据大小
                    avFrame->nb_samples, //输出采样个数
                    (const uint8_t **)(avFrame->data), // 原始数据
                    avFrame->nb_samples //输入采样个数
            );

            //获取channels
            int out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);


            //
            data_size = nb * out_channels * av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);

//            fwrite(buffer,1,data_size,q);
//            this->buffer = buffer;

            now_time = avFrame->pts * av_q2d(time_base);

            if(now_time < clock)
                now_time = clock;
            clock = now_time;

            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            swr_free(&swr_ctx);
            swr_ctx = NULL;

            break;

        }



    }

//    fclose(outFile);


    return data_size;
}

void pcmBufferCallback(SLAndroidSimpleBufferQueueItf queue,void *context)
{

    Audio *instance = static_cast<Audio *>(context);


//    instance->play();

    if(instance != NULL)
    {
        int bufferSize = instance->resampleAudio();
        if(bufferSize > 0)
        {
            instance->clock += bufferSize / ((double) instance->sample_rate * 2 * 2);

            if(instance->clock - instance->last_time >= 0.1)
            {
                instance->last_time = instance->clock;
                instance->callJava->callJavaOnProgress(instance->clock,instance->duration);
            }



            (*instance->pcmBufferQueue)->Enqueue(instance->pcmBufferQueue,instance->buffer,bufferSize);
        }
    }

}


void Audio::initSLES() {

    SLresult result;

    //engine
    result = slCreateEngine(&engineObject,0, NULL, 0, NULL, NULL);
    LOGE("ffmpeg","slCreateEngine is %d",result);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;


    result = (*engineObject)->Realize(engineObject,SL_BOOLEAN_FALSE);
    LOGE("ffmpeg","engine Realize is %d",result);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    result = (*engineObject)->GetInterface(engineObject,SL_IID_ENGINE,&engineEngine);
    LOGE("ffmpeg","engine get interface is %d",result);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    //mix
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};

    result =(*engineEngine)->CreateOutputMix(engineEngine,&outputMixObject,1,ids,req);
    LOGE("ffmpeg","CreateOutputMix is %d",result);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    result =(*outputMixObject)->Realize(outputMixObject,SL_BOOLEAN_FALSE);
    LOGE("ffmpeg","mix realize is %d",result);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;





    result = (*outputMixObject)->GetInterface(outputMixObject,SL_IID_ENVIRONMENTALREVERB,&outputMixEnvironmentReverb);

    LOGE("ffmpeg","output mix object GetInterface is %d",result);

    assert(result == SL_RESULT_SUCCESS);
    (void)result;


    result = (*outputMixEnvironmentReverb)->SetEnvironmentalReverbProperties(outputMixEnvironmentReverb,&reverbSettings);

    LOGE("ffmpeg","SetEnvironmentalReverbProperties is %d",result);

    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX,outputMixObject};

    //data source
    SLDataLocator_AndroidSimpleBufferQueue android_queue = {
            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
            2
    };

    SLDataFormat_PCM pcm={
            SL_DATAFORMAT_PCM,//播放pcm格式的数据
            2,//2个声道（立体声）
            static_cast<SLuint32>(getCurrentSampleRateForOpenSLES(sample_rate)),//44100hz的频率
            SL_PCMSAMPLEFORMAT_FIXED_16,//位数 16位
            SL_PCMSAMPLEFORMAT_FIXED_16,//和位数一致就行
            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,//立体声（前左前右）
            SL_BYTEORDER_LITTLEENDIAN//结束标志
    };

    SLDataSource slDataSource = {
            &android_queue,
            &pcm
    };

    SLDataSink audioSink = {&outputMix,NULL};

    //player
    const SLInterfaceID id[1] = {SL_IID_BUFFERQUEUE};
    const SLboolean bools[1] = {SL_BOOLEAN_TRUE};


    result = (*engineEngine)->CreateAudioPlayer(engineEngine,&pcmPlayerObject,&slDataSource,&audioSink,1,id,bools);
    LOGE("ffmpeg","CreateAudioPlayer is %d",result);

    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    result = (*pcmPlayerObject)->Realize(pcmPlayerObject,SL_BOOLEAN_FALSE);
    LOGE("ffmpeg","CreateAudioPlayer realize is %d",result);

    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    result = (*pcmPlayerObject)->GetInterface(pcmPlayerObject,SL_IID_PLAY,&pcmPlayerPlay);
    LOGE("ffmpeg","CreateAudioPlayer GetInterface is %d",result);

    assert(result == SL_RESULT_SUCCESS);
    (void)result;



    //player state
    result = (*pcmPlayerObject)->GetInterface(pcmPlayerObject,SL_IID_BUFFERQUEUE,&pcmBufferQueue);

    LOGE("ffmpeg","CreateAudioPlayer GetInterface buffer queue is %d",result);

    assert(result == SL_RESULT_SUCCESS);
    (void)result;

    result = (*pcmBufferQueue)->RegisterCallback(pcmBufferQueue, pcmBufferCallback, this);

    LOGE("ffmpeg","RegisterCallback is %d",result);

    assert(result == SL_RESULT_SUCCESS);
    (void)result;


    result = (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay,SL_PLAYSTATE_PLAYING);

    LOGE("ffmpeg","SetPlayState is %d",result);

    assert(result == SL_RESULT_SUCCESS);
    (void)result;


    pcmBufferCallback(pcmBufferQueue,this);

}

int Audio::getCurrentSampleRateForOpenSLES(int sample_rate) {

    int rate = 0;

    switch (sample_rate)
    {
        case 8000:
            rate = SL_SAMPLINGRATE_8;
            break;
        case 11025:
            rate = SL_SAMPLINGRATE_11_025;
            break;
        case 12000:
            rate = SL_SAMPLINGRATE_12;
            break;
        case 16000:
            rate = SL_SAMPLINGRATE_16;
            break;
        case 22050:
            rate = SL_SAMPLINGRATE_22_05;
            break;
        case 24000:
            rate = SL_SAMPLINGRATE_24;
            break;
        case 32000:
            rate = SL_SAMPLINGRATE_32;
            break;
        case 44100:
            rate = SL_SAMPLINGRATE_44_1;
            break;
        case 48000:
            rate = SL_SAMPLINGRATE_48;
            break;
        case 64000:
            rate = SL_SAMPLINGRATE_64;
            break;
        case 88200:
            rate = SL_SAMPLINGRATE_88_2;
            break;
        case 192000:
            rate = SL_SAMPLINGRATE_192;
            break;
        default:
            rate = SL_SAMPLINGRATE_44_1;

    }


    return rate;
}

void Audio::pause() {

    if(pcmPlayerPlay)
    (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay,SL_PLAYSTATE_PAUSED);

}

void Audio::resume() {

    if(pcmPlayerPlay)
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay,SL_PLAYSTATE_PLAYING);

}

void Audio::stop() {
    if(pcmPlayerPlay)
        (*pcmPlayerPlay)->SetPlayState(pcmPlayerPlay,SL_PLAYSTATE_STOPPED);
}

void Audio::release() {

    stop();
    if(queue)
    {
        delete queue;
        queue = NULL;
    }

    if(pcmPlayerObject)
    {
        (*pcmPlayerObject)->Destroy(pcmPlayerObject);
        pcmPlayerObject = NULL;
        pcmPlayerPlay = NULL;
        pcmBufferQueue = NULL;
    }


    if(outputMixObject)
    {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentReverb = NULL;
    }

    if(engineObject)
    {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

    if(buffer)
    {
        free(buffer);
        buffer = NULL;
    }

    if(pCodecContext)
    {
        avcodec_close(pCodecContext);
        avcodec_free_context(&pCodecContext);
        pCodecContext = NULL;

    }

    if(playStatus)
    {
        playStatus = NULL;
    }

    if(callJava)
    {
        callJava = NULL;
    }


}
