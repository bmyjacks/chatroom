package io.bmyjacks.app.chatroom.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final Socket socket;
    private final ServerSocket serverSocket;
    private final DataInputStream dataInputStream;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + serverSocket.getLocalPort());
        System.out.println("Waiting for client to connect...");

        socket = serverSocket.accept();
        System.out.println("Client connected: " + socket.getRemoteSocketAddress());
        dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        String line = "";
        while (!line.equals("exit")) {
            try {
                line = dataInputStream.readUTF();
                System.out.println(line);
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        try {
            dataInputStream.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(6676);
    }
}
