package io.bmyjacks.app.chatroom.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.mockito.Mockito.*;

class ClientHandlerTest {
    @Mock
    private Socket clientSocket;
    @Mock
    private DataInputStream streamFromClient;
    @Mock
    private DataOutputStream streamToClient;

    private ClientHandler clientHandler;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        clientHandler = new ClientHandler(clientSocket, streamFromClient, streamToClient);
    }

    @Test
    void should_send_1_message_to_all_clients_when_sendToAll_method_called() throws IOException {
        // Given
        Server.activeClient.add(clientHandler);

        // When
        clientHandler.sendToAll("Test message");

        // Then
        verify(streamToClient).writeUTF(anyString());
    }

    @Test
    void shouldSendHistoryWhenCallSendHistoryMethod() throws IOException {
        // Given
        Server.history.add("Test history message1");
        Server.history.add("Test history message2");

        // When
        clientHandler.sendHistory();

        // Then
        verify(streamToClient, times(Server.history.size())).writeUTF(anyString());
    }

    @Test
    void shouldNotSendHistoryWhenHistoryIsEmpty() throws IOException {
        // Given
        Server.history.clear();

        // When
        clientHandler.sendHistory();

        // Then
        verify(streamToClient, never()).writeUTF(anyString());
    }
}