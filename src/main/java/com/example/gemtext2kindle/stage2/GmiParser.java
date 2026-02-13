package com.example.gemtext2kindle.stage2;

import java.util.Scanner;

public class GmiParser {
    private final HtmlEscaper escaper = new HtmlEscaper();

    public String convert(String input) {
        StringBuilder html = new StringBuilder();
        Scanner scanner = new Scanner(input);
        try {
            boolean preformatted = false;
            boolean inList = false;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                
                if (line.startsWith("```")) {
                    if (preformatted) {
                        html.append("</pre>\n");
                    } else {
                        html.append("<pre>\n");
                    }
                    preformatted = !preformatted;
                    continue;
                }

                if (preformatted) {
                    html.append(escaper.escape(line)).append("\n");
                    continue;
                }

                if (line.startsWith("* ")) {
                    if (!inList) {
                        html.append("<ul>\n");
                        inList = true;
                    }
                    html.append("<li>").append(escaper.escape(line.substring(2))).append("</li>\n");
                    continue;
                } else if (inList) {
                    html.append("</ul>\n");
                    inList = false;
                }

                html.append(toHtml(line)).append("\n");
            }
            
            if (inList) html.append("</ul>\n");
            if (preformatted) html.append("</pre>\n");
            
            return html.toString();
        } finally {
            scanner.close();
        }
    }

    public String toHtml(String line) {
        if (line.startsWith("### ")) {
            return "<h3>" + escaper.escape(line.substring(4)) + "</h3>";
        } else if (line.startsWith("## ")) {
            return "<h2>" + escaper.escape(line.substring(3)) + "</h2>";
        } else if (line.startsWith("# ")) {
            return "<h1>" + escaper.escape(line.substring(2)) + "</h1>";
        } else if (line.startsWith("=> ")) {
            String content = line.substring(3).trim();
            String[] parts = content.split("\\s+", 2);
            String url = parts[0];
            String text = parts.length > 1 ? parts[1] : url;
            return "<p><a href=\"" + url + "\">" + escaper.escape(text) + "</a></p>";
        } else if (line.startsWith("* ")) {
            return "<ul><li>" + escaper.escape(line.substring(2)) + "</li></ul>";
        } else if (line.isEmpty()) {
            return "<br/>";
        } else {
            return "<p>" + escaper.escape(line) + "</p>";
        }
    }
}
