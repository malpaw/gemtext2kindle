package com.example.gemtext2kindle.common;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record ArticleMetadata(
    String feedName,
    String title,
    long timestamp,
    String url
) {
    private static final String DELIMITER = "---";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
            .withZone(ZoneId.systemDefault());

    public String serialize() {
        return DELIMITER + "\n" +
               "feed: " + feedName + "\n" +
               "title: " + title + "\n" +
               "date: " + FORMATTER.format(Instant.ofEpochSecond(timestamp)) + "\n" +
               "url: " + url + "\n" +
               DELIMITER + "\n";
    }

    public static ArticleMetadata deserialize(String content) {
        if (!content.startsWith(DELIMITER)) {
            return null;
        }
        
        String[] lines = content.split("\n");
        String feedName = null, title = null, url = null, date = null;
        
        for (String line : lines) {
            if (line.startsWith("feed: ")) feedName = line.substring(6).trim();
            else if (line.startsWith("title: ")) title = line.substring(7).trim();
            else if (line.startsWith("url: ")) url = line.substring(5).trim();
            else if (line.startsWith("date: ")) date = line.substring(6).trim();
            else if (line.equals(DELIMITER) && feedName != null) break;
        }
        
        if (feedName == null) return null;

        // Note: For deserialization back to timestamp, we'd need more logic, 
        // but for Stage 2 HTML rendering, the strings are often enough.
        // Keeping it simple for now as requested.
        return new ArticleMetadata(feedName, title, 0, url);
    }
}
