package me.flickersoul.dawn.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.CacheAudio;
import me.flickersoul.dawn.functions.CacheProcessWithIndication;
import me.flickersoul.dawn.functions.DBConnection;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

public class ProgressingAnimationController {
    private static final String GET_DIR_FROM_DB_SQL = "SELECT audio_dir FROM total_word WHERE has_audio=2";

    private static ExecutorService processThread = Executors.newFixedThreadPool(2, runnable ->{
        Thread thread = new Thread(runnable, "Processing Start Thread");
        thread.setDaemon(true);
        return thread;
    });

    @FXML
    private ProgressBar progress_bar;
    @FXML
    private ProgressIndicator progress_indicator;
    @FXML
    private Text changeable_text;
    @FXML
    private Text loading_text;
    @FXML
    private Button download_button;
    @FXML
    private Button close_button;
    @FXML
    private HBox button_box;

    private class WrappedIntegerProperty extends SimpleDoubleProperty{
        private boolean isReusing;

        public WrappedIntegerProperty(double initNum){
            super(initNum);
        }

        public void reset(){
            this.isReusing = true;
            super.setValue(0.0);
            this.isReusing = false;
        }

        public boolean isReusing(){
            return isReusing;
        }
    }

    private WrappedIntegerProperty progressProperty = new WrappedIntegerProperty(0.00);

    private Stage animationStage;

    private Future closeFuture;

    private boolean start = true;

    class CloseThread implements Runnable{
        int seconds;

        public CloseThread(int seconds){
            this.seconds = seconds;
        }

        @Override
        public void run() {
            loading_text.setVisible(false);
            button_box.setVisible(true);
            download_button.setDisable(true);

            try {
                sleep(1000);

                for(int i = seconds; i > 0; i--){
                    changeable_text.setText("Closing in " + i + " second(s)");
                    sleep(1000);
                }

                Platform.runLater(() -> animationStage.close());
            } catch (InterruptedException e) {
                System.err.println("Close Thread Interrupted");
            }
        }
    }

    class CloseAndCacheAudioThread implements Runnable{

        @Override
        public void run() {
            loading_text.setVisible(false);
            button_box.setVisible(true);

            try {
                sleep(1000);

                for(int i = 3; i > 0; i--){
                    changeable_text.setText("Closing in " + i + " second(s), and start cache audios");
                    sleep(1000);
                }
            } catch (InterruptedException e) {
                System.err.println("Close Thread Interrupted");
            }

            processThread.execute(new CacheAudioThread());
        }
    }

    class CacheAudioThread implements Runnable{
        @Override
        public void run() {

            startCachingAudio();
        }
    }

    public ProgressingAnimationController setAnimationStage(Stage stage){
        animationStage = stage;
        animationStage.setAlwaysOnTop(ClipboardOnly.getAlwaysOnStatus());
        animationStage.setOnCloseRequest(event -> {
            if(closeFuture != null){
                closeFuture.cancel(true);
            }
        });

        close_button.setOnMouseClicked(mouseEvent -> Platform.runLater(() -> animationStage.close()));
        download_button.setOnMouseClicked(mouseEvent -> processThread.execute(new CacheAudioThread()));

        return this;
    }

    public ProgressingAnimationController(){
        progressProperty.addListener((observable, oldValue, newValue) -> {
            double newV = newValue.doubleValue();
            double oldV = oldValue.doubleValue();

            if((newV <= oldV || newV > 1) && !progressProperty.isReusing()){
                progressProperty.set(oldV);
                return;
            }

            Platform.runLater(() -> {
                progress_bar.setProgress(newV);
                progress_indicator.setProgress(newV);
            });

            if(newV == 1){
                ImportPaneController.setCloseable(true);
                changeable_text.setVisible(true);
                if (start){
                    closeFuture = processThread.submit(new CloseAndCacheAudioThread());
                    start = false;
                }
                System.gc();
            }
        });
    }

    public void setProgress(double progress){
        progressProperty.setValue(progress);
    }

    public static void closeThread(){
        processThread.shutdown();
    }

    private void reuseStage(){
        button_box.setVisible(false);
        progressProperty.reset();
        loading_text.setVisible(true);
    }

    private void startCachingAudio(){

        reuseStage();

        CacheProcessWithIndication process = new CacheProcessWithIndication(this);

        Set<Callable<Boolean>> set = new HashSet<>();


        try(Connection connection = DBConnection.establishMaterialRepoConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(GET_DIR_FROM_DB_SQL)){
            while(resultSet.next()){
                set.add(process.composeThread(resultSet.getString(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int size = set.size();
        if(size != 0) {
            process.setTotalNum(size);

            CacheAudio.cacheBatchAudioFromWordDirWithFuture(set);

            process.commitChange();

            changeable_text.setText("Download Complete");

            closeFuture = processThread.submit(new CloseThread(3));

        } else {
            closeFuture = processThread.submit(new CloseThread(2));
        }
    }

    public void setChangableText(String text){
        Platform.runLater(() -> changeable_text.setText(text));
    }
}
