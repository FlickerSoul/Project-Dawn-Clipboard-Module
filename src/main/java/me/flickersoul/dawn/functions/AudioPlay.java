package me.flickersoul.dawn.functions;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaException;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.*;

public class AudioPlay {
    private static String[] audioURL = new String[15];
    private static AudioThread audioThread = new AudioThread();
    private static FutureTask<Boolean> audioTask;
    private static Desktop desktop = java.awt.Desktop.getDesktop();

    private static ExecutorService singleAudioPlayThreadPool = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable, "Audio Play Thread");
        thread.setDaemon(true);
        return thread;
    });

    public void lookupWord(String word){
        if(ClipboardFunctionQuery.lookupWord(word)){
            HistoryArray.insertSearchResult(word);
        }
    }

    public void play(int serial){
        singleAudioPlayThreadPool.execute(AudioPlay.generateSerialTask(serial));
    }

    public void netPlay(String url){
        singleAudioPlayThreadPool.execute(AudioPlay.generateUrlTask(url));
    }

    public void chooseWord(int serial){
        ClipboardFunctionQuery.decorateEnTab(String.valueOf(serial));
    }

    public void blog(){
        openBlog();
        System.gc();
    }

    public static void googleCurrentWord(String currentWord){
        if(desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create("https://www.google.com/search?q=" + currentWord.replaceAll(" ", "+")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.gc();
    }

    public static void autoPlay(){
        singleAudioPlayThreadPool.execute(generateFirstFileTask());
    }

    public static void setAudioURL(int serial, String aurioURL){
        AudioPlay.audioURL[serial] = aurioURL;
    }

    public static String getFirstAudioURL(){
        return AudioPlay.audioURL[0];
    }

    public static String getAudioURLByNum(int serial){
        return audioURL[serial];
    }

    public static void terminatePool(){
        singleAudioPlayThreadPool.shutdown();
    }

    private static FutureTask<Boolean> generateSerialTask(int serial){
        endTask();
        audioTask = new FutureTask<>(audioThread.setSource(serial));
        return audioTask;
    }

    private static FutureTask<Boolean> generateUrlTask(String url){
        endTask();
        audioTask = new FutureTask<>(audioThread.setSource(url));
        return audioTask;
    }

    private static FutureTask<Boolean> generateFirstFileTask(){
        endTask();
        audioTask = new FutureTask<>(audioThread.getFirst());
        return audioTask;
    }

    private static void endTask(){
        if(audioTask != null && !audioTask.isDone()) {
            audioThread.audioStop();
            audioTask.cancel(true);
        }
    }

    public static void submitUrlAudioTask(String url){
        singleAudioPlayThreadPool.submit(generateUrlTask(url));
    }

    public static void playAudioFromDir(String dir, int id) {
        if (CacheAudio.isFileExisted(dir)) {
            AudioPlay.submitUrlAudioTask(CacheAudio.fileDirBuilder(dir));
        } else {
            CacheAudio.cacheAudioFromWordDir(dir, id, true);
        }
    }

    public static void openBlog(){
        if(desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create("https://blog.flicker-soul.me"));
            } catch (IOException e) {
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
            if (serial == -1) {
                audioClip = new AudioClip(url);
            } else {
                audioClip = new AudioClip(AudioPlay.getAudioURLByNum(serial));
                serial = -1;
            }
            audioClip.play();
        }catch (NullPointerException e){
            System.err.println("Cannot Find Audio Due to Null Pointer Exception");
        }catch (MediaException e){
            System.err.println("Cannot Load Audio Due to Media Exception");
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

    public void audioStop(){
        if(audioClip != null)
            audioClip.stop();
    }
}