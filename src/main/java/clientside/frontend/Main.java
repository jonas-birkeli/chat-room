package clientside.frontend;

import static clientside.config.Win.PREF_HEIGHT;
import static clientside.config.Win.PREF_WIDTH;
import static clientside.config.Win.WINDOW_TITLE;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The main class for the application.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 11.06.2024
 */
public class Main extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  /**
   * Start the application.
   *
   * @param stage The primary stage, discarded
   * @throws Exception If an error occurs, discard
   * @since 1.0
   */
  @Override
  public void start(Stage stage) throws Exception {
    System.out.println("Attempting to create loader");
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml-files/mainpage.fxml"));
    System.out.println("loader created");

    VBox root = null;
    try {
      root = loader.load();
      System.out.println("Loaded root");
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (root == null) {
      System.exit(1);
    }

    Scene scene = new Scene(root, PREF_WIDTH, PREF_HEIGHT);

    Label root2 = (Label) root.getChildren().get(0);
    System.out.println(root2 == null);

    stage.setTitle(WINDOW_TITLE);
    stage.setScene(scene);
    System.out.println("Kom hit");
    stage.show();
    System.out.println("kom ikke hit");
  }

  /**
   * Stop the application.
   *
   * @since 1.0
   */
  @Override
  public void stop() {
    System.out.println("Exiting application");
  }
}
