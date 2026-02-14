package com.example.gemtext2kindle.stage1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FeedUpdater {

    public void removeEntries(Path feedsPath, List<String> urlsToRemove) throws IOException {
        // In Lagrange, entries are not removed from feeds.txt after being read.
        // Instead, their read state is tracked in visited.2.txt.
        // This method is now a no-op to match that behavior, or could be used for actual purging of old entries.
    }

    public void updateGlobalTimestamp(Path feedsPath) throws IOException {
        List<String> lines = Files.readAllLines(feedsPath, StandardCharsets.UTF_8);
        if (!lines.isEmpty()) {
            lines.set(0, String.valueOf(System.currentTimeMillis() / 1000L));
            Files.write(feedsPath, lines, StandardCharsets.UTF_8);
        }
    }
}
