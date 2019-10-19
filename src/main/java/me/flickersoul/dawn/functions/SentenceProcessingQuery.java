package me.flickersoul.dawn.functions;

import com.alibaba.fastjson.JSONObject;
import me.flickersoul.dawn.ui.ClipboardOnly;
import me.flickersoul.dawn.ui.ProgressingAnimationController;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static me.flickersoul.dawn.functions.ClipboardFunctionQuery.*;

public class SentenceProcessingQuery implements Runnable {
    private TreeMap<Integer, String> sentenceMap;
    private TreeMap<String, WordMember> wordList;
    private TreeMap<String, TrimmedWordMember> trimmedWordMemberMap;
    private File file;
    private String text;
    private String title = "unknown title";
    private URL url;
    private int sentIterator = 0;
    private boolean lastCharEndingSign = false;
    private boolean lastQuoteEndingSign = true; // TODO 制作 S. 名字
    private boolean isSingleQuoteSignClosed = true;
    private boolean isNSingleQuoteSignClosed = true; // nsq 和 sq 一次只出现一种的前提下
    private boolean isDoubleQuoteSignClosed = true;
    private boolean isParenthesisSignClosed = true;
    private boolean isSquareBracketSignClosed = true;
    private boolean isPreviousSingleQuote = false;
    private boolean isPreviousNSingleQuote = false;
    private boolean isPreviousSpaceSign = true; // 文章一开始标记为空格.
    private boolean isPreviousPeriod = false;
    private boolean isPreviousComma = false;
    private DBProcessingQuery dbProcessingQuery = new DBProcessingQuery();
    private StringBuilder wordStringBuilder = new StringBuilder();
    private StringBuilder sentStringBuilder = new StringBuilder();
    private ExecutorService processWordsPool = Executors.newFixedThreadPool(10, runnable -> new Thread(runnable, "Word Process Thread"));
    private ExecutorService wordInsertPool = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "Insert Word Thread"));
    private List<Callable<Boolean>> processesArray = new LinkedList<>();
    private Future<Boolean> wordFuture;

    private ProgressingAnimationController controller;

    private static final String ABBR_SQL = "SELECT word FROM abbr_word WHERE word=?";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern LETTER_PATTERN = Pattern.compile("[a-zA-Z]");

    class WordInsertThread implements Callable<Boolean> {

        TreeMap<String, TrimmedWordMember> trimmedWordMembers;

        protected WordInsertThread(TreeMap<String, TrimmedWordMember> trimmedWordMembers){
            this.trimmedWordMembers = trimmedWordMembers;
        }

        @Override
        public Boolean call() {
            for(TrimmedWordMember member : trimmedWordMembers.values()){
                dbProcessingQuery.insertToWordTable(member.getOriginalWord(), member.getId(), member.getLowercaseForm(), member.getLowercaseFormId(), member.getOccurrenceNum(), member.getSentRecords(), member.getDirs());
            }
            //直接传 member

            dbProcessingQuery.executeInsertWordsBatch();

            return true;
        }
    }

    public SentenceProcessingQuery(File file, ProgressingAnimationController controller) {
        this.file = file;
        this.controller = controller;
    }

    public SentenceProcessingQuery(String text, ProgressingAnimationController controller) {
        this.text = text;
        this.controller = controller;
    }

    public SentenceProcessingQuery(URL url, ProgressingAnimationController controller) {
        this.url = url;
        this.controller = controller;
    }

    /**
     * add a word that is not in word list to the list
     * @param word word that should be inputted
     */

    protected synchronized void addToWordList(String word, int sentSerial) {
        if (wordList.containsKey(word)) {
            WordMember temp = wordList.get(word);
            temp.addShowingNum().addSentSerial(sentSerial);
        } else {
            wordList.put(word, new WordMember(word, sentSerial));
        }
    }

    /**
     * used for add new sentence to total sentence queue
     * @param tempSentence sentence that should be inputted
     */
    private void addNewSent(String tempSentence){
        sentenceMap.put(++sentIterator, tempSentence);
    }

    /**
     * used for trim sentence StringBuilder Size
     */
    private void trimSentBuilderSize() {
        if(sentStringBuilder.length() > 256) {
            sentStringBuilder.setLength(256);
            sentStringBuilder.trimToSize();

        }
        sentStringBuilder.setLength(0);
    }

    /**
     * thread run method
     */
    @Override
    public void run() {
        long ST = System.currentTimeMillis();
        sentenceMap = new TreeMap<>();
        wordList = new TreeMap<>();
        trimmedWordMemberMap = new TreeMap<>();

        if(text != null && !text.equals("")){
            //TODO set Title
            char[] tempArray = text.toCharArray();
            controller.setProgress(0.1);
            for(char sub : tempArray){
                appendProcessing((int)sub);
            }

        }else if(url != null){
            String json = null;
            try {
                  json = Jsoup.connect("https://mercury.postlight.com/parser?url=" + url.toString())
                        .ignoreContentType(true)
                        .header("Accept", "application/json")
                        .header("Content-Type","application/json")
                        .header("x-api-key", "EaHXKMmVBHblTqpd9Q5Zxswu9oB68NIPImNSABbx")
                        .method(org.jsoup.Connection.Method.GET)
                        .execute().body();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Bad Connection!");
            }

            if(json != null){
                JSONObject document = JSONObject.parseObject(json);

                title = document.getString("title");

                Document content = Jsoup.parse(document.getString("content"));
                content.select("p").prepend("\\↑↓");
                char[] tempArray = content.text().replaceAll("\\\\↑↓", "\n").toCharArray();
                controller.setProgress(0.1);
                for(char sub : tempArray){
                    appendProcessing((int)sub);
                }
            }else{
                System.err.println("Bad Connection");
            }

        }else if(file != null){
            title = file.getName();

            try {
                if(file.canRead()) {
                    FileReader fr = new FileReader(file);
                    int character;
                    controller.setProgress(0.1);
                    while ((character = fr.read()) != -1) {
                        appendProcessing(character);
                    }
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        controller.setProgress(0.4);

        try {
            processWordsPool.invokeAll(processesArray);
            System.out.println("invoke all");
            sortTrimmedWordMember(sentenceMap);
            controller.setProgress(0.6);
            System.out.println("Words collection status: " + (wordFuture.get() ? "is Done" : "error!"));
            controller.setProgress(0.9);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("==========\nFinish Extracting Words!\nStart Shutting down\n==========");

        sentenceMap = null;
        wordList = null;
        trimmedWordMemberMap = null;

        processWordsPool.shutdown();
        wordInsertPool.shutdown();
        if(!(processWordsPool.isShutdown() && wordInsertPool.isShutdown())){
            System.err.println("Something wrong with the thread pool shutdown procedure");
        }

        System.out.println("\nprocesses done: " + (System.currentTimeMillis() - ST) + "ms");
        ClipboardOnly.updateMaterialTableColumns();
        controller.setProgress(1);
    }

    /**
     * start appending processing
     * @param character char in int
     */
    private void appendProcessing(int character){
        if(isSeparateSign(character)){
            if(isSpace(character)) {
                setPreviousSpaceSign(true);
                processUnclosedSingleQuotes();
            } else {
                setPreviousSpaceSign(false);
            }

            if(isPreviousPeriod) {
                if(checkAbbrWord(wordStringBuilder)) {
                    appendCharacterToWord('.');
                }else{
                    setLastCharEndingSign(true);
                }
                appendCharacterToSent('.');
                trimWordBuilderSize();
            }

            if(lastCharEndingSign){
                if(checkQuoteSignClosed()){
                    if (isPreviousSpaceSign) {
                        submitSentence(character);
                        setLastCharEndingSign(false);
                    } else {
                        appendCharacterToWord(character);
                        appendCharacterToSent(character);
                    }
                } else {
                    appendCharacterToSent(character);
                    if(isQuoteSign(character)) {
                        processQuoteSigns(character);
                    } else {
                        setLastCharEndingSign(false);
                    }
                }
            } else if(isQuoteSign(character)){
                appendCharacterToSent(character);
                processQuoteSigns(character);
            } else {
                trimWordBuilderSize();
                appendCharacterToSent(character);
            }

            if(isPreviousPeriod)
                setPreviousPeriod(false);
        }else if(isEndingSign(character)){
            if(isPreviousSpaceSign) {
                setPreviousSpaceSign(false);
            }
            if(character == '.'){
                setPreviousPeriod(true);
            }else {
                if(isPreviousPeriod)
                    setPreviousPeriod(false);
                setLastCharEndingSign(true);
                trimWordBuilderSize();
                appendCharacterToSent(character);
            }
        }else if(character == 10){
            if(isPreviousSpaceSign)
                setPreviousSpaceSign(false);
            if(isPreviousPeriod)
                setPreviousPeriod(false);
            if(sentStringBuilder.length() != 0)
                submitSentence(character);
            if(wordStringBuilder.length() != 0)
                trimWordBuilderSize();
            setLastCharEndingSign(false);
        }else {
            if(isPreviousSpaceSign)
                setPreviousSpaceSign(false);
            if(isPreviousPeriod){
                appendCharacterToWord('.');
                appendCharacterToSent('.');
                setPreviousPeriod(false);
            }
            if(isPreviousSingleQuote){
                appendCharacterToWord('’');
                setPreviousSingleQuote(false);
            }
            if(isPreviousNSingleQuote){
                appendCharacterToWord('\'');
                setPreviousNSingleQuote(false);
            }
            appendCharacterToWord(character);
            appendCharacterToSent(character);
        }

    }

    private void setLastQuoteEndingSign(boolean sign) {
        lastQuoteEndingSign = sign;
    }

    private boolean isComma(int character) {
        return character == ',';
    }

    private void appendCharacterToWord(int character){
        wordStringBuilder.append((char)character);
    }

    /**
     * set lastCharEndingSign to @param sign
     * @param sign the sign for lastCharEndingSign
     */
    private void setLastCharEndingSign(boolean sign){
        lastCharEndingSign = sign;
    }

    /**
     * submit current sentence to sentence list
     * @param character char in int
     */
    private void submitSentence(int character){
        String sentence = sentStringBuilder.append(character == 10 || character == 13 ? ' ' : (char) character).toString();
        this.addNewSent(sentence);
        processesArray.add(new SubSentenceThread(sentence, sentIterator, this));
        this.trimSentBuilderSize();
    }

    private void trimWordBuilderSize() {
        if(wordStringBuilder.length() > 10) {
            wordStringBuilder.setLength(10);
            wordStringBuilder.trimToSize();

        }
        wordStringBuilder.setLength(0);
    }



    /**
     * append @param character to temp sent
     * @param character char in int
     */
    private void appendCharacterToSent(int character){
        sentStringBuilder.append((char)character);
    }

    /**
     * check whether the character is one of the ending signs
     * @param character char in int
     * @return true when the character is one of the ending signs
     */
    private static boolean isEndingSign(int character){
        return character == '.' || character == '?' || character == '!';
    }

    /**
     * check whether the character is one of the separating signs
     * @param character char in int
     * @return return true when the character is one of the separating signs
     */
    protected static boolean isSeparateSign(int character){
        return character == ' ' || character == ';' || character == ',' || character == '\"' || character == '“' || character == '”' || character == '\'' || character == '‘' || character == '’' || character == '(' || character == ')'  || character == ':' || character == '[' || character == ']' || character == '…';
    }

    /**
     * check whether the character is one of teh quoting signs
     * @param character char in int
     * @return return true when the character is one of the quoting signs
     */
    private static boolean isQuoteSign(int character){
        return character == '“' || character == '”' || character == '\"' || character == '‘' || character == '’' || character == '\'' || character == '(' || character == ')' || character == '[' || character == ']';
    }

    /**
     * check whether the character is a space
     * @param character char in int
     * @return return true when the character is a spece
     */
    private static boolean isSpace(int character){
        return character == ' ';
    }

    /**
     * check whether it's a abbr word
     * @param tempWordMember temp word
     */
    private boolean checkAbbrWord(StringBuilder tempWordMember){
        return checkWhetherInAbbrWordList(tempWordMember.toString() + '.') || tempWordMember.toString().contains(".");
    }

    private boolean checkWhetherInAbbrWordList(String word){
        try(Connection connection = DBConnection.establishDictionaryConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(ABBR_SQL);){
            preparedStatement.setString(1, word);
            return !preparedStatement.executeQuery().isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkWhetherTheWordAreNumbers(StringBuilder tempWordMember){
        String tempWord = tempWordMember.toString();
        return NUMBER_PATTERN.matcher(tempWord).find() && !LETTER_PATTERN.matcher(tempWord).find();
    }

    private void setSingleQuoteSignClosed(boolean sign){
        isSingleQuoteSignClosed = sign;
    }

    private void setNSingleQuoteSignClosed(boolean sign){
        isNSingleQuoteSignClosed = sign;
    }

    private void setDoubleQuoteSignClosed(boolean sign){
        isDoubleQuoteSignClosed = sign;
    }

    private void setParenthesisSignClosed(boolean sign){
        isParenthesisSignClosed = sign;
    }

    private void setSquareBracketSignClosed(boolean sign){
        isSquareBracketSignClosed = sign;
    }

    private void setPreviousSingleQuote(boolean sign){
        isPreviousSingleQuote = sign;
    }
    
    private void setPreviousNSingleQuote(boolean sign){
        isPreviousNSingleQuote = sign;
    }

    private void setPreviousSpaceSign(boolean sign){
        isPreviousSpaceSign = sign;
    }

    private void setPreviousPeriod(boolean sign){
        isPreviousPeriod = sign;
    }

    private void setPreviousComma(boolean sign) {
        isPreviousComma = sign;
    }
    
    /**
     * process all quote signs
     * @param character character in int
     */
    private void processQuoteSigns(int character){
        if(character == '‘') {
            setSingleQuoteSignClosed(false);
        }else if(character == '’'){
            if(!isPreviousSingleQuote)
                setPreviousSingleQuote(true);
        }else if(character == '\''){
            //前面是否有空格，如果没有，则是缩写或者反单括号’, 如果有则是‘
            if(isPreviousSpaceSign)
                setNSingleQuoteSignClosed(false);
            if(!isPreviousNSingleQuote)
                setPreviousNSingleQuote(true);
        }else if(character == '“'){
            setDoubleQuoteSignClosed(false);
        }else if(character == '”'){
            setDoubleQuoteSignClosed(true);
        }else if(character == '\"'){
            if(isPreviousSpaceSign)
                setDoubleQuoteSignClosed(false);
            else
                setDoubleQuoteSignClosed(true);
        }else if(character == '(') {
            setParenthesisSignClosed(false);
        }else if(character == ')') {
            setParenthesisSignClosed(true);
        }else if(character == '['){
            setSquareBracketSignClosed(false);
        }else if(character == ']'){
            setSquareBracketSignClosed(true);
        }
    }

    /**
     * check whether all the signs are closed
     * @return true when all the signs are closed
     */
    private boolean checkQuoteSignClosed(){
        return (isSingleQuoteSignClosed || isNSingleQuoteSignClosed) && isDoubleQuoteSignClosed && isParenthesisSignClosed && isSquareBracketSignClosed;
    }

    /**
     * process unclosed single quote signs
     */
    private void processUnclosedSingleQuotes(){
        if(isPreviousSingleQuote){
            setSingleQuoteSignClosed(true);
            setPreviousSingleQuote(false);
        } else if (isPreviousNSingleQuote) {
            setNSingleQuoteSignClosed(true);
            setPreviousNSingleQuote(false);
        }
    }

    private TrimmedWordMember checkExistence(WordMember wordMember, TreeMap<Integer, String> sentMap){
        if(wordMember.getTempId() == WordMember.NOT_A_WORD_ID){
            return null;
        }

        String originalWord = wordMember.getOriginalWord();

        TreeMap<Integer, HashSet<String>> tempMap = new TreeMap<>();

        if(trimmedWordMemberMap.containsKey(originalWord)){
            for(int i : wordMember.getSentRecords()){
                HashSet<String> tempSet = new HashSet<>();
                tempSet.add(sentMap.get(i));
                tempMap.put(i, tempSet);
            }
            trimmedWordMemberMap.get(originalWord).addNewOccurrenceNum(wordMember.getOccurrenceNum()).addNewSentRecords(tempMap);

            return null;
        }else{
            for(int i : wordMember.getSentRecords()){
                HashSet<String> tempSet = new HashSet<>();
                tempSet.add(sentMap.get(i));
                tempMap.put(i, tempSet);
            }

            return new TrimmedWordMember(originalWord, wordMember.getLowerCaseForm(),
                                        wordMember.getTempId(), wordMember.getTempLowercaseFormId(),
                                        wordMember.getOccurrenceNum(), tempMap, wordMember.getDirs());
        }
    }

    private void sortTrimmedWordMember(TreeMap<Integer, String> sentMap) {
        for(Map.Entry<String, WordMember> entry : wordList.entrySet()){
            TrimmedWordMember trimmedWordMember = checkExistence(entry.getValue(), sentMap);
            if(trimmedWordMember != null){
                trimmedWordMemberMap.put(trimmedWordMember.getOriginalWord(), trimmedWordMember);
            }
        }

        dbProcessingQuery.insertNewMaterial(title);
        wordFuture = wordInsertPool.submit(new WordInsertThread(trimmedWordMemberMap));
    }

}

class WordMember {
    private String word;
    private String lowerCaseForm; // word -> lower case
    private String originalWord;
    private Elements dirs;
    private int tempId;
    private int tempLowercaseFormId;
    private int occurrenceNum;
    protected static final int NOT_FOUND_ID = -1;
    protected static final int NOT_A_WORD_ID = -2;
    private static final String SOMETHING_WRONG = "SOMETHING_WRONG";

    private TreeSet<Integer> sentRecords = new TreeSet<>();

    private static final String IRV_SQL = "SELECT original FROM irregularv WHERE p=? OR pp=?";
    private static final String IRN_SQL = "SELECT original FROM irregularn WHERE form1=? OR form2=? OR form3=?";

    //所有格
    private static final Pattern POSSESSIVE_S = Pattern.compile("[a-zA-Z]+[^s][']s$");
    private static final Pattern POSSESSIVES = Pattern.compile("[a-zA-Z]+[s][']$");

    /**
     * init a word member.
     * @param word the word this word member represents
     * @param firstSentSerial the sentence serial where this word first shows
     */
    public WordMember(String word, int firstSentSerial) {
        this.word = word.replaceAll("’", "'");
        lowerCaseForm = word.toLowerCase();
        wordTransformation();
        occurrenceNum = 1;
        sentRecords.add(firstSentSerial);
    }

    /**
     * add an additional serial number to the sentence record.
     * @param serial the serial number this word is in.
     */
    public void addSentSerial(int serial) {
        if(!isThisSentenceExists(serial))
            sentRecords.add(serial);
    }

    /**
     * increase the occurrence number by one
     * @return this word member
     */
    public WordMember addShowingNum() {
        occurrenceNum += 1;
        return this;
    }

    /**
     * check whether the @param serial exists in the record.
     * @param serial the serial number to be checked
     * @return true when the record contains the query sentence's serial number
     */
    public boolean isThisSentenceExists(int serial){
        return sentRecords.contains(serial);
    }

    /**
     * get this word member's word identity
     * @return the word, in string, this word member represents
     */
    protected String getWord(){
        return word;
    }

    protected String getOriginalWord(){
        return originalWord;
    }

    protected int getTempId(){
        return tempId;
    }

    protected int getTempLowercaseFormId(){
        return tempLowercaseFormId;
    }

    protected String getLowerCaseForm(){
        return lowerCaseForm;
    }

    protected int getOccurrenceNum(){
        return occurrenceNum;
    }

    protected TreeSet<Integer> getSentRecords(){
        return sentRecords;
    }

    public Elements getDirs() {
        return dirs;
    }

    private void wordTransformation(){
        String tempWord;
        tempId = getWordID(tempWord = originalWord = word);

        if(tempId == NOT_A_WORD_ID) {
            return;
        }else if(tempId == NOT_FOUND_ID) {
            tempWord = checkAndRemoveQuoteSign(word);
            tempId = getWordID(tempWord);
        }

        if(lowerCaseForm.equals(tempWord))
            tempLowercaseFormId = tempId;
        else {
            tempLowercaseFormId = getWordID(lowerCaseForm);
            if(tempLowercaseFormId == NOT_FOUND_ID) {
                lowerCaseForm = checkAndRemoveQuoteSign(lowerCaseForm);
                tempLowercaseFormId = getWordID(lowerCaseForm);
                if(tempLowercaseFormId != NOT_FOUND_ID) {
                    if (tempLowercaseFormId == tempId)
                        lowerCaseForm = tempWord;
                    else
                        lowerCaseForm = getOriginalWord(tempLowercaseFormId);
                }
            } else {
                if(tempLowercaseFormId == tempId)
                    lowerCaseForm = tempWord;
                else
                    lowerCaseForm = getOriginalWord(tempLowercaseFormId);
            }
        }

        if(tempId != NOT_FOUND_ID){
            originalWord = getOriginalWord(tempId);
        }else if(tempLowercaseFormId != NOT_FOUND_ID){
            originalWord = lowerCaseForm;
            tempId = tempLowercaseFormId;
        } else {
            originalWord = tempWord;
        }
    }

    private int getWordID(String word){
        if(!FIRST_LETTER_PATTERN.matcher(word).find()){
            return NOT_A_WORD_ID;
        }

        String tempWord = word;
        String firstLetter;

        try (Connection connection = DBConnection.establishDictionaryConnection();
             PreparedStatement irv = connection.prepareStatement(IRV_SQL);
             PreparedStatement irn = connection.prepareStatement(IRN_SQL)) {

            irv.setString(1, word);
            irv.setString(2, word);
            ResultSet queryResult = irv.executeQuery();
            if (queryResult.isClosed()) {
                irn.setString(1, word);
                irn.setString(2, word);
                irn.setString(3, word);
                queryResult = irn.executeQuery();

                if (!queryResult.isClosed()) {
                    tempWord = queryResult.getString(1);
                }
            } else {
                tempWord = queryResult.getString(1);
            }

            queryResult.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        firstLetter = String.valueOf(tempWord.toLowerCase().charAt(0));
        firstLetter = FIRST_LETTER_PATTERN.matcher(firstLetter).find() ? firstLetter : "spec_char";

        try(ResultSet resultSet = ClipboardFunctionQuery.alternativeCheck(firstLetter, tempWord)) {
            if(!resultSet.isClosed()){
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return NOT_FOUND_ID;
    }

    private String getOriginalWord(int tempId){
        try(Connection connection = DBConnection.establishDictionaryConnection();
            PreparedStatement getDefPreparedStatement = connection.prepareStatement(ClipboardFunctionQuery.GET_DEF_SQL)){

            getDefPreparedStatement.setInt(1, tempId);
            ResultSet defResultSet = getDefPreparedStatement.executeQuery();

            Document document = Jsoup.parse(defResultSet.getString(1));
            Element word = document.selectFirst("div.hw > b");

            dirs = document.select("a.au");

            defResultSet.close();

            return word.text().replaceAll("·", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return SOMETHING_WRONG;
    }

    private String checkAndRemoveQuoteSign(String word){
        if (word.contains("'")) {
            if (POSSESSIVE_S.matcher(word).matches()) {
                char[] array = word.toCharArray();
                return String.copyValueOf(array, 0, array.length - 2);
            } else if (POSSESSIVES.matcher(word).matches()) {
                char[] array = word.toCharArray();
                return String.copyValueOf(array, 0, array.length - 1);
            }
        }

        return word;
    }
}

class TrimmedWordMember{
    private String originalWord;
    private String lowercaseForm;
    private int id;
    private int lowercaseFormId;
    private int occurrenceNum;
    private TreeMap<Integer, HashSet<String>> sentRecords = new TreeMap<>();
    private Elements dirs;

    public TrimmedWordMember(String originalWord, String lowercaseForm, int id, int lowercaseFormId, int firstOccurrenceNum, TreeMap<Integer, HashSet<String>> sentRecords, Elements dirs){
        this.originalWord = originalWord;
        this.lowercaseForm = lowercaseForm;
        this.id = id;
        this.lowercaseFormId = lowercaseFormId;
        this.occurrenceNum = firstOccurrenceNum;
        this.sentRecords.putAll(sentRecords);
        this.dirs = dirs;
    }

    protected TrimmedWordMember addNewOccurrenceNum(int newOccurrenceNum){
        this.occurrenceNum += newOccurrenceNum;
        return this;
    }

    protected void addNewSentRecords(TreeMap<Integer, HashSet<String>> newSentRecords){
        this.sentRecords.putAll(newSentRecords);
    }

    protected String getOriginalWord(){
        return originalWord;
    }

    protected String getLowercaseForm(){
        return lowercaseForm;
    }

    protected int getOccurrenceNum(){
        return occurrenceNum;
    }

    protected int getId(){
        return id;
    }

    protected int getLowercaseFormId(){
        return lowercaseFormId;
    }

    protected TreeMap<Integer, HashSet<String>> getSentRecords(){
        return sentRecords;
    }

    @Override
    public String toString(){
        return new StringBuilder(getOriginalWord()).append(", has a ID ").append(id).append(", and has occurred ").append(occurrenceNum).append(" times")
                .append("\n").append("the lowercase form is \"").append(lowercaseForm).append("\" has a ID: ").append(lowercaseFormId)
                .append("\n").append("in sentence ").append(sentRecords)
                .append("\n").toString();
    }

    public Elements getDirs() {
        return dirs;
    }
}

class SubSentenceThread implements Callable<Boolean>{
    private String sentence;
    private int sentSerial;
    private StringBuilder wordStringBuilder = new StringBuilder();
    private SentenceProcessingQuery sentenceProcessingQuery;
    private boolean previousBackQuoteSign = false;
    private boolean previousNBackQuoteSign = false;

    public SubSentenceThread(String sentence, int sentSerial, SentenceProcessingQuery sentenceProcessingQuery){
        this.sentence = sentence.trim();
        this.sentSerial = sentSerial;
        this.sentenceProcessingQuery = sentenceProcessingQuery;
        System.out.println(sentSerial + " " + sentence);
    }


    private void processChar(char character){
        if(SentenceProcessingQuery.isSeparateSign(character)){
            if(character == ' '){
                if(previousBackQuoteSign) {
                    appendCharacterToWord('’');
                    previousBackQuoteSign = false;
                } else if(previousNBackQuoteSign) {
                    appendCharacterToWord('\'');
                    previousNBackQuoteSign = false;
                } else {
                    sentenceProcessingQuery.addToWordList(wordStringBuilder.toString(), sentSerial);
                    trimWordBuilderSize();
                }
            }else if(character == '’') {
                previousBackQuoteSign = true;
            } else if(character == '\'') {
                previousNBackQuoteSign = true;
            }
        } else if(character == 10){
            System.out.println("10~~~~~");
        } else {
            if(previousBackQuoteSign){
                appendCharacterToWord('’');
                previousBackQuoteSign = false;
            } else if(previousNBackQuoteSign){
                appendCharacterToWord('\'');
                previousNBackQuoteSign = false;
            }

            appendCharacterToWord(character);
        }
    }

    /**
     * used for trim word StringBuilder size
     */
    private void trimWordBuilderSize() {
        if(wordStringBuilder.length() > 10) {
            wordStringBuilder.setLength(10);
            wordStringBuilder.trimToSize();
        }
        wordStringBuilder.setLength(0);
    }

    private void appendCharacterToWord(char character){
        wordStringBuilder.append(character);
    }

    @Override
    public Boolean call() {
        char tempArray[] = sentence.toCharArray();
        for(char letter : tempArray){
            processChar(letter);
        }

        return true;
    }
}