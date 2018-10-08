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
import javafx.scene.image.Image;
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
import java.net.*;

import static java.lang.Thread.sleep;

public class ClipboardOnly extends Application implements ClipboardOwner {
    private static SimpleBooleanProperty isIconified = new SimpleBooleanProperty(false);
    private static SimpleBooleanProperty isFocused = new SimpleBooleanProperty(true);
    private static BooleanProperty isListening = new SimpleBooleanProperty(true);
    private final KeyCombination previousWordCom = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
    private final KeyCombination latterWordCom = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
    private final KeyCombination enTab = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination chTab = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination thTab = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination scTab = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination playAudio = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    private String lastWord = "";
    private String tempWord;
    private boolean isImportedFromKindle = false;
    private boolean isMultiCopyingAllowed = true;

    private ToolBar topToolBar;
    private ClipboardSearchBar clipboardSearchBar;
    private BorderPane borderPane;
    private ClipboardPane clipboardPane;
    private IOSButton isAlwaysOn;
    private IOSButton isListenerOn;
    private IOSButton isAutoPlaying;
    private IOSButton isReadingFromKindle;

    public static void main(String[] args){
        try{
            ServerSocket socket = new ServerSocket(7590, 10, InetAddress.getByAddress(new byte[] {127, 0, 0, 1}));
        }catch (BindException e){
            e.printStackTrace();
            System.exit(7);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(7);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(7);
        }

        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "1080");

        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "1080");

        Application.launch(args);

        System.out.println("Application Exited");
    }

    @Override
    public void start(Stage primaryStage) {
        this.initClipboardPane(primaryStage);
    }

    private void initClipboardPane(Stage clipboardStage){
        topToolBar = new ToolBar();
        clipboardSearchBar = new ClipboardSearchBar();
        borderPane = new BorderPane();
        clipboardPane = new ClipboardPane();
        isAlwaysOn = new IOSButton(30, false, "Whether Always On Top");
        isListenerOn = new IOSButton(30, true, "Whether Listen To Clipboard");
        isAutoPlaying = new IOSButton(30, true, "Whether Auto Play Audio");
        isReadingFromKindle = new IOSButton(30, false, "Whether Toggle Kindle Mode");

        isAlwaysOn.getSwitchOn().addListener((observable, oldValue, newValue) -> clipboardStage.setAlwaysOnTop(newValue));
        isListenerOn.getSwitchOn().addListener((observable, oldValue, newValue) -> {
            if(newValue)
                isListening.setValue(true);
            else
                isListening.setValue(false);
            isFocused.setValue(!isFocused.getValue());
        });
        isAutoPlaying.getSwitchOn().addListener((observable, oldValue, newValue) -> ClipboardFunctionQuery.setAutoPlaying(newValue));
        isReadingFromKindle.getSwitchOn().addListener(((observable, oldValue, newValue) -> isImportedFromKindle = newValue));

        topToolBar.setOrientation(Orientation.HORIZONTAL);
        topToolBar.setPadding(new Insets(5, 5, 5, 5));
        topToolBar.getItems().addAll(isAlwaysOn, isListenerOn, isAutoPlaying, isReadingFromKindle, clipboardSearchBar);

        Scene scene = new Scene(borderPane, 370, 460);
        borderPane.setCenter(clipboardPane);
        borderPane.setTop(topToolBar);

        isIconified.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> clipboardStage.setIconified(newValue));
            if(newValue){
                System.out.println("min");
            }else {
                System.out.println("focus");
                Platform.runLater(() -> clipboardStage.requestFocus());
            }
        });

        isFocused.addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                Platform.runLater(() -> {
                    if (!clipboardStage.isAlwaysOnTop()) {
                        clipboardStage.setAlwaysOnTop(true);
                        clipboardStage.setAlwaysOnTop(false);
                    }
                });
            }else{
                isIconified.setValue(true);
            }
        });

        isListening.addListener((ob, oldV, newV) -> {
            if(newV) {
                this.regainOwnership(sysClip.getContents(this));
                System.out.println("Start Listening");
            }
            else
                System.out.println("Postponed Listening...");
        });

        clipboardStage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            isIconified.setValue(newValue);
        });

        clipboardStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                clipboardSearchBar.requestSearchBoxFocused();
            }else {
                isFocused.setValue(false);
            }
        });

        Provider provider = Provider.getCurrentProvider(false);
        provider.register(KeyStroke.getKeyStroke("ctrl alt shift O"), hotKey -> {
            if(clipboardStage.isIconified()) {
                isIconified.setValue(false);
            }
            else if(!clipboardStage.isFocused())
                Platform.runLater(() -> clipboardStage.requestFocus());
            else {
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
                isIconified.setValue(true);
            }
        });

        scene.setOnKeyReleased(event -> {
            if(playAudio.match(event)){
                JSPlay.autoPlay();
            }else if(previousWordCom.match(event)){
                HistoryArray.getPreviousWord();
            }else if(latterWordCom.match(event)){
                HistoryArray.getLatterWord();
            }else if(enTab.match(event)){
                ClipboardPane.setTabSignValue(ClipboardPane.EN_TAB_NUM);
            }else if(chTab.match(event)){
                ClipboardPane.setTabSignValue(ClipboardPane.CH_TAB_NUM);
            }else if(thTab.match(event)){
                ClipboardPane.setTabSignValue(ClipboardPane.TH_TAB_NUM);
            }else if(scTab.match(event)){
                HistoryArray.lookCurrentWordInGoogle();
            }
        });

        Transferable trans = sysClip.getContents(this);
        sysClip.setContents(trans, this);
        System.out.println("Listening to board...");

        clipboardSearchBar.requestSearchBoxFocused();

        clipboardStage.setTitle("Dawn: Clipboard Listener");
        clipboardStage.getIcons().add(new Image(this.getClass().getClassLoader().getResource("icon/ico3.png").toExternalForm()));
        clipboardStage.setScene(scene);
        clipboardStage.show();
        //sense set
    }

    @Override
    public void lostOwnership(Clipboard c, Transferable t) {
        if(!isListening.getValue())return;
        try{
            sleep(150);
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

    private void processContents(Transferable t) {
        if(t.isDataFlavorSupported(DataFlavor.stringFlavor)) {

            Object object = "";

            try {
                object = t.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            tempWord = object.toString();

            if(isImportedFromKindle){
                char[] tempChar = tempWord.toCharArray();
                if(tempWord.indexOf(10) >= 0)
                    tempWord = String.copyValueOf(tempChar, 0, tempWord.indexOf(10));
            }

            if(isMultiCopyingAllowed || !tempWord.equals(lastWord)) {

                if (ClipboardFunctionQuery.lookupWord(tempWord)) {
                    HistoryArray.putSearchResult(tempWord);
                    System.out.println("Got Word");
                    isIconified.setValue(false);
                    isFocused.setValue(!isFocused.getValue());
                }else{
                    HistoryArray.setEmptyFlagTrue();
                }

                lastWord = tempWord;
            }
        }
    }

    private void regainOwnership(Transferable t) {
        sysClip.setContents(t, this);
        processContents(t);
    }
}