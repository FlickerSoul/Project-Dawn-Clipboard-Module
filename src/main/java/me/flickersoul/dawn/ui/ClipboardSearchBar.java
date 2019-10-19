package me.flickersoul.dawn.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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
        searchBox.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if(e.getCode() == KeyCode.ENTER){
                first = searchBox.getText();
                if(!first.equals(null) && !first.equals(last)){
                    last = first;
                    if(ClipboardFunctionQuery.lookupWord(last)){
                        HistoryArray.putSearchResult(last);
                    }else{
                        HistoryArray.setEmptyFlagTrue();
                    }
                    searchBox.selectAll();
                }
            }
        });

        searchBox.textProperty().addListener((observableValue, oldValue, newValue) -> text.set(newValue));

        searchButton = new Button("Search");
        searchButton.setId("search-button");
        searchButton.setCursor(Cursor.HAND);
        searchButton.setStyle("-fx-shape: \"M15.5 14h-.79l-.28-.27c1.2-1.4 1.82-3.31 1.48-5.34-.47-2.78-2.79-5-5.59-5.34-4.23-.52-7.79 3.04-7.27 7.27.34 2.8 2.56 5.12 5.34 5.59 2.03.34 3.94-.28 5.34-1.48l.27.28v.79l4.25 4.25c.41.41 1.08.41 1.49 0 .41-.41.41-1.08 0-1.49L15.5 14zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z\"; -fx-background-color: black;");
        searchButton.setText("");
        searchButton.setPrefSize(25, 15);        searchButton.autosize();
        searchButton.setTooltip(new Tooltip("Search Words"));
        searchButton.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            first = searchBox.getText();
            if(!first.equals(null) && !first.equals(last)){
                last = first;
                if(ClipboardFunctionQuery.lookupWord(last)){
                    HistoryArray.putSearchResult(last);
                }else{
                    HistoryArray.setEmptyFlagTrue();
                }
                searchBox.selectAll();
            }
        });

        text.addListener((observable, oldValue, newValue) -> {
            searchBox.setText(newValue);
        });

        this.setSpacing(3);
        this.setPadding(new Insets(2, 2, 2, 2));
        this.getChildren().addAll(searchBox, searchButton);
    }

    public void requestSearchBoxFocused(){
        searchBox.requestFocus();
        searchBox.selectAll();
    }

    public static void setTextField(String text){
        ClipboardSearchBar.text.setValue(text);
    }

    public static String getTextFromTextField(){
        return text.get();
    }
}
