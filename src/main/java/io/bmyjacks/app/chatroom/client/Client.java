package io.bmyjacks.app.chatroom.client;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
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
 * This class represents the client in the chatroom application.
 * It handles communication with the server and provides a user interface for sending and receiving messages.
 */
public class Client {
    // The host address of the server
    private final InetAddress host;
    // The port number of the server
    private final int port;
    // A history of messages received from the server
    private final Vector<Message> historyMessage = new Vector<>();
    // A list of labels for displaying the message history in the user interface
    private final Vector<Label> historyLabel = new Vector<>();
    // gui
    private final Terminal terminal = new DefaultTerminalFactory().setForceTextTerminal(false).createTerminal();
    private final Screen screen = new TerminalScreen(terminal);
    private final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);
    private Socket socket;
    private DataInputStream streamFromServer;
    private DataOutputStream streamToServer;
    private String username;

    /**
     * Constructs a new Client with the specified port.
     * The client will connect to the server at the local host address.
     *
     * @param port the port number of the server
     */
    public Client(InetAddress host, int port) throws IOException {
        this.host = host;
        this.port = port;
    }

    public InetAddress getHost() {
        return host;
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

    public Socket getSocket() {
        return socket;
    }

    ;

    public DataInputStream getStreamFromServer() {
        return streamFromServer;
    }

    public DataOutputStream getStreamToServer() {
        return streamToServer;
    }

    public String getUsername() {
        return username;
    }

    public void init() throws IOException {
        socket = new Socket(getHost(), getPort());
        streamFromServer = new DataInputStream(getSocket().getInputStream());
        streamToServer = new DataOutputStream(getSocket().getOutputStream());
    }

    public void initTerminal() throws IOException {
        screen.startScreen();
    }

    private void setUserNameViaGui() throws IOException {
        // Get the username from the user
        username = new TextInputDialogBuilder().setTitle("Welcome to chatroom!").setDescription("Please enter your username").build().showDialog(textGUI);
        // Send the username to the server
        getStreamToServer().writeUTF(getUsername());
    }

    /**
     * Starts the client.
     * It connects to the server, starts the user interface, and begins receiving and sending messages.
     *
     * @throws IOException if there is an error connecting to the server or communicating with it
     */
    public void run() throws IOException {
        initTerminal();
        setUserNameViaGui();


        // Create the main panel for the user interface
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        // Create the top panel for displaying the username and clock
        Panel topPanel = new Panel();
        topPanel.setLayoutManager(new BorderLayout());
        Label usernameLabel = new Label(username);
        Label clockLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        topPanel.addComponent(usernameLabel.setLayoutData(BorderLayout.Location.LEFT));
        topPanel.addComponent(clockLabel.setLayoutData(BorderLayout.Location.RIGHT));

        // Create the message panel for displaying the message history
        Panel messagePanelWithBorder = new Panel();
        messagePanelWithBorder.setLayoutManager(new BorderLayout());

        Panel messagePanel = new Panel();
        messagePanel.setLayoutManager(new GridLayout(1));

        messagePanelWithBorder.addComponent(messagePanel.setLayoutData(BorderLayout.Location.CENTER));
        messagePanelWithBorder.addComponent(new EmptySpace(TextColor.ANSI.CYAN, new TerminalSize(0, 1)), BorderLayout.Location.TOP);
        messagePanelWithBorder.addComponent(new EmptySpace(TextColor.ANSI.CYAN, new TerminalSize(0, 1)), BorderLayout.Location.BOTTOM);
        messagePanelWithBorder.addComponent(new EmptySpace(TextColor.ANSI.CYAN, new TerminalSize(1, 0)), BorderLayout.Location.LEFT);
        messagePanelWithBorder.addComponent(new EmptySpace(TextColor.ANSI.CYAN, new TerminalSize(1, 0)), BorderLayout.Location.RIGHT);

        // Create a panel for inputting and sending messages
        Panel sendMessagePanel = new Panel(new GridLayout(2));
        TextBox inputBox = new TextBox();
        inputBox.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, // Horizontal alignment in the grid cell and fill empty space
                GridLayout.Alignment.CENTER, // Vertical alignment in the grid cell
                true, // Give the component extra horizontal space if available
                false, // Give the component extra vertical space if available
                1, // Horizontal span
                1)); // Vertical span
        sendMessagePanel.addComponent(inputBox);

        // Create a scheduled executor service for updating the clock label every second
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))), 0, 1, java.util.concurrent.TimeUnit.SECONDS);

        // Create a "send button" for sending messages
        Screen finalScreen = screen;
        Button sendBotton = new Button("Send", () -> {
            String message = inputBox.getText();
            inputBox.setText("");
            try {
                streamToServer.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Error IOException");
            }
            if (message.equalsIgnoreCase("/exit")) {
                try {
                    executorService.shutdown();
                    streamFromServer.close();
                    streamToServer.close();
                    socket.close();
                    finalScreen.stopScreen();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.exit(0);
            }
        });
        sendBotton.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, // Horizontal alignment in the grid cell
                GridLayout.Alignment.CENTER, // Vertical alignment in the grid cell
                false, // Give the component no extra horizontal space
                false, // Give the component no extra vertical space
                1, // Horizontal span
                1)); // Vertical span
        sendMessagePanel.addComponent(sendBotton);

        // Add the top panel, message panel, and send message panel to the main panel
        mainPanel.addComponent(topPanel.setLayoutData(BorderLayout.Location.TOP));
        mainPanel.addComponent(messagePanelWithBorder.setLayoutData(BorderLayout.Location.CENTER));
        mainPanel.addComponent(sendMessagePanel.setLayoutData(BorderLayout.Location.BOTTOM));

        // Create a scheduled executor service for receiving messages from the server every 500 milliseconds
        executorService.scheduleAtFixedRate(() -> {
            try {
                while (streamFromServer.available() > 0) {
                    Message message = new Message(streamFromServer.readUTF());

                    if (message.getMessage().equals("/undo")) {
                        for (int i = historyMessage.size() - 1; i >= 0; i--) {
                            if (historyMessage.get(i).getUsername().equals(message.getUsername())) {
                                historyMessage.get(i).unsent();
                                historyLabel.get(i).setText(historyMessage.get(i).output());
                                break;
                            }
                        }
                    } else {
                        historyMessage.add(message);
                        historyLabel.add(new Label(message.output()));
                        messagePanel.addComponent(historyLabel.lastElement());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);

        // Create a window for the user interface and add the main panel to it
        BasicWindow window = new BasicWindow();
        window.setComponent(mainPanel);
        window.setHints(List.of(Window.Hint.FULL_SCREEN));

        // Create a GUI for the user interface and add the window to it
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(window);
        gui.updateScreen();
    }
}