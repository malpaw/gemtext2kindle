package com.example.gemtext2kindle.stage3;

import java.util.Properties;

public record SmtpConfig(String host, String port, String user, String password) {
    public Properties toProperties() {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);
        return prop;
    }
}
