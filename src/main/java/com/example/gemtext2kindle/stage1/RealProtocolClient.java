package com.example.gemtext2kindle.stage1;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RealProtocolClient implements ProtocolClient {
    private static final int CONNECTION_TIMEOUT = 1000; // 2 seconds

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

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            }, new java.security.SecureRandom());
            SSLSocketFactory factory = sc.getSocketFactory();
            
            try (Socket baseSocket = new Socket()) {
                baseSocket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT); 
                try (SSLSocket socket = (SSLSocket) factory.createSocket(baseSocket, host, port, true)) {
                    socket.setSoTimeout(CONNECTION_TIMEOUT); 
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
        } catch (Exception e) {
            throw new IOException("Failed to fetch from Gemini server: " + e.getMessage(), e);
        }
    }

    private String fetchGopher(URI uri) throws IOException {
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? 70 : uri.getPort();
        String path = uri.getPath();
        if (path.isEmpty()) path = "/";
        
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECTION_TIMEOUT); 
            socket.setSoTimeout(CONNECTION_TIMEOUT); 
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
