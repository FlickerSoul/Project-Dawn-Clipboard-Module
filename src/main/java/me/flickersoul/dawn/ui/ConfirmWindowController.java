package me.flickersoul.dawn.ui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class ConfirmWindowController {
    @FXML
    Button go_back_button;
    @FXML
    Button finish_button;

    public void initEvents(boolean goback_disable, EventHandler<? super MouseEvent> gobackAction, EventHandler<? super MouseEvent> finishAction){
        go_back_button.setDisable(goback_disable);
        go_back_button.setOnMouseClicked(gobackAction);
        finish_button.setOnMouseClicked(finishAction);
    }


}
