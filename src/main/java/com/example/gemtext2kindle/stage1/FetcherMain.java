package com.example.gemtext2kindle.stage1;

import com.example.gemtext2kindle.common.ArticleMetadata;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        List<FeedEntry> existingEntries = parser.parseEntries(content);
        
        // --- Discovery Phase ---
        System.out.println("Starting feed discovery...");
        FeedDiscovery discovery = new FeedDiscovery();
        FeedUpdater updater = new FeedUpdater();
        List<FeedEntry> allDiscovered = new ArrayList<>();
        Set<String> existingUrls = existingEntries.stream()
                .map(FeedEntry::url)
                .collect(Collectors.toSet());

        for (Feed feed : feeds) {
            try {
                String feedContent = client.fetch(feed.url());
                List<FeedEntry> discovered = discovery.discover(feed, feedContent);
                for (FeedEntry de : discovered) {
                    if (!existingUrls.contains(de.url())) {
                        allDiscovered.add(de);
                        existingUrls.add(de.url());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to discover from " + feed.url() + ": " + e.getMessage());
            }
        }
        
        if (!allDiscovered.isEmpty()) {
            System.out.println("Discovered " + allDiscovered.size() + " new items.");
            updater.appendEntries(feedsPath, allDiscovered);
            // Reload entries to include newly discovered ones
            content = Files.readString(feedsPath, StandardCharsets.UTF_8);
            existingEntries = parser.parseEntries(content);
        } else {
            System.out.println("No new items discovered.");
        }

        Map<String, String> feedMap = feeds.stream()
                .collect(Collectors.toMap(Feed::id, Feed::url));

        FeedStatusChecker checker = new FeedStatusChecker(visitedStore);
        
        List<String> processedUrls = new ArrayList<>();

        int totalItems = existingEntries.size();
        int estimatedItems = 0;
        int actualDownloaded = 0;

        for (FeedEntry entry : existingEntries) {
            if (checker.needsDownload(entry)) {
                estimatedItems++;
                System.out.println("Fetching new entry: " + entry.url());
                try {
                    String articleContent = client.fetch(entry.url());
                    
                    String feedUrl = feedMap.getOrDefault(entry.feedId(), "Unknown Feed");
                    ArticleMetadata meta = new ArticleMetadata(feedUrl, null, entry.title(), entry.timestamp1(), entry.url());
                    
                    String fullContent = meta.serialize() + articleContent;
                    
                    String fileName = sanitizeFileName(entry.url()) + ".gmi";
                    Files.writeString(outputDir.resolve(fileName), fullContent);
                    
                    System.out.println("Success: " + fileName);
                    actualDownloaded++;
                    
                    // Mark as visited
                    visitedStore.addVisit(entry.url());
                    processedUrls.add(entry.url());
                } catch (Exception e) {
                    System.err.println("Failed to fetch " + entry.url() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("Total items in the feed: " + totalItems);
        System.out.println("Estimated items to download: " + estimatedItems);
        System.out.println("Actual number of items downloaded: " + actualDownloaded);

        if (!processedUrls.isEmpty()) {
            updater.removeEntries(feedsPath, processedUrls);
        }
        updater.updateGlobalTimestamp(feedsPath);
    }

    private static String sanitizeFileName(String url) {
        return url.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
