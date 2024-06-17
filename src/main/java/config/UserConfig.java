package config;

/**
 * Configuration constants for the client.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 08.06.2024
 */
public class UserConfig {
  public static final String USERNAME_NOT_SET = "DEFAULT_USER";

  public static final String QUIT_COMMAND = "/quit";
  public static final String NEW_NICKNAME_COMMAND = "/nick";
  public static final String LIST_USERS_COMMAND = "/list";
  public static final String MESSAGE_USER_COMMAND = "/msg";
  public static final String HELP_COMMAND = "/help";
  public static final String KICK_COMMAND = "/kick";
  public static final String SHUTDOWN_COMMAND = "/shutdown";

  private UserConfig() {} // Prevent instantiation
}
