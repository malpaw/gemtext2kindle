package com.example.gemtext2kindle.stage1;

public class FeedStatusChecker {
    private final VisitedStore visitedStore;

    public FeedStatusChecker(VisitedStore visitedStore) {
        this.visitedStore = visitedStore;
    }

    public boolean needsDownload(FeedEntry entry) {
        if (entry == null) return false;

        String url = entry.url();
        if (url.contains("#")) {
            // Algorithm B: Heading/Fragment Entries
            String baseUrl = url.substring(0, url.indexOf("#"));
            long visTime = visitedStore.getVisitTime(baseUrl);
            return visTime < entry.timestamp1();
        } else {
            // Algorithm A: Standard Link Entries
            return !visitedStore.isVisited(url);
        }
    }
}
