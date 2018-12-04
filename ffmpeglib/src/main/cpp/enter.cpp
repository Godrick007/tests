#include "jni.h"
#include "string.h"
#include "include/androidLog.h"
#include "pthread.h"
#include "CallJava.h"
#include "Ffmpeg.h"
#include "PlayStatus.h"
extern "C"
{
#include "include/libx264/x264.h"
#include "include/libavutil/version.h"
#include "libavcodec/avcodec.h"

}

bool nativeExit = true;

JavaVM *javaVm = NULL;
CallJava *callJava = NULL;
Ffmpeg *ffmpeg = NULL;
PlayStatus *playStatus = NULL;

pthread_t threadStart;


extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    jint result = -1;
    javaVm = vm;

    JNIEnv *env;
    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    return JNI_VERSION_1_4;

}


extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1prepared(JNIEnv *env, jobject instance,
                                                       jstring source_) {
    const char *source = env->GetStringUTFChars(source_, 0);

    if (ffmpeg == NULL) {
        if (callJava == NULL) {
            callJava = new CallJava(javaVm, env, &instance);
        }

        if(!playStatus)
        {
            playStatus = new PlayStatus();
        }

        ffmpeg = new Ffmpeg(playStatus,callJava, source);
    }

    ffmpeg->prepared();


//    env->ReleaseStringUTFChars(source_, source);
}


void *startCallback(void * data)
{
    Ffmpeg *ffmpeg = static_cast<Ffmpeg *>(data);
    ffmpeg->start();
    pthread_exit(&threadStart);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1start(JNIEnv *env, jobject instance) {

    // TODO
    if (ffmpeg != NULL) {
//        ffmpeg->start();

        pthread_create(&threadStart,NULL,startCallback,ffmpeg);

    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1resume(JNIEnv *env, jobject instance) {

    if(ffmpeg)
        ffmpeg->resume();

}

extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1pause(JNIEnv *env, jobject instance) {

    if(ffmpeg)
        ffmpeg->pause();

}

extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1stop(JNIEnv *env, jobject instance) {

    if(!nativeExit)
    {
        return;
    }

    jclass clz = env->GetObjectClass(instance);
    jmethodID jmid =  env->GetMethodID(clz,"onNativeCallNext","()V");

    nativeExit = false;

    if(ffmpeg)
        ffmpeg->release();

    delete ffmpeg;

    ffmpeg = NULL;

    if(callJava)
    {
        delete(callJava);
        callJava = NULL;
    }


    if(playStatus)
    {
        delete(playStatus);
        playStatus = NULL;
    }

    nativeExit = true;

    env->CallVoidMethod(instance,jmid);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1seek(JNIEnv *env, jobject instance, jint second) {

    if(ffmpeg){
        ffmpeg->seek(second);
    }

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1getDuration(JNIEnv *env, jobject instance) {

    // TODO

    if(ffmpeg)
    {
        return ffmpeg->duration;
    }
    return 0;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1setVolume(JNIEnv *env, jobject instance,
                                                        jint percent) {

    if(ffmpeg)
    {
        ffmpeg->setVolume(percent);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_native_1setChannel(JNIEnv *env, jobject instance,
                                                         jint channel) {

    if(ffmpeg)
    {
        ffmpeg->setChannel(channel);
    }

}