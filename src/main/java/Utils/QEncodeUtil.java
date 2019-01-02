package Utils;

import java.math.BigInteger;

public class QEncodeUtil {

    public static void main(String[] args) {
        String s = "woaini";
        byte[] bytes = s.getBytes();

        System.out.println("将woaini转为不同进制的字符串：");
        System.out.println("可以转换的进制范围：" + Character.MIN_RADIX + "-" + Character.MAX_RADIX);
        System.out.println("2进制：" + binaryString(bytes, 2));
        System.out.println("5进制：" + binaryString(bytes, 5));
        System.out.println("8进制：" + binaryString(bytes, 8));
        System.out.println("16进制：" + binaryString(bytes, 16));
        System.out.println("32进制：" + binaryString(bytes, 32));
        System.out.println("64进制：" + binaryString(bytes, 64));// 这个已经超出范围，超出范围后变为10进制显示

        System.exit(0);
    }

    /**
     * 将byte[]转为各种进制的字符串
     *
     * @param bytes byte[]
     * @param radix 基数可以转换进制的范围，从Character.MIN_RADIX到Character.MAX_RADIX，超出范围后变为10进制
     * @return 转换后的字符串
     */
    public static String binaryString(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
    }

    public static BigInteger binary(byte[] bytes, int radix) {
        return new BigInteger(1, bytes);// 这里的1代表正数
    }
}
