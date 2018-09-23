package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.control.*;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.JSPlay;
import netscape.javascript.JSObject;

public class ChDefRegion extends Tab {
    private WebView webView;
    private WebEngine webEngine;
    private JSPlay app;
    private String selection;
    private ContextMenu contextMenu;

    private static SimpleStringProperty html = new SimpleStringProperty();

    public ChDefRegion(){
        super("Chinese Definition");
        this.setClosable(false);
        contextMenu = WebContextMenu.getContextMenu();

        webView = new WebView();
        webEngine = webView.getEngine();
        app = new JSPlay();

        webView.setContextMenuEnabled(false);
        webView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY){
                selection = (String)webView.getEngine().executeScript("window.getSelection().toString()");
                if(selection.toCharArray().length == 0){
                    contextMenu.show(webView, event.getScreenX(), event.getScreenY());
                }else{
                    ClipboardFunctionQuery.lookupWord(selection);
                }
            }else {
                contextMenu.hide();
            }
        });

        Platform.runLater(() -> webEngine.setUserStyleSheetLocation(this.getClass().getClassLoader().getResource("css/definition-common.css").toExternalForm()));
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

    public static void setHtml(String text){
        html.setValue(text);
    }
}
