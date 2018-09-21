package me.flickersoul.dawn.ui;

import com.tulskiy.keymaster.common.Provider;
import javafx.beans.property.BooleanProperty;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.HistoryArray;
import me.flickersoul.dawn.functions.JSPlay;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

import static java.lang.Thread.onSpinWait;
import static java.lang.Thread.sleep;

public class ClipboardOnly extends Application implements ClipboardOwner {
    public static SimpleBooleanProperty isIconified = new SimpleBooleanProperty(false);
    public static SimpleBooleanProperty isFocused = new SimpleBooleanProperty(true);
    public static BooleanProperty isListening = new SimpleBooleanProperty(true);
    private final KeyCombination priviousWordCom = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
    private final KeyCombination latterWordCom = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
    private final KeyCombination enTab = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination chTab = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination thTab = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination playAudio = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

    private String lastWord = "";

    ToolBar topToolBar;
    ClipboardSearchBar clipboardSearchBar;
    BorderPane borderPane;
    ClipboardPane clipboardPane;
    IOSButton isAlwaysOn;
    IOSButton isListenerOn;
    IOSButton isAutoPlaying;

    String tempWord;

    private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.initClipboardPane(primaryStage);
    }

    protected void initClipboardPane(Stage clipboardStage){
        topToolBar = new ToolBar();
        clipboardSearchBar = new ClipboardSearchBar();
        borderPane = new BorderPane();
        clipboardPane = new ClipboardPane();
        isAlwaysOn = new IOSButton(30, false);
        isListenerOn = new IOSButton(30, true);
        isAutoPlaying = new IOSButton(30, true);

        isAlwaysOn.switchOn.addListener((observable, oldValue, newValue) -> clipboardStage.setAlwaysOnTop(newValue));
        isListenerOn.switchOn.addListener((observable, oldValue, newValue) -> {
            if(newValue)
                isListening.setValue(true);
            else
                isListening.setValue(false);
            isFocused.setValue(!isFocused.getValue());
        });
        isAutoPlaying.switchOn.addListener((observable, oldValue, newValue) -> ClipboardFunctionQuery.setAutoPlaying(newValue));

        topToolBar.setOrientation(Orientation.HORIZONTAL);
        topToolBar.setPadding(new Insets(5, 5, 5, 5));
        topToolBar.getItems().addAll(isAlwaysOn, isListenerOn, isAutoPlaying, clipboardSearchBar);

        Scene scene = new Scene(borderPane, 360, 450);
        borderPane.setCenter(clipboardPane);
        borderPane.setTop(topToolBar);

        isIconified.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                clipboardStage.setIconified(newValue);
            });
            if(newValue){
                System.out.println("min");
            }else {
                System.out.println("focus");
                Platform.runLater(() -> {
                    clipboardStage.requestFocus();
                });
            }
        });

        isFocused.addListener(observable -> {
            if(!clipboardStage.isFocused())
                Platform.runLater(() -> {
                    clipboardStage.setAlwaysOnTop(true);
                    clipboardStage.setAlwaysOnTop(false);
                    clipboardStage.requestFocus();
                });
        });

        isListening.addListener((ob, oldV, newV) -> {
            if(newV)
                System.out.println("Start Listening");
            else
                System.out.println("Postponed Listening...");
        });

        clipboardStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                clipboardSearchBar.requestSearchBoxFocused();
            }
        });

        Provider provider = Provider.getCurrentProvider(false);
        provider.register(KeyStroke.getKeyStroke("ctrl alt shift P"), hotKey -> {
            if(clipboardStage.isIconified()) {
                isIconified.setValue(true);
                isIconified.setValue(false);
            }
            else if(!clipboardStage.isFocused())
                Platform.runLater(() -> clipboardStage.requestFocus());
            else {
                isIconified.setValue(false);
                isIconified.setValue(true);
            }
        });


        clipboardStage.setOnCloseRequest(e -> {
            provider.stop();
            JSPlay.terminatePool();
            ClipboardFunctionQuery.terminatePool();
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if(event.getCode() == KeyCode.ESCAPE){
                isIconified.setValue(false);
                isIconified.setValue(true);
            }
        });

        scene.setOnKeyReleased(event -> {
            if(playAudio.match(event)){
                JSPlay.autoPlay();
            }else if(priviousWordCom.match(event)){
                HistoryArray.getPreviousWord();
            }else if(latterWordCom.match(event)){
                HistoryArray.getLatterWord();
            }else if(enTab.match(event)){
                clipboardPane.focusOnEn();
            }else if(chTab.match(event)){
                clipboardPane.focusOnCh();
            }else if(thTab.match(event)){
                clipboardPane.focusOnTh();
            }
        });

        Transferable trans = sysClip.getContents(this);
        sysClip.setContents(trans, this);
        System.out.println("Listening to board...");

        clipboardStage.setScene(scene);
        clipboardStage.show();
        //sense set
    }

    @Override
    public void lostOwnership(Clipboard c, Transferable t) {
        try{
            sleep(200);
        }catch(Exception e){
            System.out.println("Exception: "+e);
        }
        try{
            Transferable contents = c.getContents(this);
            regainOwnership(contents);
        }catch(Exception e){
            try {
                sleep(300);
                this.regainOwnership(c.getContents(this));
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }catch (Exception ex){
                ex.printStackTrace();
                Platform.exit();
            }
            e.printStackTrace();
        }
    }

    void processContents(Transferable t) {
        if(!isListening.getValue())return;
        if(t.isDataFlavorSupported(DataFlavor.stringFlavor)) {

            Object object = null;

            try {
                object = t.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            tempWord = object.toString();

            if(!tempWord.equals(lastWord)) {

                ClipboardSearchBar.setText(tempWord);

                if (ClipboardFunctionQuery.lookupWord(tempWord)) {
                    HistoryArray.putSearchResult(tempWord);
                    System.out.println("Got Word");
                    isIconified.setValue(false);
                    isFocused.setValue(!isFocused.getValue());
                }

                lastWord = tempWord;
            }
        }
    }

    void regainOwnership(Transferable t) {
        sysClip.setContents(t, this);
        processContents(t);
    }
}
