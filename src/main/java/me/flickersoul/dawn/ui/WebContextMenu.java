package me.flickersoul.dawn.ui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import me.flickersoul.dawn.functions.HistoryArray;

public class WebContextMenu extends ContextMenu{
    private static MenuItem previousWord;
    private static MenuItem latterWord;
    private static MenuItem searchOption;
    private static WebContextMenu webContextMenu = new WebContextMenu();

    public WebContextMenu(){
        previousWord = new MenuItem("Previous (Alt + ←)");
        latterWord = new MenuItem("Forward (Alt + →)");
        searchOption = new MenuItem("Search ()");

        previousWord.setOnAction(event -> HistoryArray.getPreviousWord());
        latterWord.setOnAction(event -> HistoryArray.getLatterWord());
        searchOption.setOnAction(event -> HistoryArray.lookCurrentWordInGoogle());

        this.getItems().addAll(previousWord, latterWord, searchOption);
    }

    public static ContextMenu getContextMenu(){
        return webContextMenu;
    }
}
