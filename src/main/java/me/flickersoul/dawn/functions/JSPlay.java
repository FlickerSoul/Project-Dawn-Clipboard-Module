package me.flickersoul.dawn.functions;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaException;
import me.flickersoul.dawn.ui.ClipboardSearchBar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JSPlay {
    protected static String[] audioURL = new String[15];
    private static AudioThread audioThread = new AudioThread();
    private static ExecutorService singleThreadPool = Executors.newSingleThreadExecutor((runnable) -> new Thread(runnable, "Audio Playing Thread"));

    public void lookupWord(String word){
        ClipboardSearchBar.setText(word);
        if(ClipboardFunctionQuery.lookupWord(word)){
            HistoryArray.insertSearchResult(word);
        }
    }

    public void play(int serial){
        singleThreadPool.execute(audioThread.setSource(serial));
    }

    public void netPlay(String url){
        singleThreadPool.execute(audioThread.setSource(url));
    }

    public static void autoPlay(){
        singleThreadPool.execute(audioThread.getFirst());
    }

    public static void setAudioURL(int serial, String aurioURL){
        JSPlay.audioURL[serial] = aurioURL;
    }

    public static String getFirstAudioURL(){
        return JSPlay.audioURL[0];
    }

    public static String getAudioURLByNum(int serial){
        return audioURL[serial];
    }

    public static void terminatePool(){
        singleThreadPool.shutdown();
    }
}

class AudioThread implements Runnable{

    private AudioClip audioClip;
    private int serial = -1;
    private String url;

    @Override
    public void run(){
        try {
            if (serial != -1) {
                audioClip = new AudioClip(JSPlay.getAudioURLByNum(serial));
                serial = -1;
            } else {
                audioClip = new AudioClip(url);
            }
            audioClip.play();
        }catch (NullPointerException e){
            e.printStackTrace();

        }catch (MediaException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public AudioThread setSource(String url){
        this.url = url;
        return this;
    }

    public AudioThread setSource(int serial){
        this.serial = serial;
        return this;
    }

    public AudioThread getFirst(){
        serial = 0;
        return this;
    }
}
