package com.stephanmeijer.fileconverter.engine

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class FormatDetectorTest {

    @Test
    fun `detectFormat returns mp3 for song dot mp3`() {
        assertThat(FormatDetector.detectFormat("song.mp3")).isEqualTo("mp3")
    }

    @Test
    fun `detectFormat returns mp4 for video dot mp4`() {
        assertThat(FormatDetector.detectFormat("video.mp4")).isEqualTo("mp4")
    }

    @Test
    fun `detectFormat returns mkv for clip dot mkv`() {
        assertThat(FormatDetector.detectFormat("clip.mkv")).isEqualTo("mkv")
    }

    @Test
    fun `detectFormat returns wav for audio dot wav`() {
        assertThat(FormatDetector.detectFormat("audio.wav")).isEqualTo("wav")
    }

    @Test
    fun `detectFormat returns flac for audio dot flac`() {
        assertThat(FormatDetector.detectFormat("audio.flac")).isEqualTo("flac")
    }

    @Test
    fun `detectFormat returns ogg for audio dot ogg`() {
        assertThat(FormatDetector.detectFormat("audio.ogg")).isEqualTo("ogg")
    }

    @Test
    fun `detectFormat returns opus for audio dot opus`() {
        assertThat(FormatDetector.detectFormat("audio.opus")).isEqualTo("opus")
    }

    @Test
    fun `detectFormat returns aac for audio dot aac`() {
        assertThat(FormatDetector.detectFormat("audio.aac")).isEqualTo("aac")
    }

    @Test
    fun `detectFormat returns m4a for audio dot m4a`() {
        assertThat(FormatDetector.detectFormat("audio.m4a")).isEqualTo("m4a")
    }

    @Test
    fun `detectFormat returns mov for video dot mov`() {
        assertThat(FormatDetector.detectFormat("video.mov")).isEqualTo("mov")
    }

    @Test
    fun `detectFormat returns webm for video dot webm`() {
        assertThat(FormatDetector.detectFormat("video.webm")).isEqualTo("webm")
    }

    @Test
    fun `detectFormat returns null for unknown extension`() {
        assertThat(FormatDetector.detectFormat("unknown.xyz")).isNull()
    }

    @Test
    fun `categoryOf mp3 is AUDIO`() {
        assertThat(FormatDetector.categoryOf("mp3")).isEqualTo(FormatDetector.Category.AUDIO)
    }

    @Test
    fun `categoryOf aac is AUDIO`() {
        assertThat(FormatDetector.categoryOf("aac")).isEqualTo(FormatDetector.Category.AUDIO)
    }

    @Test
    fun `categoryOf m4a is AUDIO`() {
        assertThat(FormatDetector.categoryOf("m4a")).isEqualTo(FormatDetector.Category.AUDIO)
    }

    @Test
    fun `categoryOf wav is AUDIO`() {
        assertThat(FormatDetector.categoryOf("wav")).isEqualTo(FormatDetector.Category.AUDIO)
    }

    @Test
    fun `categoryOf flac is AUDIO`() {
        assertThat(FormatDetector.categoryOf("flac")).isEqualTo(FormatDetector.Category.AUDIO)
    }

    @Test
    fun `categoryOf ogg is AUDIO`() {
        assertThat(FormatDetector.categoryOf("ogg")).isEqualTo(FormatDetector.Category.AUDIO)
    }

    @Test
    fun `categoryOf opus is AUDIO`() {
        assertThat(FormatDetector.categoryOf("opus")).isEqualTo(FormatDetector.Category.AUDIO)
    }

    @Test
    fun `categoryOf mp4 is VIDEO`() {
        assertThat(FormatDetector.categoryOf("mp4")).isEqualTo(FormatDetector.Category.VIDEO)
    }

    @Test
    fun `categoryOf mkv is VIDEO`() {
        assertThat(FormatDetector.categoryOf("mkv")).isEqualTo(FormatDetector.Category.VIDEO)
    }

    @Test
    fun `categoryOf mov is VIDEO`() {
        assertThat(FormatDetector.categoryOf("mov")).isEqualTo(FormatDetector.Category.VIDEO)
    }

    @Test
    fun `categoryOf webm is VIDEO`() {
        assertThat(FormatDetector.categoryOf("webm")).isEqualTo(FormatDetector.Category.VIDEO)
    }

    @Test
    fun `categoryOf docx is DOCUMENT`() {
        assertThat(FormatDetector.categoryOf("docx")).isEqualTo(FormatDetector.Category.DOCUMENT)
    }

    @Test
    fun `categoryOf markdown is DOCUMENT`() {
        assertThat(FormatDetector.categoryOf("markdown")).isEqualTo(FormatDetector.Category.DOCUMENT)
    }

    @Test
    fun `categoryOf html is DOCUMENT`() {
        assertThat(FormatDetector.categoryOf("html")).isEqualTo(FormatDetector.Category.DOCUMENT)
    }

    @Test
    fun `categoryOf unknown returns null`() {
        assertThat(FormatDetector.categoryOf("unknown")).isNull()
    }

    @Test
    fun `validOutputFormats for AUDIO contains exactly the audio formats`() {
        val audioOuts = FormatDetector.validOutputFormats(FormatDetector.Category.AUDIO)
        assertThat(audioOuts).containsExactlyElementsIn(
            listOf("mp3", "aac", "m4a", "wav", "flac", "ogg", "opus")
        )
    }

    @Test
    fun `validOutputFormats for VIDEO contains video containers`() {
        val videoOuts = FormatDetector.validOutputFormats(FormatDetector.Category.VIDEO)
        assertThat(videoOuts).containsAtLeastElementsIn(listOf("mp4", "mkv", "mov", "webm"))
    }

    @Test
    fun `validOutputFormats for VIDEO contains audio extraction formats`() {
        val videoOuts = FormatDetector.validOutputFormats(FormatDetector.Category.VIDEO)
        assertThat(videoOuts).containsAtLeastElementsIn(listOf("mp3", "aac", "m4a", "wav", "flac", "ogg", "opus"))
    }

    @Test
    fun `validOutputFormats for DOCUMENT contains document formats`() {
        val docOuts = FormatDetector.validOutputFormats(FormatDetector.Category.DOCUMENT)
        assertThat(docOuts).containsAtLeastElementsIn(listOf("html", "markdown", "docx", "odt", "epub", "pptx"))
    }
}
