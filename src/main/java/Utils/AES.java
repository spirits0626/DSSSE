package Utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class AES {
    private static final String ALGORITHM = "AES";

    /**
     * 生成密钥
     *
     * @return
     * @throws Exception
     */
    public static SecretKey geneKey(String seed) throws Exception {
        //获取一个密钥生成器实例
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom random = new SecureRandom();
        if(seed != null)
            random.setSeed(seed.getBytes());//设置加密用的种子，密钥
        keyGenerator.init(128, random);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;
    }

    /**
     * 读取存储的密钥
     *
     * @param keyPath
     * @return
     * @throws Exception
     */
    private SecretKey readKey(Path keyPath) throws Exception {
        //读取存起来的密钥
        byte[] keyBytes = Files.readAllBytes(keyPath);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        return keySpec;
    }

    public static byte[] encrypt(String content, byte[] key) throws Exception {
        //1、指定算法、获取Cipher对象
        Cipher cipher = Cipher.getInstance(Global.ALGORITHM);//算法是AES
        //2、生成/读取用于加解密的密钥
        SecretKeySpec keySpec = new SecretKeySpec(key, Global.ALGORITHM);
        //3、用指定的密钥初始化Cipher对象，指定是加密模式，还是解密模式
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        //4、更新需要加密的内容
        cipher.update(content.getBytes());
        //5、进行最终的加解密操作
        return cipher.doFinal();
    }


    public static void main(String[] args) throws Exception {

        AES aes = new AES();

        for (int i = 0; i < 3; i++) {
            SecretKey secretKey = aes.geneKey(null);
            byte[] key = secretKey.getEncoded();
            for (int j = 0; j < key.length; j++) {
                System.out.print(key[j] + " ");
            }
            System.out.println();
        }
    }
}
