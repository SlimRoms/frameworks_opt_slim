LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files) \
    src/org/slim/providers/settings/EventLogTags.logtags

LOCAL_JAVA_LIBRARIES := telephony-common ims-common org.slim.framework

LOCAL_PACKAGE_NAME := SlimSettingsProvider
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)

########################
include $(call all-makefiles-under,$(LOCAL_PATH))
