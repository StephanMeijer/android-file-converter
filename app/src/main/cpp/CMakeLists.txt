cmake_minimum_required(VERSION 3.22.1)
project(ffmpeg_bridge)

add_library(ffmpeg SHARED IMPORTED)
set_target_properties(ffmpeg PROPERTIES
    IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libffmpeg.so)

add_library(ffmpeg_bridge SHARED ffmpeg_bridge.c)

target_include_directories(ffmpeg_bridge PRIVATE
    ${CMAKE_SOURCE_DIR}/ffmpeg)

target_link_libraries(ffmpeg_bridge
    ffmpeg
    android
    log)
