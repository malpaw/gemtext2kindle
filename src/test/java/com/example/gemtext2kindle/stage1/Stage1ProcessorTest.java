package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;

public class Stage1ProcessorTest {

    @Test
    public void shouldGracefullyHandleDiscoveryFailure(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path feedsPath = tempDir.resolve("feeds.txt");
        Path visitedPath = tempDir.resolve("visited.2.txt");
        Path outputDir = tempDir.resolve("queue");
        Files.createDirectories(outputDir);

        // Feed that will cause discovery failure
        String content = "100\n# Feeds\nBAD gemini://fail.com\nGOOD gemini://success.com\n# Entries\n";
        Files.writeString(feedsPath, content, StandardCharsets.UTF_8);

        ProtocolClient client = url -> {
            if (url.contains("fail.com")) throw new IOException("Network error");
            return "# Success"; // Heading without level for Algo B
        };

        VisitedStore visitedStore = new VisitedStore(visitedPath);
        Stage1Processor processor = new Stage1Processor(client, visitedStore);
        
        // Should not throw exception
        processor.process(feedsPath, outputDir, null);

        // Verify that the GOOD feed was processed despite the BAD one
        // # Success generates one heading entry
        try (Stream<Path> files = Files.list(outputDir)) {
            assertThat(files.count()).isEqualTo(1);
        }
    }

    @Test
    public void shouldGracefullyHandleDownloadFailure(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path feedsPath = tempDir.resolve("feeds.txt");
        Path visitedPath = tempDir.resolve("visited.2.txt");
        Path outputDir = tempDir.resolve("queue");
        Files.createDirectories(outputDir);

        String content = "100\n# Feeds\n01 gemini://example.com\n# Entries\n" +
                "01\n1000\n0\ngemini://example.com/fail\nTitle Fail\n" +
                "01\n1000\n0\ngemini://example.com/success\nTitle Success\n";
        Files.writeString(feedsPath, content, StandardCharsets.UTF_8);

        ProtocolClient client = url -> {
            if (url.endsWith("/fail")) throw new IOException("Download error");
            return "Article content";
        };

        VisitedStore visitedStore = new VisitedStore(visitedPath);
        Stage1Processor processor = new Stage1Processor(client, visitedStore);
        
        processor.process(feedsPath, outputDir, null);

        // Verify success article exists, failed does not
        try (Stream<Path> files = Files.list(outputDir)) {
            assertThat(files.count()).isEqualTo(1);
        }
    }
}
