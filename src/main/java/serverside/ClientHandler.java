package serverside;

import static config.UserConfig.KICK_COMMAND;
import static config.UserConfig.QUIT_COMMAND;
import static config.ConnectionConfig.PASSWORD_INCORRECT_MESSAGE;
import static config.ConnectionConfig.PASSWORD_SUCCESS_MESSAGE;
import static config.UserConfig.HELP_COMMAND;
import static config.UserConfig.LIST_USERS_COMMAND;
import static config.UserConfig.MESSAGE_USER_COMMAND;
import static config.UserConfig.NEW_NICKNAME_COMMAND;
import static config.UserConfig.SHUTDOWN_COMMAND;
import static config.UserConfig.USERNAME_NOT_SET;
import static config.ConnectionConfig.PASSWORD;
import static keyGen.KeyConfig.ASYMMETRIC_ALGORITHM_CREATE_KEY;
import static keyGen.KeyConfig.ASYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT;
import static keyGen.KeyConfig.SYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT;

import clientside.backend.Client;
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
 * @version 1.3
 * @author Jonas Birkeli
 * @since 08.06.2024
 */
public class ClientHandler extends KeyClass implements Runnable {
  private final Socket client;
  private final Server server;

  private PrintWriter out;
  private BufferedReader in;

  private String username = USERNAME_NOT_SET;
  private boolean authenticated = false;
  private boolean administrator = false;

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
      sendSecretKey();

      requestPassword();
      requestUsername();

      server.broadcastToAll(username + " has joined the chat.");

      // MAIN LOOP - Read input from the client and broadcast it to all clients
      while ((encryptedInput = in.readLine()) != null && !client.isClosed()) {
        input = symmetricDecryptMessage(encryptedInput);

        if (input == null) {
          sendEncryptedMessage("Failed to decrypt message. Mitm-attack? You will be disconnected.");
          throw new IOException();
        }

        if (!handleIfCommand(input)) {
          server.broadcastToAll(username + ": " + input);
        }
      }
    } catch (Exception ignored) {
    }
    shutdown();
  }

  /**
   * Sends the secret key to the client.
   *
   * @since 1.2
   */
  private void sendSecretKey() {
    String message = Base64.getEncoder().encodeToString(getSecretKey().getEncoded());
    try {
      // Encrypting our secret key with the client's public key
      Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT);
      cipher.init(Cipher.ENCRYPT_MODE, getOtherPartyPublicKey());
      byte[] encryptedMessageBytes = cipher.doFinal(message.getBytes());
      out.println(Base64.getEncoder().encodeToString(encryptedMessageBytes));
    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to encrypt message");
      shutdown();
    }
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
    KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM_CREATE_KEY);
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
  private void requestPassword() {
    String input;

    while (!authenticated && !client.isClosed()) {
      try {
        input = symmetricDecryptMessage(in.readLine());

        if (input == null) {
          throw new IOException();
        }

      } catch (IOException e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to read password");
        shutdown();
        continue;
      }

      administrator = input.equals(PASSWORD);  // Administrator password
      authenticated = administrator || input.equalsIgnoreCase(new PasswordFactory().getPassword());

      if (!authenticated) {
        sendEncryptedMessage(PASSWORD_INCORRECT_MESSAGE);
      }
    }
    sendEncryptedMessage(PASSWORD_SUCCESS_MESSAGE);
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
      try {
        String encryptedInput = in.readLine();
        input = symmetricDecryptMessage(encryptedInput);

      } catch (IOException e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to read username");
      }
      invalidUsername = isInvalidUsername(input);
    }

    setUsername(input);
    sendEncryptedMessage(PASSWORD_SUCCESS_MESSAGE);
    sendEncryptedMessage("Welcome " + this.username + "!");
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
    String response = "";
    if (username == null) {
      response = "Invalid username.";
    } else if (username.isEmpty() || username.isBlank()) {
      response = "Username cannot be blank.";
    } else if (server.isUsernameTaken(username)) {
      response = "Username already taken.";
    }
    if (!response.isEmpty()) {
      sendEncryptedMessage(response);
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
        if (administrator) {
          sendEncryptedMessage("/kick <username> - Kicks a user from the server");
          sendEncryptedMessage("/shutdown - Shuts down the server");
        }
        break;
      case MESSAGE_USER_COMMAND:
        if (parts.length < 3) {
          sendEncryptedMessage("Usage: /msg <username> <message>");
          break;
        }
        String recipient = parts[1];
        String message = input.substring(input.indexOf(recipient) + recipient.length() + 1);

        server
            .getClients()
            .filter(clientHandler -> clientHandler.getUsername().equals(recipient))
            .findFirst()
            .ifPresentOrElse(
                clientHandler -> clientHandler.sendEncryptedMessage(username + " whispers: " + message),
                () -> sendEncryptedMessage("User not found. Use /list to see connected users.")
            );
        break;
      case QUIT_COMMAND:
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
      case KICK_COMMAND:
        if (!administrator) {
          sendEncryptedMessage("You do not have permission to use this command.");
          break;
        }
        if (parts.length < 2) {
          sendEncryptedMessage("Usage: /kick <username>");
          break;
        }
        String userToKick = parts[1];
        server
            .getClients()
            .filter(clientHandler -> clientHandler.getUsername().equals(userToKick))
            .findFirst()
            .ifPresent(clientHandler -> {
              clientHandler.sendEncryptedMessage("You have been kicked from the server.");
              clientHandler.shutdown();
            });
        sendEncryptedMessage("User " + userToKick + " has been kicked from the server.");
        break;
      case SHUTDOWN_COMMAND:
        if (!administrator) {
          sendEncryptedMessage("You do not have permission to use this command.");
          break;
        }
        server.shutdown();
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
    server.broadcastToAll(username + " has left the chat.");
    sendEncryptedMessage(QUIT_COMMAND);

    try {
      server.removeClient(this);
      in.close();
      out.close();

      if (!client.isClosed()) {
        client.close();
      }

    } catch (IOException ignored) {/* Ignored */}
  }

  /**
   * Encrypts and sends a message to the client.
   * The message is sent to the client output stream.
   *
   * @param message The message to send
   * @since 1.0
   */
  public void sendEncryptedMessage(String message) {
    out.println(symmetricEncryptMessage(message));
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
      return;
    }
    this.username = USERNAME_NOT_SET;
  }

  /**
   * Encrypt the message using symmetric encryption, allowing for longer messages.
   * If the encryption fails, the client shuts down.
   *
   * @param message The message to encrypt
   * @return The encrypted message
   * @since 1.3
   */
  private String symmetricEncryptMessage(String message) {
    try {
      Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT);
      cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
      byte[] encryptedMessageBytes = cipher.doFinal(message.getBytes());
      return Base64.getEncoder().encodeToString(encryptedMessageBytes);
    } catch (Exception e) {
      Logger.getLogger(Client.class.getName()).severe("Failed to encrypt message");
      shutdown();
    }
    return null;
  }

  /**
   * Decrypt the message using symmetric decryption.
   * If the decryption fails, the client shuts down.
   *
   * @param encryptedMessage The message to decrypt
   * @return The decrypted message
   * @since 1.3
   */
  private String symmetricDecryptMessage(String encryptedMessage) {
    try {
      Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT);
      cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
      byte[] decryptedMessageBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
      return new String(decryptedMessageBytes);
    } catch (Exception e) {
      Logger.getLogger(Client.class.getName()).severe("Failed to decrypt message");
      System.out.println("Failed to decrypt message: " + encryptedMessage + "." + e.getMessage());
      shutdown();
    }
    return null;
  }
}
