package io.bmyjacks.app.chatroom.server;

import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {
    @Test
    void messageConstructorShouldParseReceivedMessage() {
        Message message = new Message("12:00:00#user#Hello, world!");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        assertEquals("12:00:00", formatter.format(message.getSentTime()));
        assertEquals("user", message.getUsername());
        assertEquals("Hello, world!", message.getMessage());
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
        Message message = new Message("12:00:00#user#Hello, world!");

        assertEquals("[12:00:00] user: Hello, world!", message.output());
    }

    @Test
    void outputShouldIndicateDeletedMessage() {
        Message message = new Message("12:00:00#user#Hello, world!");
        message.unsent();

        assertEquals("[12:00:00] user: This message has been deleted.", message.output());
    }

    @Test
    void outputShouldIndicateRecoveredMessage() {
        Message message = new Message("12:00:00#user#Hello, world!");
        message.unsent();
        message.unsent();

        assertEquals("[12:00:00] user: Hello, world!", message.output());
    }

    @Test
    void toStringShouldReturnFormattedString() {
        Message message = new Message("12:00:00#user#Hello, world!");

        assertEquals("12:00#user#Hello, world!", message.toString());
    }
}