package clientside.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The Lang class is responsible for holding the language strings.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 09.06.2024
 */
public class Lang {
  private static final String WELCOME_HEADER_ENGLISH = "Welcome";
  private static final String LOGIN_NAME_HEADER_ENGLISH = "Name";
  private static final String LOGIN_PASSWORD_HEADER_ENGLISH = "Password";
  private static final String LOGIN_BUTTON_ENGLISH = "Login";

  private static final String WELCOME_HEADER_NORWEGIAN = "Velkommen";
  private static final String LOGIN_NAME_HEADER_NORWEGIAN = "Navn";
  private static final String LOGIN_PASSWORD_HEADER_NORWEGIAN = "Passord";
  private static final String LOGIN_BUTTON_NORWEGIAN = "Logg inn";

  /**
   * Get the English language strings.
   *
   * @return The English language strings
   * @since 1.0
   */
  public static Map<String, String> getEnglish() {
    return getStringStringMap(WELCOME_HEADER_ENGLISH, LOGIN_NAME_HEADER_ENGLISH,
        LOGIN_PASSWORD_HEADER_ENGLISH, LOGIN_BUTTON_ENGLISH);
  }

  /**
   * Get the Norwegian language strings.
   *
   * @return The Norwegian language strings
   * @since 1.0
   */
  public static Map<String, String> getNorwegian() {
    return getStringStringMap(WELCOME_HEADER_NORWEGIAN, LOGIN_NAME_HEADER_NORWEGIAN,
        LOGIN_PASSWORD_HEADER_NORWEGIAN,
        LOGIN_BUTTON_NORWEGIAN);
  }

  /**
   * Get the language strings.
   *
   * @param welcomeHeader The welcome header text
   * @param loginNameHeader The login name header text
   * @param loginPasswordHeader The login password header text
   * @param loginButton The login button text
   * @return The language strings in a map
   * @since 1.0
   */
  private static Map<String, String> getStringStringMap(String welcomeHeader,
      String loginNameHeader, String loginPasswordHeader,
      String loginButton) {
    Map<String, String> norwegian = new HashMap<>();
    norwegian.put("welcomeHeader", welcomeHeader);
    norwegian.put("loginNameHeader", loginNameHeader);
    norwegian.put("loginPasswordHeader", loginPasswordHeader);
    norwegian.put("loginButton", loginButton);
    return norwegian;
  }
}
