package io.bmyjacks.app.chatroom.server;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a single message in the chatroom. It contains information about the sender,
 * the content of the message, and the time it was sent. It also keeps track of the number of
 * messages each user has sent.
 */
public class Message {

  // A map to keep track of the number of messages each user has sent
  private static final Map<String, Integer> userMessageCount = new ConcurrentHashMap<>();
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
   * expected to be in the format "time#username#message#deleted".
   *
   * @param receivedMessage the received message string
   */
  public Message(String receivedMessage) {
    String[] message = receivedMessage.split("#");
    this.time = LocalTime.parse(message[0]);
    this.username = message[1];
    this.message = message[2];
    this.deleted = Boolean.parseBoolean(message[3]);
    incrementMessageCount();
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
    incrementMessageCount();
  }

  private void incrementMessageCount() {
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
   * Checks if the message has been deleted.
   *
   * @return true if the message has been deleted, false otherwise
   */
  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  /**
   * Marks the message as unsent.
   */
  public void unsent() {
    setDeleted(!isDeleted());
  }

  /**
   * Returns a string representation of the message. The format is "time#username#message#deleted".
   *
   * @return a string representation of the message
   */
  public String toString() {
    return getSentTime() + "#" + getUsername() + "#" + getMessage() + "#" + isDeleted();
  }

  private String getFormattedTimeAndUsername() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    return "[" + formatter.format(getSentTime()) + "] " + getUsername();
  }

  /**
   * Returns a formatted string representation of the message for output. If the message is deleted,
   * it will return a string indicating the message has been deleted. Otherwise, it will return the
   * message in the format "[time] username: message".
   *
   * @return a formatted string representation of the message for output
   */
  public String output() {
    if (getUsername().equals("Server")) {
      return getFormattedTimeAndUsername() + ": " + getMessage();
    }
    if (deleted) {
      return getFormattedTimeAndUsername() + "[" + Message.userMessageCount.get(getUsername())
          + "]: " + "This message has been deleted.";
    } else {
      return getFormattedTimeAndUsername() + "[" + Message.userMessageCount.get(getUsername())
          + "]: " + getMessage();
    }
  }
}