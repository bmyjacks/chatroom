package io.bmyjacks.app.chatroom.client;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;


public class Client {
    private final int port;
    private final InetAddress host;

    public Client(int port) throws UnknownHostException {
        this.host = InetAddress.getLocalHost();
        this.port = port;
    }

    public void run() throws IOException {
        System.out.println("Welcome to the chatroom application!");
        Socket socket = new Socket(host, port);

        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        Terminal terminal = null;
        Screen screen = null;

        System.out.println("Starting screen");

        try {
            terminal = new DefaultTerminalFactory().setForceTextTerminal(false).createTerminal();
            screen = new TerminalScreen(terminal);
            screen.startScreen();
            System.out.println("Screen started");
        } catch (ExceptionInInitializerError e) {
            System.out.println("EIIE");
            e.printStackTrace();
        }


        final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

        String username = new TextInputDialogBuilder().setTitle("Welcome to chatroom!").setDescription("Please enter your username").build().showDialog(textGUI);

        System.out.println(username);
        dataOutputStream.writeUTF(username);

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

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

        Screen finalScreen = screen;
        Button sendBotton = new Button("Send", () -> {
            String message = inputBox.getText();
            inputBox.setText("");
            try {
                dataOutputStream.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Error IOException");
            }
            if (message.equalsIgnoreCase("exit")) {
                try {
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


        mainPanel.addComponent(new Label(username).setLayoutData(BorderLayout.Location.TOP));
        mainPanel.addComponent(messagePanelWithBorder.setLayoutData(BorderLayout.Location.CENTER));
        mainPanel.addComponent(sendMessagePanel.setLayoutData(BorderLayout.Location.BOTTOM));


        Thread readMessage = new Thread(() -> {
            while (true) {
                try {
                    String message = dataInputStream.readUTF();
                    messagePanel.addComponent(new Label(message));
                } catch (IOException e) {
                    break;
                }
            }
        });

        readMessage.start();

        BasicWindow window = new BasicWindow();
        window.setComponent(mainPanel);
        window.setHints(List.of(Window.Hint.FULL_SCREEN));
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(window);
        gui.updateScreen();
    }
}