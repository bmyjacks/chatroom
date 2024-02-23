package io.bmyjacks.app.chatroom.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class ServerTest {

  @Test
  void constructorShouldSetPortNumber() throws IOException {
    int port = 1234;

    Server server = new Server(port);

    assertEquals(port, server.getPort());
  }

  @Test
  void constructorShouldClearActiveClientsAndHistory() throws IOException {
    Server.getActiveClient().add(new ClientHandler(null, null, null));
    Server.getHistory().add(new Message("username", "message"));

    assertFalse(Server.getActiveClient().isEmpty());
    assertFalse(Server.getHistory().isEmpty());

    new Server(4372);

    assertTrue(Server.getActiveClient().isEmpty());
    assertTrue(Server.getHistory().isEmpty());
  }
}
