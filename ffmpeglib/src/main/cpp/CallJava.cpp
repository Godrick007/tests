//
// Created by Godrick Crown on 2018/10/23.
//

#include "CallJava.h"

CallJava::CallJava(JavaVM *jvm, JNIEnv *jniEnv, jobject *obj) {

    this->jvm = jvm;
    this->jniEnv = jniEnv;
    this->jobj = jniEnv->NewGlobalRef(*obj);


    jclass clz = jniEnv->GetObjectClass(*obj);

    if (!clz) {
        if (LOG_DEBUG) {
            LOGE("Ffmpeg", "get jclass error");
        }
        return;
    }

    mid_prepared = jniEnv->GetMethodID(clz, "onNativeCallPrepared", "()V");
    mid_onLoad = jniEnv->GetMethodID(clz,"onNativeCallLoad","(Z)V");
    mid_onProgress = jniEnv->GetMethodID(clz,"onNativeCallProgress","(II)V");
    mid_onError = jniEnv->GetMethodID(clz,"onNativeCallError","(ILjava/lang/String;)V");
    mid_onComplete = jniEnv->GetMethodID(clz,"onNativeCallComplete","()V");
}

CallJava::~CallJava() {
    LOGE("release","CallJava's release is called");

}


void CallJava::callJavaOnPreparedUIThread() {
    this->jniEnv->CallVoidMethod(jobj, mid_prepared);
}

void CallJava::callJavaOnLoadUIThread(bool load) {
    this->jniEnv->CallVoidMethod(jobj, mid_onLoad,load);
}

void CallJava::callJavaOnProgressUIThread(int current, int total) {
    this->jniEnv->CallVoidMethod(jobj, mid_onLoad,current,total);
}

void CallJava::callJavaOnErrorUIThread(int code, char* msg) {
    jstring jmsg = jniEnv->NewStringUTF(msg);
    this->jniEnv->CallVoidMethod(jobj, mid_onError,code,msg);
    this->jniEnv->DeleteLocalRef(jmsg);
}

void CallJava::callJavaOnCompleteUIThread() {
    this->jniEnv->CallVoidMethod(jobj, mid_onComplete);
}

void CallJava::callJavaOnProgress(int current, int total) {

    JNIEnv *env;
    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

    env->CallVoidMethod(this->jobj, mid_onProgress,current,total);

    jvm->DetachCurrentThread();
}

void CallJava::callJavaOnLoad(bool load) {
    JNIEnv *env;
    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

    env->CallVoidMethod(this->jobj, mid_onLoad,load);

    jvm->DetachCurrentThread();
}

void CallJava::callJavaOnPreparedThread() {

    JNIEnv *env;
    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

    env->CallVoidMethod(this->jobj, mid_prepared);

    jvm->DetachCurrentThread();
}



void CallJava::callJavaOnError(int code, char *msg) {
    JNIEnv *env;

    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

    jstring jmsg = jniEnv->NewStringUTF(msg);
    env->CallVoidMethod(this->jobj, mid_onError,code,jmsg);
    env->DeleteLocalRef(jmsg);


    jvm->DetachCurrentThread();
}



void CallJava::callJavaOnComplete() {

    JNIEnv *env;
    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

    env->CallVoidMethod(this->jobj, mid_onComplete);

    jvm->DetachCurrentThread();

}
