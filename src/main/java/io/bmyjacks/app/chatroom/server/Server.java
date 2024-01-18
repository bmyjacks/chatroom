package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    static Vector<ClientHandler> activeClient = new Vector<>();
    static String history = "";
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket socket;

        while (true) {
            socket = serverSocket.accept();
            System.out.println("New client request received: " + socket);

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            System.out.println("Creating a new handler for this client...");
            ClientHandler clientHandler = new ClientHandler(socket, dataInputStream, dataOutputStream);

            Thread handlerThread = new Thread(clientHandler);
            System.out.println("Adding this client to active client list");
            activeClient.add(clientHandler);
            handlerThread.start();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private String name;

    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.name = "#" + socket.getPort();
        this.socket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    @Override
    public void run() {
        String userInput = null;
        String output = "Type your nickname: ";
        try {
            dataOutputStream.writeUTF(output);
            dataOutputStream.flush();
            name = dataInputStream.readUTF();
        } catch (IOException e) {
            System.out.println("Error IOException");
        }

        try {
            dataOutputStream.writeUTF(Server.history);
        } catch (IOException e) {
            System.out.println("Error IOException");
        }

        while (true) {
            try {
                userInput = dataInputStream.readUTF();
                if (userInput.equals("exit")) {
                    break;
                }
                System.out.println(name + ": " + userInput);
                for (var clientHandler : Server.activeClient) {
                    if (clientHandler != this) {
                        clientHandler.dataOutputStream.writeUTF(name + ": " + userInput);
                    }
                }
                Server.history += name + ": " + userInput + "\n";
            } catch (IOException e) {
                System.out.println("Error: IOException");
            }
        }

        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error: IOException");
        }
    }
}