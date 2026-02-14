package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedUpdaterTest {

    @Test
    public void shouldDeleteProcessedEntries(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path feedsPath = tempDir.resolve("feeds.txt");
        Files.writeString(feedsPath, 
            "123\n" +
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
            "Title2"
        );
        
        FeedUpdater updater = new FeedUpdater();
        updater.removeEntries(feedsPath, List.of("gemini://example.com/to-delete"));
        
        List<String> lines = Files.readAllLines(feedsPath);
        assertThat(lines).hasSize(9); // 1 (ts) + 2 (feeds) + 1 (entries header) + 5 (remaining entry)
        assertThat(String.join("\n", lines)).doesNotContain("gemini://example.com/to-delete");
        assertThat(String.join("\n", lines)).contains("gemini://example.com/to-keep");
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
}
