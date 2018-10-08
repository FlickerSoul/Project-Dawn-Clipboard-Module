package me.flickersoul.dawn.functions;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

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
            int length = tempArray.length - 1;
            for(int i = 0; i <= length; i++){
                char sub = tempArray[i];
                if(sub == ' '){
                    this.addToWordList(wordStringBuilder.toString());
                    this.trimWordBuilderSize();
                }else if(sub == '.' || (sub == ';' && i == length)){
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

    public int getNum(){
        return showingNum;
    }
}

class TrimmedWordMember {
    private String originalWord;
    private TreeSet<String> showedForms = new TreeSet();
    private ArrayList<Integer> sentSerial = new ArrayList<>();
    private int showingNum;


}

class ProcessingThread implements Runnable {
    private int lastNum = 0;
    private static final Pattern PROCESS_WORD_PATTERN = Pattern.compile("[^'a-zA-Z0-9ā--\\s]");

    @Override
    public void run() {

    }
}
