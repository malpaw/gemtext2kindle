package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class BookmarkParserTest {

    @Test
    public void shouldParseBookmarks() {
        String content = "recentfolder = 130\n" +
                "\n" +
                "[128]\n" +
                "url = \"gemini://rohitfarmer.com/\"\n" +
                "title = \"Rohit Farmer, Ph.D.\"\n" +
                "icon = 0x1f657\n" +
                "\n" +
                "[80]\n" +
                "url = \"gemini://sud0nim.smol.pub/glog\"\n" +
                "title = \"sud0nim's Glog!\"\n" +
                "icon = 0x0\n";
        
        BookmarkParser parser = new BookmarkParser();
        Map<String, BookmarkParser.Bookmark> bookmarks = parser.parse(content);
        
        assertThat(bookmarks).hasSize(2);
        // 128 decimal is 80 hex
        assertThat(bookmarks.get("80").title()).isEqualTo("Rohit Farmer, Ph.D.");
        assertThat(bookmarks.get("80").icon()).isEqualTo("🙗");
        
        // 80 decimal is 50 hex
        assertThat(bookmarks.get("50").title()).isEqualTo("sud0nim's Glog!");
        assertThat(bookmarks.get("50").icon()).isNull();
    }

    @Test
    public void shouldHandleEmptyContent() {
        BookmarkParser parser = new BookmarkParser();
        assertThat(parser.parse("")).isEmpty();
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    public void shouldHandleMalformedSections() {
        BookmarkParser parser = new BookmarkParser();
        String malformed = "[not-a-number]\ntitle=\"Bad\"\n\n[10]\ntitle=\"Good\"\n";
        Map<String, BookmarkParser.Bookmark> bookmarks = parser.parse(malformed);
        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get("a").title()).isEqualTo("Good");
    }

    @Test
    public void shouldHandleMissingFields() {
        BookmarkParser parser = new BookmarkParser();
        String missing = "[10]\nurl=\"...\"\n\n[20]\ntitle=\"Only Title\"\n";
        Map<String, BookmarkParser.Bookmark> bookmarks = parser.parse(missing);
        assertThat(bookmarks).hasSize(1);
        assertThat(bookmarks.get("14").title()).isEqualTo("Only Title");
    }

    @Test
    public void shouldHandleInvalidIconFormat() {
        BookmarkParser parser = new BookmarkParser();
        String content = "[16]\ntitle=\"Test\"\nicon=0xGHIJ\n";
        Map<String, BookmarkParser.Bookmark> bookmarks = parser.parse(content);
        assertThat(bookmarks.get("10").icon()).isNull();
    }
}
