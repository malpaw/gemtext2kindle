package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedUpdaterTest {

    @Test
    public void shouldNotDeleteProcessedEntriesInLagrangeMode(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path feedsPath = tempDir.resolve("feeds.txt");
        String initialContent = "123\n" +
                "# Feeds\n" +
                "33 url\n" +
                "# Entries\n" +
                "33\n" +
                "1000\n" +
                "0\n" +
                "gemini://example.com/to-delete\n" +
                "Title\n" +
                "33\n" +
                "2000\n" +
                "9999\n" +
                "gemini://example.com/to-keep\n" +
                "Title2";
        Files.writeString(feedsPath, initialContent);
        
        FeedUpdater updater = new FeedUpdater();
        // In Lagrange mode, removeEntries is a no-op because unread state is handled by visited.2.txt
        updater.removeEntries(feedsPath, List.of("gemini://example.com/to-delete"));
        
        List<String> lines = Files.readAllLines(feedsPath);
        assertThat(lines).hasSize(14); 
        assertThat(String.join("\n", lines)).contains("gemini://example.com/to-delete");
    }

    @Test
    public void shouldUpdateGlobalTimestamp(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path feedsPath = tempDir.resolve("feeds.txt");
        Files.writeString(feedsPath, "100\n# Feeds\n...");
        
        FeedUpdater updater = new FeedUpdater();
        updater.updateGlobalTimestamp(feedsPath);
        
        List<String> lines = Files.readAllLines(feedsPath);
        assertThat(Long.parseLong(lines.get(0))).isGreaterThan(100L);
    }

    @Test
    public void shouldHandleNullInputs(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        FeedUpdater updater = new FeedUpdater();
        Path feedsPath = tempDir.resolve("feeds.txt");
        Files.writeString(feedsPath, "100\n# Feeds\n...\n# Entries\n");

        // Should not throw
        updater.removeEntries(null, List.of("url"));
        updater.removeEntries(feedsPath, null);
        updater.removeEntries(feedsPath, List.of());
        
        updater.updateGlobalTimestamp(null);
        
        updater.appendEntries(null, List.of(new FeedEntry("1", 0, 0, "u", "t")));
        updater.appendEntries(feedsPath, null);
        updater.appendEntries(feedsPath, List.of());
    }
}
