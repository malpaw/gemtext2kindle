package com.example.gemtext2kindle.stage1;

import java.io.IOException;

public interface ProtocolClient {
    String fetch(String url) throws IOException;
}
