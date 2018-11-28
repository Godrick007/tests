//
// Created by Godrick Crown on 2018/10/23.
//


#include "Audio.h"

Audio::Audio(PlayStatus *playStatus) {
    this->playStatus = playStatus;
    queue = new Queue(playStatus);
    buffer = (uint8_t *)(av_malloc(44100 * 2 * 2));
}

Audio::~Audio() {

}

void *decodePlay(void * data)
{
    Audio *instance = (Audio*)data;

    instance->resampleAudio();

    pthread_exit(&instance->thread_play);
}


void Audio::play() {

    pthread_create(&this->thread_play,NULL,decodePlay,this);

}

FILE *outFile = fopen("/storage/emulated/0/my.pcm","w");

int Audio::resampleAudio() {

    while (playStatus != NULL && !playStatus->exit)
    {
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

            fwrite(buffer,1,data_size,outFile);

            av_packet_free(&avPacket);
            av_free(avPacket);
            avPacket = NULL;
            av_frame_free(&avFrame);
            av_free(avFrame);
            avFrame = NULL;
            swr_free(&swr_ctx);
            swr_ctx = NULL;

            continue;

        }



    }

    fclose(outFile);


    return data_size;
}
