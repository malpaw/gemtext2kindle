package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedStatusCheckerTest {

    @Test
    public void shouldDetermineIfEntryNeedsDownload() {
        FeedStatusChecker checker = new FeedStatusChecker();
        
        FeedEntry anyEntry = new FeedEntry("1", 100L, 200L, "url", "title");
        
        // Presence in the file now implies unread status
        assertThat(checker.needsDownload(anyEntry)).isTrue();
    }
}
