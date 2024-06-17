package clientside.backend;

/**
 * Exception thrown when the client fails to connect to the server.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 13.06.2024
 */
public class ConnectionFailedException extends Exception {

  /**
   * Create a new ConnectionFailedException.
   *
   * @param message The exception message
   * @since 1.0
   */
  public ConnectionFailedException(String message) {
    super(message);
  }
}