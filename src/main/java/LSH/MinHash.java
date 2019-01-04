package LSH;

import Utils.Global;

import java.io.*;
import java.util.*;

public class MinHash {

    /**
     * 生成单个的LSH函数
     *
     * @param length
     * @return
     */
    public static ArrayList getLSH(int length) {
        ArrayList<Integer> lsh = new ArrayList();
        for (int i = 1; i <= length; i++) {
            lsh.add(i);
        }
        Collections.shuffle(lsh);
        //System.out.println("lsh函数：" + lsh.toString());
        return lsh;
    }

    /**
     * 批量生成LSH函数
     * 结果存放在一个二维数组中
     *
     * @param length
     * @param numOfAnd
     * @param numOfOr
     * @return
     */
    public static ArrayList<ArrayList> getLSHs(int length, int numOfAnd, int numOfOr) {
        ArrayList res = new ArrayList();
        for (int i = 0; i < numOfOr; i++) {
            ArrayList lshs = new ArrayList();
            for (int j = 0; j < numOfAnd; j++) {
                ArrayList<Integer> lsh = getLSH(length);
                if (!lshs.contains(lsh))
                    lshs.add(lsh);
            }
            res.add(lshs);
        }
        return res;
    }


    /**
     * 计算经过bloom编码的数据的LSH值
     *
     * @param hashs
     * @param data
     * @return
     */
    public static ArrayList<String> getMinHash(ArrayList<ArrayList> hashs, BitSet data) {
        HashSet<Integer> dataSet = new HashSet();
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)) {
                dataSet.add(i);
            }
        }
        //System.out.println("需要hash的数据dataSet" + dataSet.toString());
        for (int j = 0; j < hashs.size(); j++) {
            ArrayList hash = hashs.get(j);
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < hash.size(); k++) {
                ArrayList lsh = (ArrayList) hash.get(k);
                for (int i = 0; i < lsh.size(); i++) {
                    if (dataSet.contains(lsh.get(i))) {
                        sb.append(i + 1 + ",");
                        break;
                    }
                }
                sb.deleteCharAt(sb.length()-1);
                result.add(sb.toString());
            }
        }
        return result;
    }

    /**
     * 将lsh函数对象写入文件
     *
     * @param path
     * @param lsh
     * @throws IOException
     */
    public static void writeLshToFile(String path, ArrayList lsh) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
        out.writeObject(lsh);
        out.flush();
        out.close();
    }

    /**
     * 从文件中读取lsh函数
     *
     * @param file
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ArrayList readLshFromFile(String file)
            throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        ArrayList lsh = (ArrayList) in.readObject();
        in.close();
        return lsh;
    }

    public static void main(String[] args) throws IOException {

        ArrayList hash = getLSHs(Global.hashLength, Global.hashAnd, Global.hashOr);
        writeLshToFile(Global.LSHPath, hash);

        /**
         BloomFilter bloomFilter = new BloomFilter(Global.c, Global.n, Global.k);
         bloomFilter.add("john");
         BitSet set = bloomFilter.getBitSet();

         int[][] res = getMinHash(hash, set);
         for (int i = 0; i < res.length; i++) {
         System.out.println(Arrays.toString(res[i]));
         }

         */

    }

}
