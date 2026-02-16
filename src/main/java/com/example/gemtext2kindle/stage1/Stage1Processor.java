package com.example.gemtext2kindle.stage1;

import com.example.gemtext2kindle.common.ArticleMetadata;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Stage1Processor {
    private final ProtocolClient client;
    private final VisitedStore visitedStore;
    private final FeedParser parser = new FeedParser();
    private final FeedDiscovery discovery = new FeedDiscovery();
    private final FeedUpdater updater = new FeedUpdater();
    private final BookmarkParser bookmarkParser = new BookmarkParser();

    public Stage1Processor(ProtocolClient client, VisitedStore visitedStore) {
        this.client = client;
        this.visitedStore = visitedStore;
    }

    public void process(Path feedsPath, Path outputDir, Path bookmarksPath) throws IOException {
        String content = Files.readString(feedsPath, StandardCharsets.UTF_8);
        List<Feed> feeds = parser.parse(content);
        List<FeedEntry> existingEntries = parser.parseEntries(content);

        Map<String, BookmarkParser.Bookmark> bookmarks = loadBookmarks(bookmarksPath);
        Map<String, String> discoveredFeedTitles = discoverNewEntries(feedsPath, feeds, existingEntries);

        // Reload entries after discovery
        content = Files.readString(feedsPath, StandardCharsets.UTF_8);
        existingEntries = parser.parseEntries(content);

        downloadEntries(feedsPath, outputDir, feeds, existingEntries, bookmarks, discoveredFeedTitles);
        
        updater.updateGlobalTimestamp(feedsPath);
    }

    private Map<String, BookmarkParser.Bookmark> loadBookmarks(Path bookmarksPath) {
        if (bookmarksPath != null && Files.exists(bookmarksPath)) {
            try {
                return bookmarkParser.parse(Files.readString(bookmarksPath, StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println("Failed to load bookmarks: " + e.getMessage());
            }
        }
        return Map.of();
    }

    private Map<String, String> discoverNewEntries(Path feedsPath, List<Feed> feeds, List<FeedEntry> existingEntries) throws IOException {
        Set<String> existingUrls = existingEntries.stream().map(FeedEntry::url).collect(Collectors.toSet());
        List<FeedEntry> allDiscovered = new ArrayList<>();
        java.util.Map<String, String> discoveredFeedTitles = new java.util.HashMap<>();

        for (Feed feed : feeds) {
            try {
                String feedContent = client.fetch(feed.url());
                String discoveredTitle = discovery.discoverTitle(feedContent);
                if (discoveredTitle != null && !discoveredTitle.isBlank()) {
                    discoveredFeedTitles.put(feed.id(), discoveredTitle);
                }

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
            updater.appendEntries(feedsPath, allDiscovered);
        }
        return discoveredFeedTitles;
    }

    private void downloadEntries(Path feedsPath, Path outputDir, List<Feed> feeds, List<FeedEntry> existingEntries, 
                               Map<String, BookmarkParser.Bookmark> bookmarks, Map<String, String> discoveredFeedTitles) throws IOException {
        
        Map<String, String> feedMap = feeds.stream().collect(Collectors.toMap(Feed::id, Feed::url, (v1, v2) -> v1));
        FeedStatusChecker checker = new FeedStatusChecker(visitedStore);
        List<String> processedUrls = new ArrayList<>();

        for (FeedEntry entry : existingEntries) {
            try {
                if (checker.needsDownload(entry)) {
                    processEntry(entry, outputDir, bookmarks, discoveredFeedTitles, feedMap, processedUrls);
                }
            } catch (Exception e) {
                System.err.println("Error processing entry: " + entry.url() + " - " + e.getMessage());
            }
        }

        if (!processedUrls.isEmpty()) {
            updater.removeEntries(feedsPath, processedUrls);
        }
    }

    private void processEntry(FeedEntry entry, Path outputDir, Map<String, BookmarkParser.Bookmark> bookmarks, 
                            Map<String, String> discoveredFeedTitles, Map<String, String> feedMap, 
                            List<String> processedUrls) {
        try {
            String articleContent = client.fetch(entry.url());
            String feedDisplayName = getFeedDisplayName(entry.feedId(), bookmarks, discoveredFeedTitles, feedMap);
            String feedIcon = bookmarks.containsKey(entry.feedId()) ? bookmarks.get(entry.feedId()).icon() : null;

            ArticleMetadata meta = new ArticleMetadata(feedDisplayName, feedIcon, entry.title(), entry.timestamp1(), entry.url());
            String fullContent = meta.serialize() + articleContent;
            
            String fileName = sanitizeFileName(entry.url()) + ".gmi";
            Files.writeString(outputDir.resolve(fileName), fullContent);
            
            visitedStore.addVisit(entry.url());
            processedUrls.add(entry.url());
            System.out.println("Success: " + fileName);
        } catch (Exception e) {
            System.err.println("Failed to fetch article " + entry.url() + ": " + e.getMessage());
        }
    }

    private String getFeedDisplayName(String feedId, Map<String, BookmarkParser.Bookmark> bookmarks, 
                                    Map<String, String> discoveredFeedTitles, Map<String, String> feedMap) {
        if (bookmarks.containsKey(feedId)) return bookmarks.get(feedId).title();
        if (discoveredFeedTitles.containsKey(feedId)) return discoveredFeedTitles.get(feedId);
        return feedMap.getOrDefault(feedId, "Unknown Feed");
    }

    private String sanitizeFileName(String url) {
        return url.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
