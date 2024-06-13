package clientside.frontend.controllers;

import clientside.config.Lang;
import config.UserConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainPageController {
  @FXML
  private VBox mainPageRoot;
  @FXML
  private Label welcomeLabel;
  @FXML
  private Button loginButton;

  public void initialize() {
    System.out.println("initializing main-page");
    useEnglish();

  }

  private void useEnglish() {
    welcomeLabel.setText(Lang.getEnglish().get("welcomeHeader") + ", " + UserConfig.USERNAME_NOT_SET);
    loginButton.setText(Lang.getEnglish().get("loginButton"));
  }

  private void useNorwegian() {
    welcomeLabel.setText(Lang.getNorwegian().get("welcomeHeader") + ", " + UserConfig.USERNAME_NOT_SET);
    loginButton.setText(Lang.getNorwegian().get("loginButton"));
  }

  public void handleLoginButton(ActionEvent actionEvent) {

  }
}
