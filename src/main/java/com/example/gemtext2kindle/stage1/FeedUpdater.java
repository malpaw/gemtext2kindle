package com.example.gemtext2kindle.stage1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FeedUpdater {

    public void markAsRead(Path feedsPath, String entryUrl) throws IOException {
        List<String> lines = Files.readAllLines(feedsPath, StandardCharsets.UTF_8);
        List<String> updatedLines = new ArrayList<>();
        
        long now = System.currentTimeMillis() / 1000L;
        
        boolean inEntries = false;
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            updatedLines.add(line);
            if (line.startsWith("# Entries")) {
                inEntries = true;
                i++;
                continue;
            }
            
            if (inEntries && i + 3 < lines.size()) {
                String feedId = line;
                String t1 = lines.get(i+1);
                String t2 = lines.get(i+2);
                String url = lines.get(i+3);
                
                if (url.trim().equals(entryUrl)) {
                    // Update timestamp2 if it was 0
                    if (t2.trim().equals("0")) {
                        updatedLines.remove(updatedLines.size() - 1); // remove feedId we just added
                        updatedLines.add(feedId);
                        updatedLines.add(t1);
                        updatedLines.add(String.valueOf(now));
                        updatedLines.add(url);
                        updatedLines.add(lines.get(i+4));
                        i += 5;
                        continue;
                    }
                }
            }
            i++;
        }
        
        Files.write(feedsPath, updatedLines, StandardCharsets.UTF_8);
    }
}
