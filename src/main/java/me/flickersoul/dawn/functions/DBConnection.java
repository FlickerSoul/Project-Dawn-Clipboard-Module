package me.flickersoul.dawn.functions;

import java.sql.*;

public class DBConnection {
    Connection PRODBConnection;
    Connection UnfDBConnection;
    Connection FDBConnection;
    Connection DetailsDBConnection;
    Connection ExistingBooksDBConnection;

    public Connection getPRODBConnection(){
        return PRODBConnection;
    }

    public static void main(String[] args){
        DBConnection dbConnection = new DBConnection();
        dbConnection.establishDBConnections();
        dbConnection.checkIntegrity();
    }

    public void establishDBConnections(){
        try{
            String URL;

            URL = "src/main/resources/Dictionary/PRO.db";
            System.out.println("Connecting to " + URL);
            PRODBConnection = DriverManager.getConnection("jdbc:sqlite:" + URL);
            System.out.println("Connect to Dictionary DB Successfully");


            URL = "src/main/resources/PersonalFolder/UnfDB.db";
            System.out.println("Connecting to " + URL);
            UnfDBConnection = DriverManager.getConnection("jdbc:sqlite:" + URL);
            System.out.println("Connect to Unfamilar Words DB Successfully");

            URL = "src/main/resources/PersonalFolder/FDB.db";
            System.out.println("Connecting to " + URL);
            FDBConnection = DriverManager.getConnection("jdbc:sqlite:" + URL);
            System.out.println("Connect to Familar Words DB Successfully");

            URL = "src/main/resources/PersonalFolder/DetailsDB.db";
            System.out.println("Connecting to " + URL);
            DetailsDBConnection = DriverManager.getConnection("jdbc:sqlite:" + URL);
            System.out.println("Connect to Word Details DB Successfully");

            URL = "src/main/resources/PersonalFolder/ExistingBooks.db";
            System.out.println("Connecting to " + URL);
            ExistingBooksDBConnection = DriverManager.getConnection("jdbc:sqlite:" + URL);
            System.out.println("Connect to Existing Books DB Successfully");

        }catch (SQLException sqlException){
            System.out.println("Failed to Connect DB");
            sqlException.printStackTrace();
        }
    }

    public static Connection getConnection(String filePath) throws SQLException {
        return DriverManager.getConnection(filePath);
    }

    public void checkIntegrity() {
        Statement statement;
        String sql;
        try {
            statement = UnfDBConnection.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS words(" +
                    "word TEXT PRIMARY KEY NOT NULL" +
                    ");";
            statement.execute(sql);
            System.out.println("Unfamiliar Words DB is intact");

            statement = FDBConnection.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS  words(" +
                    "word TEXT PRIMARY KEY NOT NULL" +
                    ");";
            statement.execute(sql);
            System.out.println("Familiar Words DB is intact");

            statement = DetailsDBConnection.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS words(" +
                    "word TEXT PRIMARY KEY, " +
                    "pronounciation_uk TEXT NOT NULL, " +
                    "pronounciation_us TEXT NOT NULL, " +
                    "definition_cn TEXT NOT NULL, " +
                    "definition_en TEXT NOT NULL, " +
                    "eg_sentence_ol TEXT NOT NULL, " +
                    "eg_sentence_book TEXT NOT NULL, " +
                    "audio_uk_filepath TEXT NOT NULL, " +
                    "audio_us_filepath TEXT NOT NULL" +
                    ");";
            statement.execute(sql);
            System.out.println("Word Details DB is intact");

            statement = ExistingBooksDBConnection.createStatement();
            sql = "CREATE TABLE IF NOT EXISTS book(" +
                    "book_id INTEGER PRIMARY KEY NOT NULL, " +
                    "book_name TEXT NOT NULL, " +
                    "db_filepath TEXT NOT NULL " +
                    ");";
            statement.execute(sql);
            System.out.println("Book List DB is intact");

            System.out.println("Check Completed");
        }catch (SQLException e){
            System.out.println("Failed to Check Integrity");
            e.printStackTrace();
        }
    }

    public ResultSet getTestResult(){
        try {
            Statement statement = PRODBConnection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT word_value FROM all_words LIMIT 150 OFFSET 15620");
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertData(String word, String pro_uk, String pro_us, String def_cn, String def_en, String eg_ol, String eg_book, String filepath_uk, String filepath_us){
        try {
            String sql ="INSERT INTO words (word, pronounciation, definition_cn, definistion_en, eg_sentence_ol, eg_sentence_book, audio_filepath)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = DetailsDBConnection.prepareStatement(sql);
            preparedStatement.setString(1, word);
            preparedStatement.setString(2, pro_uk);
            preparedStatement.setString(3, pro_us);
            preparedStatement.setString(4, def_cn);
            preparedStatement.setString(5, def_en);
            preparedStatement.setString(6, eg_ol);
            preparedStatement.setString(7, eg_book);
            preparedStatement.setString(8, filepath_uk);
            preparedStatement.setString(9, filepath_us);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
