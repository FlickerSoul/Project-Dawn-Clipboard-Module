package me.flickersoul.dawn.functions;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class WrapedIntBoolProperty extends SimpleBooleanProperty {
    private int changedTabNum = 0;

    public WrapedIntBoolProperty(){
        super(false);
    }

    public void changeTabNum(int tabNum){
        this.changedTabNum = tabNum;
        this.set(!this.get());
    }

    public int getChangedTabNum(){
        return changedTabNum;
    }
}
