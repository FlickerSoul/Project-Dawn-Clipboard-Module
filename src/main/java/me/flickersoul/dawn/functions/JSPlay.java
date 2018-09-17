package me.flickersoul.dawn.functions;

import javafx.scene.media.AudioClip;

public class JSPlay {
    public static String[] audioURL = new String[15];

    public void play(int serial){
        new audioThread(serial).start();
        //完善逻辑
    }

    static class audioThread extends Thread{
        int serial = -1;
        String url;

        public audioThread(int serial){
            this.serial = serial;
        }

        public audioThread(String url){
            this.url = url;
        }

        @Override
        public void run(){
            if(serial != -1)
                new AudioClip(audioURL[serial]).play();
            else
                new AudioClip(url).play();
        }
    }
}
