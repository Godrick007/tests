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
    mid_onVolumeDB = jniEnv->GetMethodID(clz,"onNativeCallVolumeDB","(I)V");
    mid_pcm2AAC = jniEnv->GetMethodID(clz,"onNativeCallEncodePCM2AAC","(I[B)V");
    mid_yuv = jniEnv->GetMethodID(clz,"onNativeCallRenderYUV","(II[B[B[B)V");
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

void CallJava::callJavaOnValueDbUIThread(int db) {
    this->jniEnv->CallVoidMethod(jobj, mid_onVolumeDB,db);
}

void CallJava::callJavaPCM2AACUIThread(int size, const void *buffer) {

    jbyteArray bytes = this->jniEnv->NewByteArray(size);
    this->jniEnv->SetByteArrayRegion(bytes, 0, size, (jbyte *)buffer);
    this->jniEnv->CallVoidMethod(this->jobj, mid_pcm2AAC,size,bytes);
    this->jniEnv->DeleteLocalRef(bytes);

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



void CallJava::callJavaOnError(int code, const char *msg) {
    JNIEnv *env;

    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

//    LOGE("callJavaOnError",msg,"");

//    printf(msg,"");


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

void CallJava::callJavaOnValueDb(int db) {

    JNIEnv *env;
    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

    env->CallVoidMethod(this->jobj, mid_onVolumeDB,db);

    jvm->DetachCurrentThread();

}

void CallJava::callJavaPCM2AAC(int size, const void *buffer) {

    JNIEnv *env;
    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

    jbyteArray bytes = env->NewByteArray(size);
    env->SetByteArrayRegion(bytes, 0, size, (jbyte *)buffer);

    env->CallVoidMethod(this->jobj, mid_pcm2AAC,size,bytes);

    env->DeleteLocalRef(bytes);

    jvm->DetachCurrentThread();

}

void CallJava::callJavaYUVDataUIThread(int width,int height,uint8_t *fy,uint8_t *fu,uint8_t *fv) {

}

void CallJava::callJavaYUVData(int width,int height,uint8_t *fy,uint8_t *fu,uint8_t *fv) {

    JNIEnv *env;
    if(jvm->AttachCurrentThread(&env, 0 )!= JNI_OK){
        if(LOG_DEBUG){
            LOGD("Ffmpeg","get thread jniEnv error");
        }
        return;
    }

    jbyteArray y = env->NewByteArray(width * height);
    jbyteArray u = env->NewByteArray(width * height / 4);
    jbyteArray v = env->NewByteArray(width * height / 4);

    env->SetByteArrayRegion(y, 0, width * height, reinterpret_cast<const jbyte *>(fy));
    env->SetByteArrayRegion(u, 0, width * height / 4, reinterpret_cast<const jbyte *>(fu));
    env->SetByteArrayRegion(v, 0, width * height / 4 , reinterpret_cast<const jbyte *>(fv));


    env->CallVoidMethod(this->jobj, mid_yuv,width,height,y,u,v);

    env->DeleteLocalRef(y);
    env->DeleteLocalRef(u);
    env->DeleteLocalRef(v);

    jvm->DetachCurrentThread();

}




