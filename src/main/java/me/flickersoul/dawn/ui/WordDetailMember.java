package me.flickersoul.dawn.ui;

public class WordDetailMember {
    private String word;
    private String lowercase_word;
    private int serial;
    private int occurrence_number;
    private int primary_id;
    private int secondary_id;
    private String mapString;
    private int has_audio; //拥有audio的 1 true 0 false
    private String audio_dir;
    private boolean shown; // 1 true 0 false
    private int known;  // 2 为未分类(default) 1 已知 0 未知 -1 不需要知道(default)

                                //serial, w.word, w.word_id, w.lowercase_form, w.lowercase_id, w.occurrence_number, w.sentence_info, total.shown, total.known, total.has_audio, total.audio_dir
    public WordDetailMember(int serial, String word, int primary_id, String lowercase_word, int secondary_id, int occurrence_number, String mapString, boolean shown, int known, int has_audio, String audio_dir){
        this.serial = serial;
        this.word = word;
        this.lowercase_word = lowercase_word;
        this.occurrence_number = occurrence_number;
        this.primary_id = primary_id;
        this.secondary_id = secondary_id;
        this.mapString = mapString;
        this.shown = shown;
        this.known = known;
        this.has_audio = has_audio;
        this.audio_dir = audio_dir;
    }

    public WordDetailMember(int serial, String word, int primary_id, String mapString, boolean shown, int has_audio){
        this.serial = serial;
        this.word = word;
        this.primary_id = primary_id;
        this.mapString = mapString;
        this.shown = shown;
        this.has_audio = has_audio;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getLowercase_word() {
        return lowercase_word;
    }

    public void setLowercase_word(String lowercase_word) {
        this.lowercase_word = lowercase_word;
    }

    public int getOccurrence_number() {
        return occurrence_number;
    }

    public void setOccurrence_number(int occurrence_numnber) {
        this.occurrence_number = occurrence_numnber;
    }

    public int getPrimary_id() {
        return primary_id;
    }

    public void setPrimary_id(int primary_id) {
        this.primary_id = primary_id;
    }

    public int getSecondary_id() {
        return secondary_id;
    }

    public void setSecondary_id(int secondary_id) {
        this.secondary_id = secondary_id;
    }

    public String getMapString(){
        return mapString;
    }

    public void setMapString(String mapString){
        this.mapString = mapString;
    }

    public int getSerial() {
        return serial;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public boolean getShown(){
        return shown;
    }

    public boolean isShown() {
        return shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }

    public int getKnown() {
        return known;
    }

    public void setKnown(int known) {
        this.known = known;
    }

    public int getHas_audio() {
        return has_audio;
    }

    public void setHas_audio(int has_audio) {
        this.has_audio = has_audio;
    }

    public String getAudio_dir() {
        return audio_dir;
    }

    public void setAudio_dir(String audio_dir) {
        this.audio_dir = audio_dir;
    }

    @Override
    public String toString(){
        return "word: " + word + ", lowercase form: " + lowercase_word + ", occurrence times: " + occurrence_number + "; occurrence times: " + occurrence_number;
    }
}
