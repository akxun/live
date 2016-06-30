//
// Created by xiaofeng on 16-6-28.
//
#include <android/log.h>
#include "com_xiaofeng_audiorecord_MainActivity.h"
#include "writeFile/WriteFile.h"

static WriteFile *mWriteFile;

JNIEXPORT void JNICALL Java_com_xiaofeng_audiorecord_MainActivity_initWav
        (JNIEnv *, jobject, jint sampleBit, jint sampleRate, jint channel) {
    __android_log_print(ANDROID_LOG_INFO, "xiaofeng", "sampleBit:%d, sampleRate:%d, channel:%d", sampleBit, sampleRate, channel);
    mWriteFile = new WriteFile(sampleBit, sampleRate, channel);
    mWriteFile->writeWavFileHeader();
}

JNIEXPORT void JNICALL Java_com_xiaofeng_audiorecord_MainActivity_onPcm
        (JNIEnv *env, jobject obj, jbyteArray pcm) {
    int len = env->GetArrayLength(pcm);
    jbyte *data = env->GetByteArrayElements(pcm, 0);
    mWriteFile->writeDate((uint8_t*)data, len);
    env->ReleaseByteArrayElements(pcm, data, 0);
    __android_log_print(ANDROID_LOG_INFO, "xiaofeng", "jniTest");
}