package me.flickersoul.dawn.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class WordSelectorController {
    @FXML
    private BooleanHBox f_1;
    @FXML
    private BooleanHBox f_2;
    @FXML
    private BooleanHBox f_3;
    @FXML
    private BooleanHBox f_4;
    @FXML
    private BooleanHBox s_1;
    @FXML
    private BooleanHBox s_2;
    @FXML
    private BooleanHBox s_3;
    @FXML
    private BooleanHBox s_4;
    @FXML
    private BooleanHBox t_1;
    @FXML
    private BooleanHBox t_2;
    @FXML
    private BooleanHBox t_3;
    @FXML
    private BooleanHBox t_4;
    @FXML
    private BooleanHBox l_1;
    @FXML
    private BooleanHBox l_2;
    @FXML
    private BooleanHBox l_3;
    @FXML
    private BooleanHBox l_4;


    private HashMap<Enum<KeyCode>, BooleanHBox> keyMap = new HashMap<>();
    private ArrayList<BooleanHBox> boxList = new ArrayList<>();
    private ArrayList<Integer> ids;
    private ArrayList<String> words;
    private int nextGroupHeadSerial = 0;
    private int currentGroupHeadSerial = -16;
    private int readyGroupHeadSerial  = -32;
    private int tempGroupHeadSerial = 16;
    private boolean[] reverseList = new boolean[16];


    private static final String UPDATE_KNOW_INFO_IN_TOTAL_WORDS_SQL = "UPDATE total_word SET known=? WHERE id=?";

    private Connection connection = DBConnection.establishMaterialRepoConnection();
    private PreparedStatement updateKnowInfoInTotalWords;

    public WordSelectorController(){
        try {
            connection.setAutoCommit(false);
            updateKnowInfoInTotalWords = connection.prepareStatement(UPDATE_KNOW_INFO_IN_TOTAL_WORDS_SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initBinding() {
        keyMap.put(KeyCode.DIGIT1, f_1);
        keyMap.put(KeyCode.DIGIT2, f_2);
        keyMap.put(KeyCode.DIGIT3, f_3);
        keyMap.put(KeyCode.DIGIT4, f_4);

        keyMap.put(KeyCode.Q, s_1);
        keyMap.put(KeyCode.W, s_2);
        keyMap.put(KeyCode.E, s_3);
        keyMap.put(KeyCode.R, s_4);

        keyMap.put(KeyCode.A, t_1);
        keyMap.put(KeyCode.S, t_2);
        keyMap.put(KeyCode.D, t_3);
        keyMap.put(KeyCode.F, t_4);

        keyMap.put(KeyCode.Z, l_1);
        keyMap.put(KeyCode.X, l_2);
        keyMap.put(KeyCode.C, l_3);
        keyMap.put(KeyCode.V, l_4);

        System.out.println(keyMap);

        boxList.addAll(keyMap.values());

    }

    public void triggerKeyMapping(KeyCode keyCode){
        if(keyMap.containsKey(keyCode))
            keyMap.get(keyCode).flipKnowValue();
        else if(keyCode.equals(KeyCode.SHIFT))
            flipAll();
        else if(keyCode.equals(KeyCode.LEFT))
            previousGroup();
        else if(keyCode.equals(KeyCode.RIGHT))
            nextGroup();
    }

    private void loadContent(boolean reverseLoad){
        readyGroupHeadSerial = currentGroupHeadSerial;
        currentGroupHeadSerial = nextGroupHeadSerial;

        if (reverseLoad) {
            for (int i = 0; i < 16; i++, nextGroupHeadSerial++) {
                BooleanHBox box = boxList.get(i);
                box.setWordText(words.get(nextGroupHeadSerial));
                box.setWordId(ids.get(nextGroupHeadSerial));
                box.setKnowValue(reverseList[i]);
            }
        } else {
            for (int i = 0; i < 16; i++, nextGroupHeadSerial++) {
                BooleanHBox box = boxList.get(i);
                box.setWordText(words.get(nextGroupHeadSerial));
                box.setWordId(ids.get(nextGroupHeadSerial));
                box.setKnowValue(true);
            }
        }
    }

    public boolean initContent(){
        if(words.size() == nextGroupHeadSerial){
            showFinishedWindow(true);
            return false;
        }else {
            loadContent(false);
            return true;
        }
    }

    private void prepareWriteIntoDB() {
        try {
            updateKnowInfoInTotalWords.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int i = 0;
        for (BooleanHBox box : boxList) {
            try {
                int id = box.getWordId();
                updateKnowInfoInTotalWords.setInt(1, box.getKnowValue() ? 1 : 0);
                updateKnowInfoInTotalWords.setInt(2, id);
                updateKnowInfoInTotalWords.addBatch();
                reverseList[i++] = box.getKnowValue();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
    private void reverseDBWrite(){
        try {
            updateKnowInfoInTotalWords.clearBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tempGroupHeadSerial = currentGroupHeadSerial;
        currentGroupHeadSerial = nextGroupHeadSerial = readyGroupHeadSerial;
    }

    public void fillLists(ArrayList<Integer> idList, ArrayList<String> wordList){
        int remainder = 16 - idList.size() % 16;
        if(remainder != 16){
            for(int i = 0; i < remainder; i ++){
                idList.add(-5);
                wordList.add("");
            }
        }

        ids = idList;
        words = wordList;
    }

    @FXML
    private void nextGroup(){
        if(nextGroupHeadSerial == words.size()) {
            showFinishedWindow(false);
        } else {
            prepareWriteIntoDB();
            loadContent(false);
        }
    }

    private void showFinishedWindow(boolean goback_disable){
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/css/ConfirmWindow.fxml"));
        try {
            stage.setScene(new Scene(loader.load()));
            ConfirmWindowController controller = loader.getController();
            controller.initEvents(goback_disable, mouseEvent -> stage.close(), mouseEvent -> {
                prepareWriteIntoDB();

                try {
                    updateKnowInfoInTotalWords.executeBatch();
                    connection.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                stage.close();
                f_1.getScene().getWindow().hide();
            });
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void previousGroup() {
        System.out.println("Previous group");
        System.out.println((nextGroupHeadSerial != tempGroupHeadSerial) + " : " + nextGroupHeadSerial + " : " + tempGroupHeadSerial);
        if(nextGroupHeadSerial != tempGroupHeadSerial) {
            reverseDBWrite(); //reverse process
            loadContent(true);
        }
    }

    @FXML
    private void flipAll() {
        for(BooleanHBox box : boxList){
            box.flipKnowValue();
        }
    }

    public void uploadOnClose(){
        try {
            updateKnowInfoInTotalWords.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
