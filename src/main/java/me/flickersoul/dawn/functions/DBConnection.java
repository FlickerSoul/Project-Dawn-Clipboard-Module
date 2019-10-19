package me.flickersoul.dawn.functions;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.sql.*;

public class DBConnection {
    private static BasicDataSource DicConnection = new BasicDataSource();
    private static BasicDataSource MaterialConnection = new BasicDataSource();

    public static final String CACHE_DIR = new StringBuilder(System.getProperty("user.home"))
            .append(File.separator )
            .append("Documents")
            .append(File.separator)
            .append("PD_Cache")
            .append(File.separator)
            .toString();

    public static final String DB_DIR = new StringBuilder(CACHE_DIR).append("database").append(File.separator).toString();
    public static final String FILE_DIR = new StringBuilder("jdbc:sqlite:").append(DB_DIR).toString();
    public static final String MATERIAL_REPO = new StringBuilder(FILE_DIR).append("MaterialRepo.db").toString();
    private static final File DIR_FILE = new File(DB_DIR);

    static {
        DIR_FILE.mkdirs();
        if(DIR_FILE.canRead() && DIR_FILE.canWrite()){

        }

        String dir = "jdbc:sqlite:"+ DBConnection.class.getResource("/dic/PRO.db").toExternalForm();
        DicConnection.setDriverClassName("org.sqlite.JDBC");
        System.out.println(dir = dir.contains("jar") ? "jdbc:sqlite::resource:dic/PRO.db" : dir);
        DicConnection.setUrl(dir);
        DicConnection.setInitialSize(1);
        DicConnection.setMinIdle(1);
        DicConnection.setMaxIdle(4);

        MaterialConnection.setDriverClassName("org.sqlite.JDBC");
        MaterialConnection.setUrl(MATERIAL_REPO);
        MaterialConnection.setInitialSize(1);
        MaterialConnection.setMinIdle(1);
        MaterialConnection.setMaxIdle(1);

        //check integrity
        try(Connection connection = establishMaterialRepoConnection();
            Statement statement = connection.createStatement()){
            statement.execute("CREATE TABLE IF NOT EXISTS total_material_index(" +
                    "id INTEGER PRIMARY KEY NOT NULL, " +
                    "material_name TEXT NOT NULL," +
                    "description TEXT," +
                    "shown INTEGER DEFAULT 1)"); // 1 展示， 0 不展示
            System.out.println("total_index created");

            /*
            用于单词查询，sentence info放json，格式为"serial" : HashMap<Integer, TreeSet<Integer>>
             */
            statement.execute("CREATE TABLE IF NOT EXISTS total_word(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "word TEXT NOT NULL, " +
                    "word_id INTEGER," +
                    "known INTEGER DEFAULT 2, " + // 2 为未分类(default) 1 已知 0 未知 -1 不需要知道 //TODO 加一个分类方法 sort，一次性解决所有表列中的未分类
                    "sentence_info TEXT," +
                    "shown INTEGER DEFAULT 1," + // 1 true 0 false
                    "has_audio INTEGER DEFAULT 2," + // 1 true 0 false
                    "audio_dir TEXT)");

            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS word_name_index ON total_word (word)");
            System.out.println("total_word created and indexed");

            System.out.println("Create Word Repo");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Don't know whether the database is complete or not, exiting now");
            System.exit(-9);
        }
    }

    public static Connection establishDictionaryConnection(){
        try {
            return DicConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to connect to dictionary");
            e.printStackTrace();
        }

        return null;
    }

    public static Connection establishMaterialRepoConnection(){
        try {
            return MaterialConnection.getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to connect to material database");
            e.printStackTrace();
        }

        return null;
    }

    public static void closeDictionaryConnection(){
        try {
            DicConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeMaterialRepoConnection(){
        try{
            MaterialConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
