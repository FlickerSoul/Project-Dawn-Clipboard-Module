package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.AudioPlay;
import me.flickersoul.dawn.functions.CacheAudio;
import me.flickersoul.dawn.functions.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Thread.sleep;

public class DatabaseContentTable extends TableView {
    private static ExecutorService clickThread = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Click Differentiation Thread");
        thread.setDaemon(true);
        return thread;
    });

    private static final int DOUBLE_CLICK_INTERVAL = 230; //ms

    class SingleClickAndDoubleClick implements Runnable {
        private TableCell cell;

        public SingleClickAndDoubleClick(TableCell tableCell){
            cell = tableCell;
        }
        @Override
        public void run() {
            try{
                sleep(DOUBLE_CLICK_INTERVAL);
                //single click event
                DetailTab.loadSentenceMap(((WordDetailMember)cell.getTableView().getSelectionModel().getSelectedItem()).getMapString());
            } catch (InterruptedException e) {
                System.err.println("Double Click Detected");
                //double click event
                openWordEditor(cell);
            }
        }
    }

    private Future clickEventFuture;

    private static final String GET_MATERIAL_TABLE_INFO_SQL = "SELECT id, material_name, description, shown FROM total_material_index";
    private static final String GET_SPECIFIC_WORD_TABLE_SQL_HEAD = "SELECT serial, w.word, w.word_id, w.lowercase_form, w.lowercase_id, w.occurrence_number, w.sentence_info, total.shown, total.known, total.has_audio, total.audio_dir FROM words_"; //total_word_id?
    private static final String GET_SPECIFIC_WORD_TABLE_SQL_TAIL = " AS w INNER JOIN total_word AS total WHERE w.word = total.word AND w.word_id = total.word_id AND total.shown=1";
    private static final String GET_ALL_WORDS_FROM_UNKNOWN_REPO_SQL = "SELECT id, word, word_id, sentence_info, shown, has_audio FROM total_word WHERE known=0";
    private static final String GET_ALL_WORDS_FROM_KNOWN_REPO_SQL = "SELECT id, word, word_id, sentence_info, shown, has_audio FROM total_word WHERE known=1";

    private TableColumn<MaterialIndexMember, Integer> materialIndexTable_Serial;
    private TableColumn<MaterialIndexMember, String> materialIndexTable_Name;
    private ObservableList<TableColumn> materialTableColumns;

    private TableColumn<WordDetailMember, Integer> wordTable_index;
    private TableColumn<WordDetailMember, String> wordTable_word;
    private TableColumn<WordDetailMember, String> wordTable_lowercase;
    private TableColumn<WordDetailMember, Integer> wordTable_occurrenceNum;
    private TableColumn<WordDetailMember, Integer> wordTable_familiar_image; // known //TODO 制作生词标识
    private TableColumn<WordDetailMember, Integer> wordTable_audioPlay_image; //has_audio
    private ObservableList<TableColumn> wordDetailColumns;
    //TODO 第一次出现地方 用正则表达 [?, || [?]

    private ObservableList<TableColumn> reciteTableColumns;

    private Stage stage;
    private FXMLLoader loader;
    private WordDetailEditorWindowController controller;

    public DatabaseContentTable() {
        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);

        stage = new Stage();
        loader = new FXMLLoader(this.getClass().getResource("/css/WordDetailEditorWindow.fxml"));
        try {
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = loader.getController();
        controller.setTable(this);

        this.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if(code == KeyCode.DELETE || code == KeyCode.BACK_SPACE){
                try(Connection connection = DBConnection.establishMaterialRepoConnection();
                    PreparedStatement deleteStatement = connection.prepareStatement(WordDetailEditorWindowController.HIDE_STATEMENT_SQL)){

                    connection.setAutoCommit(false);
                    ObservableList list;
                    System.out.println(list = this.getSelectionModel().getSelectedItems());
                    for(Object object : list){
                        WordDetailMember member = (WordDetailMember)object;
                        System.out.println(member);
                        try {
                            deleteStatement.setInt(1, 0);
                            deleteStatement.setString(2, member.getWord());
                            System.out.println(member.getWord());
                            deleteStatement.addBatch();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    deleteStatement.executeBatch();
                    connection.commit();

                    for(Object o : list){
                        this.getItems().remove(o);
                    }
                    this.refresh();

                    System.out.println("Delete Successfully");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Editor");
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        changeToMaterialColumns();
    }

    private void checkWordColumnExistence(){
        if (wordDetailColumns == null && reciteTableColumns == null) {
            WordTableContextMenu contextMenu = WordTableContextMenu.getWordTableContextMenu();

            class NameFactory extends TableCell {
                @Override
                public void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        this.setWrapText(true);
                        this.setText(item.toString());
                        this.setCursor(Cursor.HAND);
                        this.setOnMouseClicked(event -> {
                            if(event.getButton() == MouseButton.PRIMARY){
                                if(clickEventFuture == null || clickEventFuture.isDone()){
                                    clickEventFuture = clickThread.submit(new SingleClickAndDoubleClick(this));
                                } else {
                                    clickEventFuture.cancel(true);
                                }
                            } else if (event.getButton() == MouseButton.SECONDARY) {
                                contextMenu.show(this, event.getScreenX(), event.getScreenY());
                            } else {
                                contextMenu.hide();
                            }
                        });
                        this.setAlignment(Pos.CENTER);
                    }
                }
            }

            class AudioPictureFactory extends TableCell<WordDetailMember, Integer> {
                @Override
                public void updateItem(Integer item, boolean empty){
                    super.updateItem(item, empty);
                    ImageView imageView;
                    if(!empty && item != null) {
                        this.setAlignment(Pos.CENTER);
                        if (item == 1) {
                            this.setCursor(Cursor.HAND);
                            imageView = new ImageView(new Image(this.getClass().getResource("/icon/play.png").toExternalForm()));
                            this.setOnMouseClicked(event -> {
                                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1){
                                    WordDetailMember member = ((WordDetailMember)getSelectionModel().getSelectedItem());
                                    AudioPlay.playAudioFromDir(member.getAudio_dir(), member.getPrimary_id());
                                }
                            });
                        } else if(item == 2){
                            this.setCursor(Cursor.HAND);
                            imageView = new ImageView(new Image(this.getClass().getResource("/icon/download.png").toExternalForm()));
                            this.setOnMouseClicked(event -> {
                                if(event.getButton() == MouseButton.PRIMARY){
                                    this.setCursor(Cursor.DEFAULT);
                                    if(clickEventFuture != null && !clickEventFuture.isDone()){
                                        clickEventFuture.cancel(true);
                                    }
                                    String dir = ((WordDetailMember)getSelectionModel().getSelectedItem()).getAudio_dir();
                                    clickEventFuture = CacheAudio.submitCacheAudioTask(CacheAudio.composeAudioURL(dir), dir, true);
                                    try {
                                        clickEventFuture.get();
                                        ImageView doneView = new ImageView(new Image(this.getClass().getResource("/icon/play.png").toExternalForm()));
                                        doneView.setFitWidth(15);
                                        doneView.setFitHeight(15);
                                        this.setGraphic(doneView);
                                        this.setCursor(Cursor.HAND);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            this.setCursor(Cursor.DEFAULT);
                            this.setOnMouseClicked(null);
                            imageView = new ImageView(new Image(this.getClass().getResource("/icon/empty.png").toExternalForm()));
                        }
                        imageView.setFitWidth(15);
                        imageView.setFitHeight(15);
                        this.setGraphic(imageView);
                    }
                }
            }

            wordTable_index = new TableColumn<>("No.");
            wordTable_index.setPrefWidth(30d);
            wordTable_index.setCellValueFactory(new PropertyValueFactory<>("serial"));
            wordTable_index.setCellFactory(column -> new NameFactory());

            wordTable_word = new TableColumn<>("Word");
            wordTable_word.setCellValueFactory(new PropertyValueFactory<>("word"));
            wordTable_word.setCellFactory(column -> new NameFactory());

            wordTable_lowercase = new TableColumn<>("Other Form");
            wordTable_lowercase.setCellValueFactory(new PropertyValueFactory<>("lowercase_word"));
            wordTable_lowercase.setCellFactory(column -> new NameFactory());

            wordTable_occurrenceNum = new TableColumn<>("Occurrence Num");
            wordTable_occurrenceNum.setCellValueFactory(new PropertyValueFactory<>("occurrence_number"));
            wordTable_occurrenceNum.setCellFactory(column -> new NameFactory());

            wordTable_audioPlay_image = new TableColumn<>("Audio");
            wordTable_audioPlay_image.setCellValueFactory(new PropertyValueFactory<>("has_audio"));
            wordTable_audioPlay_image.setCellFactory(column -> new AudioPictureFactory());

            wordDetailColumns = FXCollections.observableArrayList(wordTable_index, wordTable_word, wordTable_lowercase, wordTable_occurrenceNum, wordTable_audioPlay_image);
            reciteTableColumns = FXCollections.observableArrayList(wordTable_index, wordTable_word, wordTable_lowercase, wordTable_audioPlay_image);
        }
    }

    public void changeToMaterialColumns() {
        System.gc();
        if(materialTableColumns == null) {
            MaterialTableContextMenu contextMenu = MaterialTableContextMenu.getMaterialTableMenu();
            materialIndexTable_Serial = new TableColumn<>("No.");
            materialIndexTable_Name = new TableColumn<>("Material Name");

            materialIndexTable_Serial.setPrefWidth(30d);
            materialIndexTable_Serial.setCellValueFactory(new PropertyValueFactory<>("serial"));

            materialIndexTable_Name.setPrefWidth(400d);
            materialIndexTable_Name.setCellValueFactory(new PropertyValueFactory<>("material_name"));
            class NameFactory extends TableCell {
                @Override
                public void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        this.setWrapText(true);
                        int id = this.getIndex() + 1;
                        this.setText(item.toString());
                        this.setCursor(Cursor.HAND);
                        this.setOnMouseClicked(event -> {
                            if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                                WordDetailEditorWindowController.setMaterial_id(id);
                                System.out.println("Set ID Done: " + id);
                                ((DatabaseContentTable) this.getTableView()).changeToWordDetailColumns(id);
                            }

                            if (event.getButton() == MouseButton.SECONDARY) {
                                contextMenu.show(this, event.getScreenX(), event.getScreenY());
                            } else {
                                contextMenu.hide();
                            }
                        });
                    }
                }
            }
            materialIndexTable_Name.setCellFactory(column -> new NameFactory());

            materialTableColumns = FXCollections.observableArrayList(materialIndexTable_Serial, materialIndexTable_Name);
        }
        this.getColumns().setAll(materialTableColumns);
        this.updateMaterialTable();
    }

    public void changeToReciteTableColumns(){
        System.gc();
        checkWordColumnExistence();
        this.getColumns().setAll(reciteTableColumns);
        updateReciteTable();
    }

    public void changeToWordDetailColumns(int serial) {
        System.gc();
        checkWordColumnExistence();
        this.getColumns().setAll(wordDetailColumns);
        updateWordDetailTable(serial);
    }

    public void changeToKnownWordsViewColumns(){
        checkWordColumnExistence();
        this.getColumns().setAll(reciteTableColumns);
        updateKnownWordTable();

    }

    protected void updateMaterialTable(){
        ObservableList<MaterialIndexMember> materialIndexMembers = FXCollections.observableArrayList();
        try(Connection connection = DBConnection.establishMaterialRepoConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(GET_MATERIAL_TABLE_INFO_SQL)) {
            while(resultSet.next()){
                materialIndexMembers.add(new MaterialIndexMember(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getInt(4) == 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.getItems().setAll(materialIndexMembers);
        this.refresh();
    }

    //SELECT word, word_id, lowercase_form, lowercase_id, occurrence_number, sentence_info
    protected void updateWordDetailTable(int serial){
        try(Connection connection = DBConnection.establishMaterialRepoConnection();
            Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(GET_SPECIFIC_WORD_TABLE_SQL_HEAD + serial + GET_SPECIFIC_WORD_TABLE_SQL_TAIL);

            ObservableList<WordDetailMember> list = FXCollections.observableArrayList();
            if(resultSet.isClosed()){
                System.err.println("Cannot Not Find The Word Table! Please Check Serial Code!");
            } else {
                int i = 1;
                while (resultSet.next()) {
                    list.add(new WordDetailMember(
                            i++, //serial
                            resultSet.getString(2), //word
                            resultSet.getInt(3), //primary id
                            resultSet.getString(4), //lowercase word
                            resultSet.getInt(5), //lowercase id
                            resultSet.getInt(6), //occurrence number
                            resultSet.getString(7), //mapString
                            resultSet.getInt(8) == 1, // shown
                            resultSet.getInt(9), //known
                            resultSet.getInt(10), //has audio
                            resultSet.getString(11))); //audio_dir
                }
            }

            this.getItems().setAll(list);
            this.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateReciteTable(){
        //TODO 修改当前 html page 用于激活功能，like 播放，生词卡等

        try(Connection connection = DBConnection.establishMaterialRepoConnection();
            Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(GET_ALL_WORDS_FROM_UNKNOWN_REPO_SQL);

            ObservableList<WordDetailMember> reciteWordMember = FXCollections.observableArrayList();

            if(resultSet.isClosed()){
                System.err.println("There Is NOTHING In Unknown Word Repo");
            } else {
                while (resultSet.next()) {
                    reciteWordMember.add(new WordDetailMember(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getString(4), resultSet.getInt(5) == 1, resultSet.getInt(6)));
                }

            }

            this.getItems().setAll(reciteWordMember);
            this.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateKnownWordTable(){
        try(Connection connection = DBConnection.establishMaterialRepoConnection();
            Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(GET_ALL_WORDS_FROM_KNOWN_REPO_SQL);

            ObservableList<WordDetailMember> reciteWordMember = FXCollections.observableArrayList();

            if(resultSet.isClosed()){
                System.err.println("There Is NOTHING In Known Word Repo");
            } else {
                while (resultSet.next()) {
                    reciteWordMember.add(new WordDetailMember(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getString(4), resultSet.getInt(5) == 1, resultSet.getInt(6)));
                }
            }

            this.getItems().setAll(reciteWordMember);
            this.refresh();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void openWordEditor(TableCell node){
        controller.setPopup(stage).setIndex(node.getIndex());
        WordDetailMember member = (WordDetailMember)node.getTableView().getSelectionModel().getSelectedItem();
        controller.setWord_text_fieldText(member.getWord());
        controller.setWord_id_text_fieldText(member.getPrimary_id());
        controller.setLowercase_form_text_fieldText(member.getLowercase_word());
        controller.setLowercase_form_id_text_fieldText(member.getSecondary_id());
        controller.setOccurrence_times_text_fieldText(member.getOccurrence_number());
        controller.setSentence_text_areaText(member.getMapString());

        Platform.runLater(() -> stage.showAndWait());
    }
}