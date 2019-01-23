package BuildIndex;

import Utils.Functions;
import Utils.Global;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class GenerateTestData {

    /**
     * 随机选择num个文件，用来从索引中删除，随后再添加
     *
     * @param num
     * @return
     * @throws Exception
     */
    public static List<String> generateFileList(int num) throws Exception {
        // 获取所有文件的路径列表
        ArrayList<String> fileList = ReadFile.readFileList(Global.fileListPath);
        // 将所有文件打乱
        Collections.shuffle(fileList);
        if (fileList.size() < num)
            return fileList;
        else
            return fileList.subList(0, num);
    }

    /**
     * 读取文件
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    private static ArrayList<String> readFile(String filePath) throws Exception {
        ArrayList<String> words = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String nextLine = reader.readLine();
        while (nextLine != null) {
            words.add(nextLine.toLowerCase());
            nextLine = reader.readLine();
        }
        reader.close();
        return words;
    }

    /**
     * 从所有文件中随机选择num个关键字
     *
     * @param num
     * @throws Exception
     */
    public static List<String> generateKeywords(int num) throws Exception {
        List<String> fileList = generateFileList(num);
        HashSet<String> keywords = new HashSet<>();

        for (String filePath : fileList) {
            keywords.addAll(readFile(filePath));
        }
        List<String> result = new ArrayList<>(keywords);
        if (result.size() <= num)
            return result;
        Collections.shuffle(result);
        return result.subList(0, num);
    }

    /**
     * 随机生成关键字序列中一部分的typo形式
     *
     * @param keys
     * @param n
     * @return
     */
    public static ArrayList<String> typoGenerator(List<String> keys, int n) {
        ArrayList<String> keywords = new ArrayList();
        keywords.addAll(keys);
        ArrayList<Integer> change = new ArrayList();
        if (n == keywords.size()) {
            for (int i = 0; i < n; ++i) {
                String str = keywords.get(i);
                keywords.set(i, change(str));
            }
        } else {
            Random r = new Random();
            int i = 0;

            int j;
            while (i < n) {
                j = r.nextInt(keywords.size());
                if (!change.contains(j)) {
                    change.add(j);
                    ++i;
                }
            }

            for (j = 0; j < change.size(); ++j) {
                int index = change.get(j);
                String str = keywords.get(index);
                keywords.set(index, change(str));
            }
        }

        return keywords;
    }

    /**
     * 对关键字进行变形
     *
     * @param str
     * @return
     */
    public static String change(String str) {
        int length = str.length();
        StringBuffer buf = new StringBuffer();
        buf.append(str);
        Random r = new Random();
        int change = r.nextInt(length);
        int op = r.nextInt(3);
        if (str.length() < 3) {
            op = 0;
        }

        int choice = 0;
        // 如果是数字，则choice置为1，使用数字替换
        if (Character.isDigit(buf.charAt(change))) {
            choice = 1;
        }

        char c = Functions.randomChar(choice);
        switch (op) {
            case 0:
                if (change > 0) {
                    buf.insert(change, buf.charAt(change - 1));
                } else {
                    buf.insert(change, c);
                }
                break;
            case 1:
                buf.deleteCharAt(change);
                break;
            case 2:
                buf.setCharAt(change, c);
        }

        return buf.toString();
    }

    public static void main(String[] args) throws Exception {

//        List<String> fileList = generateFileList(4);
//        List<String> wordList = generateKeywords(20);
//
//        System.out.println(fileList.toString());
//        System.out.println(wordList.toString());

        // 获取文件列表
        System.out.println("请输入需要生成的关键字个数：");
        Scanner sc = new Scanner(System.in);
        int m = sc.nextInt();
        List<String> keywords = generateKeywords(m);
        ArrayList<String> keywords_change = typoGenerator(keywords, (int) ((double) m * 0.25D));

        for (int i = 0; i < keywords.size(); ++i) {
            if (!(keywords.get(i)).equals(keywords_change.get(i))) {
                System.out.println(keywords.get(i) + "------" + keywords_change.get(i));
            } else {
                System.out.println(keywords.get(i) + "++++++" + keywords_change.get(i));
            }
        }
    }
}
