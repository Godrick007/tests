#include "jni.h"
#include "string.h"
#include "include/androidLog.h"

extern "C"
{
#include "include/libx264/x264.h"
#include "include/libavutil/version.h"
#include "libavcodec/avcodec.h"

}

JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_nativeCodeTest(JNIEnv *env, jobject instance) {

    // TODO
    LOGE("ffmpeg", "ffmpeg's version is %d", LIBAVUTIL_VERSION_INT);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_godrick_ffmpeglib_NativeTest_getString(JNIEnv *env, jobject instance) {

//    // TODO
//    char *returnValue = "cool budi";
//
//    avcodec_register_all();
//
//    AVCodec *codec = av_codec_next(NULL);
//
//    if (codec != NULL) {
//        return env->NewStringUTF(codec->name);
//    }
//
//    return env->NewStringUTF(returnValue);

    char info[40000] = {0};


    avcodec_register_all();

    AVCodec *codec = av_codec_next(NULL);

    while (codec != NULL) {

        if (codec->decode != NULL) {
            sprintf(info, "%s[Des]", info);
        } else {
            sprintf(info, "%s[Enc]", info);
        }


        switch (codec->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s[Video]", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s[Audio]", info);
                break;
            default:
                sprintf(info, "%s[Other]", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, codec->name);
        codec = codec->next;
    }

    return env->NewStringUTF(info);
}