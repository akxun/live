LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_C_INCLUDE := $(LOCAL_PATH) \
                   $(LOCAL_PATH)/writeFile \
                   $(LOCAL_PATH)/log \

LOCAL_JNI_SRC := com_xiaofeng_audiorecord_MainActivity.cpp

LOCAL_WRITEFILE_SRC := writeFile/WriteFile.cpp

LOCAL_SRC_FILES := $(LOCAL_JNI_SRC) \
                   $(LOCAL_WRITEFILE_SRC) \

LOCAL_LDLIBS    := -lm -llog
LOCAL_MODULE    := xiaofeng
include $(BUILD_SHARED_LIBRARY)