package me.flickersoul.dawn.functions;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static me.flickersoul.dawn.functions.ClipboardFunctionQuery.*;

public class SentenceProcessingQuery implements Runnable {
    public static void main(String[] args){
//        File file = new FileChooser().returnFilesUsingChooser()[0];
//
//        File file = new File("G:\\temp\\newfile2.txt");
//        try {
//            if(file != null && file.canRead()) {
//                FileReader fr = new FileReader(file);
//                int ch;
//                while ((ch = fr.read()) != -1) {
//                    System.out.println(new StringBuilder().append((char)ch).append(' ').append(ch).toString());
//
//                }
//
//                System.out.println("Time Consumed: " + (System.currentTimeMillis() - ST) + "ms");//13 + 10
//                long ET = System.currentTimeMillis();
//                fr.close();
//                System.out.println("Time Consumed: " + (System.currentTimeMillis() - ET) + "ms" + "\n" );
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }//cannot close bug.
//
//        Thread thread = new Thread(new SentenceProcessingQuery(new File("G:\\\\temp\\\\newfile4.txt")));
//        Thread thread = new Thread(new SentenceProcessingQuery("Image A fake pop-up notice warning that a virus has infected a computer. By Vindu Goel and Suhasini Raj MUMBAI, India — You know the messages. They pop up on your computer screen with ominous warnings like, “Your computer has been infected with a virus. Call our toll-free number immediately for help.” Often they look like alerts from Microsoft, Apple or Symantec. Sometimes the warning comes in a phone call. Most people ignore these entreaties, which are invariably scams. But one in five recipients actually talks to the fake tech-support centers, and 6 percent ultimately pay the operators to “fix” the nonexistent problem, according to recent consumer surveys by Microsoft. Law enforcement authorities, working with Microsoft, have now traced many of these boiler rooms to New Delhi, India’s capital and a hub of the global call-center industry. On Tuesday and Wednesday, police from two Delhi suburbs raided 16 fake tech-support centers and arrested about three dozen people. Last month, the Delhi authorities arrested 24 people in similar raids on 10 call centers. In Gautam Budh Nagar, one of the suburbs, 50 police officers swept into eight centers on Tuesday night. Ajay Pal Sharma, the senior superintendent of police there, said the scammers had extracted money from thousands of victims, most of whom were American or Canadian. “The modus operandi was to send a pop-up on people’s systems using a fake Microsoft logo,” Mr. Sharma said. After the victims contacted the call center, the operator, pretending to be a Microsoft employee, would tell them that their system had been hacked or attacked by a virus. The victims would then be offered a package of services ranging from $99 to $1,000 to fix the problem, he said. Such scams are widespread, said Courtney Gregoire, an assistant general counsel in Microsoft’s digital crimes unit. Microsoft, whose Windows software runs most personal computers, gets 11,000 or so complaints about the scams every month, she said, and its internet monitors spot about 150,000 pop-up ads for the services every day. The company’s own tech-support forums, where people can publicly post items, also see a steady stream of posts offering fake tech-support services. Although American authorities have busted such scams in places like Florida and Ohio, the backbone of the illicit industry is in India — in large part because of the country’s experience running so many of the world’s call centers. India’s outsourcing industry, which includes call centers, generates about $28 billion in annual revenue and employs about 1.2 million people. “The success of the legitimate industry has made it easier for the illegitimate industry there,” Ms. Gregoire said. As in any con, experience helps. “You have to convince them they have a problem,” she said. “You have to have the touch.” For tech companies, combating the impersonators is complicated by the fact that many legitimate tech-support operations, including some of Microsoft’s, operate from India. The scam is quite lucrative. Researchers at Stony Brook University, who published a detailed study of fake tech-support services last year, estimated that a single pop-up campaign spread over 142 web domains brought in nearly $10 million in just two months. Najmeh Miramirkhani, lead author of the research paper, said the network of entities involved in the scams was complex, with some making their own calls and others running the sites but outsourcing the calls to India. Many of the scammers also share data with one another. “This is an organized crime,” she said. Microsoft said it was working with other tech industry leaders such as Apple and Google, as well as law enforcement, to fight the scourge, which is migrating beyond the English-speaking world to target other users in their local languages. In the 16 countries surveyed by Microsoft, people in India and China were the most likely to pay the con artists. The problem extends beyond fake tech support, too. In July, the Justice Department said 24 people in eight states had been convicted for their roles in a scheme to use Indian call-center agents to impersonate tax collectors at the Internal Revenue Service. The thieves duped more than 15,000 people out of hundreds of millions of dollars. Thirty-two contractors in India were also indicted. Mr. Sharma said that in a similar con broken up by his department, call-center agents had impersonated Canadian tax authorities. Like the I.R.S., Microsoft and other legitimate technology companies do not call their users out of the blue. Nor do they send security alerts to the screen telling customers to call them. Ms. Miramirkhani had some simple advice to avoid being conned: Don’t pick up the phone. Vindu Goel reported from Mumbai, and Suhasini Raj from New Delhi. Follow Vindu Goel and Suhasini Raj on Twitter: @vindugoel and @suhasiniraj. A version of this article appears in print on , on Page B5 of the New York edition with the headline: That Fake Virus Alert And Its $1,000 ‘Repair’ May Come From India. Order Reprints | Today’s Paper | Subscribe\n"));
//        Thread thread = null;
//        try {
//            thread = new Thread(new SentenceProcessingQuery(new URL("https://www.nytimes.com/2018/11/29/technology/microsoft-apple-worth-how.html")));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        thread.start();

        WordMember wordMember = new WordMember("I", 1);
        WordMember wordMember1 = new WordMember("Love", 2);
        WordMember wordMember2 = new WordMember("You", 2);
        WordMember wordMember3 = new WordMember("!", 3);

        wordMember.addShowingNum().addShowingNum().addShowingNum().addShowingNum();
        wordMember1.addShowingNum();
        wordMember2.addShowingNum().addShowingNum();
        wordMember3.addShowingNum().addShowingNum().addShowingNum();

        wordMember.addSentSerial(2);

        TrimmedWordMember trimmedWordMember = new TrimmedWordMember();

        Thread thread = new Thread(new ProcessingThread().setTrimmedWordMember(trimmedWordMember, wordMember));

        thread.start();

    }

    private TreeMap<Integer, String> sentenceMap = new TreeMap<>();
    private int sentIterator = 0;
    private TreeMap<String, WordMember> wordList = new TreeMap<>();
    private TreeMap<String, TrimmedWordMember> trimmedWordList = new TreeMap<>();
    private File file;
    private String text;
    private URL url;
    private boolean isLastCharEndingSign = false;
    private StringBuilder wordStringBuilder = new StringBuilder();
    private StringBuilder sentStringBuilder = new StringBuilder();
    private ExecutorService processWordsPool = Executors.newFixedThreadPool(3, runnable -> new Thread(runnable, "Word Process Thread"));

    private static final Pattern CAP_LETTER_PATTERN = Pattern.compile("['A-Z]");
    private static final Set<String> ABBR_WORDS_SET = new HashSet<>(Arrays.asList(
            "q.t.s",
            "q.t.",
            "p.r.s",
            "p.r.",
            "mr.",
            "mrs.",
            "i.e.",
            "e.g.",
            "d.t.'s",
            "St.",
            "Ste.",
            "Mss.",
            "Mses.",
            "Ms.",
            "Mrs.",
            "Mr.",
            "Messrs.",
            "A.",
            "B.",
            "C.",
            "D.",
            "E.",
            "F.",
            "G.",
            "H.",
            "I.",
            "J.",
            "K.",
            "L.",
            "M.",
            "N.",
            "O.", // 名字处理
            "P.",
            "Q.",
            "R.",
            "S.",
            "T.",
            "U.",
            "V.",
            "W.",
            "X.",
            "Y.",
            "Z.",
            "No.",
            "Jan." //数字处理
            ));

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
            temp.addShowingNum().addSentSerial(sentIterator);
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

        }
        sentStringBuilder.setLength(0);
    }

    private void trimWordBuilderSize() {
        if(wordStringBuilder.length() > 20) {
            wordStringBuilder.setLength(20);
            wordStringBuilder.trimToSize();

        }
        wordStringBuilder.setLength(0);
    }

    @Override
    public void run() {
        long ST = System.currentTimeMillis();
        if(text != null){
            char[] tempArray = text.toCharArray();
            for(char sub : tempArray){
                appendProcessing((int)sub);
            }

//            return true;
        }else if(url != null){
            try {
                 String json = Jsoup.connect(new StringBuilder("https://mercury.postlight.com/parser?url=").append(url.toString()).toString())
                        .ignoreContentType(true)
                        .header("Accept", "application/json")
                        .header("Content-Type","application/json")
                        .header("x-api-key", "EaHXKMmVBHblTqpd9Q5Zxswu9oB68NIPImNSABbx")
                        .method(org.jsoup.Connection.Method.GET)
                        .execute().body();
                 if(json != null){
                     Document content = Jsoup.parse(new JSONObject(json).getString("content"));
                     char[] tempArray = content.text().toCharArray();
                     for(char sub : tempArray){
                         appendProcessing((int)sub);
                     }
                 }else{
                     System.out.println("Cannot open URL");
                 }
            } catch (IOException e) {
                e.printStackTrace();
                //do something, a pop-up or error message.
            }

        }else if(file != null){
            try {
                if(file.canRead()) {
                    FileReader fr = new FileReader(file);
                    int character;
                    while ((character = fr.read()) != -1) {
                        appendProcessing(character);
                    }
                    fr.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Iterator iterator = wordList.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = entry.getKey().toString();
            System.out.println(key);
        }
        System.out.println("\n");
        Iterator sent = sentenceMap.entrySet().iterator();
        while(sent.hasNext()){
            Map.Entry entry = (Map.Entry) sent.next();
            System.out.println(entry.getKey().toString() + " " + entry.getValue().toString());
        }

//        return false;
        System.out.println("Time Consumed: " + (System.currentTimeMillis() - ST) + "ms");//13 + 10
    }

    /**
     * start appending processing
     * @param character char in int
     */
    private void appendProcessing(int character){
        if(isSeparateSign(character)){
            if(isLastCharEndingSign){
                if(isQuoteSign(character)){
                    appendCharacterToSent(character);
                }else if(isSpace(character)) {
                    setLastCharEndingSignFalse();
                    submitSentence(character);
                }else {
                    appendCharacterToWord(character);
                    appendCharacterToSent(character);
                }
            }else {
                submitWord();
                appendCharacterToSent(character);
            }
        }else if(isEndingSign(character)){
            if(character == '.' && checkAndProcessAbbrWord(wordStringBuilder)){
                appendCharacterToSent(character);
            }else {
                setLastCharEndingSignTrue();
                //在下一句开头时候提交句子
                submitWord();
                appendCharacterToSent(character);
            }
        }else if(character == 10){
            if(sentStringBuilder.length() == 0 && character == 10 || character == 13){

            }else {
                if(wordStringBuilder.length() != 0)
                    submitWord();
                submitSentence(character);
            }
            setLastCharEndingSignFalse();
        }else if(character == 13){

        }else {
            appendCharacterToWord(character);
            appendCharacterToSent(character);
        }
    }

    /**
     * set isLastCharEndingSign true
     */
    private void setLastCharEndingSignTrue(){
        isLastCharEndingSign = true;
    }

    /**
     * set isLastCharEndingSign false
     */
    private void setLastCharEndingSignFalse(){
        isLastCharEndingSign = false;
    }

    /**
     * submit current sentence to sentence list
     * @param character char in int
     */
    private void submitSentence(int character){
        this.addNewSent(sentStringBuilder.append(character == 10 || character == 13 ? ' ' : (char) character).toString());
        this.trimSentBuilderSize();
    }

    /**
     * append @param character to temp word
     * @param character
     */
    private void appendCharacterToWord(int character){
        wordStringBuilder.append((char)character);
    }

    /**
     * append @param character to temp sent
     * @param character char in int
     */
    private void appendCharacterToSent(int character){
        sentStringBuilder.append((char)character);
    }

    /**
     * submit temp word to word list
     */
    private void submitWord(){
        this.addToWordList(wordStringBuilder.toString());
        this.trimWordBuilderSize();
    }

    /**
     * check whether the character is one of the ending signs
     * @param character char in int
     * @return true when the character is one of the ending signs
     */
    private boolean isEndingSign(int character){
        return character == '.' || character == '?' || character == '!';
    }

    /**
     * check whether the character is one of the separating signs
     * @param character char in int
     * @return return true when the character is one of the separating signs
     */
    private boolean isSeparateSign(int character){
        return character == ' ' || character == ';' || character == ',' || character == ':' || character == '\'' || character == '(' || character == ')' || character == '\"' || character == '“' || character == '”';
    }

    /**
     * check whether the character is one of teh quoting signs
     * @param character char in int
     * @return return true when the character is one of the quoting signs
     */
    private boolean isQuoteSign(int character){
        return character == '’' || character == '\'' || character == '(' || character == ')' || character == '\"' || character == '“' || character == '”';
    }

    /**
     * check whether the character is a space
     * @param character char in int
     * @return return true when the character is a spece
     */
    private boolean isSpace(int character){
        return character == ' ';
    }

    /**
     *
     * @param tempWord
     */
    private boolean checkAndProcessAbbrWord(StringBuilder tempWord){
        if(ABBR_WORDS_SET.contains(tempWord + ".") || tempWord.toString().contains(".")){
            tempWord.append('.');
            return true;
        }else{
            return false;
        }
    }
}

class WordMember {
    private String word;
    private int occurrenceNum;
    private ArrayList<Integer> sentRecords = new ArrayList<>();

    /**
     * init a word member.
     * @param word the word this word member represents
     * @param firstSentSerial the sentence serial where this word first shows
     */
    public WordMember(String word, int firstSentSerial) {
        this.word = word;
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
    public String getWord(){
        return word;
    }

    /**
     * get the number of occurrence
     * @return this word member's occurrence number
     */
    public int getOccurrenceNum(){
        return occurrenceNum;
    }

    /**
     * get all the sentence records as a ArrayList
     * @return a ArrayList of all sentence records
     */
    public ArrayList<Integer> getSentSerial(){
        return sentRecords;
    }

}

class TrimmedWordMember {
    private String originalWord;
    private int wordID;
    private int showingNum;
    private TreeSet<String> showedForms = new TreeSet();
    private ArrayList<Integer> sentSerial = new ArrayList<>();

    public TrimmedWordMember(){

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

    @Override
    public String toString(){
        StringBuilder string = new StringBuilder("This trimmed word is ").append(originalWord).append(", has a ID ")
                .append(wordID).append(", has been shown for ").append(showingNum)
                .append(" times, and has been in these sentences: ");
        Iterator iterator = sentSerial.iterator();
        while(iterator.hasNext()){
            string.append(iterator.next()).append(" ");
        }
        return string.toString();
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
        checkWordExistence(tempTrimmedWordMember, wordMember);
    }

    public ProcessingThread setTrimmedWordMember(TrimmedWordMember tempTrimmedWordMember, WordMember wordMember){
        this.tempTrimmedWordMember = tempTrimmedWordMember;
        this.wordMember = wordMember;

        return this;
    }

    public void checkWordExistence(TrimmedWordMember trimmedWordMember, WordMember wordMember){
        String originalWord = wordMember.getWord();
        String word = ClipboardFunctionQuery.processWords(originalWord);

        if(!FIRST_LETTER_PATTERN.matcher(wordMember.getWord()).find()){
            trimmedWordMember.setWordID(NOT_FOUND_ID);
            return;
        }

        try {
            String firstLetter;
            firstLetter = String.valueOf(word.toLowerCase().charAt(0));
            firstLetter = FIRST_LETTER_PATTERN.matcher(firstLetter).find() ? firstLetter : "spec_char";

            ResultSet idResultSet = ClipboardFunctionQuery.alternativeCheck(firstLetter, originalWord);

            if(idResultSet.isClosed()) {
                if (firstLetter == "spec_char") {
                    idResultSet = ClipboardFunctionQuery.alternativeCheck(firstLetter, word);
                    if (idResultSet.isClosed()) {
                        char[] cs = word.toCharArray();
                        int i = 0;
                        for (; i < cs.length; i++) {
                            if (TRIM_FIRST_LETTER_PATTERN.matcher(String.valueOf(cs[i])).find())
                                break;
                        }
                        firstLetter = String.valueOf(cs[i]);
                        firstLetter = FIRST_LETTER_PATTERN.matcher(firstLetter).find() ? firstLetter : "spec_char";
                        idResultSet = ClipboardFunctionQuery.alternativeCheck(firstLetter, String.valueOf(cs, i, cs.length - i));
                    }
                } else {
                    idResultSet = ClipboardFunctionQuery.alternativeCheck(firstLetter, word);
                }
            }

            int id;
            if(!idResultSet.isClosed()) {
                id = idResultSet.getInt(1);

                trimmedWordMember.setShowingNum(wordMember.getOccurrenceNum());

                PreparedStatement getWordsWithSameIDStatement = connection.prepareStatement(GET_WORDS_WITH_SAME_ID_SQL);
                getWordsWithSameIDStatement.setInt(1, id);
                ResultSet wordsWithSameID = getWordsWithSameIDStatement.executeQuery();

                if (wordsWithSameID.isClosed())
                    trimmedWordMember.setShowedForms(null);
                else {
                    while (wordsWithSameID.next()) {
                        trimmedWordMember.pushShowedForms(wordsWithSameID.getString(1));
                    }
                }

                trimmedWordMember.setSentSerial(wordMember.getSentSerial());
            }else
                id = NOT_FOUND_ID;

            trimmedWordMember.setWordID(id);

            System.out.println("inner" + trimmedWordMember);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
