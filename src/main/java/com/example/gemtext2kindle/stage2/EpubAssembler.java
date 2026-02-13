package com.example.gemtext2kindle.stage2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EpubAssembler {

    public void assemble(String title, String htmlContent, Path outputPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputPath.toFile()))) {
            // mimetype file
            addFileToZip(zos, "mimetype", "application/epub+zip", false);
            
            // container.xml
            String containerXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n" +
                    "    <rootfiles>\n" +
                    "        <rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n" +
                    "    </rootfiles>\n" +
                    "</container>";
            addFileToZip(zos, "META-INF/container.xml", containerXml, true);
            
            // content.opf
            String contentOpf = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookID\" version=\"2.0\">\n" +
                    "    <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n" +
                    "        <dc:title>" + title + "</dc:title>\n" +
                    "        <dc:language>en</dc:language>\n" +
                    "        <dc:identifier id=\"BookID\">urn:uuid:12345</dc:identifier>\n" +
                    "    </metadata>\n" +
                    "    <manifest>\n" +
                    "        <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n" +
                    "        <item id=\"content\" href=\"content.html\" media-type=\"application/xhtml+xml\"/>\n" +
                    "    </manifest>\n" +
                    "    <spine toc=\"ncx\">\n" +
                    "        <itemref idref=\"content\"/>\n" +
                    "    </spine>\n" +
                    "</package>";
            addFileToZip(zos, "OEBPS/content.opf", contentOpf, true);
            
            // toc.ncx
            String tocNcx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n" +
                    "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n" +
                    "    <head>\n" +
                    "        <meta name=\"dtb:uid\" content=\"urn:uuid:12345\"/>\n" +
                    "    </head>\n" +
                    "    <docTitle><text>" + title + "</text></docTitle>\n" +
                    "    <navMap>\n" +
                    "        <navPoint id=\"navpoint-1\" playOrder=\"1\">\n" +
                    "            <navLabel><text>" + title + "</text></navLabel>\n" +
                    "            <content src=\"content.html\"/>\n" +
                    "        </navPoint>\n" +
                    "    </navMap>\n" +
                    "</ncx>";
            addFileToZip(zos, "OEBPS/toc.ncx", tocNcx, true);
            
            // content.html
            String fullHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                    "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                    "<head><title>" + title + "</title></head>\n" +
                    "<body>" + htmlContent + "</body>\n" +
                    "</html>";
            addFileToZip(zos, "OEBPS/content.html", fullHtml, true);
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
}
