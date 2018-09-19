package me.flickersoul.dawn.ui;

import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

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
