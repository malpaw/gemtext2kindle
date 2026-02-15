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
    public void shouldFetchArticlesAndMaintainReadState(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        // Setup artificial test-feeds.txt
        Path feedsPath = tempDir.resolve("test-feeds.txt");
        Path visitedPath = tempDir.resolve("visited.2.txt");

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
        FetcherMain.run(feedsPath, visitedPath, q1Dir, new GeneratorProtocolClient());

        // Verify queue 1 contents
        try (Stream<Path> files = Files.list(q1Dir)) {
            assertThat(files.count()).isEqualTo(3); // 2 standard + 1 generated from feed1 base URL via Algorithm B (Headings)
        }

        // Verify visited.2.txt was created and contains the two URLs
        String visitedContent = Files.readString(visitedPath, StandardCharsets.UTF_8);
        assertThat(visitedContent).contains("gemini://example.com/feed1/post1.gmi");
        assertThat(visitedContent).contains("gemini://example.com/feed1/post2.gmi");

        // Verify test-feeds.txt was NOT purged of entries (matching Lagrange behavior)
        String updatedContent = Files.readString(feedsPath, StandardCharsets.UTF_8);
        FeedParser parser = new FeedParser();
        var entries = parser.parseEntries(updatedContent);
        assertThat(entries).hasSize(3); // 2 original + 1 discovered from base URL heading
        
        // Verify global timestamp was updated
        assertThat(Long.parseLong(updatedContent.split("\n")[0])).isGreaterThan(100L);

        // Run fetcher AGAIN - should fetch 0 articles this time because they are visited
        Path q2Dir = tempDir.resolve("queue2");
        Files.createDirectories(q2Dir);
        FetcherMain.run(feedsPath, visitedPath, q2Dir, new GeneratorProtocolClient());
        try (Stream<Path> files = Files.list(q2Dir)) {
            assertThat(files.count()).isEqualTo(0);
        }
    }
}
