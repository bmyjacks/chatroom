package io.bmyjacks.app.chatroom.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Server class represents a chat server. It maintains a list of active clients and a history of
 * messages. It uses a thread pool to handle multiple client connections concurrently.
 */
public class Server {

  // A thread-safe list of active clients
  private static final CopyOnWriteArrayList<ClientHandler> activeClient = new CopyOnWriteArrayList<>();
  // A thread-safe list of messages for maintaining the history of messages
  private static final Vector<Message> history = new Vector<>();
  // A thread pool for handling multiple client connections concurrently
  private static final ExecutorService executorService = Executors.newCachedThreadPool();
  // The server socket for accepting client connections
  private static ServerSocket serverSocket;

  // The port number on which the server is running
  private final int port;
  // Whether the server is running
  private final boolean running = true;

  /**
   * Constructs a new Server.
   *
   * @param port the port number on which the server will run
   * @throws IOException if there is an error in binding the server socket to the specified port
   */
  public Server(int port) throws IOException {
    try {
      serverSocket = new ServerSocket();
    } catch (IOException e) {
      System.err.println("Error creating server socket: " + e.getMessage());
    }

    this.port = port;
    getServerSocket().bind(new InetSocketAddress("0.0.0.0", getPort()));
  }

  /**
   * Returns the list of active clients.
   *
   * @return the list of active clients
   */
  public static CopyOnWriteArrayList<ClientHandler> getActiveClient() {
    return activeClient;
  }

  /**
   * Returns the history of messages.
   *
   * @return the history of messages
   */
  public static Vector<Message> getHistory() {
    return history;
  }

  /**
   * Returns the executor service.
   *
   * @return the executor service
   */
  public static ExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Returns the server socket.
   *
   * @return the server socket
   */
  public static ServerSocket getServerSocket() {
    return serverSocket;
  }

  /**
   * Returns the port number on which the server is running.
   *
   * @return the port number on which the server is running
   */
  public int getPort() {
    return this.port;
  }


  /**
   * Checks if the server is running.
   *
   * @return true if the server is running, false otherwise
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Starts the server. It listens for incoming client connections, starts a new ClientHandler and
   * add them to the list of active clients for each one.
   *
   * @throws IOException if there is an error accepting a client connection
   */
  public void run() throws IOException {
    while (isRunning()) {
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
      getExecutorService().execute(clientHandler);
    }
  }
}