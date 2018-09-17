package me.flickersoul.dawn.functions;

public enum DBList {
    DictionaryDB("src/main/resources/Dictionary/dictionary.db", "Dictionary DB", "dictionary"),
    UnfDB("src/main/resources/PersonalFolder/UnfDB.db", "Unfamiliar Words DB", "words"),
    FDB("src/main/resources/PersonalFolder/FDB.db", "Familiar Words DB", "words"),
    DetailsDB("src/main/resources/PersonalFolder/DetailsDB.db", "Word Details DB", "words", ""),
    ExistingBooksDB("src/main/resources/PersonalFolder/ExistingBooks.db", "Existing Book List DB", "bookList");

    private String URL;
    private String name;
    private String[] tableList;

    DBList(String URL, String name, String... tableList){
        this.URL = URL;
        this.name = name;
        this.tableList = tableList;
    }

    public String getURL(){
        return this.URL;
    }

    public String getName(){
        return this.name;
    }
}
