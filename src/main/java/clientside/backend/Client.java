package clientside.backend;

import static config.ConnectionConfig.CONNECTION_FAILED_EXIT_CODE;

import config.ConnectionConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * The client class is responsible for handling the client side of the chatroom.
 *
 * @version 1.1
 * @author Jonas Birkeli
 * @since 09.06.2024
 */
public class Client implements Runnable {
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;

  private boolean running = true;
  private ExecutorService pool;

  private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
  private SecretKey secretKey;
  private IvParameterSpec ivParameterSpec;

  /**
   * The run method is called when the thread is started.
   *
   * @since 1.0
   */
  @Override
  public void run() {
    System.out.println("Client starting...");
    pool = Executors.newCachedThreadPool();
    try {
      socket = new Socket(ConnectionConfig.SERVER_HOST, ConnectionConfig.PORT);
      System.out.println("Connected to server!");

      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      initEncryption();

      System.out.println("Creating input and output handlers...");
      pool.execute(new InputHandler());
      pool.execute(new OutputHandler());

      System.out.println("Client started!");
    } catch (IOException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to connect to server");
      System.exit(CONNECTION_FAILED_EXIT_CODE);
    }
  }

  /**
   * Initialize encryption key and IV.
   *
   * @since 1.1
   */
  private void initEncryption() throws IOException {
    // For demo purposes, we are using a hard-coded key and IV.
    // In a real application, you would securely exchange these between the server and client.
    String key = "0123456789abcdef"; // Example key, should be securely generated
    String iv = "abcdef0123456789"; // Example IV, should be securely generated

    secretKey = new SecretKeySpec(key.getBytes(), "AES");
    ivParameterSpec = new IvParameterSpec(iv.getBytes());
  }

  /**
   * Encrypt the message using AES encryption
   */
  private String encryptMessage(String message) {
    try {
      Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
      byte[] encryptedBytes = cipher.doFinal(message.getBytes());
      return Base64.getEncoder().encodeToString(encryptedBytes);
    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to encrypt message");
      return null;
    }
  }

  /**
   * Decrypt the message using AES decryption
   */
  private String decryptMessage(String encryptedMessage) {
    try {
      Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
      byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage);
      byte[] decryptedBytes = cipher.doFinal(decodedBytes);
      return new String(decryptedBytes);
    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to decrypt message");
      return null;
    }
  }

  /**
   * Stops the thread and closes the socket.
   *
   * @since 1.0
   */
  private void shutdown() {
    running = false;

    try {
      pool.shutdown();
      in.close();
      out.close();
      socket.close();
    } catch (IOException e) {
      // Ignore
    }
  }

  /**
   * The input handler is responsible for reading input from the server.
   *
   * @version 1.0
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
          if (input == null) {
            continue;
          }

          String decryptedMessage = decryptMessage(input);
          if (decryptedMessage == null) {
            continue;
          }

          if (decryptedMessage.equals("/quit")) {
            shutdown();
            running = false;
          }
          System.out.println(decryptedMessage);
        }
      } catch (IOException e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to read input from server");
        shutdown();
      }
    }
  }

  /**
   * The output handler is responsible for sending messages to the server.
   *
   * @version 1.0
   * @author Jonas Birkeli
   * @since 09.06.2024
   */
  private class OutputHandler implements Runnable {

    /**
     * The run method is called when the thread is started.
     *
     * @since 1.0
     */
    @Override
    public void run() {
      try {
        while (running) {
          String message = System.console().readLine();
          String encryptedMessage = encryptMessage(message);
          out.println(encryptedMessage);
        }
      } catch (Exception e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to send message to server");
        shutdown();
      }
    }
  }
}
