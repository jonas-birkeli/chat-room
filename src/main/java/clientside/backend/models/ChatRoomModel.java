package clientside.backend.models;

import clientside.backend.Client;
import clientside.backend.ConnectionFailedException;
import clientside.backend.RecieveChatObserver;
import clientside.frontend.controllers.ChatRoomController;
import java.util.logging.Logger;

public class ChatRoomModel implements RecieveChatObserver {
  private final ChatRoomController controller;

  /**
   * Initializes the model.
   * The controller is passed to the model to allow the model to call methods in the controller.
   * The model is also added as a subscriber to the client.
   *
   * @param controller The controller to pass to the model
   * @since 1.0
   */
  public ChatRoomModel(ChatRoomController controller) {
    this.controller = controller;
    try {
      Client.getInstance().addSubscriber(this);
    } catch (ConnectionFailedException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to create client" + e.getMessage());
    }
  }

  /**
   * Sends a message to the server.
   *
   * @param input The message to send
   */
  public void sendMessage(String input) {
    try {
      Client.getInstance().sendSymmetricEncryptedMessage(input);
    } catch (ConnectionFailedException e) {
      Logger.getLogger(this.getClass().getName()).severe("Failed to send message" + e.getMessage());
    }
  }

  /**
   * Receives a chat message.
   *
   * @param message The message to receive
   * @since 1.0
   */
  @Override
  public void receiveChat(String message) {
    System.out.println("Received message in model: " + message);
    controller.appendMessage(message);
    controller.clearInputField();
  }
}
