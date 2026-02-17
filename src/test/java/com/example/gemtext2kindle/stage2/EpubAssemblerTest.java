package com.example.gemtext2kindle.stage2;

import com.example.gemtext2kindle.common.ArticleMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EpubAssemblerTest {

    @TempDir
    Path tempDir;

    @Test
    void testAssembleEmpty() throws IOException {
        EpubAssembler assembler = new EpubAssembler();
        Path output = tempDir.resolve("test.epub");
        assembler.assemble("Test Book", Collections.emptyMap(), output);
        assertTrue(output.toFile().exists());
    }

    @Test
    void testAssembleWithContent() throws IOException {
        EpubAssembler assembler = new EpubAssembler();
        Path output = tempDir.resolve("test2.epub");
        
        ArticleMetadata meta = new ArticleMetadata("Feed A", "⚡", "Title 1", 123456789, "url1");
        Stage2Processor.ProcessedArticle art = new Stage2Processor.ProcessedArticle(meta, "<p>Hello</p>", Path.of("source.gmi"));
        
        Map<String, List<Stage2Processor.ProcessedArticle>> grouped = Map.of("Feed A", List.of(art));
        
        assembler.assemble("Test Book 2", grouped, output);
        assertTrue(output.toFile().exists());
    }
}
