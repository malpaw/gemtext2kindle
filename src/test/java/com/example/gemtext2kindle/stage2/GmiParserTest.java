package com.example.gemtext2kindle.stage2;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class GmiParserTest {

    @Test
    public void shouldConvertHeadings() {
        GmiParser parser = new GmiParser();
        assertThat(parser.toHtml("# Title")).isEqualTo("<h1>Title</h1>");
        assertThat(parser.toHtml("## Subtitle")).isEqualTo("<h2>Subtitle</h2>");
        assertThat(parser.toHtml("### Section")).isEqualTo("<h3>Section</h3>");
    }

    @Test
    public void shouldConvertLinks() {
        GmiParser parser = new GmiParser();
        assertThat(parser.toHtml("=> gemini://example.com Link text")).isEqualTo("<p><a href=\"gemini://example.com\">Link text</a></p>");
        assertThat(parser.toHtml("=> gemini://example.com")).isEqualTo("<p><a href=\"gemini://example.com\">gemini://example.com</a></p>");
    }

    @Test
    public void shouldConvertLists() {
        GmiParser parser = new GmiParser();
        assertThat(parser.toHtml("* Item 1")).isEqualTo("<ul><li>Item 1</li></ul>");
    }

    @Test
    public void shouldHandlePreformattedText() {
        GmiParser parser = new GmiParser();
        String input = "```\npreformatted\n```";
        assertThat(parser.convert(input)).contains("<pre>").contains("preformatted").contains("</pre>");
    }
}
