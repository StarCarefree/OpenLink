package fun.moystudio.openlink.network;

import com.sun.net.httpserver.HttpServer;
import fun.moystudio.openlink.OpenLink;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class LoginGetCodeHttpServer {
    public int port;
    public String code = null;
    public HttpServer server;
    public void start(){
        int port = 23579;
        HttpServer server = null;
        while(server==null){
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
            } catch (IOException e) {port++;}
        }
        server.createContext("/", exchange -> {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    URI requestUri = exchange.getRequestURI();
                    String query = requestUri.getQuery();
                    String codeValue = null;

                    if (query != null) {
                        String[] pairs = query.split("&");
                        for (String pair : pairs) {
                            int idx = pair.indexOf("=");
                            String key = (idx > 0) ?
                                    URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
                            if ("code".equalsIgnoreCase(key)) {
                                codeValue = (idx > 0 && pair.length() > idx + 1) ?
                                        URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : "";
                                break;
                            }
                        }
                    }
                    String response;
                    exchange.getResponseHeaders().set("Content-Type", "text/html;charset=UTF-8");
                    if (codeValue != null) {
                        this.code=codeValue;
                        response = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"></head><body><h1>已成功接收登录请求！您可关闭此页面。</h1><br><h1>Successfully received login request! You can close this page.</h1></body></html>";
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                    } else {
                        response = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"></head><body><h1>出现错误！</h1><br><h1>Error!</h1></body></html>";
                        exchange.sendResponseHeaders(400, response.getBytes().length);
                    }

                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    if(this.code!=null){
                        try {
                            Request.POST(Uris.openFrpAPIUri + "oauth2/callback?code=" + this.code + "&redirect_url=http://localhost:"+this.port, Request.DEFAULT_HEADER, "{}");
                        } catch (Exception e) {
                            OpenLink.LOGGER.error("", e);
                            throw new RuntimeException(e);
                        }
                        this.stop();
                    }
                }
            } catch (Exception e) {
                OpenLink.LOGGER.error("", e);
                exchange.getResponseHeaders().set("Content-Type", "text/html;charset=UTF-8");
                exchange.sendResponseHeaders(500, 0);
            }
        });
        server.start();
        OpenLink.LOGGER.info("Login Server Started.");
        this.port=port;
        this.server=server;
    }
    public void stop(){
        this.server.stop(0);
        OpenLink.LOGGER.info("Login Server Stopped.");
    }
}