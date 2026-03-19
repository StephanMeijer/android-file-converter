cmake_minimum_required(VERSION 3.22.1)
project("pandoc_bridge")

# Wasmtime static library
add_library(wasmtime STATIC IMPORTED)
set_target_properties(wasmtime PROPERTIES
    IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/wasmtime/lib/libwasmtime.a
)

# Our JNI bridge
add_library(pandoc_bridge SHARED pandoc_bridge.cpp)

target_include_directories(pandoc_bridge PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/wasmtime/include
)

target_link_libraries(pandoc_bridge
    wasmtime
    android
    log
)
