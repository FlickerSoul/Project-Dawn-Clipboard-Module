package me.flickersoul.dawn.functions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.HashSet;
import java.util.TreeMap;

public class DBProcessingQuery {
    private int currentMaterialId;
    private String currentWordsTable;
    private String INSERT_TO_WORD_TABLE_SQL;
    private static final String INSERT_MATERIAL_SQL = "INSERT INTO total_material_index(material_name) VALUES (?);";
    private static final String GET_CONTEXT_ID_SQL = "SELECT id FROM total_word WHERE word=?";
    private static final String UPDATE_SENTENCE_MAP_SQL = "UPDATE total_word SET sentence_info=? WHERE id=?";
    private static final String GET_OLD_SENTENCE_MAP = "SELECT sentence_info FROM total_word WHERE id=?";
    private static final String INSERT_NEW_SENT_MAP ="INSERT INTO total_word(word, word_id, sentence_info, known, has_audio, audio_dir) VALUES(?, ?, ?, ?, ?, ?)";
    private static final String CREATE_NEW_WORD_TABLE_HEAD = "CREATE TABLE ";
    private static final String CREATE_NEW_WORD_TABLE_TAIL = " (serial INTEGER PRIMARY KEY AUTOINCREMENT," +
                                                                "word TEXT, " +
                                                                "word_id INTEGER, " +
                                                                "lowercase_form TEXT, " +
                                                                "lowercase_id INTEGER, " +
                                                                "occurrence_number INTEGER, " +
                                                                "sentence_info TEXT)";
    private static final String CREATE_NEW_INDEX_HEAD = "CREATE UNIQUE INDEX index_";
    private static final String CREATE_NEW_INDEX_BODY = " ON ";
    private static final String CREATE_NEW_INDEX_TAIL = " (word)";

    private Connection connection = DBConnection.establishMaterialRepoConnection();
    private PreparedStatement insertToWordTable;
    private PreparedStatement getWordContextID;
    private PreparedStatement insertNewSent;
    private PreparedStatement updateMap;


    public DBProcessingQuery(){
        try {
            connection.setAutoCommit(false);
            getWordContextID = connection.prepareStatement(GET_CONTEXT_ID_SQL);
            insertNewSent = connection.prepareStatement(INSERT_NEW_SENT_MAP);
            updateMap = connection.prepareStatement(UPDATE_SENTENCE_MAP_SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void insertNewMaterial(String materialName){
        try(PreparedStatement preparedStatement = connection.prepareStatement(INSERT_MATERIAL_SQL);
            Statement statement = connection.createStatement()) {
            preparedStatement.setString(1, materialName);

            preparedStatement.executeUpdate();

            System.out.println("Update material Table");

            ResultSet resultSet = statement.executeQuery("SELECT last_insert_rowid()");
            if(!resultSet.isClosed()) {
                currentMaterialId = resultSet.getInt(1);
                currentWordsTable = "words_" + currentMaterialId;

                INSERT_TO_WORD_TABLE_SQL = "INSERT INTO " + currentWordsTable + " (word, word_id, lowercase_form, lowercase_id, occurrence_number, sentence_info) VALUES(?, ?, ?, ?, ?, ?)";

                createNewWordTable(currentMaterialId);

                System.out.println("Create Sub Word Table");

                insertToWordTable = connection.prepareStatement(INSERT_TO_WORD_TABLE_SQL);
            }
            //顺带创建对应表格
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Cannot insert \"" + materialName + "\" to material database");
        }
    }

    private void createNewWordTable(int material_id){
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_NEW_WORD_TABLE_HEAD + currentWordsTable + CREATE_NEW_WORD_TABLE_TAIL);
            statement.executeUpdate(CREATE_NEW_INDEX_HEAD + currentWordsTable + CREATE_NEW_INDEX_BODY + currentWordsTable + CREATE_NEW_INDEX_TAIL);
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot \"" + material_id + "_words\" table");
        }
    }



    /**
     * 总更改方法
     * @param word 单词
     * @param word_id 单词的ID
     * @param sentMap 单词的句子 Map
     */
    private  void insertOrUpdateWordInTotalWord(String word, int word_id, TreeMap<Integer, HashSet<String>> sentMap, Elements dirs){
        try {

            getWordContextID.setString(1, word);

            ResultSet resultSet = getWordContextID.executeQuery();

            if(resultSet.isClosed()){
                //new word, insert to list
                try {
                    JSONObject object = new JSONObject();
                    object.put(Integer.toString(currentMaterialId), JSON.toJSONString(sentMap));

                    insertNewSent.setString(1, word);
                    insertNewSent.setInt(2, word_id);
                    insertNewSent.setString(3, object.toJSONString());
                    insertNewSent.setInt(4, word_id == -1 ? -1 : 2);

                    if(dirs != null && !dirs.isEmpty()) {
                        insertNewSent.setInt(5, 2);  // 为下载
                        insertNewSent.setString(6, dirs.first().attr("href"));
//                        dirs.forEach(value -> CacheAudio.cacheAudioFromWordDir(value.attr("href"), word_id, false));
                    } else {
                        insertNewSent.setInt(5, 0);
                        insertNewSent.setString(6, null);
                    }

                    insertNewSent.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Cannot insert new sentence map");
                }
            } else {
                //old word, update map only
                int id = resultSet.getInt(1);

                try(PreparedStatement getOldMap = connection.prepareStatement(GET_OLD_SENTENCE_MAP)){
                    getOldMap.setInt(1, id);
                    ResultSet oldMapResult = getOldMap.executeQuery();
                    if(!oldMapResult.isClosed()){
                        JSONObject object = JSONObject.parseObject(oldMapResult.getString(1));
                        object.put(Integer.toString(currentMaterialId), JSON.toJSONString(sentMap));

                        updateMap.setString(1, object.toJSONString());
                        updateMap.setInt(2, id);

                        updateMap.addBatch();
                    } else
                        System.err.println("sentence info error, the word is in the list");
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println("Cannot update sentence map");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void insertToWordTable(String word, int word_id, String lowercase_form, int lowercase_id, int occurrence_number, TreeMap<Integer, HashSet<String>> sentSet, Elements dirs){
        try {
            insertToWordTable.setString(1, word);
            insertToWordTable.setInt(2, word_id);
            insertToWordTable.setString(3, lowercase_form);
            insertToWordTable.setInt(4, lowercase_id);
            insertToWordTable.setInt(5, occurrence_number);
            insertToWordTable.setString(6, JSON.toJSONString(sentSet));

            insertToWordTable.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Cannot insert the word \"" + word + "\" to word table");
        }

        insertOrUpdateWordInTotalWord(word, word_id, sentSet, dirs);
    }

    public void executeInsertWordsBatch(){
        try {
            insertToWordTable.executeBatch();
            getWordContextID.executeBatch();
            insertNewSent.executeBatch();
            updateMap.executeBatch();

            connection.commit();

            insertToWordTable.close();
            getWordContextID.close();
            insertNewSent.close();
            updateMap.close();

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

