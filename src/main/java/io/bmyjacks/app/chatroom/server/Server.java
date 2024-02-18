package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

  // A list of active clients
  private static final Vector<ClientHandler> activeClient = new Vector<>();
  // A history of messages
  private static final Vector<Message> history = new Vector<>();
  private static final ExecutorService executorService = Executors.newCachedThreadPool();
  private static final ServerSocket serverSocket;

  static {
    try {
      serverSocket = new ServerSocket();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final int port;

  public Server(int port) throws IOException {
    this.port = port;
    serverSocket.bind(new InetSocketAddress("0.0.0.0", port));
  }

  public static ServerSocket getServerSocket() {
    return serverSocket;
  }

  public static Vector<ClientHandler> getActiveClient() {
    return activeClient;
  }

  public static Vector<Message> getHistory() {
    return history;
  }

  public int getPort() {
    return port;
  }

  /**
   * Starts the server. It listens for incoming client connections and starts a new ClientHandler
   * for each one.
   *
   * @throws IOException if there is an error accepting a client connection
   */
  public void run() throws IOException {
    while (true) {
      // Accept a client connection
      Socket clientSocket = getServerSocket().accept();

      // Create input and output streams for the client
      DataInputStream streamFromClient = new DataInputStream(clientSocket.getInputStream());
      DataOutputStream streamToClient = new DataOutputStream(clientSocket.getOutputStream());

      // Create a new ClientHandler for the client
      ClientHandler clientHandler = new ClientHandler(clientSocket, streamFromClient,
          streamToClient);

      // Add the client to the list of active clients
      getActiveClient().add(clientHandler);

      // Start the ClientHandler in a new thread
      executorService.execute(clientHandler);
    }
  }
}