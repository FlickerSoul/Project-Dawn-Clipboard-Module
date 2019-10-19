package me.flickersoul.dawn.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.AudioPlay;

import java.io.IOException;

public class MaterialTableEventHandler {
    public void importMaterial(){
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/css/ImportPane.fxml"));
            stage.setScene(new Scene(loader.load()));
            ImportPaneController controller = loader.getController();
            controller.setStage(stage);
            controller.initEvents();

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setAlwaysOnTop(ClipboardOnly.getAlwaysOnStatus());
            stage.setTitle("Import...");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openMaterialTable(){ClipboardOnly.changeToMaterialTableColumns();}

    public void openUnknownWordTable(){
        ClipboardOnly.changeToUnknownWordTableColumns();
    }

    public void openFamiliarWordTable(){
        ClipboardOnly.changeToKnownWordTableColumns();
    }

    public void openWordSelector(){
        WordSelector.openSelector();
    }

    public void openSettings(){

    }

    public void openAbout(){
        AudioPlay.openBlog();
    }
}
