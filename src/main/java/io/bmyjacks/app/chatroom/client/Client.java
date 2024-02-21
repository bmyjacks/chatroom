package io.bmyjacks.app.chatroom.client;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import io.bmyjacks.app.chatroom.server.Message;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Client class represents a client in chat room. It handles communication with the server and a
 * user interface for sending and receiving messages.
 */
public class Client {

  // The host address of the server
  // The InetAddress object representing the server's host address
  private final InetAddress host;

  // The port number of the server
  private final int port;

  // The Socket object used for communication with the server
  private final Socket socket;

  // The DataInputStream object used for receiving data from the server
  private final DataInputStream streamFromServer;

  // The DataOutputStream object used for sending data to the server
  private final DataOutputStream streamToServer;

  // A Vector object containing the history of messages received from the server
  private final Vector<Message> historyMessage = new Vector<>();

  // A Vector object containing the labels for displaying the message history in the user interface
  private final Vector<Label> historyLabel = new Vector<>();

  // The Terminal object used for the user interface
  private final Terminal terminal = new DefaultTerminalFactory().setForceTextTerminal(false)
      .createTerminal();

  // The Screen object used for the user interface
  private final Screen screen = new TerminalScreen(terminal);

  // The WindowBasedTextGUI object used for the user interface
  private final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

  // The ScheduledExecutorService object used for scheduling tasks
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

  // The BasicWindow object used for the user interface
  private final BasicWindow window = new BasicWindow();

  // The username of the client
  private String username;

  /**
   * Constructor for the Client class. It initializes the host and port for the server, and
   * establishes a socket connection to the server. It also initializes the input and output streams
   * for communication with the server.
   *
   * @param host the InetAddress object representing the server's host address
   * @param port the port number of the server
   * @throws IOException if an I/O error occurs when creating the socket or the input/output
   *                     streams
   */
  public Client(InetAddress host, int port) throws IOException {
    this.host = host;
    this.port = port;
    this.socket = new Socket(getHost(), getPort());
    streamFromServer = new DataInputStream(getSocket().getInputStream());
    streamToServer = new DataOutputStream(getSocket().getOutputStream());
  }

  /**
   * Returns the ScheduledExecutorService object used for scheduling tasks.
   *
   * @return the ScheduledExecutorService object
   */
  public ScheduledExecutorService getExecutorService() {
    return executorService;
  }

  /**
   * Returns the DataInputStream object used for receiving data from the server.
   *
   * @return the DataInputStream object
   */
  public DataInputStream getStreamFromServer() {
    return streamFromServer;
  }

  /**
   * Returns the DataOutputStream object used for sending data to the server.
   *
   * @return the DataOutputStream object
   */
  public DataOutputStream getStreamToServer() {
    return streamToServer;
  }

  /**
   * Returns the Socket object used for communication with the server.
   *
   * @return the Socket object
   */
  public Socket getSocket() {
    return socket;
  }

  /**
   * Returns the InetAddress object representing the server's host address.
   *
   * @return the InetAddress object
   */
  public InetAddress getHost() {
    return host;
  }

  /**
   * Returns the Screen object used for the user interface.
   *
   * @return the Screen object
   */
  public Screen getScreen() {
    return screen;
  }

  /**
   * Returns the port number of the server.
   *
   * @return the port number
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the Vector object containing the history of messages received from the server.
   *
   * @return the Vector object
   */
  public Vector<Message> getHistoryMessage() {
    return historyMessage;
  }

  /**
   * Returns the Vector object containing the labels for displaying the message history in the user
   * interface.
   *
   * @return the Vector object
   */
  public Vector<Label> getHistoryLabel() {
    return historyLabel;
  }

  /**
   * Returns the BasicWindow object used for the user interface.
   *
   * @return the BasicWindow object
   */
  public BasicWindow getWindow() {
    return window;
  }

  /**
   * Returns the username of the client.
   *
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the username of the client. The username is obtained by showing a dialog to the user. The
   * username is then sent to the server.
   *
   * @throws IOException if an I/O error occurs when sending the username to the server
   */
  private void setUsername() throws IOException {
    username = new TextInputDialogBuilder().setTitle("Welcome to chatroom!")
        .setDescription("Please enter your username").build().showDialog(textGUI);
    // Send the username to the server
    getStreamToServer().writeUTF(getUsername());
  }

  /**
   * This method creates a status bar panel that displays the username and a clock. The username is
   * displayed on the left and the clock on the right. The clock is updated every second to display
   * the current time.
   *
   * @return the created status bar panel
   */
  private Panel createStatusBar() {
    // Create a new panel with a border layout
    Panel topPanel = new Panel().setLayoutManager(new BorderLayout());

    // Create a new label for the username and add it to the left of the panel
    Label usernameLabel = new Label(getUsername());
    topPanel.addComponent(usernameLabel.setLayoutData(BorderLayout.Location.LEFT));

    // Create a new label for the clock and add it to the right of the panel
    Label clockLabel = new Label("TopPanel.clockLabel");
    topPanel.addComponent(clockLabel.setLayoutData(BorderLayout.Location.RIGHT));

    // Schedule a task to update the clock label with the current time every second
    getExecutorService().scheduleAtFixedRate(
        () -> clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
        0, 1, java.util.concurrent.TimeUnit.SECONDS);

    // Return the created panel
    return topPanel;
  }

  /**
   * This method creates a panel for displaying messages with a border. It includes a message panel
   * for displaying the message history and an executor service for receiving messages from the
   * server. When a message is received, it is added to the message history and displayed in the
   * message panel. If the message is "/undo", the last message from the same user is marked as
   * unsent and its display is updated. The method also adds empty space around the message panel to
   * create a border.
   *
   * @return the created panel for displaying messages with a border
   */
  private Panel createMessagePanelWithBorder() {
    // Create a new panel with a border layout
    Panel messagePanelWithBorder = new Panel().setLayoutManager(new BorderLayout());

    // Create a new panel with a grid layout of 1 column for displaying messages
    Panel messagePanel = new Panel().setLayoutManager(new GridLayout(1));

    // Schedule a task to receive messages from the server at fixed rate
    getExecutorService().scheduleAtFixedRate(() -> {
      try {
        // While there are messages available from the server
        while (getStreamFromServer().available() > 0) {
          // Create a new message from the received message string
          Message message = new Message(getStreamFromServer().readUTF());

          // If the message is "/undo"
          if (message.getMessage().equals("/undo")) {
            // Iterate over the message history in reverse order
            for (int i = getHistoryMessage().size() - 1; i >= 0; i--) {
              // If the message is from the same user
              if (getHistoryMessage().get(i).getUsername().equals(message.getUsername())) {
                // Mark the message as unsent
                getHistoryMessage().get(i).unsent();
                // Update the display of the message
                getHistoryLabel().get(i).setText(getHistoryMessage().get(i).output());
                // Break the loop
                break;
              }
            }
          } else {
            // Add the message to the message history
            getHistoryMessage().add(message);
            // Add a new label for the message to the label history
            getHistoryLabel().add(new Label(message.output()));
            // Add the label to the message panel
            messagePanel.addComponent(getHistoryLabel().lastElement());
          }
        }
      } catch (IOException e) {
        System.err.println("Error IOException receiving");
      }
    }, 0, 500, TimeUnit.MILLISECONDS);

    // Add the message panel to the center of the border layout
    messagePanelWithBorder.addComponent(messagePanel.setLayoutData(BorderLayout.Location.CENTER));

    // Add empty space around the message panel to create a border
    final TextColor borderColor = TextColor.ANSI.CYAN;
    messagePanelWithBorder.addComponent(new EmptySpace(borderColor, new TerminalSize(0, 1)),
        BorderLayout.Location.TOP);
    messagePanelWithBorder.addComponent(new EmptySpace(borderColor, new TerminalSize(0, 1)),
        BorderLayout.Location.BOTTOM);
    messagePanelWithBorder.addComponent(new EmptySpace(borderColor, new TerminalSize(1, 0)),
        BorderLayout.Location.LEFT);
    messagePanelWithBorder.addComponent(new EmptySpace(borderColor, new TerminalSize(1, 0)),
        BorderLayout.Location.RIGHT);

    // Return the created panel
    return messagePanelWithBorder;
  }

  /**
   * This method creates a panel for sending messages. It includes a text box for inputting messages
   * and a send button for sending them. When the send button is clicked, the message in the text
   * box is sent to the server and the text box is cleared. If the message is "/exit", the client
   * will close all connections and stop the screen.
   *
   * @return the created panel for sending messages
   */
  private Panel createSendMessagePanel() {
    // Create a new panel with a grid layout of 2 columns
    Panel sendMessagePanel = new Panel(new GridLayout(2));

    // Create a new text box for inputting messages
    TextBox inputBox = new TextBox().setLayoutData(
        GridLayout.createLayoutData(GridLayout.Alignment.FILL,
// Horizontal alignment in the grid cell and fill empty space
            GridLayout.Alignment.CENTER, // Vertical alignment in the grid cell
            true, // Give the component extra horizontal space if available
            false, // Give the component extra vertical space if available
            1, // Horizontal span
            1)); // Vertical span

    // Add the text box to the panel
    sendMessagePanel.addComponent(inputBox);

    // Create a new send button
    Button sendButton = new Button("Send", () -> {
      // Get the message from the text box
      String message = inputBox.getText();
      // Clear the text box
      inputBox.setText("");

      try {
        // Send the message to the server
        getStreamToServer().writeUTF(message);
      } catch (IOException e) {
        System.err.println("Error IOException sending");
      }

      // If the message is "/exit", close all connections and stop the screen
      if (message.equalsIgnoreCase("/exit")) {
        try {
          getExecutorService().shutdown();
          getStreamFromServer().close();
          getStreamToServer().close();
          getSocket().close();
          getScreen().stopScreen();
        } catch (IOException e) {
          System.err.println("Error IOException closing");
        }
        System.exit(0);
      }
    });

    // Set the layout data for the send button
    sendButton.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END,
        // Horizontal alignment in the grid cell
        GridLayout.Alignment.CENTER, // Vertical alignment in the grid cell
        false, // Give the component no extra horizontal space
        false, // Give the component no extra vertical space
        1, // Horizontal span
        1)); // Vertical span

    // Add the send button to the panel
    sendMessagePanel.addComponent(sendButton);

    // Return the created panel
    return sendMessagePanel;
  }

  /**
   * This method is responsible for running the client-side application. It sets up the user
   * interface and starts the screen.
   *
   * @throws IOException if an I/O error occurs when starting the screen
   */
  public void run() throws IOException {
    // Start the terminal screen
    getScreen().startScreen();

    // Set the username for the client
    setUsername();

    // Create the main panel for the user interface
    Panel mainPanel = new Panel().setLayoutManager(new BorderLayout());

    // Create the top panel for displaying the username and clock
    Panel statusBar = createStatusBar();

    // Create the message panel for displaying the message history
    Panel messagePanelWithBorder = createMessagePanelWithBorder();

    // Create a panel for inputting and sending messages
    Panel sendMessagePanel = createSendMessagePanel();

    // Add the created panels to the main panel
    mainPanel.addComponent(statusBar.setLayoutData(BorderLayout.Location.TOP));
    mainPanel.addComponent(messagePanelWithBorder.setLayoutData(BorderLayout.Location.CENTER));
    mainPanel.addComponent(sendMessagePanel.setLayoutData(BorderLayout.Location.BOTTOM));

    // Set the main panel as the component of the window
    getWindow().setComponent(mainPanel);
    // Set the window to be full screen
    getWindow().setHints(List.of(Window.Hint.FULL_SCREEN));

    // Create a GUI for the user interface and add the window to it
    MultiWindowTextGUI gui = new MultiWindowTextGUI(getScreen(), new DefaultWindowManager(),
        new EmptySpace(TextColor.ANSI.BLUE));
    // Add the window to the GUI and wait for it to close
    gui.addWindowAndWait(getWindow());
    // Update the screen to reflect the changes
    gui.updateScreen();
  }
}