package me.flickersoul.dawn.ui;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class FileChooserDriver {
    public static File getFile(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text File", "*.text"));
        fileChooser.setTitle("Open Text File");
        return fileChooser.showOpenDialog(stage);
    }
}
