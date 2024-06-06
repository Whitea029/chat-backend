package fun.whitea.easychatbackend.test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SocketServer {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Map<String, Socket> CLIENT_SOCKET_MAP = new HashMap<>();
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server started on port 8080");
            while (true) {
                Socket socket = serverSocket.accept();
                String ip = socket.getInetAddress().getHostAddress();
                int port = socket.getPort();
                System.out.println("Client connected: " + ip + ":" + port);

                CLIENT_SOCKET_MAP.put(ip + ":" + port, socket);

                new Thread(() -> {
                    while (true) {
                        try {
                            InputStream inputStream = socket.getInputStream();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,StandardCharsets.UTF_8);
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String s = bufferedReader.readLine();
                            System.out.println("Received message: " + s);

                            CLIENT_SOCKET_MAP.forEach((k, v) -> {
                                try {
                                    OutputStream outputStream = v.getOutputStream();
                                    PrintWriter printWriter = new PrintWriter(outputStream);
                                    printWriter.println("Received message from " + k + ":" + v + "Message: " + s);
                                    printWriter.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                } ).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
