package clientside.frontend.controllers;

import clientside.backend.models.ChatRoomModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ChatRoomController {
  private ChatRoomModel model;
  @FXML
  public ScrollPane scrollPane;
  @FXML
  private TextField inputField;
  private VBox chatBox;

  public void initialize() {
    chatBox = new VBox();
    scrollPane.setContent(chatBox);
    // Bind the prefWidthProperty and prefHeightProperty of the ScrollPane to the width and height properties of the Scene
  }

  public ChatRoomController() {
    this.model = new ChatRoomModel(this);
  }

  public void handleInput(ActionEvent actionEvent) {
    // Handle the input
    String input = inputField.getText();
    if (input == null || input.isEmpty() || input.isBlank()) {
      System.out.println("Input is empty");
      return;
    }

    model.sendMessage(input);
    // Clear the input field

  }

  public void appendMessage(String message) {
    Platform.runLater(() -> {
      // Append the message to the chat
      Label label = new Label(message);
      chatBox.getChildren().add(label);
    });
  }

  /**
   * Clears the input field.
   *
   * @since 1.0
   */
  public void clearInputField() {
    inputField.clear();
  }
}
