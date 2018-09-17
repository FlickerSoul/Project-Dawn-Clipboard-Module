package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class Thesaurus extends Tab {
    private WebView definition;
    private WebEngine webEngine;
    public static SimpleStringProperty html = new SimpleStringProperty();
    public Thesaurus(){
        super("Thesaurus");
        this.setClosable(false);

        definition = new WebView();
        webEngine = definition.getEngine();
        webEngine.loadContent("<div></div>");
        webEngine.setUserStyleSheetLocation(this.getClass().getClassLoader().getResource("CSS/definition-common.css").toExternalForm());

        html.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                webEngine.loadContent(html.getValue());
            });
        });

        this.setContent(definition);
    }

}
