package io.bmyjacks.app.chatroom.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        System.out.println("Connected to server " + socket.getRemoteSocketAddress());
        dataInputStream = new DataInputStream(System.in);
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        String line = "";
        while (!line.equals("exit")) {
            try {
                line = dataInputStream.readLine();
                dataOutputStream.writeUTF(line);
                dataOutputStream.flush();
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 6676);
    }
}
