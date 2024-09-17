package org.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream())) {

            Request request = parseRequest(in);
            if (request != null) {
                Handler handler = getHandler(request.getMethod(), request.getPath());
                if (handler != null) {
                    handler.handle(request, out);
                } else {
                    sendNotFoundResponse(out);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Request parseRequest(BufferedReader in) throws IOException {
        String line = in.readLine();
        if (line == null || line.isEmpty()) {
            return null;
        }
        String[] requestLine = line.split(" ");
        if (requestLine.length < 2) {
            return null;
        }

        String method = requestLine[0];
        String fullPath = requestLine[1];

        String path;
        String query = null;
        int queryStart = fullPath.indexOf('?');
        if (queryStart >= 0) {
            path = fullPath.substring(0, queryStart);
            query = fullPath.substring(queryStart + 1);
        } else {
            path = fullPath;
        }

        Map<String, String> queryParams = query != null ? Request.parseqQeryParams(query) : new HashMap<>();


        return new Request(method, path, queryParams);
    }

    private Handler getHandler(String method, String path) {
        return handlers.getOrDefault(method, new ConcurrentHashMap<>()).get(path);
    }

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    private void sendNotFoundResponse(BufferedOutputStream out) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n\r\n";
        out.write(response.getBytes());
        out.flush();
    }

    public void listen(int port) {
        start(port);
    }
}
