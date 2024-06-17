module chatroom {
  requires java.logging;
  requires javafx.base;
  requires javafx.controls;
  requires javafx.fxml;
  requires org.json;

  exports clientside.frontend;
  exports clientside.backend;
  exports config;

  opens clientside.frontend.controllers;
  exports clientside.backend.models;
}