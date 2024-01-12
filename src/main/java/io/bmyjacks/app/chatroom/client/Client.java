package io.bmyjacks.app.chatroom.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This class represents a client in a chatroom application.
 * It establishes a connection to a server and allows the user to send messages to the server.
 */
public class Client {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;

    /**
     * Constructs a new Client.
     * @param host The hostname of the server to connect to.
     * @param port The port number of the server to connect to.
     * @throws IOException If an I/O error occurs when creating the socket.
     */
    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        System.out.println("Connected to server " + socket.getRemoteSocketAddress());
        dataInputStream = new DataInputStream(System.in);
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        String line = "";
        // Continuously read input from the user and send it to the server until the user types "exit".
        while (!line.equals("exit")) {
            try {
                line = dataInputStream.readLine();
                dataOutputStream.writeUTF(line);
                dataOutputStream.flush();
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        // Close the input stream, output stream, and socket when done.
        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * The main method that creates a new Client and connects it to a server.
     * @param args The command line arguments. Not used in this application.
     * @throws IOException If an I/O error occurs when creating the Client.
     */
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 6676);
    }
}