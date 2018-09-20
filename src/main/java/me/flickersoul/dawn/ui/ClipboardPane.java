package me.flickersoul.dawn.ui;

import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.HistoryArray;

public class ClipboardPane extends TabPane {
    EnDefRegion enDefRegion;
    ChDefRegion chDefRegion;
    Thesaurus thesaurus;


    SingleSelectionModel<Tab> selectionModel;

    public ClipboardPane(){
        enDefRegion = new EnDefRegion();
        chDefRegion = new ChDefRegion();
        thesaurus = new Thesaurus();

        selectionModel = this.getSelectionModel();

        this.getTabs().addAll(enDefRegion, chDefRegion, thesaurus);
    }

    public void focusOnCh(){
        selectionModel.select(chDefRegion);
    }

    public void focusOnEn(){
        selectionModel.select(enDefRegion);
    }

    public void focusOnTh(){
        selectionModel.select(thesaurus);
    }
}
