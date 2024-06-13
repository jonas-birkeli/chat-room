package clientside.config;

/**
 * Configuration constants for the client.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 08.06.2024
 */
public class UserConfig {
  public static final String USERNAME_NOT_SET = "DEFAULT_USER";

  public static final String SHUTDOWN_COMMAND = "/quit";
  public static final String NEW_NICKNAME_COMMAND = "/nick";
  public static final String LIST_USERS_COMMAND = "/list";
  public static final String MESSAGE_USER_COMMAND = "/msg";
  public static final String HELP_COMMAND = "/help";

  private UserConfig() {} // Prevent instantiation
}
