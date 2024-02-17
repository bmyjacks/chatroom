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

public class Client {

  // The host address of the server
  private final InetAddress host;
  // The port number of the server
  private final int port;
  private final Socket socket;
  private final DataInputStream streamFromServer;
  private final DataOutputStream streamToServer;
  // A history of messages received from the server
  private final Vector<Message> historyMessage = new Vector<>();
  // A list of labels for displaying the message history in the user interface
  private final Vector<Label> historyLabel = new Vector<>();
  private final Terminal terminal = new DefaultTerminalFactory().setForceTextTerminal(false)
      .createTerminal();
  private final Screen screen = new TerminalScreen(terminal);
  private final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
  private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  private final BasicWindow window = new BasicWindow();
  private String username;

  public Client(InetAddress host, int port) throws IOException {
    this.host = host;
    this.port = port;
    this.socket = new Socket(getHost(), getPort());
    streamFromServer = new DataInputStream(socket.getInputStream());
    streamToServer = new DataOutputStream(socket.getOutputStream());
  }

  public ScheduledExecutorService getExecutorService() {
    return executorService;
  }

  public DataInputStream getStreamFromServer() {
    return streamFromServer;
  }

  public DataOutputStream getStreamToServer() {
    return streamToServer;
  }

  public Socket getSocket() {
    return socket;
  }

  public InetAddress getHost() {
    return host;
  }

  public Screen getScreen() {
    return screen;
  }

  public int getPort() {
    return port;
  }

  public Vector<Message> getHistoryMessage() {
    return historyMessage;
  }

  public Vector<Label> getHistoryLabel() {
    return historyLabel;
  }


  public BasicWindow getWindow() {
    return window;
  }

  public String getUsername() {
    return username;
  }

  private void setUsername() throws IOException {
    username = new TextInputDialogBuilder().setTitle("Welcome to chatroom!")
        .setDescription("Please enter your username").build().showDialog(textGUI);
    // Send the username to the server
    getStreamToServer().writeUTF(getUsername());
  }

  private Panel createStatusBar() {
    Panel topPanel = new Panel().setLayoutManager(new BorderLayout());
    Label usernameLabel = new Label(getUsername());
    Label clockLabel = new Label("TopPanel.clockLabel");
    getExecutorService().scheduleAtFixedRate(
        () -> clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))),
        0, 1, java.util.concurrent.TimeUnit.SECONDS);

    topPanel.addComponent(usernameLabel.setLayoutData(BorderLayout.Location.LEFT));
    topPanel.addComponent(clockLabel.setLayoutData(BorderLayout.Location.RIGHT));

    return topPanel;
  }

  private Panel createMessagePanelWithBorder() {
    Panel messagePanelWithBorder = new Panel().setLayoutManager(new BorderLayout());

    Panel messagePanel = new Panel().setLayoutManager(new GridLayout(1));
    executorService.scheduleAtFixedRate(() -> {
      try {
        while (getStreamFromServer().available() > 0) {
          Message message = new Message(getStreamFromServer().readUTF());

          if (message.getMessage().equals("/undo")) {
            for (int i = getHistoryMessage().size() - 1; i >= 0; i--) {
              if (getHistoryMessage().get(i).getUsername().equals(message.getUsername())) {
                getHistoryMessage().get(i).unsent();
                getHistoryLabel().get(i).setText(getHistoryMessage().get(i).output());
                break;
              }
            }
          } else {
            getHistoryMessage().add(message);
            getHistoryLabel().add(new Label(message.output()));
            messagePanel.addComponent(getHistoryLabel().lastElement());
          }
        }
      } catch (IOException e) {
        System.out.println("Error IOException receiving");
      }
    }, 0, 500, TimeUnit.MILLISECONDS);

    messagePanelWithBorder.addComponent(messagePanel.setLayoutData(BorderLayout.Location.CENTER));

    // Add empty space around the message panel
    final TextColor borderColor = TextColor.ANSI.CYAN;
    messagePanelWithBorder.addComponent(new EmptySpace(borderColor, new TerminalSize(0, 1)),
        BorderLayout.Location.TOP);
    messagePanelWithBorder.addComponent(new EmptySpace(borderColor, new TerminalSize(0, 1)),
        BorderLayout.Location.BOTTOM);
    messagePanelWithBorder.addComponent(new EmptySpace(borderColor, new TerminalSize(1, 0)),
        BorderLayout.Location.LEFT);
    messagePanelWithBorder.addComponent(new EmptySpace(borderColor, new TerminalSize(1, 0)),
        BorderLayout.Location.RIGHT);

    return messagePanelWithBorder;
  }

  private Panel createSendMessagePanel() {
    Panel sendMessagePanel = new Panel(new GridLayout(2));
    TextBox inputBox = new TextBox().setLayoutData(GridLayout.createLayoutData(
        GridLayout.Alignment.FILL, // Horizontal alignment in the grid cell and fill empty space
        GridLayout.Alignment.CENTER, // Vertical alignment in the grid cell
        true, // Give the component extra horizontal space if available
        false, // Give the component extra vertical space if available
        1, // Horizontal span
        1)); // Vertical span
    sendMessagePanel.addComponent(inputBox);
    Button sendButton = new Button("Send", () -> {
      String message = inputBox.getText();
      inputBox.setText("");
      try {
        streamToServer.writeUTF(message);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      if (message.equalsIgnoreCase("/exit")) {
        try {
          getExecutorService().shutdown();
          getStreamFromServer().close();
          getStreamToServer().close();
          getSocket().close();
          getScreen().stopScreen();
        } catch (IOException e) {
          System.out.println("Error IOException closing");
        }
        System.exit(0);
      }
    });
    sendButton.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END,
        // Horizontal alignment in the grid cell
        GridLayout.Alignment.CENTER, // Vertical alignment in the grid cell
        false, // Give the component no extra horizontal space
        false, // Give the component no extra vertical space
        1, // Horizontal span
        1)); // Vertical span
    sendMessagePanel.addComponent(sendButton);
    return sendMessagePanel;
  }

  public void run() throws IOException {
    getScreen().startScreen();
    setUsername();

    // Create the main panel for the user interface
    Panel mainPanel = new Panel().setLayoutManager(new BorderLayout());

    // Create the top panel for displaying the username and clock
    Panel statusBar = createStatusBar();

    // Create the message panel for displaying the message history
    Panel messagePanelWithBorder = createMessagePanelWithBorder();

    // Create a panel for inputting and sending messages
    Panel sendMessagePanel = createSendMessagePanel();

    mainPanel.addComponent(statusBar.setLayoutData(BorderLayout.Location.TOP));
    mainPanel.addComponent(messagePanelWithBorder.setLayoutData(BorderLayout.Location.CENTER));
    mainPanel.addComponent(sendMessagePanel.setLayoutData(BorderLayout.Location.BOTTOM));

    getWindow().setComponent(mainPanel);
    getWindow().setHints(List.of(Window.Hint.FULL_SCREEN));

    // Create a GUI for the user interface and add the window to it
    MultiWindowTextGUI gui = new MultiWindowTextGUI(getScreen(), new DefaultWindowManager(),
        new EmptySpace(TextColor.ANSI.BLUE));
    gui.addWindowAndWait(getWindow());
    gui.updateScreen();
  }
}