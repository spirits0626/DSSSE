package BuildIndex;

import Base.*;
import LSH.BloomFilter;
import LSH.MinHash;
import Utils.AES;
import Utils.Functions;
import Utils.Global;
import javafx.util.Pair;

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

    public static void init(BloomFilter bloomFilter, ArrayList<ArrayList> hashFunctions, ArrayList<String> fileList) throws Exception{
        Global.serverResponseTime = 0;
        Global.clientTime = 0;

        int i = 0;
        for (String file : fileList) {
            System.out.print(++i + "/" + fileList.size());
            Pair<ArrayList<LabelAndData>, Double> client = Functions.time(() -> OperationOnClient.addition(file, bloomFilter, hashFunctions, AES.geneKey(file).getEncoded(), true));
            Global.clientTime += client.getValue();

            Global.serverResponseTime += Functions.time(() -> OperationOnIndexServer.operate(client.getKey(), "init")).getValue();
            //System.out.println(OperationOnIndexServer.getInvertedIndex().size() + " " + Global.invertedValueSize + "分割的桶数目" + Global.cutNum);

        }
        Global.serverResponseTime += Functions.time(() -> OperationOnIndexServer.smooth(true, null)).getValue();
    }


    public static void deleteAndAdd(BloomFilter bloomFilter, ArrayList<ArrayList> hashFunctions, List<String> fileLists) {
        Global.serverResponseTime = 0;
        Global.clientTime = 0;

        // 删除
        for (String path : fileLists) {
            Pair<byte[], Double> clientResult = Functions.time(() -> OperationOnClient.deletion(path, AES.geneKey(path).getEncoded()));
            Global.clientTime += clientResult.getValue();
            ArrayList<LabelAndData> list = new ArrayList<>();
            list.add(new LabelAndData(clientResult.getKey(), null, null));
            Global.serverResponseTime += Functions.time(() -> OperationOnIndexServer.operate(list, "delete")).getValue();
        }

        // 添加
        int i = 0;
        for (String path : fileLists) {
            System.out.print(++i + "/" + fileLists.size());
            Pair<ArrayList<LabelAndData>, Double> client = Functions.time(() -> OperationOnClient.addition(path, bloomFilter, hashFunctions, AES.geneKey(path).getEncoded(), false));
            Global.clientTime += client.getValue();
            Global.serverResponseTime += Functions.time(() -> OperationOnIndexServer.operate(client.getKey(), "add")).getValue();
        }
    }

    public static void search(BloomFilter bloomFilter, ArrayList<ArrayList> hashFunctions, List<String> wordList) throws Exception {
        Global.serverResponseTime = 0;
        Global.clientTime = 0;

        System.out.println("Please input the number of keywords you want to search:");

        //List<String> wordList = new ArrayList<>();
        //wordList.add("keyboard");
        for (String keyword : wordList) {
            System.out.println("keyword:" + keyword);

            Functions.getPathList(searchKeyword(bloomFilter, hashFunctions, keyword), Global.top);
        }
    }

    static ArrayList<String> searchKeyword(BloomFilter bloomFilter, ArrayList<ArrayList> hashFunctions, String keyword) throws Exception {
        byte[] rkey = AES.geneKey(null).getEncoded();

        ArrayList<String> indPathList = new ArrayList<>();

        Pair<ArrayList<Token>, Double> pair = Functions.time(() -> OperationOnClient.searchClientToServer(keyword, bloomFilter, hashFunctions, rkey));

        Global.clientTime += pair.getValue();

        ArrayList<Token> tokens = pair.getKey();
        if (tokens != null) {
            for (Token token : tokens) {
                if (token == null || token.getTokenOfCS() == null) {
                    System.out.println("token is null!");
                    continue;
                } else {
                    Pair<ArrayList<byte[]>, Double> indexServer = Functions.time(() -> OperationOnIndexServer.searchAndUpdate(token.getTokenOfCS()));

                    Global.serverResponseTime += indexServer.getValue();
                    Global.serverResponseTime += Functions.time(() -> indPathList.addAll(OperationOnDataServer.search(indexServer.getKey(), token.getTokenOfDataServer()))).getValue();
                }
            }
        }
        return indPathList;
    }


    public static void main(String args[]) throws Exception {
        getIndex();

        Scanner sc = new Scanner(System.in);
        System.out.println("Please choose what do you want");
        System.out.println("\tInitialize the data set --- 0");
        System.out.println("\tDelete file from index ---- 1");
        System.out.println("\tSearch keyword from index - 2");
        System.out.println("\tEnd the program ------------3");


        BloomFilter bloomFilter = BloomFilter.readLshFromFile(Global.BloomFilterPath);
        ArrayList<ArrayList> hashFunctions = MinHash.readLshFromFile(Global.LSHPath);

        while (sc.hasNext()) {
            String input = sc.nextLine();
            switch (input) {
                // Initialize the data set
                case "0":

                    ArrayList<String> fileList = ReadFile.readFileList(Global.fileListPath);

                    init(bloomFilter, hashFunctions, fileList);

                    System.out.println("关键字总数：" + Global.keywordSize);
                    System.out.println("不同的关键字总数: " + OperationOnClient.getKeywordSet().size());

                    System.out.println("invertedIndex的大小: " + OperationOnIndexServer.getInvertedIndex().size());
                    System.out.println("forwardIndex的大小: " + OperationOnIndexServer.getForwardIndex().size());
                    System.out.println("DictKwd的大小: " + OperationOnClient.getDictKwd().size());

                    System.out.println("serverResponseTime: " + Global.serverResponseTime);
                    System.out.println("serverResponseTimeAvg: " + Global.serverResponseTime / fileList.size());
                    System.out.println("clientTime: " + Global.clientTime);
                    System.out.println("clientTimeAvg: " + Global.clientTime / fileList.size());
                    break;

                // Delete file from index
                case "1":

                    System.out.println("Please input the number of file you want to delete:");
                    List<String> fileLists = GenerateTestData.generateFileList(sc.nextInt());

                    deleteAndAdd(bloomFilter, hashFunctions, fileLists);

                    System.out.println("serverResponseTime: " + Global.serverResponseTime);
                    System.out.println("serverResponseTimeAvg: " + Global.serverResponseTime / fileLists.size());
                    System.out.println("clientTime: " + Global.clientTime);
                    System.out.println("clientTimeAvg: " + Global.clientTime / fileLists.size());
                    break;

                // Search keyword from index
                case "2":

                    System.out.println("Please input the number of file you want to search:");
                    List<String> wordList = GenerateTestData.generateKeywords(sc.nextInt());

                    search(bloomFilter, hashFunctions, wordList);

                    System.out.println("查询的数据中被更新的关键字数目: " + Global.updateNum + "---" + "占总数的百分比：" + Global.updateNum * 1.0 / (wordList.size() * Global.hashOr) * 100.0 + "%");
                    System.out.println("serverResponseTime: " + Global.serverResponseTime);
                    System.out.println("serverResponseTimeAvg: " + Global.serverResponseTime / wordList.size());
                    System.out.println("clientTime: " + Global.clientTime);
                    System.out.println("clientTimeAvg: " + Global.clientTime / wordList.size());
                    break;

                // End
                case "3":
                    Functions.time(() -> setIndex()).getValue();
                    return;
                default:
                    System.out.println("Finished, please input again or end the program!");

            }
        }
    }
}