package com.example.gemtext2kindle.stage1;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class RealProtocolClient implements ProtocolClient {

    @Override
    public String fetch(String url) throws IOException {
        URI uri = URI.create(url);
        String protocol = uri.getScheme();
        if ("gemini".equalsIgnoreCase(protocol)) {
            return fetchGemini(uri);
        } else if ("gopher".equalsIgnoreCase(protocol)) {
            return fetchGopher(uri);
        } else {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
    }

    private String fetchGemini(URI uri) throws IOException {
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 1965 : uri.getPort();
        
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
            socket.startHandshake();
            
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            out.print(uri.toString() + "\r\n");
            out.flush();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String header = in.readLine();
            if (header == null) throw new IOException("No response from Gemini server");
            
            if (header.startsWith("2")) { // Success
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            } else {
                throw new IOException("Gemini server returned error: " + header);
            }
        }
    }

    private String fetchGopher(URI uri) throws IOException {
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 70 : uri.getPort();
        String path = uri.getPath();
        if (path.isEmpty()) path = "/";
        
        try (Socket socket = new Socket(host, port)) {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            out.print(path + "\r\n");
            out.flush();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }
}
