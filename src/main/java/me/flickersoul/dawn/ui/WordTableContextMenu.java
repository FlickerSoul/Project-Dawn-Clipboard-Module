package me.flickersoul.dawn.ui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;

public class WordTableContextMenu extends ContextMenu {

    private WordTableContextMenu(){

        MenuItem back = new MenuItem("Go Back");
        MenuItem editItem = new MenuItem("Edit This Item");

        back.setOnAction(event -> {
            ((DatabaseContentTable)((TableCell<WordDetailMember, String>)this.getOwnerNode()).getTableView()).changeToMaterialColumns();
            WordDetailEditorWindowController.setMaterial_id(MaterialTableContextMenu.NULL_NUMBER);
        });

        editItem.setOnAction(event -> ((DatabaseContentTable)((TableCell)this.getOwnerNode()).getTableView()).openWordEditor((TableCell)this.getOwnerNode()));

        this.getItems().addAll(editItem, back);
    }

    public static WordTableContextMenu getWordTableContextMenu(){
        return new WordTableContextMenu();
    }
}
