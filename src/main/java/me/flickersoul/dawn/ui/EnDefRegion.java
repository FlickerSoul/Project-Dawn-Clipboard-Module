package me.flickersoul.dawn.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.HistoryArray;
import me.flickersoul.dawn.functions.AudioPlay;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;

public class EnDefRegion extends WebDisplayTab {
    private AudioPlay app;
    private ContextMenu contextMenu;
    private static StringProperty html = new SimpleStringProperty();
    public static void setHtml(String content){
        html.set(content);
    }

    public EnDefRegion(){
        super("English Definition", ClipboardPane.EN_TAB_NUM, "<div></div>");

        WebView webView = super.getWebView();
        WebEngine webEngine = super.getWebEngine();

        app = new AudioPlay();
        contextMenu = WebContextMenu.getContextMenu();

        webView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY){
                String selection = (String)webView.getEngine().executeScript("window.getSelection().toString()");
                if(selection.toCharArray().length == 0){
                    contextMenu.show(webView, event.getScreenX(), event.getScreenY());
                }else{
                    if(ClipboardFunctionQuery.lookupWord(selection))
                        HistoryArray.insertSearchResult(selection);
                    else
                        HistoryArray.setEmptyFlagTrue();
                }
            }else {
                contextMenu.hide();
            }
        });

        super.setStyleSheetLocation("/css/definition-common.css");
        webEngine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject player = (JSObject) webEngine.executeScript("window");
                player.setMember("appPlayer", app);
            }
        }));

        html.addListener(((observable, oldValue, newValue) -> {
            Platform.runLater(() -> webEngine.loadContent(newValue));
        }));


        this.setContent(webView);
    }
}
