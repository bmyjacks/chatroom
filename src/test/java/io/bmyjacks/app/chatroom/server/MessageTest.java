package io.bmyjacks.app.chatroom.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageTest {

  @BeforeEach
  void setUp() {
    Message.resetMessageCount();
  }

  @Test
  void messageConstructorShouldParseReceivedMessage() {
    Message message = new Message("12:00:00#user#Hello, world!#false");

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    assertEquals("12:00:00", formatter.format(message.getSentTime()));
    assertEquals("user", message.getUsername());
    assertEquals("Hello, world!", message.getMessage());
    assertFalse(message.isDeleted());
  }

  @Test
  void messageConstructorShouldSetUsernameAndMessage() {
    Message message = new Message("user", "Hello, world!");

    assertEquals("user", message.getUsername());
    assertEquals("Hello, world!", message.getMessage());
  }

  @Test
  void messageConstructorShouldSetCurrentTime() {
    Message message = new Message("user", "Hello, world!");

    assertNotNull(message.getSentTime());
  }

  @Test
  void unsentShouldToggleDeletedStatus() {
    Message message = new Message("user", "Hello, world!");

    assertFalse(message.isDeleted());
    message.unsent();
    assertTrue(message.isDeleted());
    message.unsent();
    assertFalse(message.isDeleted());
  }

  @Test
  void outputShouldReturnFormattedMessage() {
    Message message = new Message("12:00:00#user#Hello, world!#false");

    assertEquals("[12:00:00] user[1]: Hello, world!", message.output());
  }

  @Test
  void outputShouldIndicateDeletedMessage() {
    Message message = new Message("12:00:00#user#Hello, world!#false");
    message.unsent();

    assertEquals("[12:00:00] user[1]: This message has been deleted.", message.output());
  }

  @Test
  void outputShouldIndicateRecoveredMessage() {
    Message message = new Message("12:00:00#user#Hello, world!#false");
    message.unsent();
    message.unsent();

    assertEquals("[12:00:00] user[1]: Hello, world!", message.output());
  }

  @Test
  void toStringShouldReturnFormattedString() {
    Message message = new Message("12:00:00#user#Hello, world!#false");

    assertEquals("12:00#user#Hello, world!#false", message.toString());
  }

  @Test
  void messageCountShouldIncrement() {
    Message message = new Message("12:00:00#user#Hello, world!#false");
    message = new Message("12:00:00#user#Hello, world!#false");

    assertEquals("[12:00:00] user[2]: Hello, world!", message.output());
  }
}