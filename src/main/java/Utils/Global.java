package Utils;

public class Global {
    public static final String ALGORITHM = "AES";

    public static int resultNum = 5;

    /**
     * bloom编码参数设置
     */
    public static int c = 2;

    public static int n = 500;

    public static int k = 15;

    /**
     * LSH参数设置
     */
    public static int hashLength = 500;

    public static int hashAnd = 5;

    public static int hashOr = 37;

    /**
     * 索引桶大小的设置
     */
    public static int InvertedBucketSize = 500;

    public static int ForwardBucketSize = 300*hashOr;

    /**
    public static String dataPath = "C:\\Users\\spirits\\Desktop\\filter\\arora-h";

    public static String fileListPath = "C:\\study\\Experimental Data\\fileList.txt";

    public static String LSHPath = "C:\\study\\Experimental Data\\LSH.txt";

    public static String BloomFilterPath = "C:\\study\\Experimental Data\\BloomFilter.txt";

    public static String forwardIndexPath = "C:\\study\\Experimental Data\\forwardIndex.txt";
 
    public static String invertedIndexPath = "C:\\study\\Experimental Data\\invertedIndex.txt";

    public static String dictKwdPath = "C:\\study\\Experimental Data\\dictKwd.txt";
     */


    public static String dataPath = "/home/wj/DSSSE/Experimental Data/test";

    public static String fileListPath = "/home/wj/DSSSE/Experimental Data/fileList.txt";

    public static String LSHPath = "/home/wj/DSSSE/Experimental Data/LSH.txt";

    public static String BloomFilterPath = "/home/wj/DSSSE/Experimental Data/BloomFilter.txt";

    public static String forwardIndexPath = "/home/wj/DSSSE/Experimental Data/forwardIndex.txt";

    public static String invertedIndexPath = "/home/wj/DSSSE/Experimental Data/invertedIndex.txt";

    public static String dictKwdPath = "/home/wj/DSSSE/Experimental Data/dictKwd.txt";


    // 统计inverted index中值的总数
    public static int invertedValueSize = 0;

    public static int keywordSize = 0;

    // 分割的桶的数目
    public static int cutNum = 0;

    // 查询的数据中被更新的关键字
    public static int updateNum = 0;
}