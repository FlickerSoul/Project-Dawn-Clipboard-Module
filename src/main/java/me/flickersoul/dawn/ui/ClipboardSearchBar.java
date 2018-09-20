package me.flickersoul.dawn.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Tooltip;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import me.flickersoul.dawn.functions.HistoryArray;


public class ClipboardSearchBar extends HBox {
    private TextField searchBox;
    private Button searchButton;
    private static String last;
    private static String first;

    private static SimpleStringProperty text = new SimpleStringProperty();

    public ClipboardSearchBar(){
        searchBox = new TextField();
        searchBox.setPromptText("Type here to search...");
        searchBox.setOnKeyReleased(e -> {
            if(e.getCode() == KeyCode.ENTER){
                first = searchBox.getText();
                if(first != null && !first.equals(last)){
                    last = first;
                    if(ClipboardFunctionQuery.lookupWord(last)){
                        HistoryArray.putSearchResult(last);
                    }
                    searchBox.selectAll();
                }
            }
        });

        searchButton = new Button("Search");
        searchButton.setId("search-button");
        searchButton.setTooltip(new Tooltip("Search Words"));
        searchButton.setOnMouseClicked(e -> {
            first = searchBox.getText();
            if(first != null && !first.equals(last)){
                System.out.println("first: " + first);
                last = first;
                if(ClipboardFunctionQuery.lookupWord(last)){
                    HistoryArray.putSearchResult(last);
                }
                searchBox.selectAll();
            }
        });

        text.addListener((observable, oldValue, newValue) -> {
            searchBox.setText(newValue);
        });

        this.setPadding(new Insets(2, 2, 2, 2));
        this.getChildren().addAll(searchBox, searchButton);
    }

    public void requestSearchBoxFocused(){
        searchBox.requestFocus();
        searchBox.selectAll();
    }

    public static void setText(String text){
        ClipboardSearchBar.text.setValue(text);
    }
}
