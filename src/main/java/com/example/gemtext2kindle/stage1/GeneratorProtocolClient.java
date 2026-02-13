package com.example.gemtext2kindle.stage1;

public class GeneratorProtocolClient implements ProtocolClient {

    @Override
    public String fetch(String url) {
        return "# Generated Article\n\nContent for " + url + "\n\n deterministic text for testing.";
    }
}
