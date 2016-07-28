LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

## Slim framework
LOCAL_JAVA_LIBRARIES := org.slim.framework

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_CERTIFICATE := platform
LOCAL_PACKAGE_NAME := SlimSettings
LOCAL_PROGUARD_ENABLED:= disabled

include $(BUILD_PACKAGE)
