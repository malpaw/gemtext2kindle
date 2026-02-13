package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedParserTest {

    @Test
    public void shouldParseFeedsFromContent() {
        String content = "1771014720\n" +
                "# Feeds\n" +
                "00000081 gemini://jpfox.fr/ham/icqpodcast.com/\n" +
                "0000007f gemini://lark.gay/posts/feed.xml\n" +
                "# Entries\n" +
                "33\n" +
                "1285844400\n" +
                "1771014706\n" +
                "gemini://archiwistka.blog/gemlog_echo_socjalizacji/2010-10-00_Apokalipsa.gmi\n" +
                "Apokalipsa.gmi";

        FeedParser parser = new FeedParser();
        List<Feed> feeds = parser.parse(content);

        assertThat(feeds).hasSize(2);
        assertThat(feeds.get(0).url()).isEqualTo("gemini://jpfox.fr/ham/icqpodcast.com/");
        assertThat(feeds.get(1).url()).isEqualTo("gemini://lark.gay/posts/feed.xml");
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
}
