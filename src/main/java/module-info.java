module chatroom {
  requires java.logging;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.fxml;

  exports clientside.frontend;
  exports clientside.backend;
  exports config;
}