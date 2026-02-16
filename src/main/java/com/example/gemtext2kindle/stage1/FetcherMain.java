package com.example.gemtext2kindle.stage1;

import java.io.IOException;
import java.nio.file.Path;

public class FetcherMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: FetcherMain <feeds.txt path> <visited.2.txt path> <output queue dir> [bookmarks.ini path]");
            System.exit(1);
        }

        Path feedsPath = Path.of(args[0]);
        Path visitedPath = Path.of(args[1]);
        Path outputDir = Path.of(args[2]);
        Path bookmarksPath = args.length > 3 ? Path.of(args[3]) : null;
        
        run(feedsPath, visitedPath, outputDir, bookmarksPath, new RealProtocolClient());
    }

    public static void run(Path feedsPath, Path visitedPath, Path outputDir, Path bookmarksPath, ProtocolClient client) throws IOException {
        VisitedStore visitedStore = new VisitedStore(visitedPath);
        Stage1Processor processor = new Stage1Processor(client, visitedStore);
        processor.process(feedsPath, outputDir, bookmarksPath);
    }
}
