package com.stephanmeijer.fileconverter.engine

object FFmpegBridge {
    init {
        System.loadLibrary("ffmpeg_bridge")
    }

    external fun nativeExecute(args: Array<String>): Int
    external fun nativeCancel()
    external fun nativeGetMediaDuration(path: String): Long

    @JvmStatic
    var progressCallback: ((Long) -> Unit)? = null

    @JvmStatic
    fun onProgress(timeMs: Long) {
        progressCallback?.invoke(timeMs)
    }
}
