package me.flickersoul.dawn.functions;

import me.flickersoul.dawn.ui.ClipboardSearchBar;

public class JSLookup {
    public void lookupWord(String word){
        if(ClipboardFunctionQuery.lookupWord(word)) {
            HistoryArray.insertSearchResult(word);
        }
    }
}
