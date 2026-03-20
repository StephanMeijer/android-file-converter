#pragma once

#if __has_include(<android/log.h>)
#include <android/log.h>
#else

#define ANDROID_LOG_INFO 4
#define ANDROID_LOG_ERROR 6

static inline int __android_log_print(int prio, const char *tag, const char *fmt, ...) {
    (void)prio;
    (void)tag;
    (void)fmt;
    return 0;
}

#endif
