package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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

public class Thesaurus extends Tab {
    private WebView webView;
    private WebEngine webEngine;
    private JSLookup app;
    private String selection;
    private ContextMenu contextMenu;

    private static SimpleStringProperty html = new SimpleStringProperty();

    public Thesaurus(){
        super("Thesaurus");
        this.setClosable(false);
        app = new JSLookup();
        contextMenu = WebContextMenu.getContextMenu();

        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.loadContent("<div></div>");
        webEngine.setUserStyleSheetLocation(this.getClass().getClassLoader().getResource("css/definition-common.css").toExternalForm());

        webView.setContextMenuEnabled(false);
        webView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY){
                selection = (String)webView.getEngine().executeScript("window.getSelection().toString()");
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

        this.setContent(webView);
    }

    public static void setHtml(String text){
        html.setValue(text);
    }
}
