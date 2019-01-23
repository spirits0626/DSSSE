package Utils;

import BuildIndex.ExhaustiveSearch;
import javafx.util.Pair;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;

public class Functions {

    /**
     * byte数组与整数异或
     *
     * @param buff
     * @param n
     * @return
     */
    public static byte[] xor(byte[] buff, int n) {
        BigInteger buffInt = QEncodeUtil.binary(buff, 2).xor(new BigInteger(String.valueOf(n)));
        return buffInt.toByteArray();
    }

    /**
     * 两个byte数组进行异或
     *
     * @param buff1
     * @param buff2
     * @return
     */
    public static byte[] xor(byte[] buff1, byte[] buff2) {
        BigInteger buffInt = QEncodeUtil.binary(buff1, 2).xor(QEncodeUtil.binary(buff2, 2));
        return buffInt.toByteArray();
    }


    /**
     * 评估搜索效率时使用
     *
     * @param indList
     * @return
     */
    public static List<String> getPathList(ArrayList<String> indList, int top) {
        List<String> indPathList = new ArrayList<>();
        HashMap<String, Integer> indPathMap = new HashMap<>();
        for (String ind : indList) {
            Integer oldValue = indPathMap.get(ind);
            indPathMap.put(ind, oldValue == null ? 1 : oldValue + 1);
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(indPathMap.entrySet());

        Collections.sort(list, Comparator.comparing(Map.Entry<String, Integer>::getValue));

        // System.out.println(list.size());

        for (int i = list.size() - 1; i >= 0 && top > 0; i--, top--) {
            Map.Entry<String, Integer> mapping = list.get(i);
            indPathList.add(mapping.getKey());
            // System.out.println(mapping.getKey() + ":" + mapping.getValue());
        }

        return indPathList;

    }

    /**
     * 评估搜索准确率时使用
     *
     * @param indList
     * @return
     */
    public static HashSet<String> getPathSet(ArrayList<String> indList) {
        HashMap<String, Integer> indPathMap = new HashMap<>();
        for (String ind : indList) {
            Integer oldValue = indPathMap.get(ind);
            indPathMap.put(ind, oldValue == null ? 1 : oldValue + 1);
        }

        return getPathSet(indPathMap, Global.top);

    }

    /**
     * 对所有的查询结果排序，获取排名前几的结果集合
     *
     * @param indPathMap
     * @return
     */
    public static HashSet<String> getPathSet(HashMap<String, Integer> indPathMap, int top) {
        HashSet<String> indPathSet = new HashSet<>();
        int resultNum = top;
        if (indPathMap.size() < resultNum) {
            indPathSet.addAll(indPathMap.keySet());
            return indPathSet;
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(indPathMap.entrySet());

        Collections.sort(list, Comparator.comparing(Map.Entry<String, Integer>::getValue));

        // System.out.println(list.size());

        for (int i = list.size() - 1; i >= 0 && resultNum > 0; i--, resultNum--) {
            Map.Entry<String, Integer> mapping = list.get(i);
            indPathSet.add(mapping.getKey());
            // System.out.println(mapping.getKey() + ":" + mapping.getValue());
        }

        return indPathSet;
    }

    public static int getTruePositive(List<String> pathList, String query) throws IOException {
        int num = 0;

        for (String path : pathList) {
            if (ExhaustiveSearch.traverseFile(path, query) > 0) {
                ++num;
            }
        }

        return num;
    }

    public static <V> Pair<V, Double> time(Callable<V> task) {
        long begin = System.nanoTime();

        V result = null;
        try {
            result = task.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long end = System.nanoTime();
        double time = (end - begin) / 1E9;

        return new Pair<>(result, time);
    }


    /**
     * 将对象写入文件中
     *
     * @param filePath
     * @throws IOException
     */
    public static void writeObject2File(Object o, String filePath) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath));
        out.writeObject(o);
        out.flush();
        out.close();
    }

    /**
     * 从文建中读取对象的内容
     *
     * @param filePath
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObjectFromFile(String filePath) throws IOException, ClassNotFoundException {
        File file = new File(filePath);
        if (!file.exists())
            return new HashMap<>();
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
        Object o = in.readObject();
        in.close();
        return o;
    }

    public static boolean isNumber(String str) {
        String reg = "^[0-9]+(.[0-9]+)?$";
        return str.matches(reg);
    }

    public static char randomChar(int choice) {
        String s;
        if (choice != 0) {
            // 替换字母
            s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        } else {
            // 替换数字
            s = "0123456789";
        }

        char[] c = s.toCharArray();
        Random random = new Random();
        return c[random.nextInt(c.length)];
    }

    public static void main(String[] args) {

    }
}
