package com.example.gemtext2kindle.stage1;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookmarkParser {
    private static final Pattern SECTION_PATTERN = Pattern.compile("\\[(\\d+)\\]");
    private static final Pattern TITLE_PATTERN = Pattern.compile("title\\s*=\\s*\"([^\"]*)\"");
    private static final Pattern ICON_PATTERN = Pattern.compile("icon\\s*=\\s*(0x[0-9a-fA-F]+)");

    public static record Bookmark(String title, String icon) {}

    public Map<String, Bookmark> parse(String content) {
        Map<String, Bookmark> bookmarks = new HashMap<>();
        if (content == null || content.isBlank()) return bookmarks;
        String[] sections = content.split("(?=\\[\\d+\\])");
        
        for (String section : sections) {
            Matcher sectionMatcher = SECTION_PATTERN.matcher(section);
            if (sectionMatcher.find()) {
                String id = normalizeId(sectionMatcher.group(1));
                
                String title = null;
                Matcher titleMatcher = TITLE_PATTERN.matcher(section);
                if (titleMatcher.find()) {
                    title = titleMatcher.group(1);
                }
                
                String icon = null;
                Matcher iconMatcher = ICON_PATTERN.matcher(section);
                if (iconMatcher.find()) {
                    try {
                        int codePoint = Integer.decode(iconMatcher.group(1));
                        if (codePoint != 0) {
                            icon = new String(Character.toChars(codePoint));
                        }
                    } catch (Exception e) {
                        // ignore invalid icon
                    }
                }
                
                if (title != null) {
                    bookmarks.put(id, new Bookmark(title, icon));
                }
            }
        }
        return bookmarks;
    }

    private String normalizeId(String id) {
        try {
            return Long.toHexString(Long.parseLong(id));
        } catch (NumberFormatException e) {
            return id;
        }
    }
}
