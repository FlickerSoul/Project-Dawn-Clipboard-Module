package me.flickersoul.dawn.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import me.flickersoul.dawn.functions.HistoryArray;

public class WebContextMenu extends ContextMenu{

    private WebContextMenu(){
        MenuItem previousWord = new MenuItem("Previous (Alt + ←)");
        MenuItem latterWord = new MenuItem("Forward (Alt + →)");
        MenuItem searchOption = new MenuItem("Search ()");

        previousWord.setOnAction(event -> HistoryArray.getPreviousWord());
        latterWord.setOnAction(event -> HistoryArray.getLatterWord());
        searchOption.setOnAction(event -> HistoryArray.lookCurrentWordInGoogle());

        this.getItems().addAll(previousWord, latterWord, searchOption);
    }

    protected static ContextMenu getContextMenu(){
        return new WebContextMenu();
    }

    public void addNewItemToMenu(String text, int index, EventHandler<ActionEvent> eventHandler){
        MenuItem menuItem = new MenuItem(text);
        menuItem.setOnAction(eventHandler);
        this.getItems().add(index, menuItem);
    }
}
