package com.example.gemtext2kindle.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ArticleMetadataTest {

    @Test
    public void shouldSerializeAndDeserialize() {
        ArticleMetadata meta = new ArticleMetadata("My Feed", "🙗", "Hello Gemini", 1707822000L, "gemini://example.com");
        String serialized = meta.serialize();
        
        assertThat(serialized).contains("feed: My Feed");
        assertThat(serialized).contains("icon: 🙗");
        assertThat(serialized).contains("title: Hello Gemini");
        assertThat(serialized).contains("date: 2024-02-13");
        
        ArticleMetadata deserialized = ArticleMetadata.deserialize(serialized);
        assertThat(deserialized.feedName()).isEqualTo("My Feed");
        assertThat(deserialized.feedIcon()).isEqualTo("🙗");
        assertThat(deserialized.title()).isEqualTo("Hello Gemini");
        assertThat(deserialized.url()).isEqualTo("gemini://example.com");
    }

    @Test
    public void shouldSerializeWithoutIcon() {
        ArticleMetadata meta = new ArticleMetadata("My Feed", null, "Hello Gemini", 1707822000L, "gemini://example.com");
        String serialized = meta.serialize();
        
        assertThat(serialized).contains("feed: My Feed");
        assertThat(serialized).doesNotContain("icon:");
        assertThat(serialized).contains("title: Hello Gemini");
        
        ArticleMetadata deserialized = ArticleMetadata.deserialize(serialized);
        assertThat(deserialized.feedName()).isEqualTo("My Feed");
        assertThat(deserialized.feedIcon()).isNull();
    }

    @Test
    public void shouldReturnNullOnInvalidFormat() {
        assertThat(ArticleMetadata.deserialize("invalid")).isNull();
        assertThat(ArticleMetadata.deserialize("")).isNull();
        assertThat(ArticleMetadata.deserialize(null)).isNull();
    }

    @Test
    public void shouldReturnNullIfFeedNameIsMissing() {
        String content = "---\ntitle: test\n---\n";
        assertThat(ArticleMetadata.deserialize(content)).isNull();
    }
}
