package com.example.gemtext2kindle.stage3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class MailerMain {

    public static void main(String[] args) throws IOException {
        if (args.length < 8) {
            System.err.println("Usage: MailerMain <input dir> <processed dir> <host> <port> <user> <password> <to email> <subject>");
            System.exit(1);
        }

        Path inputDir = Path.of(args[0]);
        Path processedDir = Path.of(args[1]);
        SmtpConfig config = new SmtpConfig(args[2], args[3], args[4], args[5]);
        String to = args[6];
        String subject = args[7];

        Files.createDirectories(processedDir);

        Mailer mailer = new Mailer();

        try (Stream<Path> files = Files.list(inputDir)) {
            files.filter(f -> f.toString().endsWith(".epub"))
                 .forEach(f -> {
                     try {
                         System.out.println("Sending: " + f.getFileName());
                         mailer.sendEmail(config, to, subject, "Sent from gemtext2kindle", f.toAbsolutePath().toString());
                         Files.move(f, processedDir.resolve(f.getFileName()));
                         System.out.println("Success: " + f.getFileName());
                     } catch (Exception e) {
                         System.err.println("Failed to send " + f + ": " + e.getMessage());
                         e.printStackTrace();
                     }
                 });
        }
    }
}
