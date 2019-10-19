package me.flickersoul.dawn.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class WordSelector extends Stage {

    private static final String GET_INFO_FROM_TOTAL_SQL = "SELECT id, word FROM total_word WHERE known=2 OR known=0";
    private static final String GET_ALL_INFO_FROM_TOTAL_SQL = "SELECT id, word FROM total_word WHERE known!=1";

    WordSelectorController controller;

    public WordSelector()  {
       FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/css/WordSelector.fxml"));

        try {
            this.setScene(new Scene(loader.load(), 600, 450));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.initModality(Modality.APPLICATION_MODAL);
        controller = loader.getController();
        controller.initBinding();

        try(Connection connection = DBConnection.establishMaterialRepoConnection();
            Statement getInfoFromTotalWords = connection.createStatement();
            ResultSet resultSet = getInfoFromTotalWords.executeQuery(GET_INFO_FROM_TOTAL_SQL)) {
            ArrayList<String> words = new ArrayList<>();
            ArrayList<Integer> ids = new ArrayList<>();
            while(resultSet.next()){
                ids.add(resultSet.getInt(1));
                words.add(resultSet.getString(2));
            }

            controller.fillLists(ids, words);
            if(controller.initContent())
                this.showAndWait();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.setOnCloseRequest(windowEvent -> controller.uploadOnClose());
        this.setEventHandler(KeyEvent.KEY_RELEASED, event -> controller.triggerKeyMapping(event.getCode()));
    }

    public static void openSelector(){
       WordSelector wordSelector = new WordSelector();
    }
}
