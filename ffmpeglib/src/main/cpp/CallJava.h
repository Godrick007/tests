//
// Created by Godrick Crown on 2018/10/23.
//

#ifndef TESTS_CALLJAVA_H
#define TESTS_CALLJAVA_H

#include <cwchar>
#include "jni.h"
#include "androidLog.h"

class CallJava {

public:
    JavaVM *jvm = NULL;
    JNIEnv *jniEnv = NULL;
    jobject jobj;

    jmethodID mid_prepared;
    jmethodID mid_onLoad;
    jmethodID mid_onProgress;
    jmethodID mid_onError;
    jmethodID mid_onComplete;
    jmethodID mid_onVolumeDB;
    jmethodID mid_pcm2AAC;
    jmethodID mid_yuv;
    jmethodID mid_support_video;

public:
    CallJava(JavaVM *jvm, JNIEnv *jniEnv,jobject *obj);
    ~CallJava();

    void callJavaOnPreparedUIThread();
    void callJavaOnPreparedThread();
    void callJavaOnLoadUIThread(bool load);
    void callJavaOnLoad(bool load);
    void callJavaOnProgressUIThread(int current,int total);
    void callJavaOnProgress(int current,int total);
    void callJavaOnErrorUIThread(int code,char* msg);
    void callJavaOnError(int code,const char* msg);
    void callJavaOnCompleteUIThread();
    void callJavaOnComplete();
    void callJavaOnValueDbUIThread(int db);
    void callJavaOnValueDb(int db);
    void callJavaPCM2AACUIThread(int size, const void *buffer);
    void callJavaPCM2AAC(int size, const void *buffer);
    void callJavaYUVDataUIThread(int width,int height,uint8_t *fy,uint8_t *fu,uint8_t *fv);
    void callJavaYUVData(int width,int height,uint8_t *fy,uint8_t *fu,uint8_t *fv);

    bool callJavaCheckSupportVideoUIThread(const char *codeName);
    bool callJavaCheckSupportVideo(const char *codeName);
};


#endif //TESTS_CALLJAVA_H
