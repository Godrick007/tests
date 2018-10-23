//
// Created by Godrick Crown on 2018/10/23.
//

#ifndef TESTS_AUDIO_H
#define TESTS_AUDIO_H


extern "C" {
#include <libavcodec/avcodec.h>
};

class Audio {

public:

    int streamIndex = -1;
    AVCodecParameters *pCodecParameters = NULL;
    AVCodecContext *pCodecContext = NULL;

public:
    Audio();

    ~Audio();

};


#endif //TESTS_AUDIO_H
