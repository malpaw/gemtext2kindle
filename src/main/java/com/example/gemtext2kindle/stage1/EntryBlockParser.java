package com.example.gemtext2kindle.stage1;

import java.util.List;

public class EntryBlockParser {

    public FeedEntry parse(List<String> lines) {
        if (lines == null || lines.size() < 5) {
            throw new IllegalArgumentException("Incomplete entry block. Expected at least 5 lines.");
        }
        
        String feedId = lines.get(0).trim();
        long t1 = Long.parseLong(lines.get(1).trim());
        long t2 = Long.parseLong(lines.get(2).trim());
        String url = lines.get(3).trim();
        String title = lines.get(4).trim();
        
        return new FeedEntry(feedId, t1, t2, url, title);
    }
}
