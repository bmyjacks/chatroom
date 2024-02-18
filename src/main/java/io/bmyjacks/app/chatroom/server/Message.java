package io.bmyjacks.app.chatroom.server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a single message in the chatroom.
 */
public class Message {

  private static final Map<String, Integer> userMessageCount = new HashMap<>();
  // The time the message was sent
  private final LocalTime time;
  // The username of the sender
  private final String username;
  // The content of the message
  private final String message;
  // Whether the message has been deleted
  private boolean deleted = false;

  /**
   * Constructs a new Message from a received message string. The received message string is
   * expected to be in the format "time#username#message".
   *
   * @param receivedMessage the received message string
   */
  public Message(String receivedMessage) {
    String[] message = receivedMessage.split("#");
    this.time = LocalTime.parse(message[0]);
    this.username = message[1];
    this.message = message[2];
    if (!getMessage().equals("/undo")) {
      userMessageCount.put(username, userMessageCount.getOrDefault(username, 0) + 1);
    }
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
    if (!getMessage().equals("/undo")) {
      userMessageCount.put(username, userMessageCount.getOrDefault(username, 0) + 1);
    }
  }

  /**
   * Returns the username of the sender.
   *
   * @return the username of the sender
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Returns the time the message was sent.
   *
   * @return the time the message was sent
   */
  public LocalTime getSentTime() {
    return this.time;
  }

  /**
   * Returns the content of the message.
   *
   * @return the content of the message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * Marks the message as unsent by changing its content.
   */
  public void unsent() {
    this.deleted = !this.deleted;
  }

  /**
   * Returns a string representation of the message. The format is "time#username#message".
   *
   * @return a string representation of the message
   */
  public String toString() {
    return getSentTime() + "#" + getUsername() + "#" + getMessage();
  }

  /**
   * Returns a formatted string representation of the message for output. If the message is deleted,
   * it will return a string indicating the message has been deleted. Otherwise, it will return the
   * message in the format "[time] username: message".
   *
   * @return a formatted string representation of the message for output
   */
  public String output() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    if (getUsername().equals("Server")) {
      return "[" + formatter.format(getSentTime()) + "] " + getUsername() + ": " + getMessage();
    }
    if (deleted) {
      return "[" + formatter.format(getSentTime()) + "] " + getUsername() + "["
          + Message.userMessageCount.get(getUsername()) + "]: " + "This message has been deleted.";
    } else {
      return "[" + formatter.format(getSentTime()) + "] " + getUsername() + "["
          + Message.userMessageCount.get(getUsername()) + "]: " + getMessage();
    }
  }

  public boolean isDeleted() {
    return deleted;
  }
}