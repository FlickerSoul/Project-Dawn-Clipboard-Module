package me.flickersoul.dawn.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class SearchEngineRegion extends Tab {
    private WebView webView;
    private WebEngine webEngine;
    private static SimpleStringProperty content = new SimpleStringProperty();

    public SearchEngineRegion(){
        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");

        webEngine.load("https://google.com");

        content.addListener((observable, oldValue, newValue) -> {
            webEngine.load("https://www.google.com/search?q=" + newValue);
            System.gc();
        });

        this.setClosable(false);
        this.setText("Search");
        this.setContent(webView);
    }

    public static void setSearchContent(String content){
        SearchEngineRegion.content.setValue(content);
    }
}
