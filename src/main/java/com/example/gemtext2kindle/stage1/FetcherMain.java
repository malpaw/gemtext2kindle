package com.example.gemtext2kindle.stage1;

import com.example.gemtext2kindle.common.ArticleMetadata;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FetcherMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: FetcherMain <feeds.txt path> <visited.2.txt path> <output queue dir>");
            System.exit(1);
        }

        Path feedsPath = Path.of(args[0]);
        Path visitedPath = Path.of(args[1]);
        Path outputDir = Path.of(args[2]);
        
        run(feedsPath, visitedPath, outputDir, new RealProtocolClient());
    }

    public static void run(Path feedsPath, Path visitedPath, Path outputDir, ProtocolClient client) throws IOException {
        Files.createDirectories(outputDir);

        VisitedStore visitedStore = new VisitedStore(visitedPath);

        String content = Files.readString(feedsPath, StandardCharsets.UTF_8);
        FeedParser parser = new FeedParser();
        List<Feed> feeds = parser.parse(content);
        List<FeedEntry> entries = parser.parseEntries(content);
        
        Map<String, String> feedMap = feeds.stream()
                .collect(Collectors.toMap(Feed::id, Feed::url));

        FeedUpdater updater = new FeedUpdater();
        FeedStatusChecker checker = new FeedStatusChecker(visitedStore);
        
        List<String> processedUrls = new ArrayList<>();

        for (FeedEntry entry : entries) {
            if (checker.needsDownload(entry)) {
                System.out.println("Fetching new entry: " + entry.url());
                try {
                    String articleContent = client.fetch(entry.url());
                    
                    String feedUrl = feedMap.getOrDefault(entry.feedId(), "Unknown Feed");
                    ArticleMetadata meta = new ArticleMetadata(feedUrl, null, entry.title(), entry.timestamp1(), entry.url());
                    
                    String fullContent = meta.serialize() + articleContent;
                    
                    String fileName = sanitizeFileName(entry.url()) + ".gmi";
                    Files.writeString(outputDir.resolve(fileName), fullContent);
                    
                    System.out.println("Success: " + fileName);
                    
                    // Mark as visited
                    visitedStore.addVisit(entry.url());
                    processedUrls.add(entry.url());
                } catch (Exception e) {
                    System.err.println("Failed to fetch " + entry.url() + ": " + e.getMessage());
                }
            }
        }

        if (!processedUrls.isEmpty()) {
            updater.removeEntries(feedsPath, processedUrls);
        }
        updater.updateGlobalTimestamp(feedsPath);
    }

    private static String sanitizeFileName(String url) {
        return url.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
