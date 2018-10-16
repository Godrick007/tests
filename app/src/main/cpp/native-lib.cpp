#include <jni.h>
#include <string>
#include "pthread.h"
#include "android/log.h"
#include "unistd.h"

void func();

JavaVM *jvm;
jobject obj;

extern "C" JNIEXPORT jstring JNICALL
Java_com_gaosiedu_myapplication_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_gaosiedu_myapplication_MainActivity_nativeCall(JNIEnv *env, jobject instance) {

    jclass clz = env->GetObjectClass(instance);

    jmethodID mId = env->GetMethodID(clz, "call", "()V");

    env->CallVoidMethod(instance, mId);
}


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *r) {

    JNIEnv *env;

    jvm = vm;

    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_gaosiedu_myapplication_MainActivity_nativeCallThread(JNIEnv *env, jobject instance) {

    obj = env->NewGlobalRef(instance);

    // TODO
    pthread_t t;
    int id = pthread_create(&t, NULL, (void *(*)(void *)) (func), NULL);
    if (id != 0) {
        __android_log_print(ANDROID_LOG_ERROR, "tag", "thread create error code is %d", id);
        return;
    }
    sleep(5);
    pthread_exit(&t);

}

void func() {

    JNIEnv *env;

    int i = jvm->AttachCurrentThread(&env, 0);

    if (i) {
        __android_log_print(ANDROID_LOG_ERROR, "tag", "AttachCurrentThread error code is %d", i);
        return;
    }

    jclass clz = env->GetObjectClass(obj);

    jmethodID mId = env->GetMethodID(clz, "call1", "()V");

    env->CallVoidMethod(obj, mId);

    jvm->DetachCurrentThread();
}