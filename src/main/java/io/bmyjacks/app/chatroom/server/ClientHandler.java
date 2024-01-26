package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final DataInputStream streamFromClient;
    private final DataOutputStream streamToClient;
    private String username;
    private boolean isOnline;

    protected ClientHandler(Socket socket, DataInputStream streamFromClient, DataOutputStream streamToClient) {
        this.username = "#" + socket.getPort();
        this.clientSocket = socket;
        this.streamFromClient = streamFromClient;
        this.streamToClient = streamToClient;
        this.isOnline = true;
    }

    void sendToAll(String message) throws IOException {
        for (var client : Server.activeClient) {
            client.streamToClient.writeUTF(message);
        }
    }

    void unsentMessage(String username) throws IOException {
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

    void getUsername() {
        String userInput = null;
        try {
            userInput = streamFromClient.readUTF();
        } catch (IOException e) {
            System.out.println("Error: IOException");
        }

        if (userInput.equals("/exit")) {
            System.exit(0);
        }
        username = userInput;
    }

    void sendHistory() {
        try {
            for (var historyMessage : Server.history) {
                streamToClient.writeUTF(historyMessage);
            }

        } catch (IOException e) {
            System.out.println("Error: IOException");
        }
    }

    void close() {
        try {
            streamFromClient.close();
            streamToClient.close();
            clientSocket.close();
            Server.activeClient.remove(this);
        } catch (IOException e) {
            System.out.println("Error: IOException");
        }
    }

    String getFormattedCurrentTime() {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return "[" + time.format(formatter) + "] ";
    }

    void processMessage(String input) throws IOException {
        if (input.equals("/exit")) {
            isOnline = false;
            return;
        }
        if (input.equals("/undo")) {
            unsentMessage(username);
            return;
        }

        String message = getFormattedCurrentTime() + username + ": " + input;
        sendToAll(message);
        Server.history.add(message);
    }

    @Override
    public void run() {
        getUsername();

        if (!Server.history.isEmpty()) {
            sendHistory();
            try {
                streamToClient.writeUTF("-------- Above is history --------");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        while (isOnline) {
            try {
                String userInput = streamFromClient.readUTF();
                processMessage(userInput);
            } catch (IOException e) {
                System.out.println("Error: IOException");
            }
        }

        close();
    }
}
