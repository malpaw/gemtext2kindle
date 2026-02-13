package com.example.gemtext2kindle.stage1;

public class FeedStatusChecker {

    public boolean needsDownload(FeedEntry entry) {
        return entry.timestamp2() == 0;
    }
}
