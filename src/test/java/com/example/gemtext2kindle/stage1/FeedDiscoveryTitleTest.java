package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedDiscoveryTitleTest {

    @Test
    public void shouldUseUrlAsTitleWhenGemtextTitleIsMissing() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("7e", "gemini://example.com/");
        
        // Link + Date with no text after date
        String content = "=> /posts/no-title.gmi 2026-02-15";
        
        List<FeedEntry> entries = discovery.discover(feed, content);
        
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).url()).isEqualTo("gemini://example.com/posts/no-title.gmi");
        assertThat(entries.get(0).title()).isEqualTo("gemini://example.com/posts/no-title.gmi");
    }

    @Test
    public void shouldUseUrlAsTitleWhenXmlTitleIsMissing() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("7d", "gemini://example.com/");
        
        String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                "  <entry>\n" +
                "    <link href=\"gemini://example.com/news/1.gmi\"/>\n" +
                "  </entry>\n" +
                "</feed>";
        
        List<FeedEntry> entries = discovery.discover(feed, content);
        
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).url()).isEqualTo("gemini://example.com/news/1.gmi");
        assertThat(entries.get(0).title()).isEqualTo("gemini://example.com/news/1.gmi");
    }

    @Test
    public void shouldHandleEmptyTitleInXml() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("7d", "gemini://example.com/");
        
        String content = "<feed><entry><title></title><link href=\"gemini://example.com/2.gmi\"/></entry></feed>";
        
        List<FeedEntry> entries = discovery.discover(feed, content);
        
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).url()).isEqualTo("gemini://example.com/2.gmi");
        assertThat(entries.get(0).title()).isEqualTo("gemini://example.com/2.gmi");
    }

    @Test
    public void shouldHandleTitleWithOnlyWhitespaceInXml() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("7d", "gemini://example.com/");
        
        String content = "<feed><entry><title>   </title><link href=\"gemini://example.com/3.gmi\"/></entry></feed>";
        
        List<FeedEntry> entries = discovery.discover(feed, content);
        
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).title()).isEqualTo("gemini://example.com/3.gmi");
    }

    @Test
    public void shouldHandleTitleWithOnlyWhitespaceInGemtext() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("7e", "gemini://example.com/");
        
        String content = "=> /posts/4.gmi 2026-02-15    ";
        
        List<FeedEntry> entries = discovery.discover(feed, content);
        
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).title()).isEqualTo("gemini://example.com/posts/4.gmi");
    }

    @Test
    public void shouldTrimTitle() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("7e", "gemini://example.com/");
        
        String content = "=> /posts/5.gmi 2026-02-15  Some Title  ";
        
        List<FeedEntry> entries = discovery.discover(feed, content);
        
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).title()).isEqualTo("Some Title");
    }
}
