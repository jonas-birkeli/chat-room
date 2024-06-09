package clientside;

import config.ConnectionConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * The client class is responsible for handling the client side of the chatroom.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 09.06.2024
 */
public class Client implements Runnable {
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;

  private boolean running = true;

  /**
   * The run method is called when the thread is started.
   *
   * @since 1.0
   */
  @Override
  public void run() {
    try {
      socket = new Socket(ConnectionConfig.SERVER_HOST, ConnectionConfig.PORT);

      out = new PrintWriter(socket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      new Thread(new InputHandler()).start();
      new Thread(new OutputHandler()).start();
    } catch (IOException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to connect to server");
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
      in.close();
      out.close();
      if (!socket.isClosed()) {
        socket.close();
      }
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
  private class InputHandler implements Runnable{

    /**
     * The run method is called when the thread is started.
     */
    @Override
    public void run() {
      try {
        while (running) {
          String input = in.readLine();

          if (input == null) {
            continue;
          }

          if (input.equals("/quit")) {
            shutdown();
          }
          System.out.println(input);
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
          out.println(System.console().readLine());
        }
      } catch (Exception e) {
        Logger.getLogger(this.getClass().getName()).severe("Failed to send message to server");
        shutdown();
      }
    }
  }
}
