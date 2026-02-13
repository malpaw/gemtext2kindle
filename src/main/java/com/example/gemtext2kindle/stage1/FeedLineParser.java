package com.example.gemtext2kindle.stage1;

public class FeedLineParser {

    public Feed parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.trim().split("\\s+", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid feed line format: " + line);
        }
        return new Feed(parts[0], parts[1]);
    }
}
