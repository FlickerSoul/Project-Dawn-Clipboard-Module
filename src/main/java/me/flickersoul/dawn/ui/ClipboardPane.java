package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;

public class ClipboardPane extends TabPane {
    private EnDefRegion enDefRegion;
    private ChDefRegion chDefRegion;
    private Thesaurus thesaurus;
    private DetailTab detailTab;
    private ObservableList<Tab> smallTabList;

    private SingleSelectionModel<Tab> selectionModel;

    private static IntegerProperty tabSign = new SimpleIntegerProperty(0);

    public static final int EN_TAB_NUM = 0;
    public static final int CH_TAB_NUM = 1;
    public static final int TH_TAB_NUM = 2;
    public static final int DE_TAB_NUM = 3;

    private BooleanProperty tabHoverProperty;

    public ClipboardPane(){
        enDefRegion = new EnDefRegion();
        chDefRegion = new ChDefRegion();
        thesaurus = new Thesaurus();


        smallTabList = FXCollections.observableArrayList(enDefRegion, chDefRegion, thesaurus);

        enDefRegion.setOnSelectionChanged(event -> System.gc());

        chDefRegion.setOnSelectionChanged(event -> System.gc());

        selectionModel = this.getSelectionModel();

        selectionModel.selectedItemProperty().addListener((observableValue, oldTab, newTab) -> tabSign.set(((WebDisplayTab)newTab).getSerial()));

        tabSign.addListener((observable, oldValue, newValue) -> {
            switch (tabSign.get()){
                case EN_TAB_NUM:
                    this.focusOnEn();
                    break;
                case CH_TAB_NUM:
                    this.focusOnCh();
                    break;
                case TH_TAB_NUM:
                    this.focusOnTh();
                    break;
                case DE_TAB_NUM:
                    if(detailTab != null && this.getTabs().contains(detailTab)){
                        this.focusOnDe();
                    }
            }
        });

        this.getTabs().addAll(smallTabList);
    }

    public static void setTabSignValue(int tabNum){
        tabSign.set(tabNum);
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

    private void focusOnDe() {
        selectionModel.select(detailTab);
    }

    public void setObListToSmallList(){
        this.getTabs().setAll(smallTabList);
    }

    protected BooleanProperty addDetailTab(){
        if(detailTab == null) {
            detailTab = new DetailTab();
            tabHoverProperty = new SimpleBooleanProperty(false);
            tabHoverProperty.bind(
                    enDefRegion.getHoverPropertyOfWebView().or(
                        chDefRegion.getHoverPropertyOfWebView().or(
                                thesaurus.getHoverPropertyOfWebView().or(
                                        detailTab.getHoverPropertyOfWebView()))));
        }

        this.getTabs().add(0, detailTab);
        this.getSelectionModel().select(detailTab);
        return tabHoverProperty;
    }

    protected void deleteDetailTab(){
        Platform.runLater(() -> this.getTabs().remove(detailTab));
    }
}