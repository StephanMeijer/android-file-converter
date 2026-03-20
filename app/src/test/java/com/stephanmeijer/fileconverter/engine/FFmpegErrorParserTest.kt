package com.stephanmeijer.fileconverter.engine

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class FFmpegErrorParserTest {
    @Test fun `corrupt file returns friendly message`() {
        val msg = FFmpegErrorParser.parse("Invalid data found when processing input")
        assertThat(msg).isEqualTo("This file appears to be corrupted or in an unsupported format.")
    }
    @Test fun `file not found returns friendly message`() {
        val msg = FFmpegErrorParser.parse("No such file or directory")
        assertThat(msg).isEqualTo("The selected file could not be found.")
    }
    @Test fun `permission denied returns friendly message`() {
        val msg = FFmpegErrorParser.parse("Permission denied")
        assertThat(msg).isEqualTo("Cannot access this file. Permission denied.")
    }
    @Test fun `unknown error passes through raw message`() {
        val raw = "Some unexpected ffmpeg error xyz"
        assertThat(FFmpegErrorParser.parse(raw)).isEqualTo(raw)
    }
    @Test fun `empty string passes through`() {
        assertThat(FFmpegErrorParser.parse("")).isEqualTo("")
    }
}
