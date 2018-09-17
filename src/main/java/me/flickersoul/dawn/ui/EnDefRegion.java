package me.flickersoul.dawn.ui;


import me.flickersoul.dawn.functions.JSPlay;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class EnDefRegion extends Tab {
    private WebView definition;
    private WebEngine webEngine;
    private JSPlay app;
    public static SimpleStringProperty html = new SimpleStringProperty();

    public EnDefRegion(){
        super("English Definition");
        this.setClosable(false);

        app = new JSPlay();
        definition = new WebView();
        webEngine = definition.getEngine();

        System.out.println(this.getClass().getClassLoader().getResource("CSS/definition-common.css").toExternalForm());
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

        this.setContent(definition);
    }
}
