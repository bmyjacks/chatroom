package io.bmyjacks.app.chatroom.server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * This class represents a single message in the chatroom.
 */
public class Message {
    private final LocalTime time; // The time the message was sent
    private final String username; // The username of the sender
    private final String message; // The content of the message
    private boolean deleted; // Whether the message has been deleted

    public Message(String receivedMessage) {
        String[] message = receivedMessage.split("#");
        this.time = LocalTime.parse(message[0]);
        this.username = message[1];
        this.message = message[2];
    }

    /**
     * Constructs a new Message with a default type of NORMAL.
     *
     * @param username the username of the sender
     * @param message  the content of the message
     */
    public Message(String username, String message) {
        this.username = username;
        this.message = message;
        this.time = LocalTime.now();
        deleted = false;
    }

    /**
     * Returns the username of the sender.
     *
     * @return the username of the sender
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the time the message was sent.
     *
     * @return the time the message was sent
     */
    public LocalTime getSentTime() {
        return time;
    }

    /**
     * Returns the content of the message.
     *
     * @return the content of the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Marks the message as unsent by changing its content.
     */
    public void unsent() {
        this.deleted = !this.deleted;
    }

    /**
     * Returns a string representation of the message.
     *
     * @return a string representation of the message
     */
    public String toString() {
        return time + "#" + username + "#" + message;
    }


    public String output() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        if (deleted) {
            return "[" + formatter.format(time) + "] " + username + ": " + "This message has been deleted.";
        } else {
            return "[" + formatter.format(time) + "] " + username + ": " + message;
        }
    }
}