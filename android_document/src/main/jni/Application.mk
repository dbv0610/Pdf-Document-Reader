APP_STL := c++_shared
APP_CPPFLAGS += -fexceptions

APP_PLATFORM := android-23

APP_ABI :=  armeabi-v7a \
            arm64-v8a \
            x86 \
            x86_64

# 16 KB page size support (Android 15+)
APP_SUPPORT_FLEXIBLE_PAGE_SIZES := true
