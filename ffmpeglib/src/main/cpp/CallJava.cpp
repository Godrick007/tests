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

}

CallJava::~CallJava() {


}

void CallJava::callJavaOnpreparedThread() {

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

void CallJava::callJavaOnpreparedUIThread() {
    this->jniEnv->CallVoidMethod(jobj, mid_prepared);
}
