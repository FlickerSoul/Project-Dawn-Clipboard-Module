package me.flickersoul.dawn.ui;

import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

public class ClipboardSearchBar extends HBox {
    TextField searchBox;
    Button searchButton;
    private static String last;
    private static String first;

    public ClipboardSearchBar(){
        searchBox = new TextField();
        searchBox.setPromptText("Type here to search...");
        searchBox.setOnKeyReleased(e -> {
            if(e.getCode() == KeyCode.ENTER){
                first = searchBox.getText().toLowerCase();
                if(first != null && !first.equals(last)){
                    last = first;
                    ClipboardFunctionQuery.lookupWord(last);
                }
            }
        });

        searchButton = new Button("Search");
        searchButton.setId("search-button");
        searchButton.setOnMouseClicked(e -> {
            first = searchBox.getText().toLowerCase();
            if(first != null && !first.equals(last)){
                System.out.println("first: " + first);
                last = first;
                ClipboardFunctionQuery.lookupWord(last);
            }
        });

        this.setPadding(new Insets(2, 2, 2, 2));
        this.getChildren().addAll(searchBox, searchButton);
    }

    public void requestSearchBoxFocused(){
        searchBox.requestFocus();
        searchBox.selectAll();
    }
}
