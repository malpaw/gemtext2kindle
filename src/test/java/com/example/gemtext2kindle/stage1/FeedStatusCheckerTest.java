package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedStatusCheckerTest {

    @Test
    public void shouldIdentifyIfEntryNeedsDownload() throws IOException {
        VisitedStore store = new VisitedStore(null);
        String visitedContent = "1771029826 0000 gemini://archiwistka.blog/gemlog_echo_socjalizacji/\n" +
                               "1694172824 0003 gemini://calcuode.com/gemlog/2021-06-21_gmni-kobo.gmi";
        store.parseContent(visitedContent);
        
        FeedStatusChecker checker = new FeedStatusChecker(store);
        
        // Case 1: Standard Link, already visited
        FeedEntry visitedEntry = new FeedEntry("35", 1624273200L, 1771065890L, 
            "gemini://calcuode.com/gemlog/2021-06-21_gmni-kobo.gmi", "Running gmni on a Kobo e-reader");
        assertThat(checker.needsDownload(visitedEntry)).isFalse();
        
        // Case 2: Standard Link, not visited
        FeedEntry newEntry = new FeedEntry("7b", 1710327600L, 1771065886L, 
            "gemini://causa-arcana.com/pl/blog/2024/03/13/why-polish.gmi", "Dlaczego zaczynam pisać po polsku");
        assertThat(checker.needsDownload(newEntry)).isTrue();
        
        // Case 3: Fragment URL (Algorithm B)
        // URL contains #, we check timestamp of BASE URL in VisitedStore
        // Base URL: gemini://tobykurien.com/microblog.gmi
        // Entry timestamp1: 1730494260
        FeedEntry fragmentEntry = new FeedEntry("15", 1730494260L, 1730494260L,
            "gemini://tobykurien.com/microblog.gmi#2020-12-19", "2020-12-19");
            
        // Base URL not visited yet
        assertThat(checker.needsDownload(fragmentEntry)).isTrue();
        
        // Add visit for Base URL with older timestamp
        store.parseContent("1700000000 0000 gemini://tobykurien.com/microblog.gmi");
        assertThat(checker.needsDownload(fragmentEntry)).isTrue(); // 1730494260 > 1700000000
        
        // Add visit for Base URL with newer timestamp
        store.parseContent("1740000000 0000 gemini://tobykurien.com/microblog.gmi");
        assertThat(checker.needsDownload(fragmentEntry)).isFalse(); // 1730494260 < 1740000000

        // Case 4: Fragment URL from visited.2.txt example
        // 1730494396 1730494396 gemini://sud0nim.smol.pub/glog#Email%20me
        FeedEntry visitedFragmentEntry = new FeedEntry("50", 1730494396L, 1730494396L,
            "gemini://sud0nim.smol.pub/glog#Email%20me", "Email me");
        store.parseContent("1730494396 0002 gemini://sud0nim.smol.pub/glog");
        assertThat(checker.needsDownload(visitedFragmentEntry)).isFalse(); // 1730494396 == 1730494396, not strictly less
        
        store.parseContent("1730494395 0002 gemini://sud0nim.smol.pub/glog");
        assertThat(checker.needsDownload(visitedFragmentEntry)).isTrue(); // 1730494396 > 1730494395
    }
}
