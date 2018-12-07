//
// Created by Godrick Crown on 2018/12/7.
//

#include "Video.h"

Video::Video(PlayStatus *playStatus, CallJava *callJava) {
    this->playStatus = playStatus;
    this->callJava = callJava;
    pQueue = new Queue(playStatus);
}

Video::~Video() {


}
