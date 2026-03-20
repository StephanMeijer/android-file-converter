#pragma once

#include <stdint.h>

#define AV_TIME_BASE 1000000
#define AVMEDIA_TYPE_AUDIO 1

typedef struct AVStream {
    int codec_type;
} AVStream;

typedef struct AVFormatContext {
    unsigned int nb_streams;
    AVStream **streams;
    int64_t duration;
} AVFormatContext;

int avformat_open_input(AVFormatContext **ps, const char *url, void *fmt, void **options);
int avformat_find_stream_info(AVFormatContext *ic, void **options);
void avformat_close_input(AVFormatContext **s);
