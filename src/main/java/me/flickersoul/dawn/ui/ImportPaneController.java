package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.SentenceProcessingQuery;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Thread.sleep;

public class ImportPaneController {
    private static ExecutorService processThread = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "Processing Start Thread"));

    protected static void shutdownProcessThread(){
        processThread.shutdown();
    }

    @FXML
    private TextField file_import_text_field;
    @FXML
    private TextField url_import_text_field;
    @FXML
    private TextArea text_import_text_area;
    @FXML
    private TabPane content_tabPane;

    private Stage animationStage = new Stage();

    private Stage openStage;

    private static BooleanProperty closeable = new SimpleBooleanProperty(false);

    private Future fileChooseFuture;

    public ImportPaneController(){
        animationStage.setResizable(false);
        animationStage.initModality(Modality.APPLICATION_MODAL);
        animationStage.setTitle("Processing");

        animationStage.setOnCloseRequest(event -> {
            if (closeable.getValue()) {
                closeable.setValue(false);
            } else {
                event.consume();
            }
        });
    }

    public void initEvents(){
        file_import_text_field.setOnKeyReleased(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER){
                String filePath = file_import_text_field.getText();
                if(filePath != null && !filePath.trim().equals("")) {
                    processFile();
                } else {
                    openFileChooser();
                }
            }
        });

        url_import_text_field.setOnKeyReleased(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER){
                processURL();
            }
        });

        text_import_text_area.setOnKeyReleased(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ENTER){
                processText();
            }
        });

        content_tabPane.setOnKeyReleased(keyEvent -> {
            if(new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN).match(keyEvent)){
                content_tabPane.getSelectionModel().selectPrevious();
            } else if(new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN).match(keyEvent)){
                content_tabPane.getSelectionModel().selectNext();
            }
        });
    }

    public void setStage(Stage stage) {
        openStage = stage;
    }

    class FileChooserThread implements Runnable {
        @Override
        public void run() {
            Platform.runLater(() -> {
                File file = FileChooserDriver.getFile(openStage);

                if(file == null || !file.canRead() || !file.canWrite()){
                    AlertBox.displayError("Input Error", "You Seem To Choose Nothing", "Please Select A File!");
                    return;
                }

                file_import_text_field.setText(file.getPath());
            });
        }
    }

    public void openFileChooser(){
        if(fileChooseFuture != null && !fileChooseFuture.isDone())
            fileChooseFuture.cancel(false);
        else
            fileChooseFuture = processThread.submit(new FileChooserThread());
    }

    public void processFile(){
        String filePath = file_import_text_field.getText();
        if(filePath == null || filePath.trim().equals("")){
            AlertBox.displayError("Input Error", "You Seem To Choose Nothing", "Please Select A File!");
            return;
        }

        File file = new File(filePath);
        if(file.canRead() && file.getPath().endsWith(".txt")){
            try {
                processThread.submit(new SentenceProcessingQuery(file, startProcessingAnimation().setAnimationStage(animationStage)));
            } catch (IOException e) {
                AlertBox.displayError("Component Loading Error", "The Critical Component Cannot Be Loaded", "Please Contact The Stupid Developer");
                e.printStackTrace();
            }
        } else {
            AlertBox.displayError("Read Error", "The File You Choose Is Not Readable", "Please Select Another File!");
        }
    }

    public void processURL(){
        String path = url_import_text_field.getText();
        if(path == null || path.equals("")){
            AlertBox.displayError("Input Error", "You Seem To Input Nothing", "Please Input A URL!");
            return;
        }
        URL url = null;

        try {
            url = new URL(path);
        } catch (MalformedURLException e) {
            AlertBox.displayError("Input Error", "The URL Is Malformed", "Please Input A Valid URL!");
            e.printStackTrace();
        }

        try {
            if(url != null)
                processThread.submit(new SentenceProcessingQuery(url, startProcessingAnimation().setAnimationStage(animationStage)));
            else
                AlertBox.displayError("Input Error", "The URL Is Malformed", "Please Input A Valid URL!");

        } catch (IOException e) {
            e.printStackTrace();
            AlertBox.displayError("Component Loading Error", "The Critical Component Cannot Be Loaded", "Please Contact The Stupid Developer");
        }
    }

    public void processText(){
        String text = text_import_text_area.getText();

        if(text == null || text.equals("")){
            AlertBox.displayError("Input Error", "You Seem To Input Nothing", "Please Input A Piece Of Text!");
            return;
        }

        try {
            processThread.submit(new SentenceProcessingQuery(text, startProcessingAnimation().setAnimationStage(animationStage)));
        } catch (IOException e) {
            e.printStackTrace();
            AlertBox.displayError("Component Loading Error", "The Critical Component Cannot Be Loaded", "Please Contact The Stupid Developer");
        }
    }

    private ProgressingAnimationController startProcessingAnimation() throws IOException {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/css/ProcessingAnimation.fxml"));
        animationStage.setScene(new Scene(loader.load()));

        animationStage.show();

        return loader.getController();
    }

    protected static void setCloseable(boolean sign){
        closeable.setValue(sign);
    }
}
