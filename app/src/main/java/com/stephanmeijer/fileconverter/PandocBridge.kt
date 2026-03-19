package com.stephanmeijer.fileconverter

object PandocBridge {
    init {
        System.loadLibrary("pandoc_bridge")
    }

    external fun getWasmtimeVersion(): String

    // Full JNI functions added in Task 6
    external fun nativeInitEngine(assetFd: Int, assetLength: Long)
    external fun nativeShutdownEngine()
    external fun nativeQuery(jsonBytes: ByteArray): ByteArray
    external fun nativeConvert(optionsJson: ByteArray, inputFilePath: String): ByteArray
}
