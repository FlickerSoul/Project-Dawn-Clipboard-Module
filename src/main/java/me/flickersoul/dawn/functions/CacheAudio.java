package me.flickersoul.dawn.functions;

import javafx.scene.media.AudioClip;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

public class CacheAudio extends Thread {
    String dir;
    URL url;
    static final Pattern pattern = Pattern.compile("^[0-9]+");

    @Override
    public void run(){
        System.out.println("Start Caching Audios");
        if(dir!=null){
            String audioURL = "https://media.merriam-webster.com/audio/prons/en/us/mp3/";
            if (dir.startsWith("bix")) {
                audioURL += "bix/";
            } else if (dir.startsWith("gg")) {
                audioURL += "gg/";
            } else if (pattern.matcher(dir).find()) {
                audioURL += "number/";
            } else {
                audioURL += dir.charAt(0) + "/" ;
            }
            audioURL += dir + ".mp3";
            try {
                URLConnection URLconnection = new URL(audioURL).openConnection();
                InputStream inputStream = URLconnection.getInputStream();
                OutputStream outputStream = new FileOutputStream(new File("src\\main\\resources\\Files\\" + dir + ".mp3"));
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                inputStream.close();
            } catch (FileNotFoundException e) {
                System.out.println("Cannot Find File");
                e.printStackTrace();
            } catch (MalformedURLException e) {
                System.out.println("Cannot Solve URL");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Cannot Read/Write Files");
                e.printStackTrace();
            }
        }else if(url != null){
            try {
                URLConnection URLconnection = url.openConnection();
                InputStream inputStream = URLconnection.getInputStream();
                OutputStream outputStream = new FileOutputStream(new File("src\\main\\resources\\Files\\" + dir + ".mp3"));
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.close();
                inputStream.close();
            } catch (FileNotFoundException e) {
                System.out.println("Cannot Find File");
                e.printStackTrace();
            } catch (MalformedURLException e) {
                System.out.println("Cannot Solve URL");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Cannot Read/Write Files");
                e.printStackTrace();
            }
        }else{
            System.out.println("No URL found.\nExited");
        }
    }

    public static void audioPlay(String audioURL){
        AudioClip audioClip = new AudioClip(audioURL);
        audioClip.play();
    }

    public CacheAudio(String urlLink){
        this.dir = urlLink;
    }

    public CacheAudio(URL url){
        this.url = url;
    }

}
