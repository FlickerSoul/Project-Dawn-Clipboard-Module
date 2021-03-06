package me.flickersoul.dawn.functions;

import javafx.beans.property.SimpleBooleanProperty;

public class WrapedBooleanProperty extends SimpleBooleanProperty {
    private boolean notCallFromBonding = false;

    public WrapedBooleanProperty(boolean iniValue){
        super(iniValue);
    }

    public void setValue(boolean newValue, boolean notCallFromBonding){
        this.notCallFromBonding = notCallFromBonding;
        setValue(newValue); //试试先后和listener顺序？
        this.notCallFromBonding = false;
    }

    public boolean isNotCallFromBonding() {
        return notCallFromBonding;
    }
}
