package me.flickersoul.dawn.functions;

import me.flickersoul.dawn.ui.ChDefRegion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class KingSoftAPIQuery implements Runnable {
    private static final String HEAD = "http://dict-co.iciba.com/api/dictionary.php?w=";
    private static final String TAIL = "&key=43F176D8E538CE59EAEEBB0FB3F2E5C1";

    String word;

    private final String TEMPLATE = "<html>\n" +
            "    <head></head>\n" +
            "    <body style=\"font: 15px;\">\n" +
            "        <div class=\"entry\" id=\"definition\" style=\"margin-top: 10px;\">\n" +
            "            <div class=\"pos\">\n" +
            "                <div class=\"hw\">\n" +
            "                    <b id=\"key\">\n" +
            "                        <!-- 单词填写位置 -->\n" +
            "                    </b>\n" +
            "                </div>"+
            "                <div>\n" +
            "                    <div class=\"pron\" id=\"0\">\n" +
            "                        <a class=\"au\" href=\"javascript.void(0)\" onclick=\"player()\">\n" +
            "                            <svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" t=\"1536995979877\" class=\"icon\" style=\"\" viewbox=\"0 0 1109 1024\" version=\"1.1\" p-id=\"1888\" width=\"26\" height=\"26\">\n" +
            "                                <path d=\"M35.754667 338.176v348.501333h233.6l292.138666 290.346667V47.701333L269.354667 338.176H35.754667z m788.650666 174.208c0-104.533333-58.453333-191.701333-146.090666-232.362667v464.64c87.637333-40.618667 146.090667-127.658667 146.090666-232.277333zM678.314667 1.28v121.984c169.472 52.309333 292.138667 203.264 292.138666 389.162667 0 185.856-122.666667 336.896-292.138666 389.12v122.026666c233.685333-52.352 409.002667-261.418667 409.002666-511.146666 0-249.728-175.317333-458.837333-409.002666-511.146667z\" fill=\"#000000\" />\n" +
            "                            </svg>\n" +
            "                        </a>\n" +
            "                        <span class=\"unicode\">\n" +
            "                            \n" +
            "                        </span>\n" +
            "                    </div>\n" +
            "                    <div class=\"pron\" id=\"1\">\n" +
            "                        <a class=\"au\" href=\"javascript.void(0)\" onclick=\"player()\">\n" +
            "                            <svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" t=\"1536995979877\" class=\"icon\" style=\"\" viewbox=\"0 0 1109 1024\" version=\"1.1\" p-id=\"1888\" width=\"26\" height=\"26\">\n" +
            "                                <path d=\"M35.754667 338.176v348.501333h233.6l292.138666 290.346667V47.701333L269.354667 338.176H35.754667z m788.650666 174.208c0-104.533333-58.453333-191.701333-146.090666-232.362667v464.64c87.637333-40.618667 146.090667-127.658667 146.090666-232.277333zM678.314667 1.28v121.984c169.472 52.309333 292.138667 203.264 292.138666 389.162667 0 185.856-122.666667 336.896-292.138666 389.12v122.026666c233.685333-52.352 409.002667-261.418667 409.002666-511.146666 0-249.728-175.317333-458.837333-409.002666-511.146667z\" fill=\"#000000\" />\n" +
            "                            </svg>\n" +
            "                        </a>\n" +
            "                        <span class=\"unicode\">\n" +
            "                            \n" +
            "                        </span>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "                \n" +
            "                <div class=\"qdef\">\n" +
            "                    <ul id=\"ul_defi\">\n" +
            "                        <!-- 释义 -->\n" +
            "                    </ul>     \n" +
            "                </div>\n" +
            "                <div class=\"sent\" style=\"margin-top: 10px;\">\n" +
            "                    <p>例句</p>\n" +
            "                    <ul id=\"ul_sent\">\n" +
            "                        <!-- 例句位置 -->\n" +
            "                    </ul>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "       <script>\n"+
            "           function player(i){\n" +
            "               appPlayer.netPlay(i);\n" +
            "           }\n" +
            "       </script>\n" +
            "    </body>\n" +
            "</html>";

    private final String EMPTY_TEMPLATE = "<div style=\"margin-top: 10px;\"> <h3> 查找错误,无结果 </h3> </div>";

    @Override
    public void run(){
        this.parseWordFromKSAPI(this.word);
    }

    public KingSoftAPIQuery setWord(String word){
        this.word = word;
        return this;
    }

    public void parseWordFromKSAPI(String word){
        if(word.equals(null)) return;
        try {
            Document apiPage = Jsoup.parse(new URL(HEAD + word + TAIL), 4000);
            Document wordTemplate = Jsoup.parse(TEMPLATE);
            wordTemplate.getElementById("key").text(word);
            Elements pron_url = apiPage.select("pron");
            if(pron_url.size() == 2){
                for(int i = 0; i < pron_url.size(); i++){
                    wordTemplate.selectFirst("div#" + i + ">a").attr("onclick", "player(\"" + pron_url.get(i).text() + "\")");
                    wordTemplate.selectFirst("div#" + i + ">span").text("\\" + pron_url.get(i).previousElementSibling().text() + "\\");
                }
            }else if(pron_url.size() == 1){
                wordTemplate.selectFirst("div#0>a").attr("onclick", "player(" + pron_url.get(0).text() + ")");
                wordTemplate.selectFirst("div#0>span").text("\\" + pron_url.get(0).previousElementSibling().text() + "\\");
                wordTemplate.selectFirst("div#0>span").text("null");
            }else{
                wordTemplate.selectFirst("div#0>span").text("null");
                wordTemplate.selectFirst("div#1>span").text("null");
            }

            Elements defi = apiPage.select("pos");
            Element ul_defi = wordTemplate.getElementById("ul_defi");
            if(defi.size() == 0){
                ul_defi.appendChild(new Element("li").text("无结果"));
            }else {
                for (Element sub : defi) {
                    ul_defi.appendChild(new Element("li").text(sub.text()));
                    ul_defi.appendChild(new Element("li").text("     " + sub.nextElementSibling().text()));
                }
            }

            Elements sent = apiPage.select("orig");
            Element ul_sent = wordTemplate.getElementById("ul_sent");
            if(sent.size() == 0){
                ul_sent.appendChild(new Element("li").text("无结果"));
            }
            for(Element sub : sent){
                ul_sent.appendChild(new Element("li").text(sub.text()).attr("style", "margin-top: 4px; margin-bottom: 2px;"));
                ul_sent.appendChild(new Element("li").text(sub.nextElementSibling().text()).attr("style", "margin-top: 2px; margin-bottom: 4px;"));
            }
            ChDefRegion.setHtml(wordTemplate.toString());
        } catch (MalformedURLException e){
            ChDefRegion.setHtml(EMPTY_TEMPLATE);
        } catch (IOException e) {
            ChDefRegion.setHtml(EMPTY_TEMPLATE);
            e.printStackTrace();
        }
    }
}
