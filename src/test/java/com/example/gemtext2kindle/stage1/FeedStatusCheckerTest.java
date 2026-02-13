package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedStatusCheckerTest {

    @Test
    public void shouldDetermineIfEntryNeedsDownload() {
        FeedStatusChecker checker = new FeedStatusChecker();
        
        FeedEntry unread = new FeedEntry("1", 100L, 0L, "url", "title");
        FeedEntry read = new FeedEntry("1", 100L, 200L, "url", "title");
        
        assertThat(checker.needsDownload(unread)).isTrue();
        assertThat(checker.needsDownload(read)).isFalse();
    }
}
