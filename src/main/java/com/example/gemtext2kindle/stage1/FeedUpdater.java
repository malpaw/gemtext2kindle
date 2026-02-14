package com.example.gemtext2kindle.stage1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FeedUpdater {

    public void removeEntries(Path feedsPath, List<String> urlsToRemove) throws IOException {
        List<String> lines = Files.readAllLines(feedsPath, StandardCharsets.UTF_8);
        List<String> result = new ArrayList<>();
        
        int i = 0;
        // Keep header and feeds
        while (i < lines.size() && !lines.get(i).startsWith("# Entries")) {
            result.add(lines.get(i++));
        }
        
        if (i < lines.size()) {
            result.add(lines.get(i++)); // add "# Entries"
        }
        
        // Filter entries
        while (i + 4 < lines.size()) {
            String feedId = lines.get(i);
            String t1 = lines.get(i + 1);
            String t2 = lines.get(i + 2);
            String url = lines.get(i + 3);
            String title = lines.get(i + 4);
            
            if (!urlsToRemove.contains(url.trim())) {
                result.add(feedId);
                result.add(t1);
                result.add(t2);
                result.add(url);
                result.add(title);
            }
            i += 5;
        }
        
        Files.write(feedsPath, result, StandardCharsets.UTF_8);
    }

    public void updateGlobalTimestamp(Path feedsPath) throws IOException {
        List<String> lines = Files.readAllLines(feedsPath, StandardCharsets.UTF_8);
        if (!lines.isEmpty()) {
            lines.set(0, String.valueOf(System.currentTimeMillis() / 1000L));
            Files.write(feedsPath, lines, StandardCharsets.UTF_8);
        }
    }
}
