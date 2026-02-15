package com.example.gemtext2kindle.stage1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class VisitedStore {
    private final Path visitedPath;
    private final Map<String, Visit> visits = new HashMap<>();

    public record Visit(long timestamp, int flags) {}

    public VisitedStore(Path visitedPath) throws IOException {
        this.visitedPath = visitedPath;
        load();
    }

    private void load() throws IOException {
        if (visitedPath == null || !Files.exists(visitedPath)) {
            return;
        }
        parseContent(Files.readString(visitedPath, StandardCharsets.UTF_8));
    }

    public void parseContent(String content) {
        for (String line : content.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(" ", 3);
            if (parts.length == 3) {
                try {
                    long ts = Long.parseLong(parts[0]);
                    int flags = Integer.parseInt(parts[1], 16);
                    String url = parts[2].trim();
                    visits.put(url, new Visit(ts, flags));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public boolean isVisited(String url) {
        return visits.containsKey(normalizeUrl(url));
    }

    public long getVisitTime(String url) {
        Visit v = visits.get(normalizeUrl(url));
        return v != null ? v.timestamp : 0;
    }

    public void addVisit(String url) throws IOException {
        url = normalizeUrl(url);
        long now = System.currentTimeMillis() / 1000L;
        int flags = 0x0002; // kept_VisitedUrlFlag
        
        // If it's a fragment URL, we also need to update the base URL's timestamp 
        // so Algorithm B (FeedStatusChecker) works correctly.
        if (url.contains("#")) {
            String baseUrl = url.substring(0, url.indexOf("#"));
            addVisitInternal(baseUrl, now, flags);
        }
        
        addVisitInternal(url, now, flags);
    }

    private void addVisitInternal(String url, long timestamp, int flags) throws IOException {
        visits.put(url, new Visit(timestamp, flags));
        
        String line = String.format("%d %04x %s\n", timestamp, flags, url);
        Files.writeString(visitedPath, line, StandardCharsets.UTF_8, 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private String normalizeUrl(String url) {
        // Lagrange normalizes URLs, simplest is to strip fragments for base page checks
        // and ensure consistent trailing slashes etc if needed. 
        // For now, let's keep it simple as per Lagrange's Alg A/B.
        return url.trim();
    }
}
