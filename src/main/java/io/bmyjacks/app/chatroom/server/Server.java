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
 *
 * @param port The port the server is running on
 */
public record Server(int port) {
    // A list of active clients
    private static final Vector<ClientHandler> activeClient = new Vector<>();
    // A history of messages
    private static final Vector<Message> history = new Vector<>();
    private static ServerSocket serverSocket;

    public Server(int port) {
        this.port = port;
        activeClient.clear();
        history.clear();
    }

    public static ServerSocket getServerSocket() {
        return serverSocket;
    }

    public static void setServerSocket(ServerSocket serverSocket) {
        Server.serverSocket = serverSocket;
    }

    public static Vector<ClientHandler> getActiveClient() {
        return activeClient;
    }

    public static Vector<Message> getHistory() {
        return history;
    }

    /**
     * Starts the server.
     * It listens for incoming client connections and starts a new ClientHandler for each one.
     *
     * @throws IOException if there is an error accepting a client connection
     */
    public void run() throws IOException {
        setServerSocket(new ServerSocket(port));

        while (true) {
            // Accept a client connection
            Socket clientSocket = getServerSocket().accept();

            // Create input and output streams for the client
            DataInputStream streamFromClient = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream streamToClient = new DataOutputStream(clientSocket.getOutputStream());

            // Create a new ClientHandler for the client
            ClientHandler clientHandler = new ClientHandler(clientSocket, streamFromClient, streamToClient);

            // Add the client to the list of active clients
            getActiveClient().add(clientHandler);

            // Start the ClientHandler in a new thread
            Thread handlerThread = new Thread(clientHandler);
            handlerThread.start();
        }
    }
}