package me.flickersoul.dawn.ui;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import me.flickersoul.dawn.functions.HistoryArray;

public class WebContextMenu extends ContextMenu{
    private static MenuItem previousWord;
    private static MenuItem latterWord;
    private static WebContextMenu webContextMenu = new WebContextMenu();

    public WebContextMenu(){
        previousWord = new MenuItem("Previous (Alt + →)");
        latterWord = new MenuItem("Forward (Alt + ←)");

        previousWord.setOnAction(event -> HistoryArray.getPreviousWord());
        latterWord.setOnAction(event -> HistoryArray.getLatterWord());

        this.getItems().addAll(previousWord, latterWord);
    }

    public static ContextMenu getContextMenu(){
        return webContextMenu;
    }
}
