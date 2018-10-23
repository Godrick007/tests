#include "jni.h"
#include "string.h"
#include "include/androidLog.h"
#include "pthread.h"
#include "CallJava.h"
#include "Ffmpeg.h"

extern "C"
{
#include "include/libx264/x264.h"
#include "include/libavutil/version.h"
#include "libavcodec/avcodec.h"

}


JavaVM *javaVm = NULL;
CallJava *callJava = NULL;
Ffmpeg *ffmpeg = NULL;

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {

    jint result = -1;
    javaVm = vm;

    JNIEnv *env;
    if (vm->GetEnv((void **) (env), JNI_VERSION_1_4) != JNI_OK) {
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
        ffmpeg = new Ffmpeg(callJava, source);
    }

    ffmpeg->prepared();


    env->ReleaseStringUTFChars(source_, source);
}