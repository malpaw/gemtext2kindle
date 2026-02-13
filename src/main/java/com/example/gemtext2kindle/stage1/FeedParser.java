package com.example.gemtext2kindle.stage1;

import java.util.ArrayList;
import java.util.List;

public class FeedParser {
    private final FeedLineParser lineParser = new FeedLineParser();
    private final EntryBlockParser entryParser = new EntryBlockParser();

    public List<Feed> parse(String content) {
        FeedFileScanner scanner = new FeedFileScanner(List.of(content.split("\n")));
        List<String> feedLines = scanner.getLinesInSection("# Feeds", "# Entries");
        List<Feed> feeds = new ArrayList<>();
        for (String line : feedLines) {
            Feed feed = lineParser.parse(line);
            if (feed != null) feeds.add(feed);
        }
        return feeds;
    }

    public List<FeedEntry> parseEntries(String content) {
        FeedFileScanner scanner = new FeedFileScanner(List.of(content.split("\n")));
        List<String> entryLines = scanner.getLinesInSection("# Entries", null);
        List<FeedEntry> entries = new ArrayList<>();
        
        for (int i = 0; i + 4 < entryLines.size(); i += 5) {
            entries.add(entryParser.parse(entryLines.subList(i, i + 5)));
        }
        return entries;
    }
}
