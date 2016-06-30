//
// Created by xiaofeng on 16-7-1.
//

#ifndef AUDIORECORD_MLOG_H
#define AUDIORECORD_MLOG_H

#include <android/log.h>

#define TAG "xiaofeng"
#define logI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
#define logE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)

#endif //AUDIORECORD_MLOG_H
