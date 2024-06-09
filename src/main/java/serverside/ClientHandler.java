package serverside;

import static config.ClientConfig.USERNAME_NOT_SET;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * The handler class is responsible for handling the client connection.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 08.06.2024
 */
public class ClientHandler implements Runnable {
  private final Socket client;
  private final Server server;

  private PrintWriter out;
  private BufferedReader in;

  private String username;

  /**
   * Constructor for the handler class.
   *
   * @param client The client socket
   * @param server The server instance
   * @since 1.0
   */
  public ClientHandler(Socket client, Server server) {
    this.client = client;
    this.server = server;

    username = USERNAME_NOT_SET;
  }

  /**
   * The run method is called when the thread is started.
   *
   * @since 1.0
   */
  @Override
  public void run() {
    String input;
    try {
      out = new PrintWriter(client.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));

      requestUsername();
      server.broadcastToAll(username + " has joined the chat.");

      // MAIN LOOP - Read input from the client and broadcast it to all clients
      while ((input = in.readLine()) != null) {

        if (!handleIfCommand(input)) {
          server.broadcastToAll(username + ": " + input);
        }
      }
    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName())
          .severe("Failed to handle client connection. " + e.getMessage());
      shutdown();
    }
  }

  /**
   * Validates whether the username is valid.
   * Will continue to prompt the user for a username until a valid one is entered.
   *
   * @since 1.0
   */
  private void requestUsername() {
    String input = "";

    boolean invalidUsername = true;
    while (invalidUsername) {
      sendMessageToClient("Enter your username:");

      try {
        input = in.readLine();
      } catch (IOException e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to read username");
      }
      invalidUsername = isValidUsername(input);
    }

    if (username.equals(USERNAME_NOT_SET)) {
      sendMessageToClient("Welcome " + input + "!");
    } else {
      sendMessageToClient("Successfully changed username to " + input + "!");
    }
    username = input;
  }

  /**
   * Validates whether the username is valid.
   * Will send a message to the client if the username is invalid and return false.
   *
   * @param username The username to validate
   * @return whether the username is valid, true if valid, false otherwise
   * @since 1.0
   */
  private boolean isValidUsername(String username) {
    if (username == null) {
      sendMessageToClient("Invalid username.");
    } else if (username.isEmpty() || username.isBlank()) {
      sendMessageToClient("Username cannot be blank.");
    } else if (username.equals(this.username)) {
      sendMessageToClient("Using the same username.");
      return true;
    } else if (server.isUsernameTaken(username)) {
      sendMessageToClient("Username already taken.");
    }

    return username != null && !username.isEmpty() && !server.isUsernameTaken(username);
  }

  /**
   * Returns the username of the client.
   *
   * @return The username of the client
   * @since 1.0
   */
  public String getUsername() {
    return username;
  }

  /**
   * If it is a legal command, it will be handled and return true. Otherwise, it will return false.
   * It is a command if it starts with a forward slash. (e.g., /help)
   *
   * @param input The input from the client
   * @return True if the input was a command, false otherwise
   * @since 1.0
   */
  private boolean handleIfCommand(String input) {
    if (!input.startsWith("/")) {
      return false;
    }

    String[] parts = input.split(" ");
    switch (parts[0]) {
      case "/help":
        sendMessageToClient("Available commands:");
        sendMessageToClient("/help - Displays this message");
        sendMessageToClient("/quit - Disconnects from the server");
        break;
      case "/quit":
        sendMessageToClient("Goodbye!");
        shutdown();
        break;
      case "/nick":
        if (parts.length < 2) {
          sendMessageToClient("Usage: /nick <new username>");
          break;
        }

        String newUsername = parts[1];
        if (!isValidUsername(newUsername)) {
          break;
        }

        sendMessageToClient("Username changed to " + newUsername);
        server.broadcastToAll(username + " changed their username to " + newUsername);
        username = newUsername;
        break;
      default:
        sendMessageToClient("Unknown command, type /help for a list of available commands");
    }
    return true;
  }

  /**
   * Closes the connection to the client.
   *
   * @since 1.0
   */
  public void shutdown() {
    try {
      in.close();
      out.close();

      if (!client.isClosed()) {
        client.close();
      }
    } catch (IOException e) {
      // Ignore
    }
  }

  /**
   * Sends a message to the client.
   * The message is sent to the client output stream.
   *
   * @param message The message to send
   * @since 1.0
   */
  public void sendMessageToClient(String message) {
    out.println(message);
  }
}
