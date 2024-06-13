package serverside;

import static config.ConnectionConfig.WRONG_PASSWORD_TIMEOUT_MILLIS;
import static clientside.config.UserConfig.HELP_COMMAND;
import static clientside.config.UserConfig.LIST_USERS_COMMAND;
import static clientside.config.UserConfig.MESSAGE_USER_COMMAND;
import static clientside.config.UserConfig.NEW_NICKNAME_COMMAND;
import static clientside.config.UserConfig.SHUTDOWN_COMMAND;
import static clientside.config.UserConfig.USERNAME_NOT_SET;
import static config.ConnectionConfig.PASSWORD;
import static keyGen.KeyConfig.KEY_ALGORITHM_PADDED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import keyGen.KeyClass;

/**
 * The handler class is responsible for handling the client connection.
 *
 * @version 1.1
 * @author Jonas Birkeli
 * @since 08.06.2024
 */
public class ClientHandler extends KeyClass implements Runnable {
  private final Socket client;
  private final Server server;

  private PrintWriter out;
  private BufferedReader in;

  private String username;
  private boolean authenticated = false;

  /**
   * Constructor for the handler class.
   *
   * @param client The client socket
   * @param server The server instance
   * @since 1.0
   */
  public ClientHandler(Socket client, Server server) {
    super();

    this.client = client;
    this.server = server;

    setUsername(USERNAME_NOT_SET);
  }

  /**
   * The run method is called when the thread is started.
   *
   * @since 1.0
   */
  @Override
  public void run() {
    String encryptedInput;
    String input;
    try {
      out = new PrintWriter(client.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(client.getInputStream()));

      sendPublicKey();
      requestPublicKey();

      requestPassword();
      requestUsername();
      server.broadcastToAll(username + " has joined the chat.");

      // MAIN LOOP - Read input from the client and broadcast it to all clients
      while ((encryptedInput = in.readLine()) != null && !client.isClosed()) {
        input = decryptMessage(encryptedInput);

        if (input == null) {
          sendEncryptedMessage("Failed to decrypt message. You will be disconnected.");
          shutdown();
          continue;
        }

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

  /**
   * Receives the public key from the client.
   *
   * @since 1.1
   */
  private void requestPublicKey()
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    String serverPublicKeyString = in.readLine();
    byte[] serverPublicKeyBytes = Base64.getDecoder().decode(serverPublicKeyString);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(serverPublicKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    setOtherPartyPublicKey(keyFactory.generatePublic(spec));
  }

  /**
   * Sends the public key to the client.
   *
   * @since 1.1
   */
  private void sendPublicKey() {
    out.println(Base64.getEncoder().encodeToString(getPublicKey().getEncoded()));
  }

  /**
   * Requests the password from the client.
   *
   * @since 1.0
   */
  @SuppressWarnings("BusyWait")
  private void requestPassword() {
    String input = "";
    while (!authenticated) {
      sendEncryptedMessage("Enter the password:");
      try {
        input = decryptMessage(in.readLine());
      } catch (IOException e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to read password");
      }
      if (input == null) {
        sendEncryptedMessage("Failed to decrypt password.");
        continue;
      }
      authenticated = input.equals(PASSWORD);
      if (!authenticated) {

        try {
          Thread.sleep(WRONG_PASSWORD_TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          Logger.getLogger(this.getClass().getName()).severe("Thread was interrupted during cooldown");
        }

        sendEncryptedMessage("Incorrect password.");
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
      sendEncryptedMessage("Enter your username:");

      try {
        String encryptedInput = in.readLine();
        input = decryptMessage(encryptedInput);

      } catch (IOException e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to read username");
      }
      invalidUsername = isInvalidUsername(input);
    }

    if (username.equals(USERNAME_NOT_SET)) {
      sendEncryptedMessage("Welcome " + input + "!");
    } else {
      sendEncryptedMessage("Successfully changed username to " + input + "!");
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
      sendEncryptedMessage("Invalid username.");
    } else if (username.isEmpty() || username.isBlank()) {
      sendEncryptedMessage("Username cannot be blank.");
    } else if (username.equals(this.username)) {
      sendEncryptedMessage("Using the same username.");
      return false;
    } else if (server.isUsernameTaken(username)) {
      sendEncryptedMessage("Username already taken.");
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
        sendEncryptedMessage("Available commands:");
        sendEncryptedMessage("/help - Displays this message");
        sendEncryptedMessage("/list - Lists all connected users");
        sendEncryptedMessage("/msg <username> <message> - Sends a private message to a user");
        sendEncryptedMessage("/nick <new username> - Changes your username");
        sendEncryptedMessage("/quit - Disconnects from the server");
        break;
      case MESSAGE_USER_COMMAND:
        if (parts.length < 3) {
          sendEncryptedMessage("Usage: /msg <username> <message>");
          break;
        }
        String recipient = parts[1];
        String message = input.substring(input.indexOf(recipient) + recipient.length() + 1);

        if (!server.isUsernameTaken(recipient)) {
          sendEncryptedMessage("User not found.");
          break;
        }
        server
            .getClients()
            .filter(clientHandler -> clientHandler.getUsername().equals(recipient))
            .findFirst()
            .ifPresent(clientHandler -> clientHandler.sendEncryptedMessage(username + " whispers: " + message));
        break;
      case SHUTDOWN_COMMAND:
        sendEncryptedMessage("Goodbye!");
        shutdown();
        break;
      case NEW_NICKNAME_COMMAND:
        if (parts.length < 2) {
          sendEncryptedMessage("Usage: /nick <new username>");
          break;
        }

        String newUsername = parts[1];
        if (isInvalidUsername(newUsername)) {
          break;
        }

        sendEncryptedMessage("Username changed to " + newUsername);
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
        sendEncryptedMessage(String.valueOf(users));
        break;
      default:
        sendEncryptedMessage("Unknown command, type /help for a list of available commands");
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
   * Encrypts and sends a message to the client.
   * The message is sent to the client output stream.
   *
   * @param message The message to send
   * @since 1.0
   */
  public void sendEncryptedMessage(String message) {
    out.println(encryptMessage(message));
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

  /**
   * Encrypt the message using AES encryption
   * If the encryption fails, a message is logged, and null is returned
   *
   * @param message The message to encrypt
   * @return The encrypted message
   * @since 1.1
   */
  private String encryptMessage(String message) {
    try {
      Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDED);
      cipher.init(Cipher.ENCRYPT_MODE, getOtherPartyPublicKey());
      byte[] encryptedMessageBytes = cipher.doFinal(message.getBytes());
      return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to encrypt message");
      return null;
    }
  }

  /**
   * Decrypt the message using AES decryption
   * If the decryption fails, a message is logged and null is returned
   *
   * @param encryptedMessage The message to decrypt
   * @return The decrypted message
   * @since 1.1
   */
  private String decryptMessage(String encryptedMessage) {
    try {
      Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_PADDED);
      cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
      byte[] decryptedMessageBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
      return new String(decryptedMessageBytes);
    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to decrypt message");
      return null;
    }
  }
}
