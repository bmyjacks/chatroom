package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * This class represents a client handler in the chatroom server. It handles communication with a
 * single client.
 */
public class ClientHandler implements Runnable {

  // The client's socket
  private final Socket clientSocket;
  // The input stream from the client
  private final DataInputStream streamFromClient;
  // The output stream to the client
  private final DataOutputStream streamToClient;
  // The username of the client
  private String username;
  // Whether the client is online
  private boolean isOnline;

  /**
   * Constructs a new ClientHandler with the specified socket and input/output streams.
   *
   * @param socket           the client's socket
   * @param streamFromClient the input stream from the client
   * @param streamToClient   the output stream to the client
   */
  ClientHandler(Socket socket, DataInputStream streamFromClient, DataOutputStream streamToClient) {
    this.clientSocket = socket;
    this.streamFromClient = streamFromClient;
    this.streamToClient = streamToClient;
    this.username = "";
    this.isOnline = true;
  }

  /**
   * Returns the username of the client.
   *
   * @return the username of the client
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username of the client. If the username is "/exit", the system will exit.
   *
   * @param username the new username
   */
  public void setUsername(String username) {
    if (username.equals("/exit")) {
      System.exit(0);
    }
    this.username = username;
  }

  /**
   * Returns the client's socket.
   *
   * @return the client's socket
   */
  public Socket getClientSocket() {
    return clientSocket;
  }

  /**
   * Returns the output stream to the client.
   *
   * @return the output stream to the client
   */
  public DataOutputStream getStreamToClient() {
    return streamToClient;
  }

  /**
   * Returns the input stream from the client.
   *
   * @return the input stream from the client
   */
  public DataInputStream getStreamFromClient() {
    return streamFromClient;
  }

  /**
   * Returns whether the client is online.
   *
   * @return true if the client is online, false otherwise
   */
  public boolean isOnline() {
    return isOnline;
  }

  /**
   * Sets whether the client is online.
   *
   * @param online true if the client is online, false otherwise
   */
  public void setOnline(boolean online) {
    isOnline = online;
  }

  /**
   * Sends a message to all active clients.
   *
   * @param message the message to send
   * @throws IOException if there is an error writing to the output stream
   */
  void sendToAll(Message message) throws IOException {
    for (var client : Server.getActiveClient()) {
      client.getStreamToClient().writeUTF(message.toString());
    }
  }

  /**
   * Gets the username input from the client.
   */
  void getUsernameInput() {
    String usernameInput = "";
    try {
      usernameInput = getStreamFromClient().readUTF();
    } catch (IOException e) {
      System.out.println("Error: IOException");
    }
    setUsername(usernameInput);
  }

  /**
   * Sends the chat history to the client.
   */
  void sendHistory() throws IOException {
    for (var historyMessage : Server.getHistory()) {
      getStreamToClient().writeUTF(historyMessage.toString());
    }
  }

  /**
   * Closes the client's connection and removes them from the list of active clients.
   */
  void close() {
    try {
      sendToAll(new Message("Server", getUsername() + " has left the chatroom."));
      getStreamFromClient().close();
      getStreamToClient().close();
      getClientSocket().close();
      Server.getActiveClient().remove(this);
    } catch (IOException e) {
      System.out.println("Error: IOException");
    }
  }

  /**
   * Processes a message from the client and add to the Server's history If the message is "/exit",
   * the client will be set to offline.
   *
   * @param input the message from the client
   * @throws IOException if there is an error sending the message
   */
  void processMessage(String input) throws IOException {
    Message message = new Message(getUsername(), input);

    if (input.equals("/exit")) {
      setOnline(false);
      return;
    }
    sendToAll(message);
    Server.getHistory().add(message);
  }

  /**
   * The main loop for the client handler. It gets the username input, sends the chat history, and
   * processes messages from the client until they go offline.
   */
  @Override
  public void run() {
    getUsernameInput();

    try {
      sendHistory();
      sendToAll(new Message("Server", getUsername() + " has joined the chatroom."));
    } catch (IOException e) {
      throw new RuntimeException(e);
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