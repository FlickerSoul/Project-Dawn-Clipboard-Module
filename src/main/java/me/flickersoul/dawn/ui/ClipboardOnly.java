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
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.ClipboardFunctionQuery;

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

    String tempWord;

    private final Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();


    @Override
    public void start(Stage primaryStage) {
        this.initClipboardPane(primaryStage);
    }

    protected void initClipboardPane(Stage clipboardStage){
        ToolBar topToolBar = new ToolBar();
        ClipboardSearchBar clipboardSearchBar = new ClipboardSearchBar();
        BorderPane borderPane = new BorderPane();
        ClipboardPane clipboardTab = new ClipboardPane();
        IOSButton isAlwaysOn = new IOSButton(30, false);
        IOSButton isListenerOn = new IOSButton(30, true);
        IOSButton isAutoPlaying = new IOSButton(30, true);


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
        borderPane.setCenter(clipboardTab);
        borderPane.setTop(topToolBar);

        isIconified.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                System.out.println("min");
                Platform.runLater(() -> {
                    clipboardStage.setIconified(newValue);
                });
            }else{
                System.out.println("focus");
                Platform.runLater(() -> {
                    clipboardStage.setIconified(newValue);
                    clipboardStage.requestFocus();
                });
            }
        });

        isFocused.addListener(observable -> {
            if(!clipboardStage.isFocused())
                Platform.runLater(() -> {
                    clipboardStage.setAlwaysOnTop(true);
                    clipboardStage.setAlwaysOnTop(false);
//                    clipboardStage.requestFocus();
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
            if(isIconified.getValue()) {
                isIconified.setValue(false);
            }
            else if(!clipboardStage.isFocused())
                Platform.runLater(() -> clipboardStage.requestFocus());
            else
                isIconified.setValue(true);
        });


        clipboardStage.setOnCloseRequest(e -> {
            provider.stop();
        });

        isIconified.setValue(true);

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
        }catch(Exception e){e.printStackTrace();}
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

            System.out.println("copied: " + object);

            if(ClipboardFunctionQuery.lookupWord(ClipboardFunctionQuery.processWords(object.toString()).toLowerCase())) {
                System.out.println("Got Word");
                isIconified.setValue(false);
                isFocused.setValue(!isFocused.getValue());
            }
        }
    }

    void regainOwnership(Transferable t) {
        sysClip.setContents(t, this);
        processContents(t);
    }
}
