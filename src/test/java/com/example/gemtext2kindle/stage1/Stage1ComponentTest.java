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
    public void shouldFetchUnreadArticlesFromTestFeeds(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        // Setup artificial test-feeds.txt
        Path feedsPath = tempDir.resolve("test-feeds.txt");
        String initialContent = "1771014720\n" +
                "# Feeds\n" +
                "01 gemini://example.com/feed1\n" +
                "02 gopher://example.com/feed2\n" +
                "# Entries\n" +
                "01\n" +
                "1000\n" +
                "0\n" + // Unread
                "gemini://example.com/feed1/post1.gmi\n" +
                "Post 1 (Unread)\n" +
                "01\n" +
                "1100\n" +
                "1771014706\n" + // Read
                "gemini://example.com/feed1/post2.gmi\n" +
                "Post 2 (Read)\n" +
                "02\n" +
                "2000\n" +
                "0\n" + // Unread
                "gopher://example.com/feed2/article1.txt\n" +
                "Gopher Article 1 (Unread)\n";
        Files.writeString(feedsPath, initialContent, StandardCharsets.UTF_8);

        Path q1Dir = tempDir.resolve("queue1");
        Files.createDirectories(q1Dir);

        // Run fetcher with GeneratorProtocolClient
        FetcherMain.run(feedsPath, q1Dir, new GeneratorProtocolClient());

        // Verify queue 1 contents
        try (Stream<Path> files = Files.list(q1Dir)) {
            Object[] fileNames = files.map(f -> f.getFileName().toString()).toArray();
            assertThat(fileNames).hasSize(2);
            assertThat(fileNames).contains(
                "gemini___example.com_feed1_post1.gmi.gmi",
                "gopher___example.com_feed2_article1.txt.gmi"
            );
        }

        // Verify generated content
        String post1Content = Files.readString(q1Dir.resolve("gemini___example.com_feed1_post1.gmi.gmi"));
        assertThat(post1Content).contains("gemini://example.com/feed1/post1.gmi");

        // Verify test-feeds.txt was updated
        String updatedContent = Files.readString(feedsPath, StandardCharsets.UTF_8);
        FeedParser parser = new FeedParser();
        var entries = parser.parseEntries(updatedContent);
        
        assertThat(entries.get(0).timestamp2()).isGreaterThan(0L); // Was unread, now read
        assertThat(entries.get(1).timestamp2()).isEqualTo(1771014706L); // Stayed read
        assertThat(entries.get(2).timestamp2()).isGreaterThan(0L); // Was unread, now read
    }
}
