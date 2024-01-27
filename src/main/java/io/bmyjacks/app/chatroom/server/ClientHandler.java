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

    ClientHandler(Socket socket, DataInputStream streamFromClient, DataOutputStream streamToClient) {
        setUsername("#" + socket.getPort());
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

    void sendToAll(String message) throws IOException {
        for (var client : Server.activeClient) {
            client.getStreamToClient().writeUTF(message);
        }
    }

    void unsentMessage() throws IOException {
        for (int i = Server.history.size() - 1; i >= 0; i--) {
            StringTokenizer stringTokenizer = new StringTokenizer(Server.history.get(i));
            stringTokenizer.nextToken();
            String messageUsername = stringTokenizer.nextToken();
            if (messageUsername.equals(getUsername() + ":")) {
                Server.history.set(i, getUsername() + " unsent");
                break;
            }
        }
        sendToAll("#CLR");
        for (var historyMessage : Server.history) {
            sendToAll(historyMessage);
        }
    }

    void getUsernameInput() {
        String userInput = "";
        try {
            userInput = getStreamFromClient().readUTF();
        } catch (IOException e) {
            System.out.println("Error: IOException");
        }
        setUsername(userInput);
    }

    void sendHistory() {
        try {
            for (var historyMessage : Server.history) {
                getStreamToClient().writeUTF(historyMessage);
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

    String getFormattedCurrentTime() {
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return "[" + time.format(formatter) + "] ";
    }

    void processMessage(String input) throws IOException {
        if (input.equals("/exit")) {
            setOnline(false);
            return;
        }
        if (input.equals("/undo")) {
            unsentMessage();
            return;
        }

        String message = getFormattedCurrentTime() + getUsername() + ": " + input;
        sendToAll(message);
        Server.history.add(message);
    }

    @Override
    public void run() {
        getUsernameInput();

        if (!Server.history.isEmpty()) {
            sendHistory();
            try {
                streamToClient.writeUTF("-------- Above is history --------");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
