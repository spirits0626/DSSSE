package BuildIndex;

import Base.KeywordAndHashValue;
import LSH.BloomFilter;
import LSH.MinHash;
import Utils.Functions;
import Utils.Global;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ReadFile {

    /**
     * 读取filepath对应的文件内容，
     * 对文件中的单词进行bloom编码以及哈希操作
     *
     * @param filePath
     * @param bloomFilter
     * @param hashs
     * @return
     * @throws IOException
     */
    public static ArrayList<KeywordAndHashValue> readFile(String filePath, BloomFilter bloomFilter, ArrayList<ArrayList> hashs) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        ArrayList<KeywordAndHashValue> list = new ArrayList<>();
        System.out.println("---" + filePath);

        String nextLine = reader.readLine();
        while (nextLine != null) {
            // System.out.println(nextLine);
            // 处理每一个单词
            if (Functions.isNumber(nextLine) || nextLine.equals("") || nextLine.equals("\n")) {
                nextLine = reader.readLine();
                continue;
            }
            list.add(new KeywordAndHashValue(nextLine.toLowerCase(), getHashOfKeyword(nextLine, bloomFilter, hashs)));
            nextLine = reader.readLine();
        }
        reader.close();
        return list;
    }

    /**
     * 对某一个keyword进行bloom编码，随后进行minHash操作
     *
     * @param keyword
     * @param bloomFilter
     * @param hashs
     * @return
     * @throws IOException
     */
    public static ArrayList<String> getHashOfKeyword(String keyword, BloomFilter bloomFilter, ArrayList<ArrayList> hashs) {
        ArrayList<String> hashList = new ArrayList<>();
        // 处理每一个单词
        bloomFilter.clear();
        for (int i = 0; i < keyword.length() - 1; i++) {
            bloomFilter.add(keyword.substring(i, i + 2));
        }

        int[][] res = MinHash.getMinHash(hashs, bloomFilter.getBitSet());
        for (int i = 0; i < res.length; i++) {
            hashList.add(Arrays.toString(res[i]));
        }
        return hashList;
    }

    /**
     * 遍历文件目录，分别处理每一个文件中的内容
     *
     * @param pathName
     * @return
     * @throws IOException
     */
    public static ArrayList<String> find(String pathName) throws IOException {
        //获取pathName的File对象
        ArrayList<String> files = new ArrayList<>();
        File dirFile = new File(pathName);
        //判断该文件或目录是否存在，不存在时在控制台输出提醒
        if (!dirFile.exists()) {
            System.out.println("do not exit");
            return null;
        }
        //判断如果不是一个目录，就判断是不是一个文件，是文件则输出文件路径
        if (!dirFile.isDirectory()) {
            if (dirFile.isFile()) {
                //System.out.println(dirFile.getCanonicalFile());
                files.add(dirFile.getCanonicalFile().getCanonicalPath());
                return files;
            }
        }

        //获取此目录下的所有文件名与目录名
        String[] fileList = dirFile.list();
        for (int i = 0; i < fileList.length; i++) {
            //遍历文件目录
            String string = fileList[i];
            File file = new File(dirFile.getPath(), string);
            String path = file.getCanonicalPath();
            //如果是一个目录，搜索深度depth++，输出目录名后，进行递归
            if (file.isDirectory()) {
                //递归
                files.addAll(find(file.getCanonicalPath()));
            } else {
                //System.out.println(path);
                files.add(path);
            }
        }
        return files;
    }

    /**
     * 从保存所有文件路径的文件中读取数据
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static ArrayList<String> readFileList(String filePath) throws Exception {
        ArrayList<String> fileList = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        for (String line = br.readLine(); line != null && !line.equals(""); line = br.readLine()) {
            fileList.add(line);
        }
        return fileList;
    }

    public static void main(String[] args) throws IOException {

        System.out.println("开始读取所有文件");
        ArrayList<String> files = find(Global.dataPath);
        System.out.println("读取完成，将文件路径写入文件");
        try {
            FileWriter fw = new FileWriter(Global.fileListPath, false);
            for (String file : files) {
                String c = file + "\r\n";
                fw.write(c);
            }
            fw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            System.out.println("写入失败");
            System.exit(-1);
        }

    }

}
