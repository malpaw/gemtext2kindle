package com.example.gemtext2kindle.stage1;

import java.util.ArrayList;
import java.util.List;

public class FeedParser {

    public List<Feed> parse(String content) {
        List<Feed> feeds = new ArrayList<>();
        String[] lines = content.split("\n");
        boolean inFeeds = false;
        for (String line : lines) {
            if (line.startsWith("# Feeds")) {
                inFeeds = true;
                continue;
            }
            if (line.startsWith("# Entries")) {
                break;
            }
            if (inFeeds && !line.isBlank()) {
                String[] parts = line.trim().split("\\s+", 2);
                if (parts.length == 2) {
                    feeds.add(new Feed(parts[0], parts[1]));
                }
            }
        }
        return feeds;
    }

    public List<FeedEntry> parseEntries(String content) {
        List<FeedEntry> entries = new ArrayList<>();
        String[] lines = content.split("\n");
        int i = 0;
        while (i < lines.length && !lines[i].startsWith("# Entries")) {
            i++;
        }
        i++; // skip # Entries
        while (i + 4 < lines.length) {
            String feedId = lines[i++].trim();
            long t1 = Long.parseLong(lines[i++].trim());
            long t2 = Long.parseLong(lines[i++].trim());
            String url = lines[i++].trim();
            String title = lines[i++].trim();
            entries.add(new FeedEntry(feedId, t1, t2, url, title));
        }
        return entries;
    }
}
