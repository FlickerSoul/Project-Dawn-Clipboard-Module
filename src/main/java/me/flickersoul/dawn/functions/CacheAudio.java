package me.flickersoul.dawn.functions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class CacheAudio implements Runnable {
    private String fileURL;
    private String fileName;
    private static final String DIR_PATH = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "PD_Cache" + File.separator;
    private static final String FILE_PATH = "file:/" + DIR_PATH.replaceAll("\\\\", "/");
    private static final boolean isWin = System.getProperty("os.name").equals("Windows 10");
    private static final File file_path_dir = new File(DIR_PATH);

    static {
        file_path_dir.mkdirs();
        if (file_path_dir.canRead() && file_path_dir.canWrite()) {
            System.out.println("Access To Cache Folder Successfully");
        } else {
            try {
                throw new IOException("Cannot Access To Cache Folder!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public CacheAudio(String fileURL, String fileName) {
        this.fileURL = fileURL;
        this.fileName = DIR_PATH + fileName + ".mp3";
    }

    @Override
    public void run() {
        if(!isWin) return;
        try (BufferedInputStream in = new BufferedInputStream(new URL(fileURL).openStream()); FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName))) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            System.out.println("Finished Caching " + this.fileName + " from " + fileURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static CacheAudio newCacheAudioThread(String fileURL, String fileName) throws IOException {
        return new CacheAudio(fileURL, fileName);
    }
}