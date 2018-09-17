package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.control.*;
import me.flickersoul.dawn.functions.JSPlay;
import netscape.javascript.JSObject;

public class ChDefRegion extends Tab {
    private WebView webView;
    private WebEngine webEngine;
    private JSPlay app;

    public static SimpleStringProperty html = new SimpleStringProperty();

    public ChDefRegion(){
        super("Chinese Definition");
        this.setClosable(false);

        webView = new WebView();
        webEngine = webView.getEngine();
        app = new JSPlay();

        Platform.runLater(() -> webEngine.setUserStyleSheetLocation(this.getClass().getClassLoader().getResource("CSS/definition-common.css").toExternalForm()));
        webEngine.setJavaScriptEnabled(true);
        webEngine.loadContent("<div></div>");
        webEngine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject player = (JSObject) webEngine.executeScript("window");
                player.setMember("appPlayer", app);
            }
        }));

        html.addListener(((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                webEngine.loadContent(newValue);
            });
        }));

        this.setContent(webView);
    }
}
