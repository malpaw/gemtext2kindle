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
}
