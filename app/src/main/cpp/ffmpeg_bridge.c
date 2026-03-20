#include <stdlib.h>
#include <string.h>

#include "compat/android_log_compat.h"
#include "compat/jni_compat.h"
#include "libavformat/avformat.h"

#define TAG "FFmpegBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern int ffmpeg_execute(int argc, char **argv);
extern void ffmpeg_cancel(void);

static JavaVM *g_jvm = NULL;
static jclass g_bridge_class = NULL;
static jmethodID g_on_progress_method = NULL;

static void notify_progress(jlong time_ms) {
    if (g_jvm == NULL || g_bridge_class == NULL || g_on_progress_method == NULL) {
        return;
    }

    JNIEnv *env = NULL;
    jint get_env = (*g_jvm)->GetEnv(g_jvm, (void **)&env, JNI_VERSION_1_6);
    jboolean detach = JNI_FALSE;
    if (get_env == JNI_EDETACHED) {
        if ((*g_jvm)->AttachCurrentThread(g_jvm, &env, NULL) != JNI_OK) {
            LOGE("AttachCurrentThread failed");
            return;
        }
        detach = JNI_TRUE;
    } else if (get_env != JNI_OK) {
        LOGE("GetEnv failed");
        return;
    }

    (*env)->CallStaticVoidMethod(env, g_bridge_class, g_on_progress_method, time_ms);
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionClear(env);
        LOGE("onProgress callback threw an exception");
    }

    if (detach == JNI_TRUE) {
        (*g_jvm)->DetachCurrentThread(g_jvm);
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void)reserved;
    g_jvm = vm;

    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    jclass local_cls = (*env)->FindClass(env, "com/stephanmeijer/fileconverter/engine/FFmpegBridge");
    if (local_cls == NULL) {
        LOGE("FFmpegBridge class not found");
        return -1;
    }

    g_bridge_class = (jclass)(*env)->NewGlobalRef(env, local_cls);
    (*env)->DeleteLocalRef(env, local_cls);
    if (g_bridge_class == NULL) {
        LOGE("Failed to cache FFmpegBridge class");
        return -1;
    }

    g_on_progress_method =
            (*env)->GetStaticMethodID(env, g_bridge_class, "onProgress", "(J)V");
    if (g_on_progress_method == NULL) {
        LOGE("onProgress method not found");
    }

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
    (void)reserved;

    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6) == JNI_OK && g_bridge_class != NULL) {
        (*env)->DeleteGlobalRef(env, g_bridge_class);
    }

    g_bridge_class = NULL;
    g_on_progress_method = NULL;
    g_jvm = NULL;
}

JNIEXPORT jint JNICALL
Java_com_stephanmeijer_fileconverter_engine_FFmpegBridge_nativeExecute(
        JNIEnv *env, jclass clazz, jobjectArray jargs) {
    (void)clazz;

    jint argc = (*env)->GetArrayLength(env, jargs);
    char **argv = (char **)calloc((size_t)argc + 1U, sizeof(char *));
    if (argv == NULL) {
        LOGE("Failed to allocate argv");
        return -1;
    }

    for (jint i = 0; i < argc; ++i) {
        jstring jarg = (jstring)(*env)->GetObjectArrayElement(env, jargs, i);
        if (jarg == NULL) {
            argv[i] = strdup("");
            continue;
        }

        const char *utf = (*env)->GetStringUTFChars(env, jarg, NULL);
        if (utf == NULL) {
            (*env)->DeleteLocalRef(env, jarg);
            for (jint j = 0; j < i; ++j) {
                free(argv[j]);
            }
            free(argv);
            return -1;
        }

        argv[i] = strdup(utf);
        (*env)->ReleaseStringUTFChars(env, jarg, utf);
        (*env)->DeleteLocalRef(env, jarg);

        if (argv[i] == NULL) {
            LOGE("Failed to duplicate arg at index %d", i);
            for (jint j = 0; j < i; ++j) {
                free(argv[j]);
            }
            free(argv);
            return -1;
        }
    }
    argv[argc] = NULL;

    notify_progress(0);
    int result = ffmpeg_execute(argc, argv);
    notify_progress(-1);

    for (jint i = 0; i < argc; ++i) {
        free(argv[i]);
    }
    free(argv);

    return result;
}

JNIEXPORT void JNICALL
Java_com_stephanmeijer_fileconverter_engine_FFmpegBridge_nativeCancel(JNIEnv *env, jclass clazz) {
    (void)env;
    (void)clazz;
    ffmpeg_cancel();
}

JNIEXPORT jlong JNICALL
Java_com_stephanmeijer_fileconverter_engine_FFmpegBridge_nativeGetMediaDuration(
        JNIEnv *env, jclass clazz, jstring jpath) {
    (void)clazz;
    if (jpath == NULL) {
        return -1;
    }

    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    if (path == NULL) {
        return -1;
    }

    AVFormatContext *fmt_ctx = NULL;
    jlong duration_ms = -1;

    if (avformat_open_input(&fmt_ctx, path, NULL, NULL) == 0) {
        if (avformat_find_stream_info(fmt_ctx, NULL) >= 0 && fmt_ctx != NULL &&
            fmt_ctx->duration >= 0) {
            duration_ms = (jlong)(fmt_ctx->duration / (AV_TIME_BASE / 1000));
        }
        avformat_close_input(&fmt_ctx);
    }

    (*env)->ReleaseStringUTFChars(env, jpath, path);
    return duration_ms;
}
