package me.flickersoul.dawn.functions;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static me.flickersoul.dawn.functions.ClipboardFunctionQuery.*;

public class SentenceProcessingQuery implements Callable<Boolean> {
    private TreeMap<Integer, String> sentenceMap = new TreeMap<>();
    private int sentIterator = 0;
    private TreeMap<String, WordMember> wordList = new TreeMap<>();
    private TreeMap<String, TrimmedWordMember> trimmedWordList = new TreeMap<>();
    private File file;
    private String text;
    private URL url;
    private StringBuilder wordStringBuilder = new StringBuilder();
    private StringBuilder sentStringBuilder = new StringBuilder();
    private ExecutorService processWordsPool = Executors.newFixedThreadPool(3, runnable -> new Thread(runnable, "Word Process Thread"));

    private static final Pattern CAP_LETTER_PATTERN = Pattern.compile("['A-Z]");

    public SentenceProcessingQuery(File file) {
        this.file = file;
    }

    public SentenceProcessingQuery(String text) {
        this.text = text;
    }

    public SentenceProcessingQuery(URL url) {
        this.url = url;
    }

    public static boolean isText(String text){
        if(text.equals(null) || text == null) return false;
        return (CAP_LETTER_PATTERN.matcher(String.valueOf(text.charAt(0))).find() && (text.endsWith(".") || text.endsWith(";") || text.endsWith(".\"")));
    }

    public SentenceProcessingQuery setFile(File file) {
        this.file = file;
        //check whether accessible
        return this;
    }

    public SentenceProcessingQuery setText(String text) {
        this.text = text;
        return this;
    }

    public SentenceProcessingQuery setURL(URL url) {
        this.url = url;
        return this;
    }

    private void addToWordList(String word) {
        if(wordList.containsKey(word)){
            WordMember temp = wordList.get(word);
            temp.addShowingNum().addSentSerial(temp.checkExistingSerial(sentIterator) ? -1 : sentIterator);
        }else{
            wordList.put(word, new WordMember(word, sentIterator));
        }
    }

    private void addNewSent(String tempSentence){
        sentenceMap.put(sentIterator++, tempSentence);
    }

    private void trimSentBuilderSize() {
        if(sentStringBuilder.length() > 512) {
            sentStringBuilder.setLength(521);
            sentStringBuilder.trimToSize();
            sentStringBuilder.setLength(0);
        }else {
            sentStringBuilder.setLength(0);
        }
    }

    private void trimWordBuilderSize() {
        if(wordStringBuilder.length() > 20) {
            sentStringBuilder.setLength(20);
            sentStringBuilder.trimToSize();
            sentStringBuilder.setLength(0);
        }else {
            sentStringBuilder.setLength(0);
        }
    }

    @Override
    public Boolean call() {
        if(text != null){
            char[] tempArray = text.toCharArray();
            int end = tempArray.length - 1;
            for(int i = 0; i <= end; i++){
                char sub = tempArray[i];
                if(sub == ' '){
                    this.addToWordList(wordStringBuilder.toString());
                    this.trimWordBuilderSize();
                }else if(sub == '.' || sub == '?' || sub == '!' || (sub == ';' && i == end)){
                    this.addToWordList(wordStringBuilder.toString());
                    this.trimWordBuilderSize();
                    this.addNewSent(sentStringBuilder.append('.').toString());
                    this.trimSentBuilderSize();
                }else{
                    wordStringBuilder.append(sub);
                    sentStringBuilder.append(sub);
                }
            }

            System.out.println(wordList);
            System.out.println(sentenceMap);

            return true;
        }else if(url != null){

        }else if(file != null){

        }

        return false;
    }
}

class WordMember {
    private String word;
    private int showingNum;
    private ArrayList<Integer> sentSerial = new ArrayList<>();

    public WordMember(String word, int firstSentSerial) {
        this.word = word;
        showingNum = 1;
        sentSerial.add(firstSentSerial);
    }

    public void addSentSerial(int serial) {
        if(serial == -1) return;
        sentSerial.add(serial);
    }

    public WordMember addShowingNum() {
        showingNum += 1;
        return this;
    }

    public boolean checkExistingSerial(int serial){
        return sentSerial.contains(serial);
    }

    public String getWord(){
        return word;
    }

    public int getShowingNum(){
        return showingNum;
    }

    public ArrayList<Integer> getSentSerial(){
        return sentSerial;
    }

}

class TrimmedWordMember {
    private String originalWord;
    private int wordID;
    private int showingNum;
    private TreeSet<String> showedForms = new TreeSet();
    private ArrayList<Integer> sentSerial = new ArrayList<>();

    public TrimmedWordMember(WordMember wordMember){

    }

    public void setOriginalWord(String originalWord){
        this.originalWord = originalWord;
    }

    public void setWordID(int id){
        this.wordID = id;
    }

    public void setShowingNum(int num){
        this.showingNum = num;
    }

    public void setShowedForms(TreeSet<String> showedForms){
        this.showedForms = showedForms;
    }

    public void pushShowedForms(String form){
        this.showedForms.add(form);
    }

    public void setSentSerial(ArrayList<Integer> sentSerial){
        this.sentSerial = sentSerial;
    }
}

class ProcessingThread implements Runnable {
    private int lastNum = 0;
    private static final Pattern PROCESS_WORD_PATTERN = Pattern.compile("[^'a-zA-Z0-9ā--\\s]");
    private TrimmedWordMember tempTrimmedWordMember;
    private WordMember wordMember;
    private Connection connection = ClipboardFunctionQuery.connection;

    private static final int NOT_FOUND_ID = -1;
    private static final String GET_WORDS_WITH_SAME_ID_SQL = "SELECT c1word_value FROM all_words_content WHERE c0_id=?";

    @Override
    public void run() {

    }

    public ProcessingThread setTrimmedWordMember(TrimmedWordMember tempTrimmedWordMember, WordMember wordMember){
        this.tempTrimmedWordMember = tempTrimmedWordMember;
        this.wordMember = wordMember;


        return this;
    }

    public void checkWordExistence(TrimmedWordMember trimmedWordMember, WordMember wordMember){
        String word = wordMember.getWord();
        String firstLetter;
        word = ClipboardFunctionQuery.processWords(word);
        HistoryArray.setCurrentWord(word);
        firstLetter = word.toLowerCase().charAt(0) + "";
        firstLetter = FIRST_LETTER_PATTERN.matcher(firstLetter).find() ? firstLetter : "spec_char";
        try {
            PreparedStatement getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + firstLetter + GET_ID_SQL_PART2); //最快
            getIDPreparedStatement.setString(1, word);
            ResultSet idResultSet = getIDPreparedStatement.executeQuery();

            if (!idResultSet.next()) {
                getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + firstLetter + GET_ID_SQL_PART2); //最快
                getIDPreparedStatement.setString(1, word.toLowerCase());
                idResultSet = getIDPreparedStatement.executeQuery();
                if (!idResultSet.next()) {
                    getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + firstLetter + GET_ID_SQL_PART2);
                    char[] cs = word.toLowerCase().toCharArray();
                    cs[0] -= 32;
                    getIDPreparedStatement.setString(1, String.valueOf(cs));
                    idResultSet = getIDPreparedStatement.executeQuery();
                    if (!idResultSet.next()) {
                        getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART3 + firstLetter + GET_ID_SQL_PART4);
                        getIDPreparedStatement.setString(1, word.replaceAll(" ", "").replaceAll("-", ""));
                        idResultSet = getIDPreparedStatement.executeQuery();
                        if (!idResultSet.next()) {
                            getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART3 + firstLetter + GET_ID_SQL_PART4);
                            getIDPreparedStatement.setString(1, word.toLowerCase().replaceAll(" ", "").replaceAll("-", ""));
                            idResultSet = getIDPreparedStatement.executeQuery();
                            if (!idResultSet.next()) {
                                getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART3 + firstLetter + GET_ID_SQL_PART4);
                                cs = word.toLowerCase().replaceAll(" ", "").replaceAll("-", "").toCharArray();
                                cs[0] -= 32;
                                getIDPreparedStatement.setString(1, String.valueOf(cs));
                                idResultSet = getIDPreparedStatement.executeQuery();
                                if (!idResultSet.next()) {
                                    trimmedWordMember.setOriginalWord(wordMember.getWord());
                                }
                            }
                        }
                    }
                }
            }

            int id;
            if(!idResultSet.isClosed())
                id = idResultSet.getInt(1);
            else
                id = NOT_FOUND_ID;

            trimmedWordMember.setWordID(id);
            trimmedWordMember.setShowingNum(wordMember.getShowingNum());

            PreparedStatement getWordsWithSameIDStatement = connection.prepareStatement(GET_WORDS_WITH_SAME_ID_SQL);
            getWordsWithSameIDStatement.setInt(1, id);
            ResultSet wordsWithSameID = getWordsWithSameIDStatement.executeQuery();

            if(wordsWithSameID.isClosed())
                trimmedWordMember.setShowedForms(null);
            else{
                while(wordsWithSameID.next()){
                    trimmedWordMember.pushShowedForms(wordsWithSameID.getString(1));
                }
            }

            trimmedWordMember.setSentSerial(wordMember.getSentSerial());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
