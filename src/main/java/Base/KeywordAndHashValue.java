package Base;

import java.util.ArrayList;

public class KeywordAndHashValue {
    private String keyword;
    private ArrayList hashValues;

    public KeywordAndHashValue(String keyword, ArrayList hashValues) {
        this.keyword = keyword;
        this.hashValues = hashValues;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public ArrayList getHashValues() {
        return hashValues;
    }

    public void setHashValues(ArrayList hashValues) {
        this.hashValues = hashValues;
    }
}
