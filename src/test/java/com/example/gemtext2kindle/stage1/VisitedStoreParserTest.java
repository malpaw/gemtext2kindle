package com.example.gemtext2kindle.stage1;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class VisitedStoreParserTest {

    @Test
    public void shouldParseVisitedLine() {
        VisitedStoreParser parser = new VisitedStoreParser();
        
        // Data from data/visited.2.txt
        String line = "1771029826 0000 gemini://archiwistka.blog/gemlog_echo_socjalizacji/";
        
        VisitedStore.Visit visit = parser.parseLine(line);
        
        assertThat(visit).isNotNull();
        assertThat(visit.timestamp()).isEqualTo(1771029826L);
        assertThat(visit.flags()).isEqualTo(0);
        assertThat(visit.url()).isEqualTo("gemini://archiwistka.blog/gemlog_echo_socjalizacji/");
    }

    @Test
    public void shouldParseVisitedLineWithFlags() {
        VisitedStoreParser parser = new VisitedStoreParser();
        
        String line = "1694172824 0003 gemini://calcuode.com/gemlog/2021-06-21_gmni-kobo.gmi";
        
        VisitedStore.Visit visit = parser.parseLine(line);
        
        assertThat(visit).isNotNull();
        assertThat(visit.timestamp()).isEqualTo(1694172824L);
        assertThat(visit.flags()).isEqualTo(3);
        assertThat(visit.url()).isEqualTo("gemini://calcuode.com/gemlog/2021-06-21_gmni-kobo.gmi");
    }

    @Test
    public void shouldReturnNullOnInvalidLine() {
        VisitedStoreParser parser = new VisitedStoreParser();
        assertThat(parser.parseLine("invalid line")).isNull();
        assertThat(parser.parseLine("")).isNull();
        assertThat(parser.parseLine(null)).isNull();
    }
}
