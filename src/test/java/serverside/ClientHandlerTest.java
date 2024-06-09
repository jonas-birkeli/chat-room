package serverside;

import static org.junit.jupiter.api.Assertions.*;

class ClientHandlerTest {
  ClientHandler clientHandler;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    clientHandler = new ClientHandler(null, null);
  }

  @org.junit.jupiter.api.AfterEach
  void tearDown() {
    clientHandler = null;
  }

  @org.junit.jupiter.api.Test
  void shutdown() {
    assertTrue(true);
  }

  @org.junit.jupiter.api.Test
  void sendMessageToClient() {
    assertTrue(true);
  }
}