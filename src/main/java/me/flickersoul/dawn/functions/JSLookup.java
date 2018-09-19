package me.flickersoul.dawn.functions;

import me.flickersoul.dawn.ui.ClipboardSearchBar;

public class JSLookup {
    public void lookupWord(String word){
        ClipboardSearchBar.text.setValue(word);
        ClipboardFunctionQuery.lookupWord(word);
    }
}
