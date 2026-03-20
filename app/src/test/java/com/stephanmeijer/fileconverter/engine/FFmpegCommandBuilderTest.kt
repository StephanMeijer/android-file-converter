package com.stephanmeijer.fileconverter.engine

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class FFmpegCommandBuilderTest {

    @Test
    fun `audio conversion mp3 to wav HIGH quality`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/song.mp3",
            "/out/song.wav",
            ConversionPreset.AudioQuality(Level.HIGH)
        )
        assertThat(cmd.toList()).containsAtLeast("-i", "/in/song.mp3")
        assertThat(cmd.toList()).containsAtLeast("-c:a", "pcm_s16le")
        assertThat(cmd.toList()).containsAtLeast("-b:a", "320k")
        assertThat(cmd.last()).isEqualTo("/out/song.wav")
    }

    @Test
    fun `audio conversion flac to mp3 LOW quality`() {
        val cmd2 = FFmpegCommandBuilder.buildCommand(
            "/in/a.flac",
            "/out/a.mp3",
            ConversionPreset.AudioQuality(Level.LOW)
        )
        assertThat(cmd2.toList()).containsAtLeast("-c:a", "libmp3lame")
        assertThat(cmd2.toList()).containsAtLeast("-b:a", "96k")
    }

    @Test
    fun `video conversion mp4 to webm 720p`() {
        val cmd3 = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.webm",
            ConversionPreset.VideoResolution(Resolution.P720)
        )
        assertThat(cmd3.toList()).containsAtLeast("-c:v", "libvpx-vp9")
        assertThat(cmd3.toList()).containsAtLeast("-c:a", "libopus")
        assertThat(cmd3.toList()).containsAtLeast("-vf", "scale=-2:720")
    }

    @Test
    fun `video to audio extraction mp4 to mp3 MEDIUM quality`() {
        val cmd4 = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.mp3",
            ConversionPreset.AudioQuality(Level.MEDIUM)
        )
        assertThat(cmd4.toList()).contains("-vn")
        assertThat(cmd4.toList()).containsAtLeast("-c:a", "libmp3lame")
        assertThat(cmd4.toList()).containsAtLeast("-b:a", "192k")
        assertThat(cmd4.toList()).doesNotContain("-c:v")
    }

    @Test
    fun `video 1080p scale filter present`() {
        val cmd5 = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.mp4",
            ConversionPreset.VideoResolution(Resolution.P1080)
        )
        assertThat(cmd5.toList()).containsAtLeast("-vf", "scale=-2:1080")
    }

    @Test
    fun `video ORIGINAL resolution no scale filter`() {
        val cmd6 = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.mkv",
            ConversionPreset.VideoResolution(Resolution.ORIGINAL)
        )
        assertThat(cmd6.toList()).doesNotContain("-vf")
    }

    @Test
    fun `y flag always present as first element`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/song.mp3",
            "/out/song.wav",
            ConversionPreset.AudioQuality(Level.HIGH)
        )
        val cmd3 = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.webm",
            ConversionPreset.VideoResolution(Resolution.P720)
        )
        assertThat(cmd.first()).isEqualTo("-y")
        assertThat(cmd3.first()).isEqualTo("-y")
    }

    @Test
    fun `audio aac codec for aac output`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/song.mp3",
            "/out/song.aac",
            ConversionPreset.AudioQuality(Level.MEDIUM)
        )
        assertThat(cmd.toList()).containsAtLeast("-c:a", "aac")
    }

    @Test
    fun `audio aac codec for m4a output`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/song.mp3",
            "/out/song.m4a",
            ConversionPreset.AudioQuality(Level.MEDIUM)
        )
        assertThat(cmd.toList()).containsAtLeast("-c:a", "aac")
    }

    @Test
    fun `audio flac codec for flac output`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/song.mp3",
            "/out/song.flac",
            ConversionPreset.AudioQuality(Level.HIGH)
        )
        assertThat(cmd.toList()).containsAtLeast("-c:a", "flac")
    }

    @Test
    fun `audio libvorbis codec for ogg output`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/song.mp3",
            "/out/song.ogg",
            ConversionPreset.AudioQuality(Level.MEDIUM)
        )
        assertThat(cmd.toList()).containsAtLeast("-c:a", "libvorbis")
    }

    @Test
    fun `audio libopus codec for opus output`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/song.mp3",
            "/out/song.opus",
            ConversionPreset.AudioQuality(Level.MEDIUM)
        )
        assertThat(cmd.toList()).containsAtLeast("-c:a", "libopus")
    }

    @Test
    fun `video libx264 aac codec for mp4 output`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.mp4",
            ConversionPreset.VideoResolution(Resolution.P720)
        )
        assertThat(cmd.toList()).containsAtLeast("-c:v", "libx264")
        assertThat(cmd.toList()).containsAtLeast("-c:a", "aac")
    }

    @Test
    fun `video libx264 aac codec for mkv output`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.mkv",
            ConversionPreset.VideoResolution(Resolution.P720)
        )
        assertThat(cmd.toList()).containsAtLeast("-c:v", "libx264")
        assertThat(cmd.toList()).containsAtLeast("-c:a", "aac")
    }

    @Test
    fun `video libx264 aac codec for mov output`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.mov",
            ConversionPreset.VideoResolution(Resolution.P720)
        )
        assertThat(cmd.toList()).containsAtLeast("-c:v", "libx264")
        assertThat(cmd.toList()).containsAtLeast("-c:a", "aac")
    }

    @Test
    fun `output path is last element`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.webm",
            ConversionPreset.VideoResolution(Resolution.P720)
        )
        assertThat(cmd.last()).isEqualTo("/out/v.webm")
    }

    @Test
    fun `input path follows -i flag`() {
        val cmd = FFmpegCommandBuilder.buildCommand(
            "/in/v.mp4",
            "/out/v.webm",
            ConversionPreset.VideoResolution(Resolution.P720)
        )
        val list = cmd.toList()
        val iIndex = list.indexOf("-i")
        assertThat(iIndex).isGreaterThan(-1)
        assertThat(list[iIndex + 1]).isEqualTo("/in/v.mp4")
    }
}
