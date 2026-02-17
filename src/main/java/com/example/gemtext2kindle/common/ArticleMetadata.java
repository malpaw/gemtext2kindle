package com.example.gemtext2kindle.common;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record ArticleMetadata(
    String feedName,
    String feedIcon,
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
               (feedIcon != null && !feedIcon.isEmpty() ? "icon: " + feedIcon + "\n" : "") +
               "title: " + title + "\n" +
               "date: " + FORMATTER.format(Instant.ofEpochSecond(timestamp)) + "\n" +
               "url: " + url + "\n" +
               DELIMITER + "\n";
    }

    public static ArticleMetadata deserialize(String content) {
        if (content == null || !content.startsWith(DELIMITER)) {
            return null;
        }

        String[] lines = content.split("\n");
        String feedName = null, feedIcon = null, title = null, url = null, dateStr = null;

        for (String line : lines) {
            if (line.startsWith("feed: ")) feedName = line.substring(6).trim();
            else if (line.startsWith("icon: ")) feedIcon = line.substring(6).trim();
            else if (line.startsWith("title: ")) title = line.substring(7).trim();
            else if (line.startsWith("url: ")) url = line.substring(5).trim();
            else if (line.startsWith("date: ")) dateStr = line.substring(6).trim();
            else if (line.equals(DELIMITER) && feedName != null) break;
        }

        if (feedName == null || title == null) return null;

        long timestamp = 0;
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                // Try ISO_LOCAL_DATE (YYYY-MM-DD)
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                timestamp = date.atStartOfDay(java.time.ZoneId.systemDefault()).toEpochSecond();
            } catch (Exception e) {
                // Ignore parsing errors, fallback to 0
            }
        }

        return new ArticleMetadata(feedName, feedIcon, title, timestamp, url);
    }
}
