package me.flickersoul.dawn.functions;

import me.flickersoul.dawn.ui.ClipboardSearchBar;
import me.flickersoul.dawn.ui.EnDefRegion;
import me.flickersoul.dawn.ui.Thesaurus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.regex.Pattern;

public class ClipboardFunctionQuery {

    private static final String FILE_PATH = "jdbc:sqlite::resource:Dictionary/PRO.db";

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
            "</html>";;

    private static final String EMPTY_TEMPLATE = "<div style=\"text-align: center;\"> <h3> NOT FOUND </h3> <div>";

    static Connection connection;

    //定义查找的SQL
    static final String GET_ID_SQL_PART1 = "SELECT id FROM ";//做个测试，是大数据库查找快还是小数据库查找快
    static final String GET_ID_SQL_PART2 = " WHERE value = ?;";
    static final String GET_ID_SQL_PART3 = "SELECT id FROM ";
    static final String GET_ID_SQL_PART4 = " WHERE index_id = ?";
//    static final String GET_ID_TEST = "SELECT c0_id FROM all_words_content WHERE c1word_value = ?";
    static final String GET_DEF_SQL =  "SELECT contents FROM definitions WHERE id = ?";
    //定义发音筛选正则表达
    static final Pattern pattern = Pattern.compile("^[0-9]+");
    static final Pattern processWordPattern = Pattern.compile("[^'a-zA-Z0-9-\\s]");
    static Boolean isAutoPlaying = true;

    //初始化connection
    static {
        try {
            connection = DBConnection.getConnection(FILE_PATH);
            System.out.println("Connected!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean lookupWord(String word){
        try {
            new KindSoftAPIQuery(word, "KingSoftAPIThread").start();
            word = ClipboardFunctionQuery.processWords(word);
            PreparedStatement getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + word.toLowerCase().charAt(0) + GET_ID_SQL_PART2); //最快
            getIDPreparedStatement.setString(1, word);
            ResultSet idResultSet = getIDPreparedStatement.executeQuery();

            if(!idResultSet.next()){
                getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + word.toLowerCase().charAt(0) + GET_ID_SQL_PART2); //最快
                getIDPreparedStatement.setString(1, word.toLowerCase());
                idResultSet = getIDPreparedStatement.executeQuery();
                if(!idResultSet.next()) {
                    getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART1 + word.toLowerCase().charAt(0) + GET_ID_SQL_PART2);
                    char[] cs=word.toLowerCase().toCharArray();
                    cs[0]-=32;
                    getIDPreparedStatement.setString(1, String.valueOf(cs));
                    idResultSet = getIDPreparedStatement.executeQuery();
                    if (!idResultSet.next()) {
                        getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART3 + word.toLowerCase().charAt(0) + GET_ID_SQL_PART4);
                        getIDPreparedStatement.setString(1, word.replaceAll(" ", "").replaceAll("-", ""));
                        idResultSet = getIDPreparedStatement.executeQuery();
                        if (!idResultSet.next()) {
                            getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART3 + word.toLowerCase().charAt(0) + GET_ID_SQL_PART4);
                            getIDPreparedStatement.setString(1, word.toLowerCase().replaceAll(" ", "").replaceAll("-", ""));
                            idResultSet = getIDPreparedStatement.executeQuery();
                            if(!idResultSet.next()) {
                                getIDPreparedStatement = connection.prepareStatement(GET_ID_SQL_PART3 + word.toLowerCase().charAt(0) + GET_ID_SQL_PART4);
                                cs=word.toLowerCase().replaceAll(" ", "").replaceAll("-", "").toCharArray();
                                cs[0]-=32;
                                getIDPreparedStatement.setString(1, String.valueOf(cs));
                                idResultSet = getIDPreparedStatement.executeQuery();
                                if (!idResultSet.next()){
                                    EnDefRegion.html.setValue(EMPTY_TEMPLATE);
                                    Thesaurus.html.setValue(EMPTY_TEMPLATE);
                                    return false;
                                }
                            }
                        }
                    }
                }
            }

            String id= idResultSet.getString(1);

            System.out.println("The word, " + word + ", has an id: " + id);

            PreparedStatement getDefPreparedStatement = connection.prepareStatement(GET_DEF_SQL);
            getDefPreparedStatement.setString(1, id);
            ResultSet defResultSet = getDefPreparedStatement.executeQuery();

            Document document = Jsoup.parse(defResultSet.getString(1));

            Elements aTag = document.getElementsByTag("a");
            for(Element sub : aTag){
                String temp = sub.attr("href");
                if(temp.equals("#") || temp.equals("'#'") || temp.startsWith("x-mw://lookup/")){
                    sub.attr("onclick", "lookup(\"" + sub.text() + "\")");
                    sub.attr("href", "javascript.void(0)");
                }
            }

            Element definition = document.selectFirst("div#definition");

            Element thesaurus = document.selectFirst("div#thesaurus");

            if(thesaurus != null){
//                System.out.println(HEAD + thesaurus.toString() + THE_TAIL);
                Thesaurus.html.setValue(HEAD + thesaurus.toString() + THE_TAIL);
            }else{
                Thesaurus.html.setValue(EMPTY_TEMPLATE);
            }

            definition.select(".et").remove();
            definition.select(".sdef").remove();

            Elements audioElements = definition.select("a.au");

            int num = 0;

            for(Element singleAudio : audioElements) {
                String dir = singleAudio.attr("href");

                singleAudio.attr("href", "javascript.void(0)");

                singleAudio.attr("onclick", "player("+ num +")");

                singleAudio.append("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" t=\"1536995979877\" class=\"icon\" style=\"\" viewBox=\"0 0 1109 1024\" version=\"1.1\" p-id=\"1888\" width=\"16\" height=\"16\"><path " + "id=\"" + dir + "svg\" " + "d=\"M35.754667 338.176v348.501333h233.6l292.138666 290.346667V47.701333L269.354667 338.176H35.754667z m788.650666 174.208c0-104.533333-58.453333-191.701333-146.090666-232.362667v464.64c87.637333-40.618667 146.090667-127.658667 146.090666-232.277333zM678.314667 1.28v121.984c169.472 52.309333 292.138667 203.264 292.138666 389.162667 0 185.856-122.666667 336.896-292.138666 389.12v122.026666c233.685333-52.352 409.002667-261.418667 409.002666-511.146666 0-249.728-175.317333-458.837333-409.002666-511.146667z\" fill=\"#000000\"/></svg>");

                String audioURL = "https://media.merriam-webster.com/audio/prons/en/us/mp3/";
                if (dir.startsWith("bix")) {
                    audioURL += "bix/" + dir + ".mp3";
                } else if (dir.startsWith("gg")) {
                    audioURL += "gg/" + dir + ".mp3";
                } else if (pattern.matcher(dir).find()) {
                    audioURL += "number/" + dir + ".mp3";
                } else {
                    audioURL += dir.charAt(0) + "/" + dir + ".mp3";
                }

                System.out.println(audioURL);

                JSPlay.setAudioURL(num++, audioURL);
            }

            if(isAutoPlaying && audioElements.size() != 0 && JSPlay.getFirstAudioURL() != null) new JSPlay.audioThread(0).start();

            Elements fullDef = definition.select(".fulldef").addClass("collapsible");

            num = 0;

            for(Element sub : fullDef) {
                sub.attr("idx", num + "def");
                Element temp = sub.nextElementSibling();
                if(temp.className().equals("def")){
                    temp.addClass("c-content");
                    temp.attr("id", num + "def");
                }else {
                    temp = temp.nextElementSibling().addClass("c-content");
                    temp.attr("id", num + "def");
                }

                Elements otherForm = temp.getElementsByClass("infl").remove();
                if(otherForm.size() != 0){
                    sub.before("<div class=\"collapsible\" idx=\"" + num + "of\">Other Form</div>");
                    Element of_container = sub.before("<div class=\"c-content\" id=\"" + num + "of\">").previousElementSibling();
                    of_container.appendChild(otherForm.first());
                }

                otherForm = temp.getElementsByClass("runon").remove();
                if(otherForm.size() != 0){
                    sub.before("<div class=\"collapsible\" idx=\"" + num + "rn\">Run-on</div>");
                    Element runon_container = sub.before("<div class=\"c-content\" id=\"" + num + "rn\">").previousElementSibling();
                    runon_container.appendChild(otherForm.first());
                }
                num++;
            }

            Elements syn = definition.select("div.pos > div.syn").addClass("c-content");

            num = 0;

            for(Element sub : syn){
                sub.before("<div class=\"collapsible\" idx=\"" + num + "sy\" style=\"padding: 20px, 20px, 20px, 20px;\">Thesaurus</div>");
                sub.attr("id", num + "sy");
                num++;
            }

            definition.appendChild(new Element("div"));

//            System.out.println(HEAD + definition.toString() + TAIL);

            EnDefRegion.html.setValue(HEAD + definition.toString() + TAIL);

            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public static String processWords(String word){
        return processWordPattern.matcher(word).replaceAll("").trim();
    }

    public static void setAutoPlaying(Boolean isAutoPlaying){
        ClipboardFunctionQuery.isAutoPlaying = isAutoPlaying;
    }
}
