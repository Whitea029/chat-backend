package fun.whitea.easychatbackend.test;

import lombok.val;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SocketClient001 {
    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 8080);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            System.out.println("Please input message:");

            new Thread(() -> {
                while (true){
                    Scanner scanner = new Scanner(System.in);
                    String message = scanner.nextLine();
                    printWriter.println(message);
                    printWriter.flush();
                }
            } ).start();


            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            new Thread(() -> {
                while (true){
                    try {
                        val s = bufferedReader.readLine();
                        System.out.println("Received message: " + s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
