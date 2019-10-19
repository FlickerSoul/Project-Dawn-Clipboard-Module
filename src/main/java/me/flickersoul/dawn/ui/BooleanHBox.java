package me.flickersoul.dawn.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BooleanHBox extends HBox {
    private BooleanProperty selectedProperty = new SimpleBooleanProperty(false);

    public void setKnowValue(boolean sign){
        selectedProperty.set(sign);
    }

    public void flipKnowValue(){
        selectedProperty.set(!selectedProperty.get());
    }

    public boolean getKnowValue(){
        return selectedProperty.get();
    }

    public BooleanProperty getRecordProperty(){
        return selectedProperty;
    }

    private CheckBox checkBox = new CheckBox();
    private Label text = new Label("");

    private StringProperty textProperty;

    public void setWordText(String text){
        textProperty.setValue(text);
    }

    public String getWordText(){
        return textProperty.get();
    }

    private int total_word_id;

    public void setWordId(int id){
        total_word_id = id;
    }

    public int getWordId(){
        return total_word_id;
    }

    public BooleanHBox() {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.setSpacing(3);
        this.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(0.7))));
        this.setMaxSize(150, 100);
        this.setMinSize(150, 100);
        this.setPrefSize(150, 100);
        this.setDisable(true);

        text.setFont(new Font(18));
        text.setEllipsisString("...");
        checkBox.selectedProperty().bindBidirectional(selectedProperty);
        this.getChildren().addAll(checkBox, text);

        selectedProperty.addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                this.setStyle("-fx-background-color: darkgrey");
            } else {
                this.setStyle("");
            }
        });

        this.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getButton() == MouseButton.PRIMARY)
                flipKnowValue();
            else if(mouseEvent.getButton() == MouseButton.SECONDARY){
                Stage stage = new Stage();
                Text text = new Text(textProperty.get());
                text.setFont(new Font(20));
                HBox hBox = new HBox(text);
                hBox.setPadding(new Insets(5));
                hBox.setAlignment(Pos.CENTER);

                stage.setScene(new Scene(hBox, 400, 200));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            }

        });

        textProperty = text.textProperty();

        textProperty.addListener(((observableValue, oldValue, newValue) -> {
            if(newValue.equals("")){
                this.setDisable(true);
                this.setKnowValue(false);
            } else {
                this.setDisable(false);
            }
        }));
    }


}
