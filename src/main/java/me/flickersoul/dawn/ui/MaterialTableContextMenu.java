package me.flickersoul.dawn.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MaterialTableContextMenu extends ContextMenu {
    public static final int NULL_NUMBER = 0;
    private Stage stage;
    private FXMLLoader loader;

    private MaterialTableContextMenu() {
        stage = new Stage();
        loader = new FXMLLoader(this.getClass().getResource("/css/MaterialEditorWindow.fxml"));

        MenuItem editItem = new MenuItem("Edit");
        MenuItem viewItem = new MenuItem("View");
        MenuItem openItem = new MenuItem("Open");
        MenuItem editItemDescription = new MenuItem("Edit Description");

        viewItem.setOnAction(event -> {
            showEditor(false, false);
        });

        editItem.setOnAction(event -> {
            showEditor(true, true);
        });

        editItemDescription.setOnAction(event -> {
            showEditor(false, true);
        });

        openItem.setOnAction(event -> {
            WordDetailEditorWindowController.setMaterial_id(((TableCell<MaterialIndexMember, String>) getOwnerNode()).getIndex() + 1);
            ((DatabaseContentTable) ((TableCell<MaterialIndexMember, String>) getOwnerNode()).getTableView()).changeToWordDetailColumns(WordDetailEditorWindowController.getMaterial_id());
        });

        try {
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.initModality(Modality.APPLICATION_MODAL);

        this.getItems().addAll(openItem, viewItem, editItem, editItemDescription);
    }

    private void showEditor(boolean name_editable, boolean description_editable) {
        MaterialIndexMember member = ((TableCell<MaterialIndexMember, String>) this.getOwnerNode()).getTableView().getSelectionModel().getSelectedItem();

        String material_name = member.getMaterial_name();
        String description = member.getDescription();

        openEditor(material_name, name_editable, description, description_editable, member.getSerial());
    }

    private void openEditor(String name, boolean name_editable, String description, boolean description_editable, int id) {
        MaterialEditorWindowController controller = loader.getController();
        controller.setMaterialName(name, name_editable).setMaterialDescription(description, description_editable).setPopup(stage).setSubmitButtonVisible(name_editable || description_editable).setId(id);
        stage.showAndWait();
    }

    protected static MaterialTableContextMenu getMaterialTableMenu() {
        return new MaterialTableContextMenu();
    }
}