package me.flickersoul.dawn.functions;

import me.flickersoul.dawn.ui.ChDefRegion;

public class YoudaoDictQuery implements Runnable {
    private String word;
    private static final String url = "https://m.youdao.com/dict?le=eng&q=";

    @Override
    public void run() {
        loadHtml();
    }

    public YoudaoDictQuery setWord(String word){
        this.word = word.replaceAll(" ", "+");
        return this;
    }

    private void loadHtml(){
        if(word.equals("")) return;
        ChDefRegion.setHtml(url + word);
    }
}
