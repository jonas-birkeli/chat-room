package clientside.backend;

import static config.ConnectionConfig.PASSWORD_SUCCESS_MESSAGE;
import static config.ConnectionConfig.USERNAME_SUCCESS_MESSAGE;
import static keyGen.KeyConfig.ASYMMETRIC_ALGORITHM_CREATE_KEY;
import static keyGen.KeyConfig.ASYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT;
import static keyGen.KeyConfig.SYMMETRIC_ALGORITHM_CREATE_KEY;
import static keyGen.KeyConfig.SYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT;

import clientside.backend.models.ChatRoomModel;
import config.ConnectionConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import keyGen.KeyClass;

/**
 * The client class is responsible for handling the client side of the chatroom.
 *
 * @version 1.3
 * @author Jonas Birkeli
 * @since 09.06.2024
 */
public class Client extends KeyClass implements Runnable {
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;

  private boolean running = true;
  private ExecutorService pool;

  private static Client instance;

  private final List<RecieveChatObserver> observers;

  /**
   * Constructor for the client class.
   * It creates the streams for the client.
   *
   * @throws ConnectionFailedException If the connection to the server fails
   * @since 1.2
   */
  private Client() throws ConnectionFailedException {
    super();
    observers = new ArrayList<>();
    createStreams();
  }

  /**
   * Get the instance of the client.
   * If the instance is null, a new instance is created.
   *
   * @return The instance of the client
   * @throws ConnectionFailedException If the connection to the server fails
   * @since 1.2
   */
  public static Client getInstance() throws ConnectionFailedException {
    if (instance == null) {
      try {
        instance = new Client();
      } catch (ConnectionFailedException e) {
        throw new ConnectionFailedException("Failed to connect to server" + e.getMessage());
      }
    }
    return instance;
  }

  /**
   * The run method is called when the thread is started.
   *
   * @throws ConnectionFailedException If the connection to the server fails
   * @since 1.0
   */
  public void createStreams() throws ConnectionFailedException {
    try {
      socket = new Socket(ConnectionConfig.SERVER_HOST, ConnectionConfig.PORT);

      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      receiveOtherPartyPublicKeyFromServer();
      sendPublicKeyToServer();
      receiveSecretKeyFromServer();

    } catch (IOException e) {
      Logger.getLogger(Client.class.getName()).severe("Failed to connect to server");
      shutdown();
      throw new ConnectionFailedException("Failed to connect to server" + e.getMessage());
    }
  }

  @Override
  public void run() {
    pool = Executors.newCachedThreadPool();
    pool.execute(new InputHandler());
  }

  /**
   * Receive a symmetrically encrypted message from the server.
   * The Message is decrypted before being returned.
   *
   * @return The decrypted message
   * @since 1.3
   */
  public String receiveSymmetricEncryptedMessage() {
    Logger.getLogger(Client.class.getName()).info("Waiting for message");
    try {
      String input = in.readLine();
      String decryptedMessage = symmetricDecryptMessage(input);

      if (input == null || decryptedMessage == null) {
        return null;
      }

      Logger.getLogger(Client.class.getName()).info("Received message: " + decryptedMessage);
      return decryptedMessage;
    } catch (IOException e) {
      Logger.getLogger(Client.class.getName()).severe("Failed to read input from server");
      shutdown();
    }
    return null;
  }

  /**
   * Send a symmetrically encrypted message to the server.
   * If the encryption fails, a message is logged, and the client is shut down.
   *
   * @param message The message to send
   * @since 1.3
   */
  public void sendSymmetricEncryptedMessage(String message) {
    String encryptedMessage = symmetricEncryptMessage(message);
    out.println(encryptedMessage);

    Logger.getLogger(Client.class.getName()).info("Sent message: " + message);
  }

  /**
   * Encrypt the message using symmetric encryption, allowing for longer messages.
   * If the encryption fails, a message is logged, and null is returned.
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
      Logger.getLogger(Client.class.getName()).severe("Failed to encrypt message." + e.getMessage());
      shutdown();
    }
    return null;
  }

  /**
   * Decrypt the message using symmetric decryption.
   * If the decryption fails, a message is logged, and null is returned.
   *
   * @param encryptedMessage The message to decrypt
   * @return The decrypted message
   * @since 1.3
   */
  private String symmetricDecryptMessage(String encryptedMessage) {
    if (encryptedMessage == null) {
      return null;
    }
    try {
      Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT);
      cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
      byte[] decryptedMessageBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
      return new String(decryptedMessageBytes);
    } catch (Exception e) {
      Logger.getLogger(Client.class.getName()).severe("Failed to decrypt message");
      shutdown();
    }
    return null;
  }

  /**
   * Attempt to log in with a password.
   * If the password is correct, the user is logged in.
   *
   * @param password The password to attempt to log in with
   * @return True if the password is correct, false otherwise
   * @since 1.3
   */
  public boolean attemptPasswordLogin(String password) {
    sendSymmetricEncryptedMessage(password);

    String response = receiveSymmetricEncryptedMessage();
    return response.equals(PASSWORD_SUCCESS_MESSAGE);
  }

  /**
   * Attempt to login with a username.
   *
   * @param username The username to attempt to log in with
   * @return True if the username is accepted, false otherwise
   * @since 1.3
   */
  public boolean attemptUsernameLogin(String username) {
    sendSymmetricEncryptedMessage(username);

    String response = receiveSymmetricEncryptedMessage();

    if (response == null) {
      return false;
    }

    // Start the input handler if the username is accepted
    if (response.equals(USERNAME_SUCCESS_MESSAGE)) {
      startInputHandlerThread();
      return true;
    }
    return false;
  }

  /**
   * Start the input handler thread.
   *
   * @since 1.3
   */
  private void startInputHandlerThread() {
    this.run();
  }

  /**
   * Stops the thread and closes the socket.
   *
   * @since 1.0
   */
  public void shutdown() {
    running = false;

    try {
      if (pool != null) {
        pool.shutdown();
      }
      if (socket != null) {
        socket.close();
      }
    } catch (IOException ignored) {
    }
  }

  /**
   * Receive the public key of the other party from the server.
   *
   * @since 1.1
   */
  private void receiveOtherPartyPublicKeyFromServer() {
    try {
      String serverPublicKeyString = in.readLine();
      byte[] serverPublicKeyBytes = Base64.getDecoder().decode(serverPublicKeyString);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(serverPublicKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALGORITHM_CREATE_KEY);

      setOtherPartyPublicKey(keyFactory.generatePublic(spec));

    } catch (Exception e) {
      Logger.getLogger(Client.class.getName()).severe("Failed to read public key from server. " + e.getMessage());
      shutdown();
    }
  }

  /**
   * Receive the secret key from the server.
   *
   * @since 1.3
   */
  private void receiveSecretKeyFromServer() {
    try {
      Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM_ENCRYPT_DECRYPT);
      cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
      byte[] decryptedMessageBytes = cipher.doFinal(Base64.getDecoder().decode(in.readLine()));
      String serverSecretKeyString = new String(decryptedMessageBytes);
      byte[] serverSecretKeyBytes = Base64.getDecoder().decode(serverSecretKeyString);
      setSecretKey(new SecretKeySpec(serverSecretKeyBytes, SYMMETRIC_ALGORITHM_CREATE_KEY));
    } catch (Exception e) {
      Logger.getLogger(Client.class.getName()).severe("Failed to read secret key from server. " + e);
      shutdown();
    }
  }

  /**
   * Send the public key to the server.
   *
   * @since 1.1
   */
  private void sendPublicKeyToServer() {
    String publicKeyString = Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
    out.println(publicKeyString);
  }

  /**
   * Add a subscriber to the client.
   *
   * @param chatRoomModel The subscriber to add
   * @since 1.1
   */
  public void addSubscriber(ChatRoomModel chatRoomModel) {
    observers.add(chatRoomModel);
  }

  /**
   * Remove a subscriber from the client.
   * If the subscriber is not found, nothing happens.
   *
   * @param chatRoomModel The subscriber to remove
   * @since 1.1
   */
  public void removeSubscriber(ChatRoomModel chatRoomModel) {
    observers.remove(chatRoomModel);
  }

  /**
   * Update all subscribers with a message.
   * If the message is null, nothing happens.
   *
   * @param message The message to send to the subscribers
   * @since 1.1
   */
  public void updateSubscribers(String message) {
    for (RecieveChatObserver observer : observers) {
      observer.receiveChat(message);
    }
  }

  /**
   * The input handler is responsible for reading input from the server.
   *
   * @version 1.1
   * @author Jonas Birkeli
   * @since 09.06.2024
   */
  private class InputHandler implements Runnable {

    /**
     * The run method is called when the thread is started.
     *
     * @since 1.0
     */
    @Override
    public void run() {
      try {
        while (running) {
          String input = in.readLine();
          String decryptedMessage = symmetricDecryptMessage(input);

          if (input == null || decryptedMessage == null) {
            continue;
          }

          updateSubscribers(decryptedMessage);
        }
      } catch (IOException e) {
        Logger.getLogger(Client.class.getName()).severe("Failed to read input from server");
        shutdown();
      }
    }
  }

  /**
   * The main method is the entry point of the program.
   *
   * @param args The command line arguments, discarded
   * @since 1.0
   */
  public static void main(String[] args) {
    try {
      new Client().run();
    } catch (ConnectionFailedException e) {
      Logger.getLogger(Client.class.getName()).severe("Failed to connect to server. Exiting...");
    }
  }
}
