package com.example.gemtext2kindle.stage2;

import java.io.IOException;
import java.nio.file.Path;

public class ConverterMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: ConverterMain <input dir> <output dir> <processed dir>");
            System.exit(1);
        }

        Path inputDir = Path.of(args[0]);
        Path outputDir = Path.of(args[1]);
        Path processedDir = Path.of(args[2]);

        GmiParser parser = new GmiParser();
        EpubAssembler assembler = new EpubAssembler();
        Stage2Processor processor = new Stage2Processor(parser, assembler);

        processor.process(inputDir, outputDir, processedDir);
    }
}
