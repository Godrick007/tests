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

public:
    CallJava(JavaVM *jvm, JNIEnv *jniEnv,jobject *obj);
    ~CallJava();

    void callJavaOnpreparedUIThread();
    void callJavaOnpreparedThread();
};


#endif //TESTS_CALLJAVA_H
