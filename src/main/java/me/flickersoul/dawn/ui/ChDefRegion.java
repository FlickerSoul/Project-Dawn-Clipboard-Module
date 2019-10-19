package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.control.*;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.HistoryArray;
import me.flickersoul.dawn.functions.AudioPlay;
import me.flickersoul.dawn.functions.KingSoftAPIQuery;
import netscape.javascript.JSObject;

public class ChDefRegion extends WebDisplayTab {
    private AudioPlay app;
    private ContextMenu contextMenu;
    private static StringProperty html = new SimpleStringProperty();
    public static void setHtml(String content){
        html.set(content);
    }

    private static final String INCORRECT_URL_HTML = "<h1>You Cannot Open Other Websites</h1>";

    public ChDefRegion(){
        super("Chinese Definition", ClipboardPane.CH_TAB_NUM, "<div></div>");
        contextMenu = WebContextMenu.getContextMenu();
        ((WebContextMenu)contextMenu).addNewItemToMenu("Use KingSoft Dict", 0, actionEvent -> {
            if(ClipboardFunctionQuery.getCQueryIndicator() == ClipboardFunctionQuery.KING_SOFT){
                ((MenuItem)actionEvent.getSource()).setText("Use Youdao Dict");
                ClipboardFunctionQuery.setCQueryIndicatorToYoudao();
            } else {
                ((MenuItem)actionEvent.getSource()).setText("Use KingSoft Dict");
                ClipboardFunctionQuery.setCQueryIndicatorToKingSoft();
            }
        });

//        html.bind();

        WebView webView = getWebView();
        WebEngine webEngine = getWebEngine();
        app = new AudioPlay();

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
            Platform.runLater(() -> {
                if(newValue.startsWith("https://m.youdao.com"))
                    webEngine.load(newValue);
                else if(newValue.startsWith("<html>\n <head></head>") || newValue.equals(KingSoftAPIQuery.EMPTY_TEMPLATE))
                    webEngine.loadContent(newValue);
                else
                    webEngine.loadContent(INCORRECT_URL_HTML);
            });
        }));

        this.setContent(webView);
    }
}
