package com.example.gemtext2kindle.stage1;

public class FeedStatusChecker {

    public boolean needsDownload(FeedEntry entry) {
        // Any entry present in the file is considered unread per the updated vision
        return entry != null;
    }
}
