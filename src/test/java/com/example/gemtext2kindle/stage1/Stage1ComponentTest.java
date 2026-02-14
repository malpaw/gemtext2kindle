package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

public class Stage1ComponentTest {

    @Test
    public void shouldFetchArticlesAndRemoveThemFromFeedsFile(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        // Setup artificial test-feeds.txt
        Path feedsPath = tempDir.resolve("test-feeds.txt");
        String initialContent = "100\n" +
                "# Feeds\n" +
                "01 gemini://example.com/feed1\n" +
                "# Entries\n" +
                "01\n" +
                "1000\n" +
                "1111\n" +
                "gemini://example.com/feed1/post1.gmi\n" +
                "Post 1\n" +
                "01\n" +
                "2000\n" +
                "2222\n" +
                "gemini://example.com/feed1/post2.gmi\n" +
                "Post 2\n";
        Files.writeString(feedsPath, initialContent, StandardCharsets.UTF_8);

        Path q1Dir = tempDir.resolve("queue1");
        Files.createDirectories(q1Dir);

        // Run fetcher with GeneratorProtocolClient
        FetcherMain.run(feedsPath, q1Dir, new GeneratorProtocolClient());

        // Verify queue 1 contents
        try (Stream<Path> files = Files.list(q1Dir)) {
            assertThat(files.count()).isEqualTo(2);
        }

        // Verify test-feeds.txt was updated: entries removed, timestamp updated
        String updatedContent = Files.readString(feedsPath, StandardCharsets.UTF_8);
        FeedParser parser = new FeedParser();
        var entries = parser.parseEntries(updatedContent);
        
        assertThat(entries).isEmpty();
        assertThat(Long.parseLong(updatedContent.split("\n")[0])).isGreaterThan(100L);
    }
}
