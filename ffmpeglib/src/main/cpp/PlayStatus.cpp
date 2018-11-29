//
// Created by Godrick Crown on 2018/11/28.
//

#include <androidLog.h>
#include "PlayStatus.h"

PlayStatus::PlayStatus() {
    exit = false;
}

PlayStatus::~PlayStatus() {
    LOGE("release","PlayStatus's release is called");
}
