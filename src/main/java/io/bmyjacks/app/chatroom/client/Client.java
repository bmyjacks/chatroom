package io.bmyjacks.app.chatroom.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {
    private final int port;
    private final InetAddress host;

    public Client(int port) throws UnknownHostException {
        this.host = InetAddress.getLocalHost();
        this.port = port;
    }

    public void run() throws IOException {
        System.out.println("Welcome to the chatroom application!");
        Socket socket = new Socket(host, port);

        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        Scanner scanner = new Scanner(System.in);

        Thread sendMessage = new Thread(() -> {
            while (true) {
                String message = scanner.nextLine();
                try {
                    dataOutputStream.writeUTF(message);
                } catch (IOException e) {
                    System.out.println("Error IOException");
                }
            }
        });

        Thread readMessage = new Thread(() -> {
            while (true) {
                try {
                    String message = dataInputStream.readUTF();
                    System.out.println(message);
                } catch (IOException e) {
                    System.out.println("Error IOException");
                }
            }
        });

        sendMessage.start();
        readMessage.start();
    }
}