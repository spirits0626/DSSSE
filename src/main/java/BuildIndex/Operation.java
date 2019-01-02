package BuildIndex;

import Base.*;
import LSH.BloomFilter;
import LSH.MinHash;
import Utils.AES;
import Utils.Functions;
import Utils.Global;
import javafx.util.Pair;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Operation {

    /**
     * 获取索引信息
     *
     * @throws Exception
     */
    public static Object getIndex() throws Exception {
        System.out.println("Loading index from file");
        OperationOnIndexServer.setForwardIndex((HashMap<Label, ArrayList<Label>>) Functions.readObjectFromFile(Global.forwardIndexPath));
        OperationOnIndexServer.setInvertedIndex((HashMap<Label, ArrayList<InvertedIndexValue>>) Functions.readObjectFromFile(Global.invertedIndexPath));

        OperationOnClient.setDictKwd((HashMap<String, Key>) Functions.readObjectFromFile(Global.dictKwdPath));

        System.out.println("Size of forward index:" + OperationOnIndexServer.getForwardIndex().size());
        System.out.println("Size of inverted index:" + OperationOnIndexServer.getInvertedIndex().size());
        System.out.println("Size of dictKwd:" + OperationOnClient.getDictKwd().size());

        System.out.println("Loading completed");
        return null;
    }

    /**
     * 更新索引信息
     *
     * @throws Exception
     */
    public static Object setIndex() throws Exception {
        System.out.println("Writing index to file");
        Functions.writeObject2File(OperationOnIndexServer.getForwardIndex(), Global.forwardIndexPath);
        Functions.writeObject2File(OperationOnIndexServer.getInvertedIndex(), Global.invertedIndexPath);

        Functions.writeObject2File(OperationOnClient.getDictKwd(), Global.dictKwdPath);

        System.out.println("Size of forward index:" + OperationOnIndexServer.getForwardIndex().size());
        System.out.println("Size of inverted index:" + OperationOnIndexServer.getInvertedIndex().size());
        System.out.println("Size of dictKwd:" + OperationOnClient.getDictKwd().size());

        OperationOnClient.setDictKwd(null);
        OperationOnIndexServer.setInvertedIndex(null);
        OperationOnIndexServer.setForwardIndex(null);

        System.out.println("Writing completed");
        return null;
    }

    public static void main(String args[]) throws Exception {
        getIndex();

        Scanner sc = new Scanner(System.in);
        System.out.println("Please choose what do you want");
        System.out.println("\tInitialize thr data set --- 0");
        System.out.println("\tDelete file from index ---- 1");
        System.out.println("\tSearch keyword from index - 2");
        System.out.println("\tEnd the program ------------3");

        double serverResponseTime;
        double clientTime;

        BloomFilter bloom = BloomFilter.readLshFromFile(Global.BloomFilterPath);
        ArrayList<ArrayList> hashFunctions = MinHash.readLshFromFile(Global.LSHPath);

        while (sc.hasNext()) {
            String input = sc.nextLine();
            switch (input) {
                case "0":
                    serverResponseTime = 0;
                    clientTime = 0;

                    ArrayList<String> fileList = ReadFile.readFileList(Global.fileListPath);

                    int i = 0;
                    for (String file : fileList) {
                        System.out.print(++i + "/" + fileList.size());
                        Pair<ArrayList<LabelAndData>, Double> client = Functions.time(() -> OperationOnClient.addition(file, bloom, hashFunctions, AES.geneKey(file).getEncoded(), true));
                        clientTime += client.getValue();

                        serverResponseTime += Functions.time(() -> OperationOnIndexServer.operate(client.getKey(), "init")).getValue();
                        System.out.println(OperationOnIndexServer.getInvertedIndex().size() + " " + Global.invertedValueSize + "分割的桶数目" + Global.cutNum);

                    }

                    serverResponseTime += Functions.time(() -> OperationOnIndexServer.smooth(true, null)).getValue();

                    System.out.println("关键字总数：" + Global.keywordSize);
                    System.out.println("不同的关键字总数: " + OperationOnClient.getKeywordSet().size());

                    System.out.println("invertedIndex的大小:" + OperationOnIndexServer.getInvertedIndex().size());
                    System.out.println("forwardIndex的大小:" + OperationOnIndexServer.getForwardIndex().size());
                    System.out.println("DictKwd的大小:" + OperationOnClient.getDictKwd().size());

                    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

                    MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage(); //椎内存使用情况

                    long totalMemorySize = memoryUsage.getInit(); //初始的总内存

                    long maxMemorySize = memoryUsage.getMax(); //最大可用内存

                    long usedMemorySize = memoryUsage.getUsed(); //已使用的内存

                    System.out.println("TotalMemory---" + totalMemorySize / (1024 * 1024) + "M");
                    System.out.println("FreeMemory---" + (totalMemorySize - usedMemorySize) / (1024 * 1024) + "M");
                    System.out.println("MaxMemory---" + maxMemorySize / (1024 * 1024) + "M");
                    System.out.println("UsedMemory---" + usedMemorySize / (1024 * 1024) + "M");

                    System.out.println("serverResponseTime:" + serverResponseTime);
                    System.out.println("serverResponseTimeAvg:" + serverResponseTime / fileList.size());
                    System.out.println("clientTime:" + clientTime);
                    System.out.println("clientTimeAvg:" + clientTime / fileList.size());
                    break;

                case "1":
                    serverResponseTime = 0;
                    clientTime = 0;

                    System.out.println("Please input the number of file you want to delete:");

                    List<String> fileLists = GenerateTestData.generateFileList(sc.nextInt());
                    for (String path : fileLists) {
                        Pair<byte[], Double> clientResult = Functions.time(() -> OperationOnClient.deletion(path, AES.geneKey(path).getEncoded()));
                        clientTime += clientResult.getValue();
                        ArrayList<LabelAndData> list = new ArrayList<>();
                        list.add(new LabelAndData(clientResult.getKey(), null, null));
                        serverResponseTime += Functions.time(() -> OperationOnIndexServer.operate(list, "delete")).getValue();
                    }

                    System.out.println("serverResponseTime:" + serverResponseTime);
                    System.out.println("serverResponseTimeAvg:" + serverResponseTime / fileLists.size());
                    System.out.println("clientTime:" + clientTime);
                    System.out.println("clientTimeAvg:" + clientTime / fileLists.size());

                    serverResponseTime = 0;
                    clientTime = 0;

                    for (String path : fileLists) {
                        Pair<ArrayList<LabelAndData>, Double> client = Functions.time(() -> OperationOnClient.addition(path, bloom, hashFunctions, AES.geneKey(path).getEncoded(), false));
                        clientTime += client.getValue();
                        serverResponseTime += Functions.time(() -> OperationOnIndexServer.operate(client.getKey(), "add")).getValue();
                    }

                    System.out.println("serverResponseTime:" + serverResponseTime);
                    System.out.println("serverResponseTimeAvg:" + serverResponseTime / fileLists.size());
                    System.out.println("clientTime:" + clientTime);
                    System.out.println("clientTimeAvg:" + clientTime / fileLists.size());
                    break;

                case "2":
                    clientTime = 0;
                    serverResponseTime = 0;

                    System.out.println("Please input the number of keywords you want to search:");
                    int num = sc.nextInt();
                    List<String> wordList = GenerateTestData.generateKeywords(num);
                    //List<String> wordList = new ArrayList<>();
                    //wordList.add("keyboard");
                    for (String keyword : wordList) {
                        System.out.println("keyword:" + keyword);
                        byte[] rkey = AES.geneKey(null).getEncoded();

                        ArrayList<String> indPathList = new ArrayList<>();

                        BloomFilter bloomFilter = BloomFilter.readLshFromFile(Global.BloomFilterPath);
                        ArrayList<ArrayList> hashs = MinHash.readLshFromFile(Global.LSHPath);

                        Pair<ArrayList<Token>, Double> pair = Functions.time(() -> OperationOnClient.searchClientToServer(keyword, bloomFilter, hashs, rkey));

                        clientTime += pair.getValue();

                        ArrayList<Token> tokens = pair.getKey();
                        if (tokens != null) {
                            for (Token token : tokens) {
                                if (token == null || token.getTokenOfCS() == null) {
                                    System.out.println("token is null!");
                                    continue;
                                } else {
                                    Pair<ArrayList<byte[]>, Double> indexServer = Functions.time(() -> OperationOnIndexServer.searchAndUpdate(token.getTokenOfCS()));

                                    serverResponseTime += indexServer.getValue();
                                    serverResponseTime += Functions.time(() -> indPathList.addAll(OperationOnDataServer.search(indexServer.getKey(), token.getTokenOfDataServer()))).getValue();
                                }
                            }
                        }
                        Functions.getPathList(indPathList, Global.resultNum);
                    }

                    System.out.println("查询的数据中被更新的关键字数目:" + Global.updateNum + "---" + "占总数的百分比：" + Global.updateNum * 1.0 / (num * Global.hashOr) * 100.0 + "%");
                    System.out.println("serverResponseTime:" + serverResponseTime);
                    System.out.println("serverResponseTimeAvg:" + serverResponseTime / wordList.size());
                    System.out.println("clientTime:" + clientTime);
                    System.out.println("clientTimeAvg:" + clientTime / wordList.size());

                    break;

                case "3":
                    Functions.time(() -> setIndex()).getValue();
                    return;
                default:
                    System.out.println("Finished, please input again or end the program!");

            }
        }
    }
}