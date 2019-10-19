package me.flickersoul.dawn.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class DetailTab extends WebDisplayTab {
    private static StringProperty lists = new SimpleStringProperty();
    private Document document;
    private Element ulList;

    public DetailTab() {
        super("Detail", ClipboardPane.DE_TAB_NUM, "");

        WebView webView = getWebView();
        WebEngine webEngine = getWebEngine();
        MaterialTableEventHandler materialTableEventHandler = new MaterialTableEventHandler();

        //TODO 制作设置以及超链接
        //TODO 搞定menuBar的按钮图标
        super.setStyleSheetLocation("/css/detail.css");

        webEngine.load(this.getClass().getResource("/css/detail.html").toExternalForm());
        webEngine.getLoadWorker().stateProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("windows", materialTableEventHandler);
            }
        }));

        lists.addListener((observable, oldValue, newValue) -> {
            TreeMap<Integer, TreeSet<String>> sentenceMap = JSON.parseObject(newValue, new TypeReference<>() {});

            ulList.empty();

            for(Map.Entry<Integer, TreeSet<String>> entry : sentenceMap.entrySet()){
                String list = entry.getKey() + ": " + entry.getValue().pollFirst();
                ulList.appendChild(new Element("li").addClass("list-item").text(list));
            }

            Platform.runLater(() -> webEngine.loadContent(document.toString()));

        });

        this.setContent(webView);

        try {
            document = Jsoup.parse(this.getClass().getResourceAsStream("/css/detail.html"), null, "");
            document.getElementsByTag("head").first().prependChild(new Element("base").attr("href", webEngine.getLocation().replace("detail.html", "")));
            ulList = document.getElementById("content-ul");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-7);
        }
    }

    public static void loadSentenceMap(String sentMap){
        lists.set(sentMap);
    }

}
