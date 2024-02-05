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
import java.net.UnknownHostException;
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

    /**
     * Constructs a new Client with the specified port.
     * The client will connect to the server at the local host address.
     *
     * @param port the port number of the server
     * @throws UnknownHostException if the local host name could not be resolved into an address
     */
    public Client(int port) throws UnknownHostException {
        this.host = InetAddress.getLocalHost();
        this.port = port;
        historyMessage.clear();
        historyLabel.clear();
    }

    /**
     * Starts the client.
     * It connects to the server, starts the user interface, and begins receiving and sending messages.
     *
     * @throws IOException if there is an error connecting to the server or communicating with it
     */
    public void run() throws IOException {
        // Connect to the server
        Socket socket = new Socket(host, port);

        // Create input and output streams for communication with the server
        DataInputStream streamFromServer = new DataInputStream(socket.getInputStream());
        DataOutputStream streamToServer = new DataOutputStream(socket.getOutputStream());

        // Create the terminal and screen for the user interface
        Terminal terminal;
        Screen screen = null;

        System.out.println("Starting screen");

        try {
            terminal = new DefaultTerminalFactory().setForceTextTerminal(false).createTerminal();
            screen = new TerminalScreen(terminal);
            screen.startScreen();
        } catch (ExceptionInInitializerError e) {
            System.out.println("EIIE");
        }

        // Create the text GUI for the user interface
        final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

        // Get the username from the user
        String username = new TextInputDialogBuilder().setTitle("Welcome to chatroom!").setDescription("Please enter your username").build().showDialog(textGUI);
        // Send the username to the server
        streamToServer.writeUTF(username);

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

        // Create the send message panel for inputting and sending messages
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

        // Create a send button for sending messages
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