package me.flickersoul.dawn.functions;

import javafx.scene.media.AudioClip;
import me.flickersoul.dawn.ui.ClipboardSearchBar;

public class JSPlay {
    private static String[] audioURL = new String[15];

    public void lookupWord(String word){
        ClipboardSearchBar.setText(word);
        if(ClipboardFunctionQuery.lookupWord(word)){
            HistoryArray.insertSearchResult(word);
        }
    }

    public void play(int serial){
        new audioThread(serial).start();
    }

    public void netPlay(String url){
        new audioThread(url).start();
    }

    static class audioThread extends Thread{
        static AudioClip audioClip;
        int serial = -1;
        String url;

        public audioThread(){
            super("Audio Playing Thread");
        }

        public audioThread(int serial){
            this.serial = serial;
        }

        public audioThread(String url){
            this.url = url;
        }

        @Override
        public void run(){
            if(serial != -1){
                if(audioClip != null)
                    audioClip.stop();
                audioClip = new AudioClip(audioURL[serial]);
                audioClip.play();
                serial = -1;
            }else {
                if(audioClip != null)
                    audioClip.stop();
                audioClip = new AudioClip(url);
                audioClip.play();
            }
        }
    }

    public static void setAudioURL(int serial, String aurioURL){
        JSPlay.audioURL[serial] = aurioURL;
    }

    public static String getFirstAudioURL(){
        return JSPlay.audioURL[0];
    }
}
