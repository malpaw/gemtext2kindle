package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedStatusCheckerTest {

    @TempDir
    Path tempDir;

    @Test
    public void shouldDetermineIfEntryNeedsDownload() throws IOException {
        VisitedStore store = new VisitedStore(null);
        FeedStatusChecker checker = new FeedStatusChecker(store);
        
        FeedEntry entry = new FeedEntry("1", 100L, 200L, "url", "title");
        
        // Initially not visited
        assertThat(checker.needsDownload(entry)).isTrue();
        
        // After visit
        store.parseContent("1000 0000 url\n");
        assertThat(checker.needsDownload(entry)).isFalse();
    }

    @Test
    public void shouldHandleHeadingEntries() throws IOException {
        VisitedStore store = new VisitedStore(null);
        FeedStatusChecker checker = new FeedStatusChecker(store);
        
        // entry timestamp is 1000
        FeedEntry headingEntry = new FeedEntry("1", 1000L, 1000L, "baseurl#heading", "title");
        
        // Case 1: Base URL not visited
        assertThat(checker.needsDownload(headingEntry)).isTrue();
        
        // Case 2: Base URL visited EARLIER than entry timestamp (e.g. at 500)
        store.parseContent("500 0000 baseurl\n");
        assertThat(checker.needsDownload(headingEntry)).isTrue();

        // Case 3: Base URL visited LATER than entry timestamp (e.g. at 1500)
        store.parseContent("1500 0000 baseurl\n");
        assertThat(checker.needsDownload(headingEntry)).isFalse();
    }
}
