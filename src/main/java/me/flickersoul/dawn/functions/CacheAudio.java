package me.flickersoul.dawn.functions;

import me.flickersoul.dawn.ui.ClipboardOnly;
import me.flickersoul.dawn.ui.ProgressingAnimationController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class CacheAudio {

    public static final String DIR_PATH = new StringBuilder(ClipboardOnly.CACHE_DIR)
                                                            .append("audio")
                                                            .append(File.separator)
                                                            .toString(); //写入
    private static final String FILE_PATH = "file:/" + DIR_PATH.replaceAll("\\\\", "/");
    private static final File file_path_dir = new File(DIR_PATH);

    private static ExecutorService multiThreadsAudioCachePool = Executors.newFixedThreadPool(5, runnable -> new Thread(runnable, "Audio Caching Thread_Copy Thread"));

    static {
        file_path_dir.mkdirs();
        if (file_path_dir.canRead() && file_path_dir.canWrite()) {
            System.out.println("Access To Cache Folder Successfully");
        } else
            System.exit(-7);
    }

    public static boolean isFileExisted(String dir) {
        return new File(CacheAudio.dirBuilder(dir)).canRead();
    }

    public static String dirBuilder(String dir) {
        return DIR_PATH + dir + ".mp3";
    }

    public static String fileDirBuilder(String dir) {
        return FILE_PATH + dir + ".mp3";
    }

    public static Future<String> submitCacheAudioTask(String url, String dir, boolean isAutoPlay){
        return multiThreadsAudioCachePool.submit(new CacheProcess(url, dir, isAutoPlay));
    }

    public static void shutdownMultiThreadsAudioCachePool(){
        multiThreadsAudioCachePool.shutdown();
    }

    //TODO 扫描本地

    public static void cacheAudioFromWordDir(String dir, int id, boolean autoPlay){
        multiThreadsAudioCachePool.execute(new CacheProcessByWordDir(dir, id, autoPlay));
    }


    public static void cacheBatchAudioFromWordDirWithFuture(Set<Callable<Boolean>> set){
        try {
            multiThreadsAudioCachePool.invokeAll(set);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String composeAudioURL(String dir){
        StringBuilder url = new StringBuilder("https://media.merriam-webster.com/audio/prons/en/us/mp3/");
        if (dir.startsWith("bix")) {
            url.append("bix/").append(dir);
        } else if (dir.startsWith("gg")) {
            url.append("gg/").append(dir);
        } else if (ClipboardFunctionQuery.NUM_PATTERN.matcher(dir).find()) {
            url.append("number/").append(dir);
        } else {
            url.append(dir.charAt(0)).append("/").append(dir);
        }
        url.append(".mp3");

        return url.toString();
    }
}

class CacheProcess implements Callable<String>{
    private String fileURL;
    private String fileName;
    private boolean isAutoPlay;


    public CacheProcess(String fileURL, String fileName, boolean isAutoPlay) {
        this.fileURL = fileURL;
        this.fileName = fileName;
        this.isAutoPlay = isAutoPlay;
    }

    @Override
    public String call() {
        File file = new File(new StringBuilder(CacheAudio.DIR_PATH).append(fileName).append(".mp3").toString());
        try(ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(fileURL).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileChannel fileChannel = fileOutputStream.getChannel()){
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

            if(isAutoPlay){
                AudioPlay.submitUrlAudioTask(CacheAudio.fileDirBuilder(fileName));
            }
        } catch (IOException e) {
            System.err.println("Cannot Cache Audio: " + file.getPath());
        }

        return null;
    }
}

class CacheProcessByWordDir implements Runnable{
    private String dir;
    private int id;
    private boolean autoPlay;

    public CacheProcessByWordDir(String dir, int id, boolean autoPlay) {
        this.dir = dir;
        this.id = id;
        this.autoPlay = autoPlay;
    }

    @Override
    public void run() {
        if(CacheAudio.isFileExisted(dir)) {
            System.out.println(dir + ".mp3 has already been downloaded to local storage");
        } else {
            String url = CacheAudio.composeAudioURL(dir);
            System.out.println(url);
            CacheAudio.submitCacheAudioTask(url, dir, autoPlay);
        }
    }

}