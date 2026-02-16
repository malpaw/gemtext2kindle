package com.example.gemtext2kindle.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ArticleMetadataTest {

    @Test
    public void shouldSerializeAndDeserialize() {
        ArticleMetadata meta = new ArticleMetadata("My Feed", "Hello Gemini", 1707822000L, "gemini://example.com");
        String serialized = meta.serialize();
        
        assertThat(serialized).contains("feed: My Feed");
        assertThat(serialized).contains("title: Hello Gemini");
        assertThat(serialized).contains("date: 2024-02-13");
        
        ArticleMetadata deserialized = ArticleMetadata.deserialize(serialized);
        assertThat(deserialized.feedName()).isEqualTo("My Feed");
        assertThat(deserialized.title()).isEqualTo("Hello Gemini");
        assertThat(deserialized.url()).isEqualTo("gemini://example.com");
    }
}
