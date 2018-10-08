package me.flickersoul.dawn.functions;

public class JSLookup {
    public void lookupWord(String word){
        if(ClipboardFunctionQuery.lookupWord(word)) {
            HistoryArray.insertSearchResult(word);
        }
    }
}
