package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    static public Vector<ClientHandler> activeClient = new Vector<>();
    static public Vector<Message> history = new Vector<>();
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException, SecurityException, IllegalArgumentException {
        ServerSocket serverSocket;
        serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSocket = serverSocket.accept();

            DataInputStream streamFromClient = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream streamToClient = new DataOutputStream(clientSocket.getOutputStream());

            ClientHandler clientHandler = new ClientHandler(clientSocket, streamFromClient, streamToClient);

            activeClient.add(clientHandler);

            Thread handlerThread = new Thread(clientHandler);
            handlerThread.start();
        }
    }
}

