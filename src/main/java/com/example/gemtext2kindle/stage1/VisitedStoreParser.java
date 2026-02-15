package com.example.gemtext2kindle.stage1;

import java.util.List;

public class VisitedStoreParser {
    public VisitedStore.Visit parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split(" ", 3);
        if (parts.length == 3) {
            try {
                long ts = Long.parseLong(parts[0]);
                int flags = Integer.parseInt(parts[1], 16);
                String url = parts[2].trim();
                return new VisitedStore.Visit(ts, flags, url);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
