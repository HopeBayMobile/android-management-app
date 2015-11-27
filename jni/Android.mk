LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libHCFS_api
LOCAL_SRC_FILES := mylibs/$(TARGET_ARCH_ABI)/libHCFS_api.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libjansson
LOCAL_SRC_FILES := mylibs/$(TARGET_ARCH_ABI)/libjansson.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := HCFSAPI
LOCAL_SRC_FILES := HCFSAPI.c 
LOCAL_SHARED_LIBRARIES := libHCFS_api libjansson
include $(BUILD_SHARED_LIBRARY)