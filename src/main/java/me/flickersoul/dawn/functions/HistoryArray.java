package me.flickersoul.dawn.functions;

public class HistoryArray {
    private static String[] historyArray = new String[30];

    private static String[] tempArray = new String[30];

    private static int head = 29; // pointer <= head; pointer >= tail;
    private static int tail = 0;
    private static int pointer = 0;
    private static int lastSearchResult = 0; //后续实现

    public static void putSearchResult(String word){
        if (++head == 30) {
            head = 0;
            pointer = 0;
            tail = 1;
            historyArray[head] = word;
        } else if(head == 29){
            tail = 0;
            pointer = 29;
            historyArray[head] = word;
        } else {
            pointer = head;
            tail = head + 1;
            historyArray[head] = word;
        }
    }

    public static void insertSearchResult(String word){
        if(pointer == head || pointer == tail){
            putSearchResult(word);
            return;
        }else if(pointer < head && head < tail) {
            for(int i = head; i >= pointer + 1; i--){
                historyArray[i + 1] = historyArray[i];
            }
            historyArray[++pointer] = word;
            if(++tail == 30){
                tail = 0;
            }
            head += 1;
        }else if(tail < pointer && pointer < head){
            historyArray[0] = historyArray[29];
            for(int i = 28; i >= pointer + 1; i--){
                historyArray[i + 1] = historyArray[i];
            }
            head = 0;
            tail = 1;
            historyArray[++pointer] = word;
        }else if(tail < pointer){
            for(int i = head; i >= 0; i--){
                historyArray[i + 1] = historyArray[i];
            }
            if(pointer == 29) {
                pointer = 0;
                historyArray[0] = word;
                return;
            }else {
                for (int i = 28; i >= pointer + 1; i++) {
                    historyArray[i + 1] = historyArray[i];
                }
                historyArray[++pointer] = word;
            }
        }
    }

    public static void getPreviousWord(){
        if(pointer == tail){
            return;
        }

        if(--pointer < 0){
            pointer = 29;
        }

        if(historyArray[pointer] == null){
            if(++pointer == 30){
                pointer = 0;
            }
        }

        ClipboardFunctionQuery.lookupWord(historyArray[pointer]);
    }

    public static void getLatterWord(){

        if(pointer == head){
            if(historyArray[pointer] != null)
                return ;
            else
                return ;
        }

        if(++pointer == 30){
            pointer = 0;
        }

        ClipboardFunctionQuery.lookupWord(historyArray[pointer]);
    }
}
