package BuildIndex;

import LSH.BloomFilter;
import LSH.MinHash;
import Utils.Global;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Evaluate {

    public static void main(String args[]) throws Exception {

        // 打开保存实验结果的文件
        FileOutputStream fos = new FileOutputStream(new File(Global.experimentalResult));
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);

        Scanner sc = new Scanner(System.in);

        BloomFilter bloomFilter = BloomFilter.readLshFromFile(Global.BloomFilterPath);
        ArrayList<ArrayList> hashFunctions = MinHash.readLshFromFile(Global.LSHPath);

        // Initialize the data set
        ArrayList<String> fileList = ReadFile.readFileList(Global.fileListPath);

        Operation.init(bloomFilter, hashFunctions, fileList);

        bw.write("文件总数：" + fileList.size() + "\r\n");
        bw.write("关键字总数：" + Global.keywordSize + "\r\n");
        bw.write("不同的关键字总数: " + OperationOnClient.getKeywordSet().size() + "\r\n");

        bw.write("invertedIndex的大小: " + OperationOnIndexServer.getInvertedIndex().size() + "\r\n");
        bw.write("forwardIndex的大小: " + OperationOnIndexServer.getForwardIndex().size() + "\r\n");
        bw.write("DictKwd的大小: " + OperationOnClient.getDictKwd().size() + "\r\n");

        bw.write("初始化安全索引花费的时间如下: " + "\r\n");
        bw.write("\tserverResponseTime: " + Global.serverResponseTime + "\r\n");
        bw.write("\tserverResponseTimeAvg: " + Global.serverResponseTime / fileList.size() + "\r\n");
        bw.write("\tclientTime: " + Global.clientTime + "\r\n");
        bw.write("\tclientTimeAvg: " + Global.clientTime / fileList.size() + "\r\n\r\n");

        // 文件未更新时，搜索结果统计
        bw.write("文件未更新时，搜索结果统计:" + "\r\n");
        for (int i = 0; i < Global.searchLoopTime; i++) {
            int num = Global.searchTime;
            List<String> wordList = GenerateTestData.generateKeywords(num);
            Operation.search(bloomFilter, hashFunctions, wordList);

            bw.write("serverResponseTime: " + Global.serverResponseTime + "\r\n");
            bw.write("serverResponseTimeAvg: " + Global.serverResponseTime / num + "\r\n");
            bw.write("clientTime: " + Global.clientTime + "\r\n");
            bw.write("clientTimeAvg: " + Global.clientTime / num + "\r\n\r\n");
        }

        System.out.println("请输入删除再添加的文件占所有文件的比例：");
        double per = sc.nextDouble();
        List<String> fileLists = GenerateTestData.generateFileList((int) (per * fileList.size()));

        Operation.deleteAndAdd(bloomFilter, hashFunctions, fileLists);
        bw.write("删除" + (int) per * fileList.size() + "个文件花费的时间如下: \r\n");
        bw.write("\tserverResponseTime: " + Global.serverResponseTime + "\r\n");
        bw.write("\tserverResponseTimeAvg: " + Global.serverResponseTime / fileLists.size() + "\r\n");
        bw.write("\tclientTime: " + Global.clientTime + "\r\n");
        bw.write("\tclientTimeAvg: " + Global.clientTime / fileLists.size() + "\r\n\r\n");

        bw.write("文件更新后，搜索结果统计:" + "\r\n");
        for (int i = 0; i < Global.searchLoopTime; i++) {
            int num = Global.searchTime;
            List<String> wordList = GenerateTestData.generateKeywords(num);
            Operation.search(bloomFilter, hashFunctions, wordList);

            bw.write("查询的数据中被更新的关键字数目: " + Global.updateNum + "---" + "占总数的百分比：" + Global.updateNum * 1.0 / (num * Global.hashOr) * 100.0 + "%\r\n");
            bw.write("serverResponseTime: " + Global.serverResponseTime + "\r\n");
            bw.write("serverResponseTimeAvg: " + Global.serverResponseTime / num + "\r\n");
            bw.write("clientTime: " + Global.clientTime + "\r\n");
            bw.write("clientTimeAvg: " + Global.clientTime / num + "\r\n\r\n");
        }

        //注意关闭的先后顺序，先打开的后关闭，后打开的先关闭
        bw.close();
        osw.close();
        fos.close();
    }
}
