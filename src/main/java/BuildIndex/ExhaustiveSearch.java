package BuildIndex;

import Utils.Functions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ExhaustiveSearch {

    /**
     * 暴力搜索关键字
     *
     * @param keyword
     * @return
     * @throws Exception
     */
    public static HashSet<String> bruteForceSearch(ArrayList<String> fileList, String keyword, int top) throws Exception {
        HashMap<String, Integer> map = new HashMap<>();
        for (String filePath : fileList) {
            int i = traverseFile(filePath, keyword);
            if (i > 0) {
                map.put(filePath, i);
            }
        }

        return Functions.getPathSet(map, top);
    }

    public static int traverseFile(String filePath, String query) throws IOException {
        int num = 0;
        ArrayList<String> words = ReadFile.readFile(filePath);

        for (String word : words) {
            if (word.equals(query)) {
                num++;
            }
        }
        return num;
    }

    public static void main(String[] args) throws IOException {

    }
}
