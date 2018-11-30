package me.flickersoul.dawn.functions;

import javafx.application.Platform;
import me.flickersoul.dawn.ui.ClipboardSearchBar;
import me.flickersoul.dawn.ui.EnDefRegion;
import me.flickersoul.dawn.ui.Thesaurus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class ClipboardFunctionQuery {

    private static final String FILE_PATH = "jdbc:sqlite::resource:dic/PRO.db";

    private static final String HEAD ="<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>" +
            "</head>\n" +
            "<body>\n";

    private static final String TAIL = "<script>\n" +
            "   var coll = document.getElementsByClassName(\"collapsible\");\n" +
            "       for (i = 0; i < coll.length; i++) {\n" +
            "           coll[i].addEventListener(\"click\", function() {\n" +
            "               this.classList.toggle(\"active\");\n" +
            "               var content = document.getElementById(this.getAttribute(\"idx\"));\n" +
            "               if (content.style.maxHeight){\n" +
            "                   content.style.maxHeight = null;\n" +
            "               } else {\n" +
            "                   content.style.maxHeight = content.scrollHeight + \"px\";\n" +
            "               }\n" +
            "           });\n" +
            "       }\n" +
            "       function player(i){\n" +
            "           appPlayer.play(i);\n" +
            "       }\n" +
            "       function lookup(word){\n" +
            "           appPlayer.lookupWord(word)\n" +
            "       }\n"+
            "       function blog(){\n" +
            "           appPlayer.blog();\n" +
            "       }\n"+
            "</script>\n" +
            "</body>\n" +
            "</html>";

    private static final String THE_TAIL =
            "   <script>\n" +
            "       function lookup(word){\n" +
            "           appPlayer.lookupWord(word)\n" +
            "       }\n" +
            "   </script>\n" +
            "</body>\n" +
            "</html>";

    private static final String EMPTY_TEMPLATE = "<div style=\"text-align: center;\" style=\"margin-top: 10px;\"> <h3> NOT FOUND </h3> <div>";

    private static final String SVG_HTML = "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" t=\"1536995979877\" class=\"icon\" style=\"\" viewBox=\"0 0 1109 1024\" version=\"1.1\" p-id=\"1888\" width=\"26\" height=\"26\"><path " + "d=\"M35.754667 338.176v348.501333h233.6l292.138666 290.346667V47.701333L269.354667 338.176H35.754667z m788.650666 174.208c0-104.533333-58.453333-191.701333-146.090666-232.362667v464.64c87.637333-40.618667 146.090667-127.658667 146.090666-232.277333zM678.314667 1.28v121.984c169.472 52.309333 292.138667 203.264 292.138666 389.162667 0 185.856-122.666667 336.896-292.138666 389.12v122.026666c233.685333-52.352 409.002667-261.418667 409.002666-511.146666 0-249.728-175.317333-458.837333-409.002666-511.146667z\" fill=\"#000000\"/></svg>";

    protected static Connection connection;

    //定义查找的SQL
    protected static final String GET_ID_SQL_PART1 = "SELECT id FROM ";//做个测试，是大数据库查找快还是小数据库查找快
    protected static final String GET_ID_SQL_PART2 = " WHERE value = ?;";
    protected static final String GET_ID_SQL_PART3 = "SELECT id FROM ";
    protected static final String GET_ID_SQL_PART4 = " WHERE index_id = ?";
//    static final String GET_ID_TEST = "SELECT c0_id FROM all_words_content WHERE c1word_value = ?";
    private static final String GET_DEF_SQL =  "SELECT contents FROM definitions WHERE id = ?";
    //定义发音筛选正则表达
    private static final Pattern NUM_PATTERN = Pattern.compile("^[0-9]+");
    private static final Pattern PROCESS_WORD_PATTERN = Pattern.compile("[^'a-zA-Z0-9ā--\\s]");
    protected static final Pattern TRIM_FIRST_LETTER_PATTERN = Pattern.compile("[a-zA-Z0-9-]");
    protected static final Pattern FIRST_LETTER_PATTERN = Pattern.compile("[a-zA-Z]");
    private static Boolean isAutoPlaying = true;

    private static ExecutorService singThreadPool_API = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "KingSoft API Searching Thread"));
    private static ExecutorService multiThreadsPool_Audio = Executors.newFixedThreadPool(5, runnable -> new Thread(runnable, "Audio Caching Thread_Copy Thread"));

    private static KingSoftAPIQuery kingSoftAPIQuery;


    //静态初始化connection 和 Thread Pool
    static {
        try {
            connection = DBConnection.getConnection(FILE_PATH);
            kingSoftAPIQuery = new KingSoftAPIQuery();
            System.out.println("Connected!");
        } catch (SQLException e) {
            Platform.exit();
            e.printStackTrace();
        }
    }

    public static boolean lookupWord(String originalWord){
        long ST = System.currentTimeMillis();

        String word = ClipboardFunctionQuery.processWords(originalWord);
        System.out.println(word);
        ClipboardSearchBar.setText(word);

        try {
            String firstLetter;
            HistoryArray.setCurrentWord(word);
            singThreadPool_API.execute(kingSoftAPIQuery.setWord(word));
            firstLetter = String.valueOf(word.toLowerCase().charAt(0));
            firstLetter = FIRST_LETTER_PATTERN.matcher(firstLetter).find() ? firstLetter : "spec_char";

            ResultSet idResultSet = ClipboardFunctionQuery.alternativeCheck(firstLetter, originalWord);

            if(idResultSet.isClosed()) {
                if (firstLetter == "spec_char") {
                    idResultSet = ClipboardFunctionQuery.alternativeCheck(firstLetter, word);
                    if (idResultSet.isClosed()) {
                        System.out.println("second Phrase");
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

            String id = idResultSet.getString(1);

            System.out.println("The word, " + word + ", has an id: " + id);

            PreparedStatement getDefPreparedStatement = connection.prepareStatement(GET_DEF_SQL);
            getDefPreparedStatement.setString(1, id);
            ResultSet defResultSet = getDefPreparedStatement.executeQuery();

            Document document = Jsoup.parse(defResultSet.getString(1));

            getDefPreparedStatement.close();
            defResultSet.close();

            Element definition = document.selectFirst("div#definition");
            definition.attr("style", "margin-top: 10px;");

            Element thesaurus = document.selectFirst("div#thesaurus");

            if(thesaurus != null){
                thesaurus.attr("style", "margin-top: 10px;");
                Elements aTag = thesaurus.getElementsByTag("a");
                for(Element sub : aTag){
                    sub.attr("onclick", "lookup(\"" + sub.text() + "\")");
                    sub.attr("href", "javascript.void(0)");
                }
                Thesaurus.setHtml(HEAD + thesaurus.toString() + THE_TAIL);
            }else{
                Thesaurus.setHtml(EMPTY_TEMPLATE);
            }

            definition.select(".et").remove();
            definition.select(".sdef").remove();

            Elements audioElements = definition.select("a.au");

            int num = 0;

            for(Element singleAudio : audioElements) {
                String dir = singleAudio.attr("href");
                String audioURL;

                singleAudio.attr("href", "mp3");
                singleAudio.attr("onclick", "player(" + num + ")");
                singleAudio.append(SVG_HTML);

                if(CacheAudio.isFileExisted(dir)) {
                    audioURL = CacheAudio.fileDirBuilder(dir);
                }else{
                    audioURL = "https://media.merriam-webster.com/audio/prons/en/us/mp3/";
                    if (dir.startsWith("bix")) {
                        audioURL += "bix/" + dir + ".mp3";
                    } else if (dir.startsWith("gg")) {
                        audioURL += "gg/" + dir + ".mp3";
                    } else if (NUM_PATTERN.matcher(dir).find()) {
                        audioURL += "number/" + dir + ".mp3";
                    } else {
                        audioURL += dir.charAt(0) + "/" + dir + ".mp3";
                    }

                    multiThreadsPool_Audio.execute(CacheAudio.newCacheAudioThread(audioURL, dir));
                }
                System.out.println(audioURL);

                JSPlay.setAudioURL(num++, audioURL);
            }

            if(isAutoPlaying && audioElements.size() != 0 && JSPlay.getFirstAudioURL() != null) JSPlay.autoPlay();

            Elements aTag = definition.getElementsByTag("a");
            for(Element sub : aTag){
                String temp = sub.attr("href");
                if(!temp.equals("mp3") || temp.equals("#") || temp.equals("'#'") || temp.startsWith("x-mw://lookup/")){
                    sub.attr("onclick", "lookup(\"" + sub.text() + "\")");
                }
                sub.attr("href", "javascript.void(0)");
            }

            Elements fullDef = definition.select(".fulldef").addClass("collapsible");

            num = 0;

            for(Element sub : fullDef) {
                sub.attr("idx", num + "def");
                Element temp = sub.nextElementSibling();
                if(temp.className().equals("def")){
                    temp.addClass("marginbox")
                            .wrap("<div>")
                            .parent()
                            .addClass("c-content")
                            .attr("id", num + "def")
                            .attr("style", "box-shadow: inset 0px 11px 8px -10px #CCC, inset 0px -11px 8px -10px #CCC;");
                }else {
                    temp = temp.nextElementSibling()
                            .addClass("marginbox")
                            .wrap("<div>")
                            .parent()
                            .addClass("c-content")
                            .attr("id", num + "def")
                            .attr("style", "box-shadow: inset 0px 11px 8px -10px #CCC, inset 0px -11px 8px -10px #CCC;");
                }

                Elements otherForm = temp.getElementsByClass("infl").remove();
                if(otherForm.size() != 0){
                    sub.before("<div class=\"collapsible\" idx=\"" + num + "of\">Other Form</div>");
                    Element of_container = sub.before("<div>").previousElementSibling().addClass("marginbox").wrap("<div class=\"c-content\" id=\"" + num + "of\" style=\"box-shadow: inset 0px 11px 8px -10px #CCC, inset 0px -11px 8px -10px #CCC;\">");
                    of_container.appendChild(otherForm.first());
                }

                otherForm = temp.getElementsByClass("runon").remove();
                if(otherForm.size() != 0){
                    sub.before(new StringBuilder().append("<div class=\"collapsible\" idx=\"").append(num).append("rn\">Run-on</div>").toString());
                    Element runon_container = sub.before("<div>").previousElementSibling().addClass("marginbox").wrap("<div class=\"c-content\" id=\"" + num + "rn\" style=\"box-shadow: inset 0px 11px 8px -10px #CCC, inset 0px -11px 8px -10px #CCC;\">");
                    runon_container.appendChild(otherForm.first());
                }
                num++;
            }

            Elements syn = definition.select("div.pos > div.syn").addClass("marginbox").wrap("<div class=\"c-content\" style=\"box-shadow: inset 0px 11px 8px -10px #CCC, inset 0px -11px 8px -10px #CCC;\">");//.parents().addClass("c-content").attr("style", "box-shadow: inset 0px 11px 8px -10px #CCC, inset 0px -11px 8px -10px #CCC;");

            num = 0;

            for(Element sub : syn){
                sub.parent().before("<div class=\"collapsible\" idx=\"" + num + "sy\" style=\"padding: 20px, 20px, 20px, 20px;\">Thesaurus</div>");
                sub.parent().attr("id", num + "sy");
                num++;
            }

            definition.select("div.art > img").attr("alt", "Cannot Load Pic!");

            definition.appendChild(new Element("div"));

            EnDefRegion.setHtml(HEAD + definition.toString() + TAIL);

            System.out.println("Time Consumed: " + (System.currentTimeMillis() - ST) + "ms");

            System.gc();

            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }catch (StringIndexOutOfBoundsException e){
            e.printStackTrace();
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static String processWords(String word){
        return PROCESS_WORD_PATTERN.matcher(word).replaceAll("").trim();
    }

    public static void setAutoPlaying(Boolean isAutoPlaying){
        ClipboardFunctionQuery.isAutoPlaying = isAutoPlaying;
    }

    public static void terminatePool(){
        singThreadPool_API.shutdown();
        multiThreadsPool_Audio.shutdown();
    }

    protected static ResultSet alternativeCheck(String firstLetter, String word){
        ResultSet idResultSet = null;
        PreparedStatement getIDPreparedStatement = null;
        try {
            getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + firstLetter + GET_ID_SQL_PART2);
            getIDPreparedStatement.setString(1, word);
            idResultSet = getIDPreparedStatement.executeQuery();
            if(!idResultSet.next()){
                getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + firstLetter + GET_ID_SQL_PART2);
                getIDPreparedStatement.setString(1, word.toLowerCase());
                idResultSet = getIDPreparedStatement.executeQuery();
                System.out.println(word.toLowerCase() + "; " + firstLetter);
                if(!idResultSet.next()) {
                    getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + firstLetter + GET_ID_SQL_PART2);
                    char[] cs=word.toLowerCase().toCharArray();
                    cs[0]-=32;
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
                            if(!idResultSet.next()) {
                                getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART3 + firstLetter + GET_ID_SQL_PART4);
                                cs=word.toLowerCase().replaceAll(" ", "").replaceAll("-", "").toCharArray();
                                cs[0]-=32;
                                getIDPreparedStatement.setString(1, String.valueOf(cs));
                                idResultSet = getIDPreparedStatement.executeQuery();
                                if (!idResultSet.next()){
                                    EnDefRegion.setHtml(EMPTY_TEMPLATE);
                                    Thesaurus.setHtml(EMPTY_TEMPLATE);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }

        return idResultSet;
    }
}
