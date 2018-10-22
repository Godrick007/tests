#include <jni.h>
#include <unistd.h>
#include "androidLog.h"
#include "pthread.h"
#include "time.h"
#include "queue"

pthread_t tProduct;
pthread_t tCustomer;
pthread_mutex_t mP;
pthread_cond_t cP;
std::queue<int> queue;


void *fP(void *data);

void *fC(void *data);

extern "C"
JNIEXPORT void JNICALL
Java_com_godrick_ffmpeglib_NativeTest_thread2(JNIEnv *env, jobject instance) {

    pthread_create(&tProduct, NULL, fP, NULL);
    pthread_create(&tCustomer, NULL, fC, NULL);

    pthread_mutex_init(&mP, NULL);

    pthread_cond_init(&cP, NULL);


}


void *fP(void *data) {

    while (1) {
        queue.push(1);
        if (queue.size() > 5)
            break;
    }

    while (1) {
        pthread_mutex_lock(&mP);
        queue.push(1);
        LOGE("TAG", "生产者,数量为%d", queue.size());
        pthread_cond_signal(&cP);
        pthread_mutex_unlock(&mP);
        sleep(3);
    }
    pthread_exit(&tProduct);
}

void *fC(void *data) {

    while (1) {
        pthread_mutex_lock(&mP);
        if (queue.size() > 0) {
            queue.pop();
            LOGI("TAG", "消费者,数量剩余为%d", queue.size());
        } else {
            LOGW("TAG", "消费完了,没有可以消费的了");
            pthread_cond_wait(&cP, &mP);
        }
        pthread_mutex_unlock(&mP);
        usleep(500 * 1000);
    }
    pthread_exit(&tCustomer);
}