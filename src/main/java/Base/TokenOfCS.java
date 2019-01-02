package Base;

/**
 * client端进行搜索操作时向server1发送的数据
 */
public class TokenOfCS {
    private byte[] labelKeyword;
    private byte[] labelUkeyword;
    private byte[] labelNewKeyword;
    private byte[] keyAndNkey;
    private byte[] uKeyAndNkey;
    private byte[] nkeyAndRkey;
    private byte[] keyAndRkey;

    public TokenOfCS(byte[] labelKeyword, byte[] labelUkeyword, byte[] labelNewkeyword, byte[] keyAndNkey, byte[] uKeyAndNkey, byte[] nkeyAndRkey, byte[] keyAndRkey) {
        this.labelKeyword = labelKeyword;
        this.labelUkeyword = labelUkeyword;
        this.labelNewKeyword = labelNewkeyword;
        this.keyAndNkey = keyAndNkey;
        this.uKeyAndNkey = uKeyAndNkey;
        this.nkeyAndRkey = nkeyAndRkey;
        this.keyAndRkey = keyAndRkey;
    }

    public byte[] getLabelKeyword() {
        return labelKeyword;
    }

    public void setLabelKeyword(byte[] labelKeyword) {
        this.labelKeyword = labelKeyword;
    }

    public byte[] getLabelUkeyword() {
        return labelUkeyword;
    }

    public void setLabelUkeyword(byte[] labelUkeyword) {
        labelUkeyword = labelUkeyword;
    }

    public byte[] getLabelNewKeyword() {
        return labelNewKeyword;
    }

    public void setLabelNewKeyword(byte[] labelNewKeyword) {
        this.labelNewKeyword = labelNewKeyword;
    }

    public byte[] getkAndNkey() {
        return keyAndNkey;
    }

    public void setkAndNkey(byte[] keyAndNkey) {
        this.keyAndNkey = keyAndNkey;
    }

    public byte[] getKeyAndNkey() {
        return keyAndNkey;
    }

    public void setKeyAndNkey(byte[] keyAndNkey) {
        this.keyAndNkey = keyAndNkey;
    }

    public byte[] getuKeyAndNkey() {
        return uKeyAndNkey;
    }

    public void setuKeyAndNkey(byte[] uKeyAndNkey) {
        this.uKeyAndNkey = uKeyAndNkey;
    }

    public byte[] getNkeyAndRkey() {
        return nkeyAndRkey;
    }

    public void setNkeyAndRkey(byte[] nkeyAndRkey) {
        this.nkeyAndRkey = nkeyAndRkey;
    }

    public byte[] getKeyAndRkey() {
        return keyAndRkey;
    }

    public void setKeyAndRkey(byte[] keyAndRkey) {
        this.keyAndRkey = keyAndRkey;
    }
}
