package me.flickersoul.dawn.functions;

import me.flickersoul.dawn.ui.ProgressingAnimationController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class CacheProcessWithIndication {

    private static final String UPDATE_AUDIO_INFO_SQL = "UPDATE total_word SET has_audio=? WHERE audio_dir=?";
    private Connection connection;
    private PreparedStatement updateAudioInfoStatement;
    Set<Callable<Boolean>> failedSet = new HashSet<>();


    public void commitChange(){
        int failedSize = failedSet.size();
        if(failedSize != 0) {
            controller.setChangableText("Trying download failed files: " + failedSize);
            CacheAudio.cacheBatchAudioFromWordDirWithFuture(failedSet);
        }

        try {
            updateAudioInfoStatement.executeBatch();
            connection.commit();

            updateAudioInfoStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ProgressingAnimationController controller;
    private double totalNum;
    private int currentNum;

    public void setTotalNum(int num){
        totalNum = num;
    }

    public CacheProcessWithIndication(ProgressingAnimationController c) {
        this.controller = c;
        connection = DBConnection.establishMaterialRepoConnection();
        try {
            connection.setAutoCommit(false);
            updateAudioInfoStatement = connection.prepareStatement(UPDATE_AUDIO_INFO_SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    class ComposingThread implements Callable<Boolean> {
        private String dir;

        public ComposingThread(String fileName){
            this.dir = fileName;
        }

        @Override
        public Boolean call() {
            if (!CacheAudio.isFileExisted(dir)) {
                String fileURL = CacheAudio.composeAudioURL(dir);

                File file = new File(new StringBuilder(CacheAudio.DIR_PATH).append(dir).append(".mp3").toString());
                try(ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(fileURL).openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(file)){
                    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    System.out.println("finish caching: " + fileURL);
                    controller.setProgress(++currentNum / totalNum);
                    controller.setChangableText(currentNum + "/" + totalNum + " | " + "Audio Info Updated");
                } catch (IOException e) {
                    System.err.println("Cannot Cache Audio: " + file.getPath());
                    failedSet.add(composeThread(dir));
                    return false;
                }

            }

            updateAudioInfo();

            return true;
        }

        private void updateAudioInfo(){
            try {
                updateAudioInfoStatement.setInt(1, 1);
                updateAudioInfoStatement.setString(2, dir);
                updateAudioInfoStatement.addBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ComposingThread composeThread(String dir){
        return new ComposingThread(dir);
    }
}
