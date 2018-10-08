package me.flickersoul.dawn.ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ClipboardPane extends TabPane {
    private EnDefRegion enDefRegion;
    private ChDefRegion chDefRegion;
    private Thesaurus thesaurus;
    private SearchEngineRegion searchEngineRegion;

    private SingleSelectionModel<Tab> selectionModel;

    private static SimpleIntegerProperty tabSign = new SimpleIntegerProperty(0); // 0->en 1->ch 2->th 3->search

    public static final int EN_TAB_NUM = 0;
    public static final int CH_TAB_NUM = 1;
    public static final int TH_TAB_NUM = 2;
    public static final int SC_TAB_NUM = 3;

    public ClipboardPane(){
        enDefRegion = new EnDefRegion();
        chDefRegion = new ChDefRegion();
        thesaurus = new Thesaurus();
        searchEngineRegion = new SearchEngineRegion();

        selectionModel = this.getSelectionModel();

        tabSign.addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()){
                case 0:
                    this.focusOnEn();
                    break;
                case 1:
                    this.focusOnCh();
                    break;
                case 2:
                    this.focusOnTh();
                    break;
                case 3:
                    this.focusOnSc();
                    break;
            }
        });

        this.getTabs().addAll(enDefRegion, chDefRegion, thesaurus, searchEngineRegion);
    }

    public static void setTabSignValue(int tabNum){
        tabSign.setValue(tabNum);
    }

    private void focusOnCh(){
        selectionModel.select(chDefRegion);
    }

    private void focusOnEn(){
        selectionModel.select(enDefRegion);
    }

    private void focusOnTh(){
        selectionModel.select(thesaurus);
    }

    private void focusOnSc(){
        selectionModel.select(searchEngineRegion);
    }
}
