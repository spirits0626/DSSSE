package Base;

import java.io.Serializable;

public class Key implements Serializable {
    private byte[] key;
    private byte[] newKey;
    private boolean isSearch;
    private String keyword;

    public Key(byte[] key, byte[] newKey, boolean isSearch, String keyword) {
        this.key = key;
        this.newKey = newKey;
        this.isSearch = isSearch;
        this.keyword = keyword;
    }

    public byte[] getKey() {
        return key;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getNewKey() {
        return newKey;
    }

    public void setNewKey(byte[] newKey) {
        this.newKey = newKey;
    }

    public boolean isSearch() {
        return isSearch;
    }

    public void setSearch(boolean search) {
        isSearch = search;
    }
}
