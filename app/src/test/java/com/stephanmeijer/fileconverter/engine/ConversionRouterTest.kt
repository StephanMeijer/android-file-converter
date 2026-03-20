package com.stephanmeijer.fileconverter.engine

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ConversionRouterTest {

    // Audio → FFmpeg
    @Test fun `mp3 routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("mp3")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `wav routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("wav")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `flac routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("flac")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `aac routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("aac")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `m4a routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("m4a")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `ogg routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("ogg")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `opus routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("opus")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    // Video → FFmpeg
    @Test fun `mp4 routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("mp4")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `mkv routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("mkv")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `mov routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("mov")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    @Test fun `webm routes to FFMPEG`() {
        assertThat(ConversionRouter.getEngine("webm")).isEqualTo(ConversionRouter.EngineType.FFMPEG)
    }

    // Document → Pandoc
    @Test fun `markdown routes to PANDOC`() {
        assertThat(ConversionRouter.getEngine("markdown")).isEqualTo(ConversionRouter.EngineType.PANDOC)
    }

    @Test fun `docx routes to PANDOC`() {
        assertThat(ConversionRouter.getEngine("docx")).isEqualTo(ConversionRouter.EngineType.PANDOC)
    }

    @Test fun `html routes to PANDOC`() {
        assertThat(ConversionRouter.getEngine("html")).isEqualTo(ConversionRouter.EngineType.PANDOC)
    }

    @Test fun `epub routes to PANDOC`() {
        assertThat(ConversionRouter.getEngine("epub")).isEqualTo(ConversionRouter.EngineType.PANDOC)
    }

    // validateConversion: audio → audio (valid)
    @Test fun `mp3 to wav is valid`() {
        assertThat(ConversionRouter.validateConversion("mp3", "wav")).isTrue()
    }

    @Test fun `flac to mp3 is valid`() {
        assertThat(ConversionRouter.validateConversion("flac", "mp3")).isTrue()
    }

    // validateConversion: video → video (valid)
    @Test fun `mp4 to mkv is valid`() {
        assertThat(ConversionRouter.validateConversion("mp4", "mkv")).isTrue()
    }

    // validateConversion: video → audio (valid — audio extraction)
    @Test fun `mp4 to mp3 is valid`() {
        assertThat(ConversionRouter.validateConversion("mp4", "mp3")).isTrue()
    }

    @Test fun `mkv to wav is valid`() {
        assertThat(ConversionRouter.validateConversion("mkv", "wav")).isTrue()
    }

    // validateConversion: document → document (valid)
    @Test fun `docx to html is valid`() {
        assertThat(ConversionRouter.validateConversion("docx", "html")).isTrue()
    }

    @Test fun `markdown to epub is valid`() {
        assertThat(ConversionRouter.validateConversion("markdown", "epub")).isTrue()
    }

    // validateConversion: cross-category (invalid)
    @Test fun `docx to mp4 is invalid`() {
        assertThat(ConversionRouter.validateConversion("docx", "mp4")).isFalse()
    }

    @Test fun `docx to mp3 is invalid`() {
        assertThat(ConversionRouter.validateConversion("docx", "mp3")).isFalse()
    }

    @Test fun `mp3 to docx is invalid`() {
        assertThat(ConversionRouter.validateConversion("mp3", "docx")).isFalse()
    }

    @Test fun `mp4 to docx is invalid`() {
        assertThat(ConversionRouter.validateConversion("mp4", "docx")).isFalse()
    }

    // validateConversion: unknown format (invalid)
    @Test fun `xyz to mp3 is invalid`() {
        assertThat(ConversionRouter.validateConversion("xyz", "mp3")).isFalse()
    }

    @Test fun `mp3 to xyz is invalid`() {
        assertThat(ConversionRouter.validateConversion("mp3", "xyz")).isFalse()
    }
}
