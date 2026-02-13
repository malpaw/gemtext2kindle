package com.example.gemtext2kindle.stage3;

import org.junit.jupiter.api.Test;
import java.util.Properties;
import static org.assertj.core.api.Assertions.assertThat;

public class SmtpConfigTest {

    @Test
    public void shouldCreatePropertiesFromConfig() {
        SmtpConfig config = new SmtpConfig("host", "587", "user", "pass");
        Properties props = config.toProperties();
        
        assertThat(props.getProperty("mail.smtp.host")).isEqualTo("host");
        assertThat(props.getProperty("mail.smtp.port")).isEqualTo("587");
        assertThat(props.getProperty("mail.smtp.auth")).isEqualTo("true");
        assertThat(props.getProperty("mail.smtp.starttls.enable")).isEqualTo("true");
    }
}
