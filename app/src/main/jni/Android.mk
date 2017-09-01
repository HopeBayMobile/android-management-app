LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libhcfsapi
LOCAL_SRC_FILES := mylibs/$(TARGET_ARCH_ABI)/libhcfsapi.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libjansson
LOCAL_SRC_FILES := mylibs/$(TARGET_ARCH_ABI)/libjansson.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libcrypto
LOCAL_SRC_FILES := mylibs/$(TARGET_ARCH_ABI)/libcrypto.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
OCAL_CFLAGS     += CLIENT_SIDE
LOCAL_MODULE    := terafonnapi
LOCAL_SRC_FILES := terafonnapi.c crypt.c b64encode.c enc.c mix.c util.c
LOCAL_SHARED_LIBRARIES := libhcfsapi libjansson libcrypto
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
