//
// Created by Godrick Crown on 2018/10/17.
//

#include "android/log.h"

#ifndef TESTS_ANDROIDLOG_H
#define TESTS_ANDROIDLOG_H


#endif //TESTS_ANDROIDLOG_H

#define LOG_DEBUG 1

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,__VA_ARGS__)
