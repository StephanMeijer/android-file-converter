package com.stephanmeijer.fileconverter.ui.components

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class MimeTypesTest {
    
    // forFormat() tests for audio formats
    @Test
    fun forFormat_mp3_returnsAudioMpeg() {
        assertThat(MimeTypes.forFormat("mp3")).isEqualTo("audio/mpeg")
    }
    
    @Test
    fun forFormat_aac_returnsAudioAac() {
        assertThat(MimeTypes.forFormat("aac")).isEqualTo("audio/aac")
    }
    
    @Test
    fun forFormat_m4a_returnsAudioMp4() {
        assertThat(MimeTypes.forFormat("m4a")).isEqualTo("audio/mp4")
    }
    
    @Test
    fun forFormat_wav_returnsAudioWav() {
        assertThat(MimeTypes.forFormat("wav")).isEqualTo("audio/wav")
    }
    
    @Test
    fun forFormat_flac_returnsAudioFlac() {
        assertThat(MimeTypes.forFormat("flac")).isEqualTo("audio/flac")
    }
    
    @Test
    fun forFormat_ogg_returnsAudioOgg() {
        assertThat(MimeTypes.forFormat("ogg")).isEqualTo("audio/ogg")
    }
    
    @Test
    fun forFormat_opus_returnsAudioOpus() {
        assertThat(MimeTypes.forFormat("opus")).isEqualTo("audio/opus")
    }
    
    // forFormat() tests for video formats
    @Test
    fun forFormat_mp4_returnsVideoMp4() {
        assertThat(MimeTypes.forFormat("mp4")).isEqualTo("video/mp4")
    }
    
    @Test
    fun forFormat_mkv_returnsVideoMatroska() {
        assertThat(MimeTypes.forFormat("mkv")).isEqualTo("video/x-matroska")
    }
    
    @Test
    fun forFormat_mov_returnsVideoQuicktime() {
        assertThat(MimeTypes.forFormat("mov")).isEqualTo("video/quicktime")
    }
    
    @Test
    fun forFormat_webm_returnsVideoWebm() {
        assertThat(MimeTypes.forFormat("webm")).isEqualTo("video/webm")
    }
    
    // extensionForFormat() tests for audio formats
    @Test
    fun extensionForFormat_mp3_returnsMp3() {
        assertThat(MimeTypes.extensionForFormat("mp3")).isEqualTo("mp3")
    }
    
    @Test
    fun extensionForFormat_aac_returnsAac() {
        assertThat(MimeTypes.extensionForFormat("aac")).isEqualTo("aac")
    }
    
    @Test
    fun extensionForFormat_m4a_returnsM4a() {
        assertThat(MimeTypes.extensionForFormat("m4a")).isEqualTo("m4a")
    }
    
    @Test
    fun extensionForFormat_wav_returnsWav() {
        assertThat(MimeTypes.extensionForFormat("wav")).isEqualTo("wav")
    }
    
    @Test
    fun extensionForFormat_flac_returnsFlac() {
        assertThat(MimeTypes.extensionForFormat("flac")).isEqualTo("flac")
    }
    
    @Test
    fun extensionForFormat_ogg_returnsOgg() {
        assertThat(MimeTypes.extensionForFormat("ogg")).isEqualTo("ogg")
    }
    
    @Test
    fun extensionForFormat_opus_returnsOpus() {
        assertThat(MimeTypes.extensionForFormat("opus")).isEqualTo("opus")
    }
    
    // extensionForFormat() tests for video formats
    @Test
    fun extensionForFormat_mp4_returnsMp4() {
        assertThat(MimeTypes.extensionForFormat("mp4")).isEqualTo("mp4")
    }
    
    @Test
    fun extensionForFormat_mkv_returnsMkv() {
        assertThat(MimeTypes.extensionForFormat("mkv")).isEqualTo("mkv")
    }
    
    @Test
    fun extensionForFormat_mov_returnsMov() {
        assertThat(MimeTypes.extensionForFormat("mov")).isEqualTo("mov")
    }
    
    @Test
    fun extensionForFormat_webm_returnsWebm() {
        assertThat(MimeTypes.extensionForFormat("webm")).isEqualTo("webm")
    }
}
