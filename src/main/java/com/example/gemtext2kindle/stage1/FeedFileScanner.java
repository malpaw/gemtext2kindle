package com.example.gemtext2kindle.stage1;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedFileScanner {
    private final List<String> lines;

    public FeedFileScanner(List<String> lines) {
        this.lines = lines;
    }

    public Optional<Integer> findSection(String sectionHeader) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith(sectionHeader)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public List<String> getLinesInSection(String currentSection, String nextSection) {
        List<String> result = new ArrayList<>();
        Optional<Integer> start = findSection(currentSection);
        if (start.isEmpty()) return result;

        Optional<Integer> end = nextSection != null ? findSection(nextSection) : Optional.of(lines.size());
        int endIndex = end.orElse(lines.size());

        for (int i = start.get() + 1; i < endIndex; i++) {
            String line = lines.get(i);
            if (!line.isBlank()) {
                result.add(line);
            }
        }
        return result;
    }
}
