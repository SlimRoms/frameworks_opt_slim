LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := services.slim
LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_PROGUARD_ENABLED:= disabled

LOCAL_JAVA_LIBRARIES := \
    org.slim.framework \
    services

include $(BUILD_JAVA_LIBRARY)
