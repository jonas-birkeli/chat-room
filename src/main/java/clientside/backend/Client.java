package clientside.backend;

import static config.ConnectionConfig.CONNECTION_FAILED_EXIT_CODE;
import static keyGen.KeyConfig.KEY_ALGORITHM;
import static keyGen.KeyConfig.KEY_ALGORITHM_PADDED;

import config.ConnectionConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import keyGen.KeyClass;

/**
 * The client class is responsible for handling the client side of the chatroom.
 *
 * @version 1.2
 * @author Jonas Birkeli
 * @since 09.06.2024
 */
public class Client extends KeyClass implements Runnable {
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;

  private boolean running = true;
  private ExecutorService pool;

  /**
   * Constructor for the client class.
   *
   * @since 1.2
   */
  public Client() {
    super();
  }

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
   * Stops the thread and closes the socket.
   *
   * @since 1.0
   */
  private void shutdown() {
    running = false;

    try {
      if (pool != null) {
        pool.shutdown();
      }
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      // Ignore
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
     * Receive the public key of the other party from the server.
     *
     * @since 1.1
     */
    private void receiveOtherPartyPublicKeyFromServer() {
      try {
        String serverPublicKeyString = in.readLine();
        byte[] serverPublicKeyBytes = Base64.getDecoder().decode(serverPublicKeyString);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(serverPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_PADDED);

        setOtherPartyPublicKey(keyFactory.generatePublic(spec));

      } catch (Exception e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to read public key from server");
        shutdown();
      }
    }

    /**
     * Decrypt the message using AES decryption
     * If the decryption fails, a message is logged, and null is returned
     *
     * @param encryptedMessage The message to decrypt
     * @return The decrypted message
     * @since 1.2
     */
    private String decryptMessage(String encryptedMessage) {
      try {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        byte[] decryptedMessageBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        return new String(decryptedMessageBytes);
      } catch (Exception e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to decrypt message");
        shutdown();
      }
      return null;
    }

    /**
     * The run method is called when the thread is started.
     *
     * @since 1.0
     */
    @Override
    public void run() {
      receiveOtherPartyPublicKeyFromServer();

      try {
        while (running) {
          String input = in.readLine();
          String decryptedMessage = decryptMessage(input);

          if (input == null || decryptedMessage == null) {
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
   * @version 1.1
   * @author Jonas Birkeli
   * @since 09.06.2024
   */
  private class OutputHandler implements Runnable {

    /**
     * Send the public key to the server.
     *
     * @since 1.1
     */
    private void sendPublicKeyToServer() {
      String publicKeyString = Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
      try {
        out.println(publicKeyString);
      } catch (Exception e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to send public key to server");
        shutdown();
      }
    }

    /**
     * Encrypt the message using AES encryption
     * If the encryption fails, a message is logged, and null is returned
     *
     * @param message The message to encrypt
     * @return The encrypted message
     * @since 1.2
     */
    private String encryptMessage(String message) {
      try {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getOtherPartyPublicKey());
        byte[] encryptedMessageBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedMessageBytes);
      } catch (Exception e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to encrypt message");
        shutdown();
      }
      return null;
    }

    /**
     * The run method is called when the thread is started.
     *
     * @since 1.0
     */
    @Override
    public void run() {
      sendPublicKeyToServer();

      try {
        while (running) {
          String message = System.console().readLine();
          System.out.print("\033[1A\033[2K");  // Clear the last line sent

          String encryptedMessage = encryptMessage(message);
          out.println(encryptedMessage);

          if (message.equals("/quit")) {
            // Graceful termination on the server side by sending /quit
            // before shutting down the client
            shutdown();
            running = false;
          }
        }
      } catch (Exception e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to send message to server");
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
    new Client().run();
  }
}
