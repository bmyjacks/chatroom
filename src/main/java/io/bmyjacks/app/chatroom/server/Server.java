package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * This class represents the server in the chatroom application.
 * It maintains a list of active clients and a history of messages.
 * It listens for incoming client connections and starts a new ClientHandler for each one.
 */
public class Server {
    // A history of messages
    static Vector<Message> history = new Vector<>();
    // A list of active clients
    static Vector<ClientHandler> activeClient = new Vector<>();
    // The port the server is running on
    private final int port;

    /**
     * Constructs a new Server with the specified port.
     *
     * @param port the port the server will run on
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Starts the server.
     * It listens for incoming client connections and starts a new ClientHandler for each one.
     *
     * @throws IOException              if there is an error accepting a client connection
     * @throws SecurityException        if a security manager exists and its checkListen method doesn't allow the operation
     * @throws IllegalArgumentException if the port parameter is outside the specified range of valid port values
     */
    public void run() throws IOException, SecurityException, IllegalArgumentException {
        ServerSocket serverSocket;
        serverSocket = new ServerSocket(port);

        while (true) {
            // Accept a client connection
            Socket clientSocket = serverSocket.accept();

            // Create input and output streams for the client
            DataInputStream streamFromClient = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream streamToClient = new DataOutputStream(clientSocket.getOutputStream());

            // Create a new ClientHandler for the client
            ClientHandler clientHandler = new ClientHandler(clientSocket, streamFromClient, streamToClient);

            // Add the client to the list of active clients
            activeClient.add(clientHandler);

            // Start the ClientHandler in a new thread
            Thread handlerThread = new Thread(clientHandler);
            handlerThread.start();
        }
    }
}