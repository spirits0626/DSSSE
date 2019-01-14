package BuildIndex;

import LSH.BloomFilter;
import LSH.MinHash;
import Utils.Functions;
import Utils.Global;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
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

        System.out.println("请选择评估搜索效率还是搜索精确度：");
        System.out.println("评估搜索效率请输入1：");
        System.out.println("评估搜索精确度请输入2：");
        System.out.println("结束程序请请输入0：");
        while (sc.hasNext()) {
            String input = sc.nextLine();
            switch (input) {
                case "1":
                    // 搜索效率统计
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
                    bw.write("删除" + (int) (per * fileList.size()) + "个文件花费的时间如下: \r\n");
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
                case "2":
                    // 搜索结果准确率统计
                    bw.write("暴力索搜和相似性搜索结果对比:" + "\r\n");
                    for (int i = 0; i < Global.searchLoopTime; i++) {
                        int num = Global.searchTime;
                        int bfNum = 0;
                        List<String> wordList = GenerateTestData.generateKeywords(num);

                        for (String keyword : wordList) {
                            System.out.println(keyword);
                            // 相似性搜索获得的结果
                            HashSet<String> ssResult = Functions.getPathSet(Operation.searchKeyword(bloomFilter, hashFunctions, keyword));
                            // 暴力搜索获得的结果
                            HashSet<String> bfResult = Functions.getPathSet(Functions.bruteForceSearch(keyword));

                            ssResult.retainAll(bfResult);

                            bfNum += bfResult.size();
                            Global.intersectionNum += ssResult.size();

                        }
                        System.out.println("相似性查询结果与报搜结果的交集占所有暴搜结果的比例：" + Global.intersectionNum * (1.0) / bfNum);
                        bw.write("相似性查询结果与报搜结果的交集占所有暴搜结果的比例：" + (Global.intersectionNum * (1.0) / bfNum) * 100 + "%\r\n");
                        Global.intersectionNum = 0;
                    }
                case "0":
                    // 注意关闭的先后顺序，先打开的后关闭，后打开的先关闭
                    bw.close();
                    osw.close();
                    fos.close();
                    return;
                default:
                    System.out.println("输入有误，请重新输入：");
                    System.out.println("评估搜索效率请输入1：");
                    System.out.println("评估搜索精确度请输入2：");
                    System.out.println("结束程序请请输入0：");

            }
        }

    }
}
