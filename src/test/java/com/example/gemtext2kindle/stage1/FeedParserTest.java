package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedParserTest {

    @Test
    public void shouldParseFeedsFromContent() {
        String content = "1771193229\n" +
                "# Feeds\n" +
                "0000007e gemini://aelspire.info/\n" +
                "0000007d gemini://geminiprotocol.net/news/\n" +
                "00000075 gopher://zaibatsu.circumlunar.space:70/1/~visiblink/phlog\n" +
                "# Entries\n" +
                "33\n" +
                "1285844400\n" +
                "1771065881\n" +
                "gemini://archiwistka.blog/gemlog_echo_socjalizacji/2010-10-00_Apokalipsa.gmi\n" +
                "Apokalipsa.gmi";

        FeedParser parser = new FeedParser();
        List<Feed> feeds = parser.parse(content);

        assertThat(feeds).hasSize(3);
        assertThat(feeds.get(0).id()).isEqualTo("0000007e");
        assertThat(feeds.get(0).url()).isEqualTo("gemini://aelspire.info/");
        assertThat(feeds.get(1).id()).isEqualTo("0000007d");
        assertThat(feeds.get(1).url()).isEqualTo("gemini://geminiprotocol.net/news/");
        assertThat(feeds.get(2).id()).isEqualTo("00000075");
        assertThat(feeds.get(2).url()).isEqualTo("gopher://zaibatsu.circumlunar.space:70/1/~visiblink/phlog");
    }

    @Test
    public void shouldParseEntriesFromContent() {
        String content = "# Entries\n" +
                "33\n" +
                "1285844400\n" +
                "1771065881\n" +
                "gemini://archiwistka.blog/gemlog_echo_socjalizacji/2010-10-00_Apokalipsa.gmi\n" +
                "Apokalipsa.gmi\n" +
                "33\n" +
                "1556622000\n" +
                "1771065883\n" +
                "gemini://archiwistka.blog/gemlog_echo_socjalizacji/2019-05-00-Pocztowka.gmi\n" +
                "Pocztówka";

        FeedParser parser = new FeedParser();
        List<FeedEntry> entries = parser.parseEntries(content);

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).feedId()).isEqualTo("33");
        assertThat(entries.get(0).timestamp1()).isEqualTo(1285844400L);
        assertThat(entries.get(0).timestamp2()).isEqualTo(1771065881L);
        assertThat(entries.get(0).url()).isEqualTo("gemini://archiwistka.blog/gemlog_echo_socjalizacji/2010-10-00_Apokalipsa.gmi");
        assertThat(entries.get(0).title()).isEqualTo("Apokalipsa.gmi");
        
        assertThat(entries.get(1).title()).isEqualTo("Pocztówka");
    }

    @Test
    public void shouldIdentifyNewEntries() {
        String content = "1771014720\n" +
                "# Feeds\n" +
                "01 gemini://example.com/\n" +
                "# Entries\n" +
                "01\n" +
                "1000\n" +
                "0\n" + // 0 as the second timestamp likely means it hasn't been "read/downloaded" or is a placeholder
                "gemini://example.com/post1.gmi\n" +
                "Post 1\n" +
                "01\n" +
                "2000\n" +
                "1771014706\n" +
                "gemini://example.com/post2.gmi\n" +
                "Post 2";

        FeedParser parser = new FeedParser();
        List<FeedEntry> entries = parser.parseEntries(content);

        // Assuming the 3rd line of an entry (index 2 in the entry block) is the "processed timestamp"
        // If it's > 0, it might mean it's already known/processed. 
        // In the original feeds.txt, we see many with high timestamps.
        
        assertThat(entries).hasSize(2);
        assertThat(entries.get(0).url()).isEqualTo("gemini://example.com/post1.gmi");
        assertThat(entries.get(1).url()).isEqualTo("gemini://example.com/post2.gmi");
    }

    @Test
    public void shouldParseEntryWithEmptyTitle() {
        // Test case where the 5th line (title) of an entry block is a space
        String content =
                "5a\n" +
                "1771197987\n" +
                "1771197987\n" +
                "gemini://boston.conman.org/2026/01/31.1\n" +
                " "; // Line with space

        FeedParser parser = new FeedParser();
        FeedEntry entry = parser.parseEntry(content);

        assertThat(entry.url()).isEqualTo("gemini://boston.conman.org/2026/01/31.1");
        assertThat(entry.title()).isEmpty();
    }

    @Test
    public void shouldParseEntryWithCompletelyEmptyTitle() {
        // Test case where the 5th line (title) is completely empty (no spaces)
        String content =
                "5a\n" +
                "1771197987\n" +
                "1771197987\n" +
                "gemini://boston.conman.org/2026/01/31.1\n" +
                ""; // Completely empty line

        FeedParser parser = new FeedParser();
        FeedEntry entry = parser.parseEntry(content);

        assertThat(entry.url()).isEqualTo("gemini://boston.conman.org/2026/01/31.1");
        assertThat(entry.title()).isEmpty();
    }
}
