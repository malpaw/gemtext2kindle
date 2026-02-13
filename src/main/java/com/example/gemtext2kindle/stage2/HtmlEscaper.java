package com.example.gemtext2kindle.stage2;

public class HtmlEscaper {

    public String escape(String text) {
        if (text == null) return null;
        StringBuilder out = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '<') out.append("&" + "lt;");
            else if (c == '>') out.append("&" + "gt;");
            else if (c == '&') out.append("&" + "amp;");
            else if (c == '"') out.append("&" + "quot;");
            else out.append(c);
        }
        return out.toString();
    }
}
