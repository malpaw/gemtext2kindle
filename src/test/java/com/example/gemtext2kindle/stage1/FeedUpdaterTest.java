package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedUpdaterTest {

    @Test
    public void shouldUpdateProcessedTimestamp(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path feedsPath = tempDir.resolve("feeds.txt");
        Files.writeString(feedsPath, 
            "123\n" +
            "# Feeds\n" +
            "33 url\n" +
            "# Entries\n" +
            "33\n" +
            "1000\n" +
            "0\n" +
            "gemini://example.com/unread\n" +
            "Title\n" +
            "33\n" +
            "2000\n" +
            "9999\n" +
            "gemini://example.com/read\n" +
            "Title2"
        );
        
        FeedUpdater updater = new FeedUpdater();
        updater.markAsRead(feedsPath, "gemini://example.com/unread");
        
        List<String> lines = Files.readAllLines(feedsPath);
        // Find line after the unread URL's position. 
        // Index 6 should be the timestamp for the first entry.
        assertThat(lines.get(6)).isNotEqualTo("0");
        assertThat(Long.parseLong(lines.get(6))).isGreaterThan(1000L);
        
        // Ensure read one didn't change
        assertThat(lines.get(11)).isEqualTo("9999");
    }
}
