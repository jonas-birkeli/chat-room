package serverside;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * The PasswordFactory class is responsible for generating passwords for the Wordle game.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 13.06.2024
 */
public class PasswordFactory {
  private String password;

  public PasswordFactory() {
    this.password = getWordleSolution();
  }

  /**
   * Get the password.
   *
   * @return the password as a string
   * @since 1.0
   */
  public String getPassword() {
    return this.password;
  }

  /**
   * Get the Wordle password for the current date.
   *
   * @return The Wordle password
   * @since 1.0
   */
  private String getWordleSolution() {
    String solution = "";
    LocalDate date = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    try {
      String urlString = "https://www.nytimes.com/svc/wordle/v2/" + date.format(formatter) + ".json";

      JSONObject jsonResponse = new JSONObject(createRequestUrl(urlString));
      return(jsonResponse.getString("solution"));
    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to get Wordle solution");
    }
    return solution.toLowerCase();
  }

  /**
   * Create a request to the given URL.
   *
   * @param urlString The URL to request
   * @throws IOException If an error occurs
   * @since 1.0
   */
  private String createRequestUrl(String urlString) throws IOException {
    URL url = new URL(urlString);

    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");

    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String inputLine;
    StringBuilder content = new StringBuilder();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }

    in.close();
    connection.disconnect();

    return String.valueOf(content);
  }
}
