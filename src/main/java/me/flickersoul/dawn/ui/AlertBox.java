package me.flickersoul.dawn.ui;

import javafx.scene.control.Alert;

public class AlertBox {
    protected static void displayError(String title, String headerText, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        alert.showAndWait();
    }

    protected static void displayWarning(String title, String hearderText, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(hearderText);
        alert.setContentText(content);

        alert.showAndWait();
    }
}
