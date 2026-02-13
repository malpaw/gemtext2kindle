package com.example.gemtext2kindle.stage2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ConverterMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: ConverterMain <input dir> <output dir> <processed dir>");
            System.exit(1);
        }

        Path inputDir = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        Path processedDir = Path.of(args[2]);

        Files.createDirectories(outputDir);
        Files.createDirectories(processedDir);

        GmiParser parser = new GmiParser();
        EpubAssembler assembler = new EpubAssembler();

        try (Stream<Path> files = Files.list(inputDir)) {
            files.filter(f -> f.toString().endsWith(".gmi") || f.toString().endsWith(".txt"))
                 .forEach(f -> {
                     try {
                         System.out.println("Converting: " + f.getFileName());
                         String content = Files.readString(f, StandardCharsets.UTF_8);
                         String html = parser.convert(content);
                         String title = f.getFileName().toString();
                         String epubName = title + ".epub";
                         
                         assembler.assemble(title, html, outputDir.resolve(epubName));
                         
                         Files.move(f, processedDir.resolve(f.getFileName()));
                         System.out.println("Success: " + epubName);
                     } catch (Exception e) {
                         System.err.println("Failed to convert " + f + ": " + e.getMessage());
                     }
                 });
        }
    }
}
