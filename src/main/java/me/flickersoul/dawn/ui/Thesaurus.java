package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import me.flickersoul.dawn.functions.JSLookup;
import me.flickersoul.dawn.functions.JSPlay;
import netscape.javascript.JSObject;

public class Thesaurus extends Tab {
    private WebView definition;
    private WebEngine webEngine;
    private JSLookup app;
    public static SimpleStringProperty html = new SimpleStringProperty();

    public Thesaurus(){
        super("Thesaurus");
        this.setClosable(false);
        app = new JSLookup();

        definition = new WebView();
        webEngine = definition.getEngine();
        webEngine.loadContent("<div></div>");
        webEngine.setUserStyleSheetLocation(this.getClass().getClassLoader().getResource("CSS/definition-common.css").toExternalForm());

        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject player = (JSObject) webEngine.executeScript("window");
                player.setMember("appPlayer", app);
            }
        }));

        html.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                webEngine.loadContent(html.getValue());
            });
        });

        this.setContent(definition);
    }

}
