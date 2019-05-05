package BuildIndex;

import Base.*;
import LSH.BloomFilter;
import Utils.AES;
import Utils.Functions;
import Utils.Global;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * client端的相关操作
 */
public class OperationOnClient {

    private static HashSet<String> keywordSet = new HashSet<>();

    private static HashMap<String, Key> DictKwd = new HashMap<>();

    /**
     * 添加一个文件到索引中
     * client端对文件进行处理，生成label和data
     * 将处理之后的结果发送给server端，server将数据插入到索引中
     *
     * @param filePath
     * @param bloomFilter
     * @param hashFunction
     * @param aesForInd
     * @return label和data集合
     * @throws Exception
     */

    public static ArrayList<LabelAndData> addition(String filePath, BloomFilter bloomFilter, ArrayList<ArrayList> hashFunction, byte[] aesForInd, boolean isInit) throws Exception {

        byte[] labelInd = AES.encrypt(filePath, aesForInd);

        ArrayList<LabelAndData> labelAndDataList = new ArrayList<>();
        ArrayList<KeywordAndHashValue> list = ReadFile.readFile(filePath, bloomFilter, hashFunction);

        if (list.size() <= 0) {
            return null;
        }
        if (isInit) {
            for (KeywordAndHashValue keywordAndHashValue : list) {
                // 从保存密钥的dict中获取该keyword对应的key
                Global.keywordSize++;
                // System.out.println(keywordAndHashValue.getKeyword());
                keywordSet.add(keywordAndHashValue.getKeyword());

                ArrayList<String> hashs = keywordAndHashValue.getHashValues();
                for (String hash : hashs) {

                    Key key = DictKwd.get(hash);
                    if (key == null) {
                        // System.out.println(keyword);
                        key = new Key(AES.geneKey(null).getEncoded(), null, false, keywordAndHashValue.getKeyword());
                        DictKwd.put(hash, key);
                    }

                    byte[] labelKey = AES.encrypt(hash, key.getKey());
                    // labelInd与ind异或
                    byte[] data = Functions.xor(filePath.getBytes("UTF-8"), labelKey);
                    labelAndDataList.add(new LabelAndData(labelInd, labelKey, data));
                }
            }
        } else {
            for (KeywordAndHashValue keywordAndHashValue : list) {
                // 从保存密钥的dict中获取该keyword对应的key
                Global.keywordSize++;
                // System.out.println(keywordAndHashValue.getKeyword());
                keywordSet.add(keywordAndHashValue.getKeyword());

                ArrayList<String> hashs = keywordAndHashValue.getHashValues();
                for (String hash : hashs) {

                    byte[] labelKey;

                    Key key = DictKwd.get(hash);

                    if (key != null) {
                        if (key.isSearch()) {
                            if (key.getNewKey() == null) {
                                key.setNewKey(AES.geneKey(null).getEncoded());
                                labelKey = AES.encrypt(hash, key.getNewKey());
                            } else {
                                labelKey = AES.encrypt(hash, key.getNewKey());
                            }
                        } else {
                            // 已有数据，但是未被搜索过
                            labelKey = AES.encrypt(hash, key.getKey());
                        }
                    } else {
                        key = new Key(AES.geneKey(null).getEncoded(), null, false, keywordAndHashValue.getKeyword());
                        DictKwd.put(hash, key);
                        labelKey = AES.encrypt(hash, key.getKey());
                    }

                    // labelInd与ind异或
                    byte[] data = Functions.xor(filePath.getBytes("UTF-8"), labelKey);
                    labelAndDataList.add(new LabelAndData(labelInd, labelKey, data));
                }
            }
        }


        return labelAndDataList;
    }

    /**
     * 根据文件的ind将该文件从索引中删除
     * client端生成ind对应的token，发送至server端
     * server从索引中移除相关的数据
     *
     * @param filePath
     * @param aesForInd
     * @return
     * @throws Exception
     */
    public static byte[] deletion(String filePath, byte[] aesForInd) throws Exception {
        return AES.encrypt(filePath, aesForInd);
    }

    /**
     * 对关键字进行bloom编码，再进行hash，生成多个hash值
     * 对每个hash值生成token，将token列表发送给server
     *
     * @param keyword
     * @param bloomFilter
     * @param hashs
     * @param rkey
     * @return
     * @throws Exception
     */
    public static ArrayList<Token> searchClientToServer(String keyword, BloomFilter bloomFilter, ArrayList<ArrayList> hashs, byte[] rkey) throws Exception {
        ArrayList<Token> tokens = new ArrayList<>();
        ArrayList<String> hashList = ReadFile.getHashOfKeyword(keyword, bloomFilter, hashs);

        for (String hash : hashList) {

            Key keys = DictKwd.get(hash);
            if (keys == null) {
                continue;
            }
            byte[] key = keys.getKey();
            byte[] uKey = keys.getNewKey();
            TokenOfCS tokenOfCS;
            // 没有数据更新，不更新索引
            if (uKey == null) {
                byte[] cipherOfRkey = AES.encrypt(hash, rkey);
                if (key != null) {
                    byte[] cipherOfKey = AES.encrypt(hash, key);
                    tokenOfCS = new TokenOfCS(cipherOfKey, null, null,
                            null, null, null, Functions.xor(cipherOfKey, cipherOfRkey));
                    tokens.add(new Token(tokenOfCS, cipherOfRkey));
                }
                keys.setSearch(true);
                DictKwd.put(hash, keys);
            } else {
                // 两次搜索间隙添加了新数据
                SecretKey newKey = AES.geneKey(null);
                byte[] cipherOfKey = AES.encrypt(hash, key);
                byte[] cipherOfUkey = AES.encrypt(hash, uKey);
                byte[] cipherOfNewkey = AES.encrypt(hash, newKey.getEncoded());
                byte[] cipherOfRkey = AES.encrypt(hash, rkey);

                tokenOfCS = new TokenOfCS(cipherOfKey, cipherOfUkey, cipherOfNewkey, Functions.xor(cipherOfKey, cipherOfNewkey),
                        Functions.xor(cipherOfUkey, cipherOfNewkey), Functions.xor(cipherOfNewkey, cipherOfRkey), null);

                tokens.add(new Token(tokenOfCS, cipherOfRkey));
                // 更新密钥
                DictKwd.put(hash, new Key(newKey.getEncoded(), null, true, keyword));
            }

        }

        return tokens;
    }

    public static HashSet<String> getKeywordSet() {
        return keywordSet;
    }

    public static HashMap<String, Key> getDictKwd() {
        return DictKwd;
    }

    public static void setDictKwd(HashMap<String, Key> dictKwd) {
        DictKwd = dictKwd;
    }
}
