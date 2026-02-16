package com.example.gemtext2kindle.stage1;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeedDiscovery {

    private static final Pattern LINK_DATE_PATTERN = Pattern.compile("^=>\\s*([^\\s]+)\\s+([0-9]{4}-[0-1][0-9]-[0-3][0-9])\\s*(.*)$");
    private static final Pattern GEMTEXT_H1_PATTERN = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern XML_TITLE_PATTERN = Pattern.compile("<title>(.*?)</title>");

    public String discoverTitle(String content) {
        if (content == null || content.isBlank()) return null;
        if (isXml(content)) {
            Matcher m = XML_TITLE_PATTERN.matcher(content);
            if (m.find()) return m.group(1).trim();
        } else {
            Matcher m = GEMTEXT_H1_PATTERN.matcher(content);
            if (m.find()) return m.group(1).trim();
        }
        return null;
    }

    public List<FeedEntry> discover(Feed feed, String content) {
        List<FeedEntry> entries = new ArrayList<>();
        if (feed == null || content == null || content.isBlank()) return entries;

        try {
            if (isXml(content)) {
                entries.addAll(parseXml(feed, content));
            } else {
                entries.addAll(parseGemtext(feed, content));
            }
        } catch (Exception e) {
            System.err.println("Error during feed discovery for " + feed.url() + ": " + e.getMessage());
        }
        return entries;
    }

    private boolean isXml(String content) {
        String trimmed = content.trim();
        return trimmed.startsWith("<?xml") || trimmed.startsWith("<feed") || trimmed.startsWith("<rss");
    }

    private List<FeedEntry> parseXml(Feed feed, String content) {
        List<FeedEntry> entries = new ArrayList<>();
        String feedId = normalizeFeedId(feed.id());
        // Simple regex-based XML parsing for Atom/RSS to avoid heavy dependencies
        Pattern entryPattern = Pattern.compile("<(entry|item)>.*?</\\1>", Pattern.DOTALL);
        Pattern linkPattern = Pattern.compile("<link(?:[^>]*href=\"([^\"]+)\"|[^>]*>(.*?)</link>)");
        Pattern titlePattern = Pattern.compile("<title>(.*?)</title>");
        Pattern datePattern = Pattern.compile("<(?:published|updated|pubDate)>(.*?)</(?:published|updated|pubDate)>");

        Matcher entryMatcher = entryPattern.matcher(content);
        while (entryMatcher.find()) {
            String entryContent = entryMatcher.group();
            
            String url = "";
            Matcher lm = linkPattern.matcher(entryContent);
            if (lm.find()) {
                url = lm.group(1) != null ? lm.group(1).trim() : (lm.group(2) != null ? lm.group(2).trim() : "");
            }

            String title = "";
            Matcher tm = titlePattern.matcher(entryContent);
            if (tm.find()) title = tm.group(1).trim();
            
            if (title.isEmpty()) title = url;

            long timestamp = System.currentTimeMillis() / 1000L;
            Matcher dm = datePattern.matcher(entryContent);
            if (dm.find()) {
                // Simplified date parsing - should ideally handle ISO8601/RFC822
                timestamp = parseDate(dm.group(1));
            }

            if (!url.isEmpty()) {
                entries.add(new FeedEntry(feedId, timestamp, System.currentTimeMillis() / 1000L, url, title));
            }
        }
        return entries;
    }

    private List<FeedEntry> parseGemtext(Feed feed, String content) {
        List<FeedEntry> entries = new ArrayList<>();
        String feedId = normalizeFeedId(feed.id());
        String[] lines = content.split("\n");
        long now = System.currentTimeMillis() / 1000L;

        for (String line : lines) {
            line = line.trim();
            
            // 1. Link + Date (Algorithm A)
            Matcher lm = LINK_DATE_PATTERN.matcher(line);
            if (lm.matches()) {
                String url = resolveUrl(feed.url(), lm.group(1));
                long posted = parseDate(lm.group(2));
                String title = lm.group(3).trim();
                if (title.isEmpty()) title = url;
                entries.add(new FeedEntry(feedId, posted, now, url, title));
                continue;
            }

            // 2. Headings (Algorithm B)
            if (line.startsWith("#")) {
                int level = 0;
                while (level < line.length() && line.charAt(level) == '#') level++;
                String title = line.substring(level).trim();
                if (!title.isEmpty()) {
                    String fragment = URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "%20");
                    String url = feed.url();
                    if (url.contains("#")) url = url.substring(0, url.indexOf("#"));
                    url += "#" + fragment;
                    entries.add(new FeedEntry(feedId, now, now, url, title));
                }
            }
        }
        return entries;
    }

    private String normalizeFeedId(String id) {
        if (id == null) return "";
        try {
            // Convert hex string to long and back to hex to remove leading zeros
            return Long.toHexString(Long.parseLong(id, 16));
        } catch (NumberFormatException e) {
            return id.trim();
        }
    }

    private String resolveUrl(String baseUrl, String relativeUrl) {
        if (baseUrl == null) return relativeUrl;
        try {
            return URI.create(baseUrl).resolve(relativeUrl).toString();
        } catch (Exception e) {
            return relativeUrl;
        }
    }

    private long parseDate(String dateStr) {
        try {
            // Support YYYY-MM-DD
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(dateStr).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
            }
            // Fallback for discovery
            return System.currentTimeMillis() / 1000L;
        } catch (Exception e) {
            return System.currentTimeMillis() / 1000L;
        }
    }
}
