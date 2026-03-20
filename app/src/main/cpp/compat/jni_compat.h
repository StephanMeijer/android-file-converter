#pragma once

#if __has_include(<jni.h>)
#include <jni.h>
#else

#include <stdint.h>
#include <stdarg.h>

typedef int jint;
typedef jint jsize;
typedef int64_t jlong;
typedef unsigned char jboolean;
typedef void *jobject;
typedef jobject jclass;
typedef jobject jstring;
typedef jobject jobjectArray;
typedef void *jmethodID;

#define JNI_FALSE 0
#define JNI_TRUE 1
#define JNI_OK 0
#define JNI_EDETACHED (-2)
#define JNI_VERSION_1_6 0x00010006

#ifndef JNIEXPORT
#define JNIEXPORT
#endif

#ifndef JNICALL
#define JNICALL
#endif

struct JNINativeInterface_;
typedef const struct JNINativeInterface_ *JNIEnv;

struct JNIInvokeInterface_;
typedef const struct JNIInvokeInterface_ *JavaVM;

struct JNINativeInterface_ {
    jclass (*FindClass)(JNIEnv *, const char *);
    jobject (*NewGlobalRef)(JNIEnv *, jobject);
    void (*DeleteGlobalRef)(JNIEnv *, jobject);
    jmethodID (*GetStaticMethodID)(JNIEnv *, jclass, const char *, const char *);
    void (*CallStaticVoidMethod)(JNIEnv *, jclass, jmethodID, ...);
    jboolean (*ExceptionCheck)(JNIEnv *);
    void (*ExceptionClear)(JNIEnv *);
    jsize (*GetArrayLength)(JNIEnv *, jobjectArray);
    jobject (*GetObjectArrayElement)(JNIEnv *, jobjectArray, jsize);
    const char *(*GetStringUTFChars)(JNIEnv *, jstring, jboolean *);
    void (*ReleaseStringUTFChars)(JNIEnv *, jstring, const char *);
    void (*DeleteLocalRef)(JNIEnv *, jobject);
};

struct JNIInvokeInterface_ {
    jint (*DestroyJavaVM)(JavaVM *);
    jint (*AttachCurrentThread)(JavaVM *, JNIEnv **, void *);
    jint (*DetachCurrentThread)(JavaVM *);
    jint (*GetEnv)(JavaVM *, void **, jint);
    jint (*AttachCurrentThreadAsDaemon)(JavaVM *, JNIEnv **, void *);
};

#endif
