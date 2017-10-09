LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := dict
LOCAL_SRC_FILES := main.cpp\
                   dict.cpp\
		   bridge.cpp\
		   util.cpp
LOCAL_CFLAGS	:= -std=c++11 -fpermissive -DDEBUG -O0 -DANDROID
LOCAL_LDLIBS := -lz -llog
include $(BUILD_SHARED_LIBRARY)
