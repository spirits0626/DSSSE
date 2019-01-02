package Base;

import java.io.Serializable;
import java.util.Arrays;

public class Label implements Serializable {
    private byte[] label;
    private int hashCode;

    public Label() {
        label = new byte[20];
        hashCode = 0;
    }

    public Label(byte[] label) {
        this.label = label;
        hashCode = Arrays.hashCode(label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label)) return false;
        Label label1 = (Label) o;
        if(this.hashCode != label1.hashCode)
            return false;
        return Arrays.equals(label, label1.label);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    public byte[] getLabel() {
        return label;
    }
}
