package io.bmyjacks.app.chatroom.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void messageConstructorShouldParseReceivedMessage() {
        Message message = new Message("12:00:00#user#Hello, world!");

        assertEquals("12:00", message.getSentTime().toString().substring(0, 5));
        assertEquals("user", message.getUsername());
        assertEquals("Hello, world!", message.getMessage());
    }

    @Test
    void messageConstructorShouldSetCurrentTime() {
        Message message = new Message("user", "Hello, world!");

        assertNotNull(message.getSentTime());
    }

    @Test
    void messageConstructorShouldSetUsernameAndMessage() {
        Message message = new Message("user", "Hello, world!");

        assertEquals("user", message.getUsername());
        assertEquals("Hello, world!", message.getMessage());
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

        assertTrue(message.output().contains("This message has been deleted."));
    }
}