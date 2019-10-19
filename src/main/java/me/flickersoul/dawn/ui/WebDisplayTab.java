package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class WebDisplayTab extends Tab {

    private WebView webView;
    private WebEngine webEngine;
    private int serial;

    public WebDisplayTab(String name, int serial, String initLoad){
        super(name);
        this.serial = serial;
        this.setClosable(false);

        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webView.setContextMenuEnabled(false);
        webEngine.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
        webEngine.loadContent(initLoad);
    }

    public int getSerial(){
        return serial;
    }

    public void setStyleSheetLocation(String location){
        Platform.runLater(() -> webEngine.setUserStyleSheetLocation(this.getClass().getResource(location).toExternalForm()));
    }

    protected WebView getWebView() {
        return webView;
    }

    protected WebEngine getWebEngine(){
        return webEngine;
    }

    public ReadOnlyBooleanProperty getHoverPropertyOfWebView(){
        return webView.hoverProperty();
    }
}
