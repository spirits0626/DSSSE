package BuildIndex;

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
        if(fileList.size() < num)
            return fileList;
        else
            return fileList.subList(0, num);
    }

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
        Collections.shuffle(result);
        return result.subList(0, num);
    }

    public static void main(String[] args) throws Exception {

        List<String> fileList = generateFileList(4);
        List<String> wordList = generateKeywords(20);

        System.out.println(fileList.toString());

        System.out.println(wordList.toString());
    }
}
