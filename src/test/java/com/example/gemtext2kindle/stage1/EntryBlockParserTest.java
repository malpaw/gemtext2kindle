package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EntryBlockParserTest {

    @Test
    public void shouldParseValidEntryBlock() {
        List<String> lines = List.of(
            "33",
            "1285844400",
            "1771014706",
            "gemini://example.com/post.gmi",
            "Post Title"
        );
        
        EntryBlockParser parser = new EntryBlockParser();
        FeedEntry entry = parser.parse(lines);
        
        assertThat(entry.feedId()).isEqualTo("33");
        assertThat(entry.timestamp1()).isEqualTo(1285844400L);
        assertThat(entry.timestamp2()).isEqualTo(1771014706L);
        assertThat(entry.url()).isEqualTo("gemini://example.com/post.gmi");
        assertThat(entry.title()).isEqualTo("Post Title");
    }

    @Test
    public void shouldThrowExceptionOnIncompleteBlock() {
        List<String> lines = List.of("33", "123", "456");
        EntryBlockParser parser = new EntryBlockParser();
        assertThatThrownBy(() -> parser.parse(lines))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionOnInvalidTimestamps() {
        List<String> lines = List.of("33", "not-a-number", "456", "url", "title");
        EntryBlockParser parser = new EntryBlockParser();
        assertThatThrownBy(() -> parser.parse(lines))
            .isInstanceOf(NumberFormatException.class);
    }
}
