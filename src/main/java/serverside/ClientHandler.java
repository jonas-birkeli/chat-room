package serverside;

import static config.UserConfig.HELP_COMMAND;
import static config.UserConfig.LIST_USERS_COMMAND;
import static config.UserConfig.MESSAGE_USER_COMMAND;
import static config.UserConfig.NEW_NICKNAME_COMMAND;
import static config.UserConfig.SHUTDOWN_COMMAND;
import static config.UserConfig.USERNAME_NOT_SET;
import static config.ConnectionConfig.PASSWORD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * The handler class is responsible for handling the client connection.
 *
 * @version 1.1
 * @author Jonas Birkeli
 * @since 08.06.2024
 */
public class ClientHandler implements Runnable {
  private final Socket client;
  private final Server server;

  private PrintWriter out;
  private BufferedReader in;

  private String username;
  private boolean authenticated = false;
  private final SecretKey secretKey;
  private final IvParameterSpec ivParameterSpec;
  private Cipher encryptCipher;
  private Cipher decryptCipher;

  /**
   * Constructor for the handler class.
   *
   * @param client The client socket
   * @param server The server instance
   * @param secretKey The secret key for encryption
   * @param ivParameterSpec The IV for encryption
   * @since 1.0
   */
  public ClientHandler(Socket client, Server server, SecretKey secretKey, IvParameterSpec ivParameterSpec) {
    this.client = client;
    this.server = server;
    this.secretKey = secretKey;
    this.ivParameterSpec = ivParameterSpec;

    setUsername(USERNAME_NOT_SET);
    initCiphers();
  }


  /**
   * Initializes the encryption and decryption ciphers.
   * Uses AES encryption with CBC mode and PKCS5 padding.
   *
   * @since 1.2
   */
  private void initCiphers() {
    try {
      encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

      decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to initialize ciphers");
    }
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
      out = new PrintWriter(new CipherOutputStream(client.getOutputStream(), encryptCipher), true);
      in = new BufferedReader(new InputStreamReader(new CipherInputStream(client.getInputStream(), decryptCipher)));
      requestPassword();

      requestUsername();
      server.broadcastToAll(username + " has joined the chat.");

      // MAIN LOOP - Read input from the client and broadcast it to all clients
      while ((input = in.readLine()) != null) {

        if (!handleIfCommand(input)) {
          server.broadcastToAll(username + ": " + input);
        }
      }
      // END MAIN LOOP - Client disconnected
    } catch (Exception e) {
      shutdown();
    }
    shutdown();
  }

  private void requestPassword() {
    String input = "";
    while (!authenticated) {
      sendMessageToClient("Enter the password:");
      try {
        input = in.readLine();
      } catch (IOException e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to read password");
      }
      authenticated = input.equals(PASSWORD);
      if (!authenticated) {
        try {
          Thread.sleep(3000); // 3-second cooldown
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          Logger.getLogger(this.getClass().getName()).severe("Thread was interrupted during cooldown");
        }
        sendMessageToClient("Incorrect password.");
      }
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
      invalidUsername = isInvalidUsername(input);
    }

    if (username.equals(USERNAME_NOT_SET)) {
      sendMessageToClient("Welcome " + input + "!");
    } else {
      sendMessageToClient("Successfully changed username to " + input + "!");
    }
    setUsername(input);
  }

  /**
   * Validates whether the username is valid.
   * Will send a message to the client if the username is invalid and return true.
   *
   * @param username The username to validate
   * @return whether the username is valid, true if invalid, false otherwise
   * @since 1.0
   */
  private boolean isInvalidUsername(String username) {
    if (username == null) {
      sendMessageToClient("Invalid username.");
    } else if (username.isEmpty() || username.isBlank()) {
      sendMessageToClient("Username cannot be blank.");
    } else if (username.equals(this.username)) {
      sendMessageToClient("Using the same username.");
      return false;
    } else if (server.isUsernameTaken(username)) {
      sendMessageToClient("Username already taken.");
    }

    return username == null || username.isEmpty() || server.isUsernameTaken(username);
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
      case HELP_COMMAND:
        sendMessageToClient("Available commands:");
        sendMessageToClient("/help - Displays this message");
        sendMessageToClient("/list - Lists all connected users");
        sendMessageToClient("/msg <username> <message> - Sends a private message to a user");
        sendMessageToClient("/nick <new username> - Changes your username");
        sendMessageToClient("/quit - Disconnects from the server");
        break;
      case MESSAGE_USER_COMMAND:
        if (parts.length < 3) {
          sendMessageToClient("Usage: /msg <username> <message>");
          break;
        }
        String recipient = parts[1];
        String message = input.substring(input.indexOf(recipient) + recipient.length() + 1);

        if (server.isUsernameTaken(recipient)) {
          sendMessageToClient("User not found.");
          break;
        }
        server
            .getClients()
            .filter(clientHandler -> clientHandler.getUsername().equals(recipient))
            .findFirst()
            .ifPresent(clientHandler -> clientHandler.sendMessageToClient(username + " whispers: " + message));
        break;
      case SHUTDOWN_COMMAND:
        sendMessageToClient("Goodbye!");
        shutdown();
        break;
      case NEW_NICKNAME_COMMAND:
        if (parts.length < 2) {
          sendMessageToClient("Usage: /nick <new username>");
          break;
        }

        String newUsername = parts[1];
        if (isInvalidUsername(newUsername)) {
          break;
        }

        sendMessageToClient("Username changed to " + newUsername);
        server.broadcastToAll(username + " changed their username to " + newUsername);
        username = newUsername;
        break;
      case LIST_USERS_COMMAND:
        StringBuilder users = new StringBuilder("Connected users:");
        server
            .getClients()
            .forEach(
                clientHandler -> users.append("\n").append(clientHandler.getUsername())
            );
        sendMessageToClient(String.valueOf(users));
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

  /**
   * Sets the username of the client.
   * Must not be null, empty or blank.
   *
   * @param username The username to set
   * @since 1.1
   */
  public void setUsername(String username) {
    if (username != null && !username.isEmpty() && !username.isBlank()) {
      this.username = username;
    } else {
      this.username = USERNAME_NOT_SET;
    }
  }
}
