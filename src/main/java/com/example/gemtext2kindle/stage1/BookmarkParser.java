package com.example.gemtext2kindle.stage1;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookmarkParser {
    private static final Pattern SECTION_PATTERN = Pattern.compile("\\[(\\d+)\\]");
    private static final Pattern TITLE_PATTERN = Pattern.compile("title\\s*=\\s*\"([^\"]*)\"");

    public Map<String, String> parse(String content) {
        Map<String, String> bookmarks = new HashMap<>();
        String[] sections = content.split("(?=\\[\\d+\\])");
        
        for (String section : sections) {
            Matcher sectionMatcher = SECTION_PATTERN.matcher(section);
            if (sectionMatcher.find()) {
                String id = normalizeId(sectionMatcher.group(1));
                Matcher titleMatcher = TITLE_PATTERN.matcher(section);
                if (titleMatcher.find()) {
                    bookmarks.put(id, titleMatcher.group(1));
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
