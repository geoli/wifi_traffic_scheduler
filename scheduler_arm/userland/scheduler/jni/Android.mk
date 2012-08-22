LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := scheduler

LOCAL_SRC_FILES := \
                   ts_pcap.c \
                   scheduler.c

LOCAL_MODULE_TAGS := optional
LOCAL_GFLAGS := -O0  –g
LOCAL_LDLIBS := -ldl -llog
                
#the static library this project scheduler module depends on.
LOCAL_STATIC_LIBRARIES := native_pcap

include $(BUILD_EXECUTABLE)

#reference to the libpcap native library sources. Recommended to be the last command of an Android.mk file
$(call import-module, android/native_libpcap)
