package me.flickersoul.dawn.functions;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaException;

import java.util.concurrent.*;

public class JSPlay {
    protected static String[] audioURL = new String[15];
    private static AudioThread audioThread = new AudioThread();
    private static FutureTask<Boolean> audioTask;

    private static ExecutorService singleThreadPool = Executors.newSingleThreadExecutor((runnable) -> new Thread(runnable, "Audio Playing Thread"));

    public void lookupWord(String word){
        if(ClipboardFunctionQuery.lookupWord(word)){
            HistoryArray.insertSearchResult(word);
        }
    }

    public void play(int serial){
        JSPlay.endTask(5l);
        singleThreadPool.execute(JSPlay.generateSerialTask(serial));
    }

    public void netPlay(String url){
        JSPlay.endTask(3l);
        singleThreadPool.execute(generateUrlTask(url));
    }

    public static void autoPlay(){
        JSPlay.endTask(5l);
        singleThreadPool.execute(generateFirstFileTask());
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

    private static FutureTask<Boolean> generateSerialTask(int serial){
        audioTask = new FutureTask<>(audioThread.setSource(serial));
        return audioTask;
    }

    private static FutureTask<Boolean> generateUrlTask(String url){
        audioTask = new FutureTask<>(audioThread.setSource(url));
        return audioTask;
    }

    private static FutureTask<Boolean> generateFirstFileTask(){
        audioTask = new FutureTask<>(audioThread.getFirst());
        return audioTask;
    }

    private static void endTask(long timeout){
        if(audioTask != null) {
            try {
                audioTask.get(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
}

class AudioThread implements Callable<Boolean> {

    private AudioClip audioClip;
    private int serial = -1;
    private String url;

    public Boolean call(){
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

        System.gc();
        return true;
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