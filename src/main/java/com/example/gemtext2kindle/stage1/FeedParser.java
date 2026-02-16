package com.example.gemtext2kindle.stage1;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedParser {
    public static final String FEEDS_SECTION = "# Feeds";
    public static final String ENTRIES_SECTION = "# Entries";

    private final FeedLineParser lineParser = new FeedLineParser();
    private final EntryBlockParser entryParser = new EntryBlockParser();

    public List<Feed> parse(String content) {
        FeedFileScanner scanner = new FeedFileScanner(List.of(content.split("\n")));
        List<String> feedLines = scanner.getLinesInSection(FEEDS_SECTION, ENTRIES_SECTION);
        List<Feed> feeds = new ArrayList<>();
        for (String line : feedLines) {
            Feed feed = lineParser.parse(line);
            if (feed != null) feeds.add(feed);
        }
        return feeds;
    }

    public List<FeedEntry> parseEntries(String content) {
        List<String> allLines = List.of(content.split("\n", -1));
        FeedFileScanner scanner = new FeedFileScanner(allLines);
        
        List<String> entryLines = scanner.getLinesInSection(ENTRIES_SECTION, null);

        List<FeedEntry> entries = new ArrayList<>();
        for (int i = 0; i + 4 < entryLines.size(); i += 5) {
            entries.add(entryParser.parse(entryLines.subList(i, i + 5)));
        }
        return entries;
    }

    public FeedEntry parseEntry(String content) {
        List<String> lines = List.of(content.split("\n", -1));
        return entryParser.parse(lines);
    }
}
