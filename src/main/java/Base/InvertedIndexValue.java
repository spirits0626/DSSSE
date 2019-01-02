package Base;

import java.io.Serializable;
import java.util.Arrays;

/**
 * inverted索引中的保存的ind表示和data
 */
public class InvertedIndexValue implements Serializable {
    private byte[] labelInd;
    private byte[] data;
    private int hashcode;

    public InvertedIndexValue(byte[] labelInd, byte[] data) {
        this.labelInd = labelInd;
        this.data = data;
        this.hashcode = 31 * Arrays.hashCode(labelInd) + Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvertedIndexValue)) return false;
        InvertedIndexValue that = (InvertedIndexValue) o;
        if(this.hashcode != that.hashcode)
            return false;
        return Arrays.equals(labelInd, that.labelInd) &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return this.hashcode;
    }

    public byte[] getLabelInd() {
        return labelInd;
    }

    public byte[] getData() {
        return data;
    }
}
