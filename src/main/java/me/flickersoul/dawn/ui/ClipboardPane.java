package me.flickersoul.dawn.ui;

import javafx.scene.control.TabPane;

public class ClipboardPane extends TabPane {
    EnDefRegion enDefRegion;
    ChDefRegion chDefRegion;
    Thesaurus thesaurus;
    public ClipboardPane(){
        enDefRegion = new EnDefRegion();
        chDefRegion = new ChDefRegion();
        thesaurus = new Thesaurus();

        this.getTabs().addAll(enDefRegion, chDefRegion, thesaurus);
    }

}
