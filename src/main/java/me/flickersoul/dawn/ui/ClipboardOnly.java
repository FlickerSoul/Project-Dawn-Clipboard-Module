package me.flickersoul.dawn.ui;

import com.tulskiy.keymaster.common.Provider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.flickersoul.dawn.functions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static java.lang.Thread.sleep;

public class ClipboardOnly extends Application implements ClipboardOwner {
    public static final String CACHE_DIR = new StringBuilder(System.getProperty("user.home"))
                                                            .append(File.separator )
                                                            .append("Documents")
                                                            .append(File.separator)
                                                            .append("PD_Cache")
                                                            .append(File.separator)
                                                            .toString();

    private static WrapedBooleanProperty isClipboardIconified = new WrapedBooleanProperty(false);
    private static WrapedBooleanProperty isClipboardFocused = new WrapedBooleanProperty(true);
    private static BooleanProperty isListening = new SimpleBooleanProperty(true);
    private final KeyCombination previousWordCom = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.ALT_DOWN);
    private final KeyCombination latterWordCom = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.ALT_DOWN);
    private final KeyCombination enTab = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination chTab = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination thTab = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination scTab = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
    private final KeyCombination playAudio = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
    private final KeyCombination focusKey = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
    private final KeyCombination escKey = new KeyCodeCombination(KeyCode.ESCAPE);
    private final Border windowBorder = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(2), new BorderWidths(1)));
    private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

    private final EventHandler<MouseEvent> dragClipboardWindowEvent = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            clipboardWindowStage.setX(windowLeftUpCornerStartPointX.getValue()  + event.getScreenX() - clickStartPointInScreenX.get());
            clipboardWindowStage.setY(windowLeftUpCornerStartPointY.getValue()  + event.getScreenY() - clickStartPointInScreenY.get());
        }
    };
    private final EventHandler<MouseEvent> dragMainWindowEvent = new EventHandler<>() {
        @Override
        public void handle(MouseEvent event) {
            mainWindowStage.setX(windowLeftUpCornerStartPointX.getValue()  + event.getScreenX() - clickStartPointInScreenX.get());
            clipboardWindowStage.setX(mainWindowStage.getX() + differenceOfWidth);
            mainWindowStage.setY(windowLeftUpCornerStartPointY.getValue()  + event.getScreenY() - clickStartPointInScreenY.get());
            clipboardWindowStage.setY(mainWindowStage.getY() + differenceOfHeight);
        }
    };

    class TableHandler extends SimpleBooleanProperty{
        static final int DEFAULT = 0;
        static final int MATERIAL_TABLE = 1;
        static final int RECITE_TABLE = 4;
        static final int KNOWN_WORD_TABLE = 5;
        static final int UPDATE_MATERIAL_TABLE = 3;

        int table = DEFAULT;

        public TableHandler(){
            addListener(observable -> {
                Platform.runLater(() -> {
                    switch (table){
                        case MATERIAL_TABLE:
                            contentTable.changeToMaterialColumns();
                            break;
                        case UPDATE_MATERIAL_TABLE:
                            contentTable.updateMaterialTable();
                            break;
                        case RECITE_TABLE:
                            contentTable.changeToReciteTableColumns();
                            break;
                        case KNOWN_WORD_TABLE:
                            contentTable.changeToKnownWordsViewColumns();
                            break;
                    }
                });

                table = DEFAULT;
            });
        }

        public void set(int num){
            super.set(!get());
            table = num;
        }
    }

    class MainStage extends Stage {
        //TODO 同步放大缩小，同步Always Top
        BorderPane outerPane = new BorderPane();
        AnchorPane anchorPane = new AnchorPane();
        Scene mainWindowScene = new Scene(outerPane, mainWindowWidth, mainWindowHeight, Color.TRANSPARENT);

        public MainStage(){
            super(StageStyle.TRANSPARENT);
            this.initOwner(clipboardWindowStage);
            this.initModality(Modality.NONE);
            this.setResizable(false);
            this.setOnCloseRequest(Event::consume);
            outerPane.setStyle("-fx-background-color: transparent");
            outerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(2), new BorderWidths(1))));
            anchorPane.setStyle("-fx-background-color: transparent");
            outerPane.setCenter(anchorPane);
            anchorPane.getChildren().addAll(contentTable);
            contentTable.setPrefSize(430, 540);
            AnchorPane.setLeftAnchor(contentTable, 0d);
            AnchorPane.setTopAnchor(contentTable, 0d);
            this.setScene(mainWindowScene);
        }

        private void openMainWindow() {
            mainWindowStage.show();
        }

        private void setIntegrateMenuToMainWindow() {
            outerPane.setTop(integrateMenu);
        }

        private void setHideButtonEventToMainWindow() {
            closeButton.setOnMouseClicked(event -> {
                changeToClipboardWindow();
                setHideButtonEventToClipboardWindow();
            });
        }

        private void setHideButtonEventToClipboardWindow(){
            closeButton.setOnMouseClicked(mouseEvent -> clipboardWindowStage.hide());
        }

        private void setMenuBarMovingEventToMainWindow() {
            menuBar.setOnMousePressed(event -> {
                windowLeftUpCornerStartPointX.set(mainWindowStage.getX());
                windowLeftUpCornerStartPointY.set(mainWindowStage.getY());
                clickStartPointInScreenX.set(event.getScreenX());
                clickStartPointInScreenY.set(event.getScreenY());
            });

            initNewPositionOfClipboardStage();
            menuBar.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragClipboardWindowEvent);
            menuBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragMainWindowEvent);
        }

        private void setMenuBarMovingEventToClipboard() {
            menuBar.setOnMousePressed(event -> {
                windowLeftUpCornerStartPointX.set(clipboardWindowStage.getX());
                clickStartPointInScreenX.set(event.getScreenX());
                windowLeftUpCornerStartPointY.set(clipboardWindowStage.getY());
                clickStartPointInScreenY.set(event.getScreenY());
            });
            menuBar.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragMainWindowEvent);
            menuBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragClipboardWindowEvent);
        }

        private void initNewPositionOfClipboardStage() {
            differenceOfHeight = menuBar.getHeight() + topToolBar.getHeight();
            clipboardWindowStage.setX(mainWindowStage.getX() + differenceOfWidth);
            clipboardWindowStage.setY(mainWindowStage.getY() + differenceOfHeight);
            setClipboardWindowHeight(mainWindowHeight - differenceOfHeight);
            System.out.println("init position");
        }

        public void setScrollEventHandler(EventHandler<? super ScrollEvent> eventHandler){
            anchorPane.setOnScroll(eventHandler);
        }

        public void setMouseClickPenetrateHandler(EventHandler<? super MouseEvent> eventHandler){
            outerPane.addEventHandler(MouseEvent.ANY, eventHandler);
        }
    }

    private SimpleDoubleProperty clickStartPointInScreenX = new SimpleDoubleProperty();
    private SimpleDoubleProperty clickStartPointInScreenY = new SimpleDoubleProperty();
    private SimpleDoubleProperty windowLeftUpCornerStartPointX = new SimpleDoubleProperty();
    private SimpleDoubleProperty windowLeftUpCornerStartPointY = new SimpleDoubleProperty();
    private static BooleanProperty alwaysOnProperty;
    private static TableHandler tableHandler;
    private BooleanProperty tabHoverProperty;


    private String lastWord = "";
    private String tempWord;
    private boolean isImportedFromKindle = false;
    private boolean isMultiCopyingAllowed = true;
    private boolean isFirstGainControl = false;
    private double clipboardWindowWidth = 370;
    private double clipboardWindowHeight = 540;
    private double mainWindowWidth = 800;
    private double mainWindowHeight = 600;
    private double differenceOfWidth =  mainWindowWidth - clipboardWindowWidth;
    private double differenceOfHeight;

    private HBox topToolBar;
    private ClipboardSearchBar clipboardSearchBar;
    private BorderPane outerBorderPane;
    private ClipboardPane clipboardPane;
    private IOSButton isAlwaysOn;
    private IOSButton isListenerOn;
    private IOSButton isAutoPlaying;
    private IOSButton isReadingFromKindle;
    private HBox menuBar;
    private VBox integrateMenu;
    private Button minButton;
    private Button closeButton;
    private Button resizeButton;
    private MainStage mainWindowStage;
    private Stage clipboardWindowStage;
    private Set<Node> smallSet;
    private Set<Node> bigSet;
    private DatabaseContentTable contentTable = new DatabaseContentTable();

    private static Provider provider;

    public static void main(String[] args){
        //TODO 安全防护，搞定了当前任务后才能退出
        try{
            ServerSocket socket = new ServerSocket(7590, 10, InetAddress.getByAddress(new byte[] {127, 0, 0, 1}));
        }catch (BindException e){
            System.err.println("Cannot bind port; Maybe the application has already been set running!");
            System.exit(7);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(8);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(9);
        }

        Application.launch(args);

        provider.stop();
        AudioPlay.terminatePool();
        CacheAudio.shutdownMultiThreadsAudioCachePool();
        ClipboardFunctionQuery.terminatePool();
        DBConnection.closeDictionaryConnection();
        DBConnection.closeMaterialRepoConnection();
        ImportPaneController.shutdownProcessThread();
        ProgressingAnimationController.closeThread();
        System.out.println("Application Exited");
    }

    @Override
    public void start(Stage primaryStage) {
        this.initClipboardPane(clipboardWindowStage = primaryStage);
    }

    private void initClipboardPane(Stage clipboardStage){
        clipboardStage.initStyle(StageStyle.UNDECORATED);
        clipboardStage.setTitle("Dawn: Clipboard Listener");
        clipboardStage.getIcons().add(new Image(this.getClass().getResource("/icon/ico3.png").toExternalForm()));

        minButton = new Button("Iconify");
        closeButton = new Button("Close");
        resizeButton = new Button("Resize");
        topToolBar = new HBox();
        clipboardSearchBar = new ClipboardSearchBar();
        outerBorderPane = new BorderPane();
        clipboardPane = new ClipboardPane();
        menuBar = new HBox();
        isAlwaysOn = new IOSButton(30, false, "Always On Top");
        isListenerOn = new IOSButton(30, true, "Listen To Clipboard");
        isAutoPlaying = new IOSButton(30, true, "Auto Play Audio");
        isReadingFromKindle = new IOSButton(30, false, "Toggle Kindle Mode");

        isAutoPlaying.getSwitchOn().addListener((observable, oldValue, newValue) -> ClipboardFunctionQuery.setAutoPlaying(newValue));
        isReadingFromKindle.getSwitchOn().addListener(((observable, oldValue, newValue) -> isImportedFromKindle = newValue));

        smallSet = new LinkedHashSet<>(Arrays.asList(isAlwaysOn, isListenerOn, isAutoPlaying, isReadingFromKindle, clipboardSearchBar));
        bigSet  = new LinkedHashSet<>(Arrays.asList(isAlwaysOn, new Text("Always Top Status"),
                isListenerOn, new Text("Listener Status"),
                isAutoPlaying, new Text("Auto Play Status"),
                isReadingFromKindle, new Text("Kindle Mode Status"),
                clipboardSearchBar));

        final double BUTTON_SIZE = 15;
        minButton.setStyle("-fx-shape: \"M8.12 9.29L12 13.17l3.88-3.88c.39-.39 1.02-.39 1.41 0 .39.39.39 1.02 0 1.41l-4.59 4.59c-.39.39-1.02.39-1.41 0L6.7 10.7c-.39-.39-.39-1.02 0-1.41.39-.38 1.03-.39 1.42 0z\"; -fx-background-color: black;");
        minButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE/2);
        minButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE/2);
        minButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE/2);
        minButton.setText("");
        minButton.setCursor(Cursor.HAND);
        minButton.setOnMouseClicked(event -> minWindow());
        minButton.addEventHandler(MouseEvent.MOUSE_DRAGGED, Event::consume);

        closeButton.setStyle("-fx-shape: \"M18.3 5.71c-.39-.39-1.02-.39-1.41 0L12 10.59 7.11 5.7c-.39-.39-1.02-.39-1.41 0-.39.39-.39 1.02 0 1.41L10.59 12 5.7 16.89c-.39.39-.39 1.02 0 1.41.39.39 1.02.39 1.41 0L12 13.41l4.89 4.89c.39.39 1.02.39 1.41 0 .39-.39.39-1.02 0-1.41L13.41 12l4.89-4.89c.38-.38.38-1.02 0-1.4z\"; -fx-background-color: darkred;");
        closeButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        closeButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        closeButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        closeButton.setText("");
        closeButton.setCursor(Cursor.HAND);
        closeButton.setOnMouseClicked(event -> clipboardStage.hide());
        closeButton.addEventHandler(MouseEvent.MOUSE_DRAGGED, Event::consume);

        resizeButton.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        resizeButton.setMinSize(BUTTON_SIZE, BUTTON_SIZE);
        resizeButton.setMaxSize(BUTTON_SIZE, BUTTON_SIZE);
        resizeButton.setText("");
        resizeButton.setCursor(Cursor.HAND);
        setResizeButtonToExpand();

        menuBar.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        menuBar.getChildren().addAll(closeButton, minButton, resizeButton, new Text(clipboardStage.getTitle()));
        menuBar.setSpacing(10d);
        menuBar.setMaxHeight(30d);
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setPadding(new Insets(5, 10, 5, 10));

        menuBar.setOnMousePressed(event -> {
            clickStartPointInScreenX.set(event.getScreenX());
            clickStartPointInScreenY.set(event.getScreenY()); //screen 的坐标是指鼠标在整个屏幕的坐标
            windowLeftUpCornerStartPointX.set(clipboardStage.getX());
            windowLeftUpCornerStartPointY.set(clipboardStage.getY()); //Stage 的坐标是窗口所在整个屏幕的坐标
        });
        menuBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragClipboardWindowEvent);

        topToolBar.setMaxHeight(30d);
        topToolBar.setAlignment(Pos.CENTER_LEFT);
        topToolBar.setSpacing(5d);
        topToolBar.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        topToolBar.setPadding(new Insets(2, 5, 2, 5));
        topToolBar.getChildren().addAll(smallSet);
        topToolBar.autosize();

        integrateMenu = new VBox(menuBar, topToolBar);

        outerBorderPane.setCenter(clipboardPane);
        outerBorderPane.setBorder(windowBorder);


        Scene clipboardScene = new Scene(outerBorderPane, clipboardWindowWidth, clipboardWindowHeight);

        setIntegrateMenuToClipboard();

        clipboardSearchBar.requestSearchBoxFocused();

        clipboardStage.setScene(clipboardScene);

        clipboardStage.show();

        ClipboardFunctionQuery.lookupWord("Welcome User!");

        this.callUpUI();

        mainWindowStage = new MainStage();
        tableHandler = new TableHandler();

        clipboardStage.iconifiedProperty().addListener((observable, oldValue, newValue) -> {
            isClipboardIconified.setValue(newValue);
        });


        clipboardStage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            isClipboardFocused.setValue(newValue);
        });

        isClipboardIconified.addListener((observable, oldValue, newValue) -> {
            if(isClipboardIconified.isNotCallFromBonding()){
                Platform.runLater(() -> {
                    clipboardStage.setIconified(newValue);
                    if(newValue){
                        System.out.println("minimize window —— clipboard");
                    }else {
                        System.out.println("maximize window —— clipboard");
                        clipboardSearchBar.requestSearchBoxFocused();
                    }
                });
            }
        });


        isClipboardFocused.addListener((observable, oldValue, newValue) -> {
            if(isClipboardFocused.isNotCallFromBonding()){
                Platform.runLater(() -> {
                    clipboardStage.setIconified(true);
                    clipboardStage.setIconified(false);
                    clipboardSearchBar.requestSearchBoxFocused();
                });
                System.out.println("focus window —— clipboard");
            }
        });

        isListening.addListener((observableValue, oldValue, newValue) -> {
            if(newValue) {
                this.regainOwnership(sysClip.getContents(this));
                System.out.println("Start Listening");
            }
            else
                System.out.println("Postponed Listening...");
        });

        Transferable trans = sysClip.getContents(this);
        sysClip.setContents(trans, this);
        System.out.println("Listening to board...");

        provider = Provider.getCurrentProvider(false);
        provider.register(KeyStroke.getKeyStroke("ctrl alt X"), hotKey -> { //Cap letter
            if(clipboardStage.isIconified()) {
                this.maximizeWindow();
            }
            else if(!clipboardStage.isFocused()){
                this.focusWindow();
            }else {
                this.minWindow();
            }
        });

        clipboardScene.setOnKeyReleased(event -> {
            if(playAudio.match(event)){
                AudioPlay.autoPlay();
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
            }else if(focusKey.match(event)){
                clipboardSearchBar.requestSearchBoxFocused();
            }else if(escKey.match(event)){
                this.minWindow();
            }
        });

        alwaysOnProperty = isAlwaysOn.getSwitchOn();
        isAlwaysOn.getSwitchOn().addListener((observable, oldValue, newValue) -> {
            clipboardStage.setAlwaysOnTop(newValue);
            mainWindowStage.setAlwaysOnTop(newValue);
        });

        isListenerOn.getSwitchOn().addListener((observable, oldValue, newValue) -> {
            if(newValue)
                isListening.setValue(true);
            else{
                isListening.setValue(false);
                isFirstGainControl = true;
            }
        });

        mainWindowStage.setScrollEventHandler(event -> {
            if(tabHoverProperty.get())
                ((WebDisplayTab)clipboardPane.getSelectionModel().selectedItemProperty().get()).getWebView().fireEvent(event);
        });

        mainWindowStage.setMouseClickPenetrateHandler(mouseEvent -> {
            if (outerBorderPane.hoverProperty().get()){
                outerBorderPane.fireEvent(mouseEvent);
            }
        });
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
                    this.callUpUI();
                }else{
                    HistoryArray.setEmptyFlagTrue();
                }

                lastWord = tempWord;
            }
        }
    }

    private void regainOwnership(Transferable t) {
        sysClip.setContents(t, this);
        if(isFirstGainControl)
            isFirstGainControl = false;
        else
            processContents(t);
    }

    private void callUpUI(){
        if(isClipboardIconified.getValue()){
            this.maximizeWindow();
            this.focusWindow();
        }else{
            this.focusWindow();
        }
    }

    private void minWindow(){
        isClipboardIconified.setValue(true, true);
    }

    private void maximizeWindow(){
        isClipboardIconified.setValue(false, true);
    }

    private void focusWindow(){
        isClipboardFocused.setValue(true, true);
    }

    private void setClipboardWindowWidth(double width){
        clipboardWindowWidth = width;
        clipboardWindowStage.setMinWidth(width);
        clipboardWindowStage.setMaxWidth(width);
    }

    private void setClipboardWindowHeight(double height){
        clipboardWindowHeight = height;
        clipboardWindowStage.setMinHeight(height);
        clipboardWindowStage.setMaxHeight(height);
    }

    private void setIntegrateMenuToClipboard() {
        outerBorderPane.setTop(integrateMenu);
    }

    private void setResizeButtonToExpand(){
        resizeButton.setStyle("-fx-shape: \"M855 160.1l-189.2 23.5c-6.6 0.8-9.3 8.8-4.7 13.5l54.7 54.7-153.5 153.5c-3.1 3.1-3.1 8.2 0 11.3l45.1 45.1c3.1 3.1 8.2 3.1 11.3 0l153.6-153.6 54.7 54.7c4.7 4.7 12.7 1.9 13.5-4.7L863.9 169c0.7-5.2-3.7-9.6-8.9-8.9zM416.6 562.3c-3.1-3.1-8.2-3.1-11.3 0L251.8 715.9l-54.7-54.7c-4.7-4.7-12.7-1.9-13.5 4.7L160.1 855c-0.6 5.2 3.7 9.5 8.9 8.9l189.2-23.5c6.6-0.8 9.3-8.8 4.7-13.5l-54.7-54.7 153.6-153.6c3.1-3.1 3.1-8.2 0-11.3l-45.2-45z\"; -fx-background-color: black;");
        resizeButton.setOnMouseClicked(mouseEvent -> changeToMainWindow());
    }

    private void changeToMainWindow(){
        outerBorderPane.setBorder(null);
        mainWindowStage.openMainWindow();
        setResizeButtonToContract();
        mainWindowStage.setIntegrateMenuToMainWindow();
        mainWindowStage.setMenuBarMovingEventToMainWindow();
        mainWindowStage.setHideButtonEventToMainWindow();
        expandToolBar();
        tabHoverProperty = clipboardPane.addDetailTab();
    }

    private void setResizeButtonToContract(){
        resizeButton.setStyle("-fx-shape: \"M881.7 187.4l-45.1-45.1c-3.1-3.1-8.2-3.1-11.3 0L667.8 299.9l-54.7-54.7c-4.7-4.7-12.7-1.9-13.5 4.7L576.1 439c-0.6 5.2 3.7 9.5 8.9 8.9l189.2-23.5c6.6-0.8 9.3-8.8 4.7-13.5l-54.7-54.7 157.6-157.6c3-3 3-8.1-0.1-11.2zM439 576.1l-189.2 23.5c-6.6 0.8-9.3 8.9-4.7 13.5l54.7 54.7-157.5 157.5c-3.1 3.1-3.1 8.2 0 11.3l45.1 45.1c3.1 3.1 8.2 3.1 11.3 0l157.6-157.6 54.7 54.7c4.7 4.7 12.7 1.9 13.5-4.7L447.9 585c0.7-5.2-3.7-9.6-8.9-8.9z\"; -fx-background-color: black;");
        resizeButton.setOnMouseClicked(mouseEvent -> changeToClipboardWindow());
    }

    private void changeToClipboardWindow(){
        mainWindowStage.hide();
        setResizeButtonToExpand();
        outerBorderPane.setBorder(windowBorder);
        setIntegrateMenuToClipboard();
        mainWindowStage.setMenuBarMovingEventToClipboard();
        mainWindowStage.setHideButtonEventToClipboardWindow();
        contractToolBar();
        clipboardPane.deleteDetailTab();
        System.gc();
    }

    private void expandToolBar(){
        topToolBar.getChildren().setAll(bigSet);
    }

    private void contractToolBar(){
        topToolBar.getChildren().setAll(smallSet);
    }


    public static void changeToMaterialTableColumns(){
        tableHandler.set(TableHandler.MATERIAL_TABLE);
    }

    public static void updateMaterialTableColumns(){
        tableHandler.set(TableHandler.UPDATE_MATERIAL_TABLE);
    }

    protected static void changeToUnknownWordTableColumns(){
        tableHandler.set(TableHandler.RECITE_TABLE);
    }

    protected static void changeToKnownWordTableColumns(){
        tableHandler.set(TableHandler.KNOWN_WORD_TABLE);
    }

    public static boolean getAlwaysOnStatus(){
        return alwaysOnProperty.getValue();
    }
}