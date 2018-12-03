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
    void callJavaOnError(int code,char* msg);
    void callJavaOnCompleteUIThread();
    void callJavaOnComplete();
};


#endif //TESTS_CALLJAVA_H
