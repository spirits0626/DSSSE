package Base;

public class LabelAndData {
    private byte[] labelKeyword;
    private byte[] labelInd;
    private byte[] data;

    public LabelAndData(byte[] labelInd, byte[] labelKeyword, byte[] data) {
        this.labelInd = labelInd;
        this.labelKeyword = labelKeyword;
        this.data = data;
    }

    public byte[] getLabelKeyword() {
        return labelKeyword;
    }

    public void setLabelKeyword(byte[] labelKeyword) {
        this.labelKeyword = labelKeyword;
    }

    public byte[] getLabelInd() {
        return labelInd;
    }

    public void setLabelInd(byte[] labelInd) {
        this.labelInd = labelInd;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
