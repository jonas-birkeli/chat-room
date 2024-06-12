package clientside.frontend.controllers;

import config.UserConfig;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MainpageController {
  @FXML
  private VBox mainPageRoot;
  @FXML
  private Label welcomeLabel;

  public void initialize() {
    System.out.println("initializing main-page");
    welcomeLabel.setText("Welcome, " + UserConfig.USERNAME_NOT_SET);
  }
}
