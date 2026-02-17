package com.example.gemtext2kindle.stage2;

import com.example.gemtext2kindle.common.ArticleMetadata;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Stage2Processor {

    private final GmiParser parser;
    private final EpubAssembler assembler;

    public Stage2Processor(GmiParser parser, EpubAssembler assembler) {
        this.parser = parser;
        this.assembler = assembler;
    }

    public void process(Path inputDir, Path outputDir, Path processedDir) throws IOException {
        Files.createDirectories(outputDir);
        Files.createDirectories(processedDir);

        List<ProcessedArticle> articles = new ArrayList<>();
        List<Path> processedFiles = new ArrayList<>();

        try (Stream<Path> files = Files.list(inputDir)) {
            List<Path> candidates = files
                    .filter(f -> f.toString().endsWith(".gmi") || f.toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            for (Path file : candidates) {
                try {
                    String content = Files.readString(file, StandardCharsets.UTF_8);
                    ArticleMetadata meta = ArticleMetadata.deserialize(content);
                    if (meta == null) {
                        System.err.println("Skipping file with missing metadata: " + file);
                        continue;
                    }

                    String gmiContent = content;
                    int delimiterIndex = content.indexOf("---\n", 4);
                    if (delimiterIndex != -1) {
                        gmiContent = content.substring(delimiterIndex + 4);
                    }

                    String html = parser.convert(gmiContent);
                    articles.add(new ProcessedArticle(meta, html, file));
                    processedFiles.add(file);
                } catch (Exception e) {
                    System.err.println("Failed to parse " + file + ": " + e.getMessage());
                }
            }
        }

        if (articles.isEmpty()) {
            System.out.println("No articles to process.");
            return;
        }

        // Group by feed and sort by date
        Map<String, List<ProcessedArticle>> grouped = articles.stream()
                .collect(Collectors.groupingBy(a -> a.metadata().feedName()));

        for (List<ProcessedArticle> feedArticles : grouped.values()) {
            feedArticles.sort(Comparator.comparingLong(a -> a.metadata().timestamp()));
        }

        String epubName = "Gemini_Articles_" + System.currentTimeMillis() / 1000 + ".epub";
        Path outputPath = outputDir.resolve(epubName);

        try {
            assembler.assemble("Gemini Articles", grouped, outputPath);
            System.out.println("Successfully created EPUB: " + epubName);

            // Cleanup
            for (Path file : processedFiles) {
                Files.move(file, processedDir.resolve(file.getFileName()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Failed to assemble EPUB: " + e.getMessage());
            throw e;
        }
    }

    public record ProcessedArticle(ArticleMetadata metadata, String htmlContent, Path sourceFile) {}
}
