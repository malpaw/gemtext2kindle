package com.example.gemtext2kindle.stage2;

import com.example.gemtext2kindle.common.ArticleMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Stage2ProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    void testProcessGroupingAndSorting() throws IOException {
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Path processedDir = tempDir.resolve("processed");
        Files.createDirectories(inputDir);

        // Feed A, newer article
        String art1 = "---\nfeed: Feed A\ntitle: Art 1\ndate: 2024-02-18\n---\nContent 1";
        Files.writeString(inputDir.resolve("a1.gmi"), art1);

        // Feed A, older article
        String art2 = "---\nfeed: Feed A\ntitle: Art 2\ndate: 2024-02-17\n---\nContent 2";
        Files.writeString(inputDir.resolve("a2.gmi"), art2);

        // Feed B, single article
        String art3 = "---\nfeed: Feed B\ntitle: Art 3\ndate: 2024-02-17\n---\nContent 3";
        Files.writeString(inputDir.resolve("b1.gmi"), art3);

        // Mock Assembler to capture calls
        final Map<String, List<Stage2Processor.ProcessedArticle>>[] capturedGrouped = new Map[1];
        EpubAssembler mockAssembler = new EpubAssembler() {
            @Override
            public void assemble(String title, Map<String, List<Stage2Processor.ProcessedArticle>> groupedArticles, Path outputPath) throws IOException {
                capturedGrouped[0] = groupedArticles;
                Files.createFile(outputPath);
            }
        };

        Stage2Processor processor = new Stage2Processor(new GmiParser(), mockAssembler);
        processor.process(inputDir, outputDir, processedDir);

        Map<String, List<Stage2Processor.ProcessedArticle>> grouped = capturedGrouped[0];
        assertNotNull(grouped);
        assertEquals(2, grouped.size());
        
        // Check sorting in Feed A
        List<Stage2Processor.ProcessedArticle> feedA = grouped.get("Feed A");
        assertEquals(2, feedA.size());
        assertEquals("Art 2", feedA.get(0).metadata().title()); // 17th
        assertEquals("Art 1", feedA.get(1).metadata().title()); // 18th

        // Check cleanup
        assertFalse(Files.exists(inputDir.resolve("a1.gmi")));
        assertTrue(Files.exists(processedDir.resolve("a1.gmi")));
    }
}
