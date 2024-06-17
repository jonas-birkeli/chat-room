package clientside.backend.models;

import clientside.backend.Client;
import clientside.backend.ConnectionFailedException;
import java.util.logging.Logger;

public class MainPageModel {
  private Client client;
  private boolean passwordCorrect;


  public MainPageModel() {
    try {
      client = Client.getInstance();
    } catch (ConnectionFailedException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to create client" + e.getMessage());
    }
  }

  public boolean checkPassword(String input) {
    if (passwordCorrect) {
      return true;
    }

    if (client.attemptPasswordLogin(input)) {
      passwordCorrect = true;
      return true;
    }
    return false;
  }

  /**
   * Checks if the username is correct.
   *
   * @param name The username to check
   * @return True if the username is correct, false otherwise
   * @since 1.0
   */
  public boolean checkUsername(String name) {
    return client.attemptUsernameLogin(name);
  }

  /**
   * Returns whether the correct password has been entered.
   *
   * @return True if the correct password has been entered, false otherwise
   * @since 1.0
   */
  public boolean isPasswordCorrect() {
    return passwordCorrect;
  }
}
