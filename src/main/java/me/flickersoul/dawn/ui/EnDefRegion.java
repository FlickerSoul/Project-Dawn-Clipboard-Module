package me.flickersoul.dawn.ui;


import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.HistoryArray;
import me.flickersoul.dawn.functions.JSPlay;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Worker;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class EnDefRegion extends Tab {
    private WebView webView;
    private WebEngine webEngine;
    private JSPlay app;
    private ContextMenu contextMenu;
    private String selection;
    private static SimpleStringProperty html = new SimpleStringProperty();

    public EnDefRegion(){
        super("English Definition");
        this.setClosable(false);

        app = new JSPlay();
        webView = new WebView();
        webEngine = webView.getEngine();
        contextMenu = WebContextMenu.getContextMenu();

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
