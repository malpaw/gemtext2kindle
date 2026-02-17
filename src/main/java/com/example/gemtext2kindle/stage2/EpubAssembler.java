package com.example.gemtext2kindle.stage2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EpubAssembler {

    public void assemble(String title, Map<String, List<Stage2Processor.ProcessedArticle>> groupedArticles, Path outputPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputPath.toFile()))) {
            addFileToZip(zos, "mimetype", "application/epub+zip", false);
            
            String containerXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n" +
                    "    <rootfiles>\n" +
                    "        <rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n" +
                    "    </rootfiles>\n" +
                    "</container>";
            addFileToZip(zos, "META-INF/container.xml", containerXml, true);
            
            StringBuilder manifest = new StringBuilder();
            StringBuilder spine = new StringBuilder();
            
            manifest.append("<item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n");
            manifest.append("<item id=\"toc\" href=\"toc.html\" media-type=\"application/xhtml+xml\"/>\n");
            spine.append("<itemref idref=\"toc\"/>\n");

            int feedIdx = 0;
            int artIdx = 0;
            
            List<String> sortedFeedNames = new ArrayList<>(groupedArticles.keySet());
            Collections.sort(sortedFeedNames);

            for (String feedName : sortedFeedNames) {
                String feedId = "feed_" + feedIdx++;
                String feedHref = feedId + ".html";
                manifest.append("<item id=\"").append(feedId).append("\" href=\"").append(feedHref).append("\" media-type=\"application/xhtml+xml\"/>\n");
                spine.append("<itemref idref=\"").append(feedId).append("\"/>\n");
                
                List<Stage2Processor.ProcessedArticle> articles = groupedArticles.get(feedName);
                for (Stage2Processor.ProcessedArticle article : articles) {
                    String artId = "art_" + artIdx++;
                    String artHref = artId + ".html";
                    manifest.append("<item id=\"").append(artId).append("\" href=\"").append(artHref).append("\" media-type=\"application/xhtml+xml\"/>\n");
                    spine.append("<itemref idref=\"").append(artId).append("\"/>\n");
                }
            }

            String contentOpf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookID\" version=\"2.0\">\n" +
                    "    <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n" +
                    "        <dc:title>" + title + "</dc:title>\n" +
                    "        <dc:language>en</dc:language>\n" +
                    "        <dc:identifier id=\"BookID\">urn:uuid:" + UUID.randomUUID() + "</dc:identifier>\n" +
                    "    </metadata>\n" +
                    "    <manifest>\n" +
                    manifest.toString() +
                    "    </manifest>\n" +
                    "    <spine toc=\"ncx\">\n" +
                    spine.toString() +
                    "    </spine>\n" +
                    "</package>";
            addFileToZip(zos, "OEBPS/content.opf", contentOpf, true);
            
            StringBuilder ncxNav = new StringBuilder();
            StringBuilder htmlToc = new StringBuilder();
            htmlToc.append("<h1>Table of Contents</h1><ul>");
            
            int playOrder = 1;
            feedIdx = 0;
            artIdx = 0;
            
            for (String feedName : sortedFeedNames) {
                String feedId = "feed_" + feedIdx++;
                String feedHref = feedId + ".html";
                List<Stage2Processor.ProcessedArticle> articles = groupedArticles.get(feedName);
                String feedIcon = articles.get(0).metadata().feedIcon();
                if (feedIcon == null) feedIcon = "";

                ncxNav.append("<navPoint id=\"").append(feedId).append("\" playOrder=\"").append(playOrder++).append("\">\n")
                      .append("  <navLabel><text>").append(escapeXml(feedName)).append("</text></navLabel>\n")
                      .append("  <content src=\"").append(feedHref).append("\"/>\n");
                
                htmlToc.append("<li><a href=\"").append(feedHref).append("\">").append(escapeXml(feedName)).append("</a><ul>");

                String feedHtml = "<html><head><title>" + escapeXml(feedName) + "</title></head><body>" +
                        "<div style='text-align:center; margin-top: 20%;'>" +
                        "<div style='font-size: 5em;'>" + escapeXml(feedIcon) + "</div>" +
                        "<h1>" + escapeXml(feedName) + "</h1>" +
                        "</div></body></html>";
                addFileToZip(zos, "OEBPS/" + feedHref, feedHtml, true);

                for (Stage2Processor.ProcessedArticle article : articles) {
                    String artId = "art_" + artIdx++;
                    String artHref = artId + ".html";
                    String artTitle = article.metadata().title();

                    ncxNav.append("  <navPoint id=\"").append(artId).append("\" playOrder=\"").append(playOrder++).append("\">\n")
                          .append("    <navLabel><text>").append(escapeXml(artTitle)).append("</text></navLabel>\n")
                          .append("    <content src=\"").append(artHref).append("\"/>\n")
                          .append("  </navPoint>\n");
                    
                    htmlToc.append("<li><a href=\"").append(artHref).append("\">").append(escapeXml(artTitle)).append("</a></li>");

                    String artHtml = "<html><head><title>" + escapeXml(artTitle) + "</title></head><body>" +
                            article.htmlContent() +
                            "</body></html>";
                    addFileToZip(zos, "OEBPS/" + artHref, artHtml, true);
                }
                ncxNav.append("</navPoint>\n");
                htmlToc.append("</ul></li>");
            }
            htmlToc.append("</ul>");

            String ncxHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n" +
                    "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n" +
                    "    <head><meta name=\"dtb:uid\" content=\"uuid\"/></head>\n" +
                    "    <docTitle><text>" + title + "</text></docTitle>\n" +
                    "    <navMap>\n";
            String ncxFooter = "    </navMap>\n" +
                    "</ncx>";
            addFileToZip(zos, "OEBPS/toc.ncx", ncxHeader + ncxNav.toString() + ncxFooter, true);
            
            String tocHtmlFull = "<html><head><title>TOC</title></head><body>" + htmlToc.toString() + "</body></html>";
            addFileToZip(zos, "OEBPS/toc.html", tocHtmlFull, true);
        }
    }

    private void addFileToZip(ZipOutputStream zos, String path, String content, boolean compress) throws IOException {
        ZipEntry entry = new ZipEntry(path);
        if (!compress) {
            entry.setMethod(ZipEntry.STORED);
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            entry.setSize(bytes.length);
            entry.setCrc(calculateCrc(bytes));
        }
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private long calculateCrc(byte[] bytes) {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(bytes);
        return crc.getValue();
    }

    private String escapeXml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}
