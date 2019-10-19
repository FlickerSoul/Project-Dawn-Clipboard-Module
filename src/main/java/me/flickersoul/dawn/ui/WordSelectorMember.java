package me.flickersoul.dawn.ui;

public class WordSelectorMember {
    String first;
    String second;
    String third;
    String fourth;

    public WordSelectorMember(String first, String second, String third, String fourth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.fourth = fourth;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getThird() {
        return third;
    }

    public void setThird(String third) {
        this.third = third;
    }

    public String getFourth() {
        return fourth;
    }

    public void setFourth(String fourth) {
        this.fourth = fourth;
    }

    public String select(int i){
        switch (i){
            case 1:
                return first;
            case 2:
                return second;
            case 3:
                return third;
            case 4:
                return fourth;
        }

        return null;
    }
}
