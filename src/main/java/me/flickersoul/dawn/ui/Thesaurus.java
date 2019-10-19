package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.HistoryArray;
import me.flickersoul.dawn.functions.JSLookup;
import netscape.javascript.JSObject;

public class Thesaurus extends WebDisplayTab {
    private JSLookup app;
    private ContextMenu contextMenu;
    private static StringProperty html = new SimpleStringProperty();
    public static void setHtml(String content){
        html.set(content);
    }

    public Thesaurus(){
        super("Thesaurus", ClipboardPane.TH_TAB_NUM, "<div></div>");

        app = new JSLookup();
        contextMenu = WebContextMenu.getContextMenu();

        WebView webView = getWebView();
        WebEngine webEngine = getWebEngine();

        super.setStyleSheetLocation("/css/definition-common.css");

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

        webEngine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject player = (JSObject) webEngine.executeScript("window");
                player.setMember("appPlayer", app);
            }
        }));

        html.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                webEngine.loadContent(newValue);
            });
        });

        this.setContent(webView);
    }
}
