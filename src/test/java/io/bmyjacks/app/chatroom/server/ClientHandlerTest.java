package io.bmyjacks.app.chatroom.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientHandler = new ClientHandler(clientSocket, streamFromClient, streamToClient);
        Server.activeClient.clear();
        Server.history.clear();
    }

    @Test
    void set_username_with_normal_string() {
        // Given
        String username = "Test username";

        // When
        clientHandler.setUsername(username);

        // Then
        assertEquals(username, clientHandler.getUsername());
    }

    @Test
    void set_username_with_empty_string() {
        // Given
        String username = "";

        // When
        clientHandler.setUsername(username);

        // Then
        assertEquals(username, clientHandler.getUsername());
    }

    @Test
    void send_history_when_history_is_not_empty() throws IOException {
        // Given
//        Server.history.add("Test history message1");
//        Server.history.add("Test history message2");

        // When
        clientHandler.sendHistory();

        // Then
//        verify(streamToClient, times(Server.history.size())).writeUTF(anyString());
    }

    @Test
    void not_send_history_when_history_is_empty() throws IOException {
        // Given
        Server.history.clear();

        // When
        clientHandler.sendHistory();

        // Then
        verify(streamToClient, never()).writeUTF(anyString());
    }


    @Test
    void sendToAll() throws IOException {
        // Given
        ClientHandler clientHandler1 = mock(ClientHandler.class);
        ClientHandler clientHandler2 = mock(ClientHandler.class);
        ClientHandler clientHandler3 = mock(ClientHandler.class);

        DataOutputStream streamToClient1 = mock(DataOutputStream.class);
        DataOutputStream streamToClient2 = mock(DataOutputStream.class);
        DataOutputStream streamToClient3 = mock(DataOutputStream.class);

        when(clientHandler1.getStreamToClient()).thenReturn(streamToClient1);
        when(clientHandler2.getStreamToClient()).thenReturn(streamToClient2);
        when(clientHandler3.getStreamToClient()).thenReturn(streamToClient3);

        Server.activeClient.add(clientHandler1);
        Server.activeClient.add(clientHandler2);
        Server.activeClient.add(clientHandler3);

        // When
        clientHandler.sendToAll("Test message");

        // Then
        verify(streamToClient1, times(1)).writeUTF(anyString());
        verify(streamToClient2, times(1)).writeUTF(anyString());
        verify(streamToClient3, times(1)).writeUTF(anyString());
    }
}