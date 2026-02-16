package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedDiscoveryTest {

    @Test
    public void shouldDiscoverEntriesFromGemlog() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("0000007e", "gemini://aelspire.info/");
        
        String content = "# Aelspire's Log\n" +
                "\n" +
                "=> /posts/2026-02-15-first.gmi 2026-02-15 First Post\n" +
                "=> gemini://example.com/other 2026-01-01 External Post\n" +
                "## Section Heading\n" +
                "Some text here.";
        
        List<FeedEntry> entries = discovery.discover(feed, content);
        
        // Should discover:
        // 1. Link + Date
        // 2. Headings (level 1 and 2)
        
        assertThat(entries).hasSize(4); // # Aelspire's Log, 2 links, ## Section Heading
        
        // Link 1 resolved
        assertThat(entries).anySatisfy(e -> {
            assertThat(e.url()).isEqualTo("gemini://aelspire.info/posts/2026-02-15-first.gmi");
            assertThat(e.title()).isEqualTo("First Post");
            // 2026-02-15
            assertThat(e.timestamp1()).isEqualTo(java.time.LocalDate.parse("2026-02-15").atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC));
        });

        // Heading 1
        assertThat(entries).anySatisfy(e -> {
            assertThat(e.url()).isEqualTo("gemini://aelspire.info/#Aelspire%27s%20Log");
            assertThat(e.title()).isEqualTo("Aelspire's Log");
        });
    }

    @Test
    public void shouldDiscoverEntriesFromAtomFeed() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("0000007d", "gemini://geminiprotocol.net/news/");
        
        String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<feed xmlns=\"http://www.w3.org/2005/Atom\">\n" +
                "  <title>Gemini Protocol News</title>\n" +
                "  <entry>\n" +
                "    <title>Mailing list downtime</title>\n" +
                "    <link href=\"gemini://geminiprotocol.net/news/2022_01_16.gmi\"/>\n" +
                "    <published>2022-01-16</published>\n" + // parseDate only supports YYYY-MM-DD
                "  </entry>\n" +
                "</feed>";
        
        List<FeedEntry> entries = discovery.discover(feed, content);
        
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).url()).isEqualTo("gemini://geminiprotocol.net/news/2022_01_16.gmi");
        assertThat(entries.get(0).title()).isEqualTo("Mailing list downtime");
        assertThat(entries.get(0).timestamp1()).isEqualTo(1642291200L); // 2022-01-16
    }

    @Test
    public void shouldHandleNullOrEmptyInputs() {
        FeedDiscovery discovery = new FeedDiscovery();
        Feed feed = new Feed("1", "gemini://example.com/");
        
        assertThat(discovery.discoverTitle(null)).isNull();
        assertThat(discovery.discoverTitle("")).isNull();
        assertThat(discovery.discoverTitle("  ")).isNull();
        
        assertThat(discovery.discover(null, "content")).isEmpty();
        assertThat(discovery.discover(feed, null)).isEmpty();
        assertThat(discovery.discover(feed, "")).isEmpty();
        assertThat(discovery.discover(feed, "  ")).isEmpty();
    }
}
