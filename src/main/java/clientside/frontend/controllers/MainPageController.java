package clientside.frontend.controllers;

import clientside.backend.models.MainPageModel;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The MainPageController class is responsible for handling the main page of the chatroom.
 *
 * @version 1.2
 * @author Jonas Birkeli
 * @since 13.06.2024
 */
public class MainPageController {
  private MainPageModel model;
  @FXML
  private VBox mainPageRoot;
  @FXML
  private Label header;
  @FXML
  private TextField inputField;

  /**
   * Initializes the controller.
   * Called when loading the FXML file.
   *
   * @since 1.0
   */
  public void initialize() {
    this.model = new MainPageModel();
    inputField.sceneProperty().addListener((observable, oldScene, newScene) -> {
      if (newScene != null) {
        inputField.minWidthProperty().bind(newScene.widthProperty().multiply(0.25));
        inputField.maxWidthProperty().bind(newScene.widthProperty().multiply(0.25));
      }
    });
  }

  /**
   * Handles the login button.
   *
   * @param actionEvent disregarded
   * @since 1.0
   */
  public void handleLoginButton(ActionEvent actionEvent) {
    actionEvent.consume();
    String input = inputField.getText();
    if (input.isEmpty() || input.isBlank()) {
      return;
    }
    boolean result;

    if (!model.isPasswordCorrect()) {
      result = model.checkPassword(input);

      if (result) {
        showSuccessBox("Correct password entered!");
        header.setText("Enter your username");
        inputField.clear();
        inputField.setPromptText("Username");
      } else {
        showErrorBox("Incorrect password!");
      }
      return;
    }

    result = model.checkUsername(input);

    if (result) {
      showSuccessBox("Welcome!");
      loadChatPage();
    } else {
      showErrorBox("Name already taken.");
    }
  }

  /**
   * Loads the chat page.
   *
   * @since 1.1
   */
  private void loadChatPage() {
    try {
      Parent root = FXMLLoader.load(
          Objects.requireNonNull(getClass().getResource("/fxml-files/chatroom.fxml")));
      Stage primaryStage = (Stage) mainPageRoot.getScene().getWindow();
      primaryStage.setScene(new Scene(root, primaryStage.getWidth(), primaryStage.getHeight()));
      primaryStage.show();

    } catch (Exception e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to load chat page");
    }
  }

  /**
   * Shows a success message.
   */
  private void showSuccessBox(String message) {
    Label label = new Label();
    label.setId("successLabel");
    createLabelBox(message, label);
  }

  /**
   * Shows an error message.
   *
   * @param message The message to show
   * @since 1.1
   */
  private void showErrorBox(String message) {
    Label label = new Label();
    label.setId("errorLabel");
    createLabelBox(message, label);
  }

  /**
   * Creates a label box.
   * Shows the message for a short period of time.
   *
   * @param message The message to show
   * @param label The label to show
   * @since 1.2
   */
  private void createLabelBox(String message, Label label) {
    label.setText(message);
    label.setOpacity(0);
    mainPageRoot.getChildren().add(label);

    FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.2), label);
    fadeIn.setToValue(1);

    PauseTransition pause = new PauseTransition(Duration.seconds(1));

    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2), label);
    fadeOut.setToValue(0);

    SequentialTransition sequence = new SequentialTransition(fadeIn, pause, fadeOut);
    sequence.setOnFinished(event -> mainPageRoot.getChildren().remove(label));
    sequence.play();
  }

  /**
   * Handles the input.
   *
   * @param actionEvent disregarded
   * @since 1.0
   */
  public void handleInput(ActionEvent actionEvent) {
    handleLoginButton(actionEvent);
  }
}
