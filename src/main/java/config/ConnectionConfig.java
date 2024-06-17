package config;

/**
 * Configuration constants for the connection.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 08.06.2024
 */
public class ConnectionConfig {
  public static final int PORT = 8687;
  public static final String LOCALHOST = "::1";  // IPv6 localhost because it looks cooler
  public static final String SERVER_HOST = LOCALHOST;

  public static final boolean USE_WORDLE_SOLUTION_AS_PASSWORD = true;
  public static final String PASSWORD = "password";
  public static final int WRONG_PASSWORD_TIMEOUT_MILLIS = 0;
  public static final String PASSWORD_SUCCESS_MESSAGE = "approved";
  public static final String PASSWORD_INCORRECT_MESSAGE = "denied";
  public static final String USERNAME_SUCCESS_MESSAGE = "approved";

  public static final int CONNECTION_FAILED_EXIT_CODE = 50;

  private ConnectionConfig() {} // Prevent instantiation
}
