package com.example.gemtext2kindle.stage2;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class HtmlEscaperTest {

    @Test
    public void shouldEscapeSpecialCharacters() {
        HtmlEscaper escaper = new HtmlEscaper();
        assertThat(escaper.escape("<")).isEqualTo("&" + "lt;");
        assertThat(escaper.escape(">")).isEqualTo("&" + "gt;");
        assertThat(escaper.escape("&")).isEqualTo("&" + "amp;");
        assertThat(escaper.escape("\"")).isEqualTo("&" + "quot;");
    }

    @Test
    public void shouldNotEscapeNormalCharacters() {
        HtmlEscaper escaper = new HtmlEscaper();
        assertThat(escaper.escape("abc 123")).isEqualTo("abc 123");
    }
}
