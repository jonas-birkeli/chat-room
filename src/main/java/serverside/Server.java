package serverside;

import static config.ConnectionConfig.PORT;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The server class is responsible for handling the server side of the chatroom.
 * It will listen for incoming connections and create a new thread for each connection.
 *
 * @version 1.2
 * @author Jonas Birkeli
 * @since 08.06.2024
 */
public class Server implements Runnable {
  private ServerSocket serverSocket;
  private final List<ClientHandler> clients;
  private boolean running;
  private ExecutorService pool;

  /**
   * Constructor for the server class.
   *
   * @since 1.0
   */
  public Server() {
    clients = new ArrayList<>();
    running = true;
  }

  /**
   * The run method is called when the thread is started.
   *
   * @since 1.0
   */
  @Override
  public void run() {
    try {
      Logger.getLogger(this.getClass().getName()).info("Server starting...");

      serverSocket = new ServerSocket(PORT);
      pool = Executors.newCachedThreadPool();

      Logger.getLogger(this.getClass().getName()).info("Server started on port " + PORT + "!");


      while (running) {
        Socket client = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(client, this);
        clients.add(clientHandler);
        pool.execute(clientHandler);
      }
    } catch (IOException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to accept client connection");
      shutdown();
    }
  }

  /**
   * Checks if the username is already taken.
   *
   * @param username The username to validate
   * @return True if the username is invalid, false otherwise
   * @since 1.0
   */
  public boolean isUsernameTaken(String username) {
    return username == null
        || username.isEmpty()
        || clients.stream().anyMatch(clientHandler -> clientHandler.getUsername().equals(username));
  }

  /**
   * Broadcasts a message to all connected clients.
   *
   * @param message The message to broadcast
   * @since 1.0
   */
  public void broadcastToAll(String message) {
    clients.stream()
        .filter(Objects::nonNull)
        .forEach(clientHandler -> clientHandler.sendEncryptedMessage(message));
  }

  /**
   * Shuts down the server and all connected clients.
   *
   * @since 1.0
   */
  public void shutdown() {
    try {
      running = false;

      if (serverSocket != null && !serverSocket.isClosed()) {
        serverSocket.close();
      }
      clients.stream()
          .filter(Objects::nonNull)
          .forEach(ClientHandler::shutdown);

      pool.shutdown();

    } catch (IOException e) {
      // Ignore
    }
  }

  /**
   * Returns a stream of all connected clients.
   *
   * @return A stream of all connected clients
   * @since 1.0
   */
  public Stream<ClientHandler> getClients() {
    return clients.parallelStream();
  }

  /**
   * Removes a client from the server.
   *
   * @param clientHandler The client to remove
   * @since 1.2
   */
  public void removeClient(ClientHandler clientHandler) {
    clients.remove(clientHandler);
  }
}
