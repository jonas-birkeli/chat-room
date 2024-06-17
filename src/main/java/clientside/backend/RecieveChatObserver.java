package clientside.backend;

/**
 * The RecieveChatObserver interface is used to observe the recieving of chat messages.
 *
 * @version 1.0
 * @author Jonas Birkeli
 * @since 16.06.2024
 */
public interface RecieveChatObserver {

  /**
   * Receives a chat message.
   *
   * @param message The message to recieve
   * @since 1.0
   */
  void receiveChat(String message);
}
