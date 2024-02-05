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


public class Client {
    private final InetAddress host;
    private final int port;
    private final Vector<Message> historyMessage = new Vector<>();
    private final Vector<Label> historyLabel = new Vector<>();

    public Client(int port) throws UnknownHostException {
        this.host = InetAddress.getLocalHost();
        this.port = port;
        historyMessage.clear();
        historyLabel.clear();
    }

    public void run() throws IOException {
        Socket socket = new Socket(host, port);

        DataInputStream streamFromServer = new DataInputStream(socket.getInputStream());
        DataOutputStream streamToServer = new DataOutputStream(socket.getOutputStream());
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


        final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

        String username = new TextInputDialogBuilder().setTitle("Welcome to chatroom!").setDescription("Please enter your username").build().showDialog(textGUI);
        streamToServer.writeUTF(username);

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        Panel topPanel = new Panel();
        topPanel.setLayoutManager(new BorderLayout());
        Label usernameLabel = new Label(username);
        Label clockLabel = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        topPanel.addComponent(usernameLabel.setLayoutData(BorderLayout.Location.LEFT));
        topPanel.addComponent(clockLabel.setLayoutData(BorderLayout.Location.RIGHT));


        Panel messagePanelWithBorder = new Panel();
        messagePanelWithBorder.setLayoutManager(new BorderLayout());

        Panel messagePanel = new Panel();
        messagePanel.setLayoutManager(new GridLayout(1));

        messagePanelWithBorder.addComponent(messagePanel.setLayoutData(BorderLayout.Location.CENTER));
        messagePanelWithBorder.addComponent(new EmptySpace(TextColor.ANSI.CYAN, new TerminalSize(0, 1)), BorderLayout.Location.TOP);
        messagePanelWithBorder.addComponent(new EmptySpace(TextColor.ANSI.CYAN, new TerminalSize(0, 1)), BorderLayout.Location.BOTTOM);
        messagePanelWithBorder.addComponent(new EmptySpace(TextColor.ANSI.CYAN, new TerminalSize(1, 0)), BorderLayout.Location.LEFT);
        messagePanelWithBorder.addComponent(new EmptySpace(TextColor.ANSI.CYAN, new TerminalSize(1, 0)), BorderLayout.Location.RIGHT);

        Panel sendMessagePanel = new Panel(new GridLayout(2));
        TextBox inputBox = new TextBox();
        inputBox.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.FILL, // Horizontal alignment in the grid cell and fill empty space
                GridLayout.Alignment.CENTER, // Vertical alignment in the grid cell
                true, // Give the component extra horizontal space if available
                false, // Give the component extra vertical space if available
                1, // Horizontal span
                1)); // Vertical span
        sendMessagePanel.addComponent(inputBox);


        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> clockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))), 0, 1, java.util.concurrent.TimeUnit.SECONDS);

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


        mainPanel.addComponent(topPanel.setLayoutData(BorderLayout.Location.TOP));
        mainPanel.addComponent(messagePanelWithBorder.setLayoutData(BorderLayout.Location.CENTER));
        mainPanel.addComponent(sendMessagePanel.setLayoutData(BorderLayout.Location.BOTTOM));

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

        BasicWindow window = new BasicWindow();
        window.setComponent(mainPanel);
        window.setHints(List.of(Window.Hint.FULL_SCREEN));
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(window);
        gui.updateScreen();
    }
}