package com.example.gemtext2kindle.stage2;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import static org.assertj.core.api.Assertions.assertThat;

public class EpubAssemblerTest {

    @Test
    public void shouldCreateValidZipStructure(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        Path epubPath = tempDir.resolve("test.epub");
        EpubAssembler assembler = new EpubAssembler();
        
        assembler.assemble("Test Title", "<p>Hello</p>", epubPath);
        
        assertThat(epubPath).exists();
        
        try (ZipFile zipFile = new ZipFile(epubPath.toFile())) {
            assertThat(zipFile.getEntry("mimetype")).isNotNull();
            assertThat(zipFile.getEntry("META-INF/container.xml")).isNotNull();
            assertThat(zipFile.getEntry("OEBPS/content.opf")).isNotNull();
            assertThat(zipFile.getEntry("OEBPS/toc.ncx")).isNotNull();
            assertThat(zipFile.getEntry("OEBPS/content.html")).isNotNull();
        }
    }
}
