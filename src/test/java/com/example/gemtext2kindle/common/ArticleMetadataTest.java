package com.example.gemtext2kindle.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArticleMetadataTest {

    @Test
    void testDeserializeValid() {
        String content = "---\nfeed: My Feed\nicon: ⚡\ntitle: My Title\ndate: 2024-02-17\nurl: gemini://example.com\n---\nContent";
        ArticleMetadata meta = ArticleMetadata.deserialize(content);
        assertNotNull(meta);
        assertEquals("My Feed", meta.feedName());
        assertEquals("⚡", meta.feedIcon());
        assertEquals("My Title", meta.title());
        assertEquals("gemini://example.com", meta.url());
        // 2024-02-17 at start of day
        assertTrue(meta.timestamp() > 0);
    }

    @Test
    void testDeserializeNull() {
        assertNull(ArticleMetadata.deserialize(null));
    }

    @Test
    void testDeserializeEmpty() {
        assertNull(ArticleMetadata.deserialize(""));
    }

    @Test
    void testDeserializeMissingFeed() {
        String content = "---\ntitle: My Title\n---\n";
        assertNull(ArticleMetadata.deserialize(content));
    }

    @Test
    void testDeserializeMissingTitle() {
        String content = "---\nfeed: My Feed\n---\n";
        assertNull(ArticleMetadata.deserialize(content));
    }

    @Test
    void testDeserializeMalformedDate() {
        String content = "---\nfeed: My Feed\ntitle: T\ndate: not-a-date\n---\n";
        ArticleMetadata meta = ArticleMetadata.deserialize(content);
        assertNotNull(meta);
        assertEquals(0, meta.timestamp());
    }
}
