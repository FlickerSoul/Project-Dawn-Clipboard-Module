package me.flickersoul.dawn.ui;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.DBConnection;

import java.sql.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static me.flickersoul.dawn.functions.ClipboardFunctionQuery.FIRST_LETTER_PATTERN;

public class WordDetailEditorWindowController {
    @FXML
    TextField word_text_field;
    @FXML
    TextField word_id_text_field;
    @FXML
    TextField lowercase_form_text_field;
    @FXML
    TextField lowercase_form_id_text_field;
    @FXML
    TextField occurrence_times_text_field;
    @FXML
    TextArea sentence_text_area;
    @FXML
    Button to_lowercase_button;
    @FXML
    Button submit_button;
    @FXML
    Button cancel_button;
    @FXML
    Button delete_button;
    @FXML
    Text changeable_text;

    private static final int NOT_FOUND_ID = -1;

    private String originalWord;
    private String lowercaseOriginalWord;

    private static final String UPDATE_STATEMENT_SQL_HEAD = "UPDATE words_";
    private static final String UPDATE_STATEMENT_SQL_TAIL = " SET word=?, word_id=?, lowercase_form=?, lowercase_id=? WHERE word=?";

    private static final String INSERT_NEW_WORDS_TO_TOTAL_WORD_SQL = "UPDATE total_word SET word=?, word_id=? WHERE word=?";

    protected static final String HIDE_STATEMENT_SQL = "UPDATE total_word SET shown=? WHERE word=?";

    private static final String DELETE_FROM_WORD_LIST_SQL_HEAD = "DELETE FROM words_";
    private static final String DELETE_FROM_WORD_LIST_SQL_TAIL = " WHERE serial=?";

    private static final String CHECK_EXISTENCE_IN_WORDS_SQL_HEAD = "SELECT serial FROM words_";
    private static final String CHECK_EXISTENCE_IN_WORDS_SQL_TAIL = " WHERE word=?";

    private static final String INTEGRATE_STATEMENT_SQL_HEAD = "UPDATE words_";
    private static final String INTEGRATE_STATEMENT_SQL_TAIL = " SET occurrence_number=?, sentence_info=? WHERE serial=?";

    private static final String UPDATE_SENT_MAP_IN_TOTAL_WORD_TABLE_SQL = "UPDATE total_word SET sentence_info=? WHERE id=?";
    private static final String DELETE_EMPTY_RECORD_IN_TOTAL_WORD_SQL = "DELETE FROM total_word WHERE id=?";

    private static final String FETCH_SENT_MAP_FROM_TOTAL_WORD_TABLE_SQL = "SELECT id, sentence_info FROM total_word WHERE word=?";

    private Connection connection;
    private PreparedStatement updateStatement;
    private PreparedStatement integrateStatement;
    PreparedStatement delRecordInWordList;


    private Stage stage;
    private int index;
    private DatabaseContentTable table;
    private static IntegerProperty material_id = new SimpleIntegerProperty(-1);
    private String mapString;

    public static void setMaterial_id(int id){
        material_id.setValue(id);
    }

    public static int getMaterial_id(){
        return material_id.getValue();
    }

    public WordDetailEditorWindowController(){
        connection = DBConnection.establishMaterialRepoConnection();

        try {
            connection.setAutoCommit(false);

        } catch (SQLException e) {
            e.printStackTrace();
        }


        material_id.addListener((observable, oldValue, newValue) -> {
            System.out.println("Enter Table words_" + newValue);
            if(newValue.intValue() != 0) {
                try {
                    updateStatement = connection.prepareStatement(UPDATE_STATEMENT_SQL_HEAD + newValue + UPDATE_STATEMENT_SQL_TAIL);
                    integrateStatement = connection.prepareStatement(INTEGRATE_STATEMENT_SQL_HEAD + newValue + INTEGRATE_STATEMENT_SQL_TAIL);
                    delRecordInWordList = connection.prepareStatement(DELETE_FROM_WORD_LIST_SQL_HEAD + newValue + DELETE_FROM_WORD_LIST_SQL_TAIL);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public WordDetailEditorWindowController setIndex(int index){
        this.index = index;
        return this;
    }

    public WordDetailEditorWindowController setPopup(Stage stage){
        this.stage = stage;
        return this;
    }

    public void toLowercaseAction(){
        word_text_field.setText(originalWord.toLowerCase());
        if(!originalWord.equals(word_text_field.getText()))
            word_id_text_field.setText(Integer.toString(getWordID(word_text_field.getText())));
    }

    public void submitAction(){
        if(originalWord.equals(word_text_field.getText()) && lowercaseOriginalWord.equals(lowercase_form_text_field.getText())){
            cancelAction();
        } else {
            if(!originalWord.equals(word_text_field.getText()))
                word_id_text_field.setText(Integer.toString(getWordID(word_text_field.getText())));
            if(!lowercaseOriginalWord.equals(lowercase_form_text_field.getText()))
                lowercase_form_id_text_field.setText(Integer.toString(getWordID(lowercase_form_text_field.getText())));

            String wordIdentity = word_text_field.getText();
            int word_id = Integer.valueOf(word_id_text_field.getText());
            try (PreparedStatement checkExistence = connection.prepareStatement(CHECK_EXISTENCE_IN_WORDS_SQL_HEAD + material_id.get() + CHECK_EXISTENCE_IN_WORDS_SQL_TAIL)) {
                checkExistence.setString(1, wordIdentity);
                ResultSet subWordListExistence = checkExistence.executeQuery();

                if (subWordListExistence.isClosed()) {
                    //不存在，直接更改
                    System.out.println("Does no Exist in word list");
                    updateStatement.setString(1, word_text_field.getText());
                    updateStatement.setInt(2, Integer.valueOf(word_id_text_field.getText()));
                    updateStatement.setString(3, lowercase_form_text_field.getText());
                    updateStatement.setInt(4, Integer.valueOf(lowercase_form_id_text_field.getText()));
                    updateStatement.setString(5, originalWord);

                    updateStatement.executeUpdate();

                } else {
                    //存在，更新后删除原纪录
                    System.out.println("Does Exist in word list");
                    int serial = subWordListExistence.getInt(1);
                    WordDetailMember member = (WordDetailMember)table.getItems().get(serial - 1);
                    System.out.println(member + ": " + member.getOccurrence_number());


                    System.out.println(member.getOccurrence_number() + Integer.valueOf(occurrence_times_text_field.getText()));
                    member.setOccurrence_number(member.getOccurrence_number() + Integer.valueOf(occurrence_times_text_field.getText()));
                    TreeMap<Integer, TreeSet<String>> targetMap = JSON.parseObject(member.getMapString(), new TypeReference<>() {});
                    TreeMap<Integer, TreeSet<String>> insertMap = JSON.parseObject(mapString, new TypeReference<>() {});
                    insertMap.forEach(targetMap::putIfAbsent);
                    member.setMapString(JSON.toJSONString(targetMap));


                    integrateStatement.setInt(1, member.getOccurrence_number());
                    integrateStatement.setString(2, member.getMapString());
                    integrateStatement.setInt(3, serial);

                    integrateStatement.executeUpdate();

                    checkExistence.setString(1, originalWord);
                    ResultSet originalWordExistence = checkExistence.executeQuery();
                    if(!originalWordExistence.isClosed()){
                        int delSerial = originalWordExistence.getInt(1);
                        delRecordInWordList.setInt(1, delSerial);
                        delRecordInWordList.executeUpdate();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            try(PreparedStatement fetchSentMapFromTotalWordTable = connection.prepareStatement(FETCH_SENT_MAP_FROM_TOTAL_WORD_TABLE_SQL)){
                fetchSentMapFromTotalWordTable.setString(1, word_text_field.getText());
                ResultSet targetSentMapResult = fetchSentMapFromTotalWordTable.executeQuery();

                if(targetSentMapResult.isClosed()){
                    //总表也不存在 直接修改
                    try(PreparedStatement updateInfoInTotalWord = connection.prepareStatement(INSERT_NEW_WORDS_TO_TOTAL_WORD_SQL)){
                        updateInfoInTotalWord.setString(1, wordIdentity);
                        updateInfoInTotalWord.setInt(2, word_id);
                        updateInfoInTotalWord.setString(3, originalWord);

                        updateInfoInTotalWord.executeUpdate();
                    }
                } else {
                    //总表内存在 插入句子
                    PreparedStatement updateSentMapToTotalWordTable = connection.prepareStatement(UPDATE_SENT_MAP_IN_TOTAL_WORD_TABLE_SQL);

                    int id = targetSentMapResult.getInt(1);

                    JSONObject totalWordSentJsonObject = JSONObject.parseObject(targetSentMapResult.getString(2));

                    Object wordListObject = totalWordSentJsonObject.remove(Integer.toString(material_id.getValue()));

                    if(wordListObject != null && !wordListObject.equals("")){
                        TreeMap<Integer, TreeSet<String>> insertMap = JSON.parseObject(mapString, new TypeReference<>() {});

                        TreeMap<Integer, TreeSet<String>> wordListMap = JSON.parseObject((String)(wordListObject), new TypeReference<>() {});

                        insertMap.forEach(wordListMap::putIfAbsent);
                        totalWordSentJsonObject.put(Integer.toString(material_id.getValue()), JSON.toJSONString(wordListMap));
                        updateSentMapToTotalWordTable.setString(1, JSON.toJSONString(totalWordSentJsonObject));
                        updateSentMapToTotalWordTable.setInt(2, id);

                        updateSentMapToTotalWordTable.executeUpdate();
                    }

                    //删除原总表句子
                    fetchSentMapFromTotalWordTable.setString(1, originalWord);
                    ResultSet originalWordSentMapResult = fetchSentMapFromTotalWordTable.executeQuery();
                    if(!originalWordSentMapResult.isClosed()){
                        int wordId = targetSentMapResult.getInt(1);

                        JSONObject object = JSONObject.parseObject(targetSentMapResult.getString(2));
                        object = object.fluentRemove(Integer.toString(material_id.get()));
                        if(object.isEmpty()) {
                            //删除记录，如果为空
                            PreparedStatement deleteStatement = connection.prepareStatement(DELETE_EMPTY_RECORD_IN_TOTAL_WORD_SQL);
                            deleteStatement.setInt(1, wordId);
                            deleteStatement.executeUpdate();
                        } else {
                            updateSentMapToTotalWordTable.setString(1, JSON.toJSONString(object));
                            updateSentMapToTotalWordTable.setInt(2, id);

                            updateSentMapToTotalWordTable.executeUpdate();
                        }
                    }

                }

                connection.commit();
                table.updateWordDetailTable(material_id.get());
                stage.hide();
                System.out.println("Successfully updated");
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
    }

    public void deleteAction(){
        table.getItems().remove(index);
        try(PreparedStatement deleteStatement = connection.prepareStatement(HIDE_STATEMENT_SQL)){
            deleteStatement.setInt(1, 0);
            deleteStatement.setString(2, originalWord);
            deleteStatement.executeUpdate();
            table.updateWordDetailTable(material_id.getValue());
            connection.commit();
            stage.hide();
            System.out.println("Delete Successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancelAction(){
        Platform.runLater(() -> stage.hide());
    }

    public void setWord_text_fieldText(String word){
        word_text_field.setText(word);
        originalWord = word;
    }

    public void setWord_id_text_fieldText(int id) {
        word_id_text_field.setText(Integer.toString(id));
    }

    public void setLowercase_form_text_fieldText(String text){
        lowercase_form_text_field.setText(text);
        lowercaseOriginalWord = text;
    }

    public void setLowercase_form_id_text_fieldText(int id){
        lowercase_form_id_text_field.setText(Integer.toString(id));
    }

    public void setOccurrence_times_text_fieldText(int times){
        occurrence_times_text_field.setText(Integer.toString(times));
    }

    public void setSentence_text_areaText(String mapString){
        this.mapString = mapString;
        TreeMap<Integer, TreeSet<String>> sentenceMap = JSON.parseObject(mapString, new TypeReference<>() {});
        StringBuilder sentBuilder = new StringBuilder();
        for(Map.Entry<Integer, TreeSet<String>> entry : sentenceMap.entrySet()){
            sentBuilder.append(entry.getKey()).append(": ").append(entry.getValue().pollFirst()).append("\n\n");
        }

        sentence_text_area.setText(sentBuilder.toString());
    }

    public void setTable(DatabaseContentTable table){
        this.table = table;
    }

    private int getWordID(String word){
        String tempWord = word;
        String firstLetter;

        firstLetter = String.valueOf(tempWord.toLowerCase().charAt(0));
        firstLetter = FIRST_LETTER_PATTERN.matcher(firstLetter).find() ? firstLetter : "spec_char";

        try(ResultSet resultSet = ClipboardFunctionQuery.alternativeCheck(firstLetter, tempWord)) {
            if(!resultSet.isClosed()){
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return NOT_FOUND_ID;
    }

}
