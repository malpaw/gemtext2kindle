package com.example.gemtext2kindle.stage1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FetcherMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: FetcherMain <feeds.txt path> <output queue dir>");
            System.exit(1);
        }

        Path feedsPath = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        Files.createDirectories(outputDir);

        String content = Files.readString(feedsPath, StandardCharsets.UTF_8);
        FeedParser parser = new FeedParser();
        List<FeedEntry> entries = parser.parseEntries(content);
        
        ProtocolClient client = new ProtocolClient();

        Files.readAllLines(feedsPath, StandardCharsets.UTF_8);
        
        FeedUpdater updater = new FeedUpdater();
        for (FeedEntry entry : entries) {
            if (entry.timestamp2() == 0) {
                System.out.println("Fetching new entry: " + entry.url());
                try {
                    String articleContent = client.fetch(entry.url());
                    String fileName = sanitizeFileName(entry.url()) + ".gmi";
                    Files.writeString(outputDir.resolve(fileName), articleContent);
                    
                    System.out.println("Success: " + fileName);
                    updater.markAsRead(feedsPath, entry.url());
                } catch (Exception e) {
                    System.err.println("Failed to fetch " + entry.url() + ": " + e.getMessage());
                }
            }
        }
    }

    private static String sanitizeFileName(String url) {
        return url.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
