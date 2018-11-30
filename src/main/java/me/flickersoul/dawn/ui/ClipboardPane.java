package me.flickersoul.dawn.ui;

import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import me.flickersoul.dawn.functions.WrapedIntBoolProperty;

public class ClipboardPane extends TabPane {
    private EnDefRegion enDefRegion;
    private ChDefRegion chDefRegion;
    private Thesaurus thesaurus;

    private static SingleSelectionModel<Tab> selectionModel;

    private static WrapedIntBoolProperty tabSign = new WrapedIntBoolProperty(); // 0->en 1->ch 2->th 3->search

    public static final int EN_TAB_NUM = 0;
    public static final int CH_TAB_NUM = 1;
    public static final int TH_TAB_NUM = 2;

    public ClipboardPane(){
        enDefRegion = new EnDefRegion();
        chDefRegion = new ChDefRegion();
        thesaurus = new Thesaurus();

        enDefRegion.setOnSelectionChanged(event -> System.gc());

        chDefRegion.setOnSelectionChanged(event -> System.gc());

        selectionModel = this.getSelectionModel();

        tabSign.addListener((observable, oldValue, newValue) -> {
            switch (tabSign.getChangedTabNum()){
                case 0:
                    this.focusOnEn();
                    break;
                case 1:
                    this.focusOnCh();
                    break;
                case 2:
                    this.focusOnTh();
                    break;
            }
        });

        this.getTabs().addAll(enDefRegion, chDefRegion, thesaurus);
    }

    public static void setTabSignValue(int tabNum){
        if (!ClipboardPane.selectionModel.isSelected(tabNum)){
            tabSign.changeTabNum(tabNum);
        }
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
}
