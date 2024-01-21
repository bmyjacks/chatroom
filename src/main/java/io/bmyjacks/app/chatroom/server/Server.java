package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;
import java.util.Vector;

public class Server {
    static Vector<ClientHandler> activeClient = new Vector<>();
    static Vector<String> history = new Vector<>();
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket;

        while (true) {
            clientSocket = serverSocket.accept();
//            System.out.println("New client request received: " + socket);

            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

//            System.out.println("Creating a new handler for this client...");
            ClientHandler clientHandler = new ClientHandler(clientSocket, dataInputStream, dataOutputStream);

            Thread handlerThread = new Thread(clientHandler);
//            System.out.println("Adding this client to active client list");
            activeClient.add(clientHandler);
            handlerThread.start();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private String username;

    public ClientHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.username = "#" + socket.getPort();
        this.clientSocket = socket;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
    }

    public void sendToAll(String message) throws IOException {
        for (var client : Server.activeClient) {
            client.dataOutputStream.writeUTF(message);
        }
    }

    public void unsentMessage(String username) throws IOException {
        for (int i = Server.history.size() - 1; i >= 0; i--) {
            StringTokenizer stringTokenizer = new StringTokenizer(Server.history.get(i));
            stringTokenizer.nextToken();
            String messageUsername = stringTokenizer.nextToken();
            if (messageUsername.equals(username + ":")) {
                Server.history.set(i, username + " unsent");
                break;
            }
        }
        sendToAll("#CLR");
        for (var historyMessage : Server.history) {
            sendToAll(historyMessage);
        }
    }

    @Override
    public void run() {
        String userInput = null;

        try {
            username = dataInputStream.readUTF();
        } catch (IOException e) {
            System.out.println("Error IOException");
        }

        if (!Server.history.isEmpty()) {
            try {
                for (var historyMessage : Server.history) {
                    dataOutputStream.writeUTF(historyMessage);
                }
                dataOutputStream.writeUTF("-------- Above is history --------");
            } catch (IOException e) {
                System.out.println("Error IOException");
            }
        }


        while (true) {
            try {
                userInput = dataInputStream.readUTF();
                if (userInput.equals("/exit")) {
                    break;
                }
                if (userInput.equals("/undo")) {
                    unsentMessage(username);
                    continue;
                }
                LocalTime time = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String message = "[" + time.format(formatter) + "] " + username + ": " + userInput;
                System.out.println(message);
                for (var client : Server.activeClient) {
                    if (client == this) {
                        client.dataOutputStream.writeUTF(message);
                    } else {
                        client.dataOutputStream.writeUTF(message);
                    }
                }
                Server.history.add(message);
            } catch (IOException e) {
                System.out.println("Error: IOException");
                break;
            }
        }

        try {
            dataInputStream.close();
            dataOutputStream.close();
            clientSocket.close();
            Server.activeClient.remove(this);
        } catch (IOException e) {
            System.out.println("Error: IOException");
        }
    }
}