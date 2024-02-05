package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final DataInputStream streamFromClient;
    private final DataOutputStream streamToClient;
    private String username;
    private boolean isOnline;

    ClientHandler(Socket socket, DataInputStream streamFromClient, DataOutputStream streamToClient) {
        this.clientSocket = socket;
        this.streamFromClient = streamFromClient;
        this.streamToClient = streamToClient;
        this.isOnline = true;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username.equals("/exit")) {
            System.exit(0);
        }
        this.username = username;
    }


    public Socket getClientSocket() {
        return clientSocket;
    }


    public DataOutputStream getStreamToClient() {
        return streamToClient;
    }


    public DataInputStream getStreamFromClient() {
        return streamFromClient;
    }


    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    void sendToAll(Message message) throws IOException {
        for (var client : Server.activeClient) {
            client.getStreamToClient().writeUTF(message.toString());
        }
    }

    void getUsernameInput() {
        String usernameInput = "";
        try {
            usernameInput = getStreamFromClient().readUTF();
        } catch (IOException e) {
            System.out.println("Error: IOException");
        }
        setUsername(usernameInput);
    }

    void sendHistory() {
        try {
            for (var historyMessage : Server.history) {
                getStreamToClient().writeUTF(historyMessage.toString());
            }

        } catch (IOException e) {
            System.out.println("Error: IOException");
        }
    }

    void close() {
        try {
            getStreamFromClient().close();
            getStreamToClient().close();
            getClientSocket().close();
            Server.activeClient.remove(this);
        } catch (IOException e) {
            System.out.println("Error: IOException");
        }
    }

    void processMessage(String input) throws IOException {
        Message message = new Message(getUsername(), input);

        if (input.equals("/exit")) {
            setOnline(false);
            return;
        }

        sendToAll(message);

        if (!input.equals("/undo")) {
            Server.history.add(message);
        }
    }

    @Override
    public void run() {
        getUsernameInput();

        if (!Server.history.isEmpty()) {
            sendHistory();
        }

        while (isOnline()) {
            try {
                String userInput = getStreamFromClient().readUTF();
                processMessage(userInput);
            } catch (IOException e) {
                System.out.println("Error: IOException");
            }
        }

        close();
    }

}
