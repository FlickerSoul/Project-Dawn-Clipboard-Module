package me.flickersoul.dawn.ui;

import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class IOSButton extends Parent {
    public BooleanProperty switchOn = new SimpleBooleanProperty(false);

    private TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.25));

    private FillTransition fillTransition = new FillTransition(Duration.seconds(0.25));

    private ParallelTransition parallelTransition = new ParallelTransition(translateTransition, fillTransition);


    public IOSButton(int wid_size, boolean initState){
        Rectangle base = new Rectangle(wid_size, wid_size>>1);
        base.setArcHeight(wid_size>>1);
        base.setArcWidth(wid_size>>1);
        base.setFill(Color.LIGHTGRAY);
        base.setStroke(Color.GRAY);

        Circle dot = new Circle(wid_size/4);
        dot.setCenterX(wid_size/4);
        dot.setCenterY(wid_size/4+0.5);
        dot.setFill(Color.WHITE);
//        dot.setStroke(Color.BLACK);

        translateTransition.setNode(dot);
        fillTransition.setShape(base);

        this.getChildren().addAll(base, dot);

        switchOn.addListener((abs, oldState, newState) -> {
            boolean isOn = newState.booleanValue();
            translateTransition.setToX(isOn ? wid_size - wid_size/2 : 0);
            fillTransition.setFromValue(isOn ? Color.LIGHTGREY : Color.LIGHTGREEN);
            fillTransition.setFromValue(isOn ? Color.LIGHTGREEN : Color.LIGHTGRAY);
            parallelTransition.play();
        });

        this.setOnMouseClicked(e -> {
            switchOn.set(!switchOn.get());
        });

        switchOn.setValue(initState);
    }
}
