package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class FeedFileScannerTest {

    @Test
    public void shouldIdentifySections() {
        List<String> lines = List.of(
            "1771014720",
            "# Feeds",
            "00000081 url",
            "# Entries",
            "33", "1", "2", "u", "t"
        );
        
        FeedFileScanner scanner = new FeedFileScanner(lines);
        
        assertThat(scanner.findSection("# Feeds")).isPresent();
        assertThat(scanner.findSection("# Entries")).isPresent();
        assertThat(scanner.findSection("# NonExistent")).isEmpty();
    }

    @Test
    public void shouldGetLinesInSection() {
        List<String> lines = List.of(
            "header",
            "# Feeds",
            "line1",
            "line2",
            "# Entries",
            "line3"
        );
        
        FeedFileScanner scanner = new FeedFileScanner(lines);
        List<String> feedLines = scanner.getLinesInSection("# Feeds", "# Entries");
        
        assertThat(feedLines).containsExactly("line1", "line2");
    }
}
