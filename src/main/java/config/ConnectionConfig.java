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
  public static final String LOCALHOST = "127.0.0.1";
  public static final String SERVER_HOST = LOCALHOST;
  public static final String PASSWORD = "password";

  public static final int CONNECTION_FAILED_EXIT_CODE = 50;

  private ConnectionConfig() {} // Prevent instantiation
}
