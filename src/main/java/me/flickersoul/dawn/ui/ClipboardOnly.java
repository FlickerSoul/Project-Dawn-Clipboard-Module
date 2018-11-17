package me.flickersoul.dawn.ui;

import com.tulskiy.keymaster.common.Provider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;
import me.flickersoul.dawn.functions.HistoryArray;
import me.flickersoul.dawn.functions.JSPlay;
import me.flickersoul.dawn.functions.UIProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.datatransfer.Clipboard;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import static java.lang.Thread.sleep;

public class ClipboardOnly extends Application implements ClipboardOwner {
    private static UIProperty isIconified = new UIProperty(false);
    private static UIProperty isFocused = new UIProperty(true);
    private static BooleanProperty isListening = new SimpleBooleanProperty(true);
    private final KeyCombination previousWordCom = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
    private final KeyCombination latterWordCom = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
    private final KeyCombination enTab = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination chTab = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination thTab = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination scTab = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination playAudio = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    SimpleDoubleProperty clickStartPointSX = new SimpleDoubleProperty();
    SimpleDoubleProperty clickStartPointSY = new SimpleDoubleProperty();
    SimpleDoubleProperty windowStartPointX = new SimpleDoubleProperty();
    SimpleDoubleProperty windowStartPointY = new SimpleDoubleProperty();

    private String lastWord = "";
    private String tempWord;
    private boolean isImportedFromKindle = false;
    private boolean isMultiCopyingAllowed = true;
    private boolean isFirstSwitched = false;

    private ToolBar topToolBar;
    private ClipboardSearchBar clipboardSearchBar;
    private BorderPane borderPane;
    private BorderPane outerBorderPane;
    private ClipboardPane clipboardPane;
    private IOSButton isAlwaysOn;
    private IOSButton isListenerOn;
    private IOSButton isAutoPlaying;
    private IOSButton isReadingFromKindle;
    private HBox menuBar;

    private static Provider provider;

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

        provider.stop();
        JSPlay.terminatePool();
        ClipboardFunctionQuery.terminatePool();

        System.out.println("Application Exited");
    }

    @Override
    public void start(Stage primaryStage) {
        this.initClipboardPane(primaryStage);
    }

    private void initClipboardPane(Stage clipboardStage){
        clipboardStage.initStyle(StageStyle.UNDECORATED);
        clipboardStage.setTitle("Dawn: Clipboard Listener");
        clipboardStage.getIcons().add(new Image(this.getClass().getClassLoader().getResource("icon/ico3.png").toExternalForm()));

        Button minButton = new Button("Iconify");
        Button hideButton = new Button("Close");

        topToolBar = new ToolBar();
        clipboardSearchBar = new ClipboardSearchBar();
        borderPane = new BorderPane();
        outerBorderPane = new BorderPane();
        clipboardPane = new ClipboardPane();
        menuBar = new HBox();
        isAlwaysOn = new IOSButton(30, false, "Whether Always On Top");
        isListenerOn = new IOSButton(30, true, "Whether Listen To Clipboard");
        isAutoPlaying = new IOSButton(30, true, "Whether Auto Play Audio");
        isReadingFromKindle = new IOSButton(30, false, "Whether Toggle Kindle Mode");

        isAlwaysOn.getSwitchOn().addListener((observable, oldValue, newValue) -> clipboardStage.setAlwaysOnTop(newValue));
        isListenerOn.getSwitchOn().addListener((observable, oldValue, newValue) -> {
            if(newValue)
                isListening.setValue(true);
            else{
                isListening.setValue(false);
                isFirstSwitched = true;
            }
        });
        isAutoPlaying.getSwitchOn().addListener((observable, oldValue, newValue) -> ClipboardFunctionQuery.setAutoPlaying(newValue));
        isReadingFromKindle.getSwitchOn().addListener(((observable, oldValue, newValue) -> isImportedFromKindle = newValue));

        minButton.setCursor(Cursor.HAND);
        minButton.setOnMouseClicked(event -> {
            this.hideWindow();
            System.out.println("min through button");
        });
        minButton.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> event.consume());

        hideButton.setCursor(Cursor.HAND);
        hideButton.setOnMouseClicked(event -> {
            clipboardStage.hide();
        });
        hideButton.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> event.consume());

        menuBar.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        menuBar.getChildren().addAll(minButton, new Text(clipboardStage.getTitle()), hideButton);
        menuBar.setSpacing(5d);
        menuBar.setMaxHeight(30d);
        menuBar.setAlignment(Pos.CENTER);
        menuBar.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        menuBar.setOnMousePressed(event -> {
            clickStartPointSX.set(event.getScreenX());
            clickStartPointSY.set(event.getScreenY());
            windowStartPointX.set(clipboardStage.getX());
            windowStartPointY.set(clipboardStage.getY());
        });
        menuBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            clipboardStage.setX(windowStartPointX.getValue()  + event.getScreenX() - clickStartPointSX.get());
            clipboardStage.setY(windowStartPointY.getValue()  + event.getScreenY() - clickStartPointSY.get());
        });

        topToolBar.setOrientation(Orientation.HORIZONTAL);
        topToolBar.setPadding(new Insets(5, 5, 5, 5));
        topToolBar.getItems().addAll(isAlwaysOn, isListenerOn, isAutoPlaying, isReadingFromKindle, clipboardSearchBar);

        Scene scene = new Scene(outerBorderPane, 370, 490);
        borderPane.setCenter(clipboardPane);
        borderPane.setTop(topToolBar);
        outerBorderPane.setTop(menuBar);
        outerBorderPane.setCenter(borderPane);

        isListening.addListener((observableValue, oldValue, newValue) -> {
            if(newValue) {
                this.regainOwnership(sysClip.getContents(this));
                System.out.println("Start Listening");
            }
            else
                System.out.println("Postponed Listening...");
        });

        clipboardStage.iconifiedProperty().addListener((observable, oldValue, newValue) -> isIconified.setValue(newValue));
        isIconified.addListener((observable, oldValue, newValue) -> {
            if(isIconified.isNotCallFromBonding()){
                Platform.runLater(() -> clipboardStage.setIconified(newValue));
                if(newValue){
                    System.out.println("minimize window");
                }else {
                    System.out.println("maximize window");
                }
            }
        });

        clipboardStage.focusedProperty().addListener((observable, oldValue, newValue) -> isFocused.setValue(newValue));
        isFocused.addListener((observable, oldValue, newValue) -> {
            if(isFocused.isNotCallFromBonding()){
                Platform.runLater(() -> {
                    clipboardStage.setIconified(true);
                    clipboardStage.setIconified(false);
                });
                System.out.println("focus window");
            }
        });


        provider = Provider.getCurrentProvider(false);
        provider.register(KeyStroke.getKeyStroke("ctrl alt shift O"), hotKey -> { //Cap letter
            if(clipboardStage.isIconified()) {
                this.maximizeWindow();
            }
            else if(!clipboardStage.isFocused()){
                this.focusWindow();
            }else {
                this.hideWindow();
            }
        });

        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if(event.getCode() == KeyCode.ESCAPE){
                this.hideWindow();
                System.out.println("esc");
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

        clipboardStage.setScene(scene);
        clipboardStage.show();

        this.callUI();
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
                    this.callUI();
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

    private void callUI(){
        if(isIconified.getValue()){
            this.maximizeWindow();
            this.focusWindow();
        }else{
            this.focusWindow();
        }
    }

    private void hideWindow(){
        isIconified.setValue(true, true);
    }

    private void maximizeWindow(){
        isIconified.setValue(false, true);
    }

    private void focusWindow(){
        isFocused.setValue(true, true);
    }
}