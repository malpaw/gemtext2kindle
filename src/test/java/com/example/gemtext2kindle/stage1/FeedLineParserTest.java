package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FeedLineParserTest {

    @Test
    public void shouldParseValidFeedLine() {
        FeedLineParser parser = new FeedLineParser();
        Feed feed = parser.parse("00000081 gemini://jpfox.fr/ham/icqpodcast.com/");
        
        assertThat(feed.id()).isEqualTo("00000081");
        assertThat(feed.url()).isEqualTo("gemini://jpfox.fr/ham/icqpodcast.com/");
    }

    @Test
    public void shouldParseFeedLineWithExtraSpaces() {
        FeedLineParser parser = new FeedLineParser();
        Feed feed = parser.parse("   abc   gemini://example.com   ");
        
        assertThat(feed.id()).isEqualTo("abc");
        assertThat(feed.url()).isEqualTo("gemini://example.com");
    }

    @Test
    public void shouldThrowExceptionOnInvalidFormat() {
        FeedLineParser parser = new FeedLineParser();
        assertThatThrownBy(() -> parser.parse("invalidline"))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void shouldReturnNullOnEmptyLine() {
        FeedLineParser parser = new FeedLineParser();
        assertThat(parser.parse("   ")).isNull();
        assertThat(parser.parse(null)).isNull();
    }

    @Test
    public void shouldThrowExceptionOnEmptyIdOrUrl() {
        FeedLineParser parser = new FeedLineParser();
        // These will throw because split(" ", 2) will not produce 2 parts if there is no space
        assertThatThrownBy(() -> parser.parse("onlyid"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
