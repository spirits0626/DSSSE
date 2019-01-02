package BuildIndex;

import Utils.Functions;

import java.util.ArrayList;

/**
 * 保存数据，数据与文件标识的数据结构
 * 以及相关操作
 */
public class OperationOnDataServer {

    public static ArrayList<String> search(ArrayList<byte[]> indXorRkeyList, byte[] rkey) throws Exception {
        ArrayList<String> indList = new ArrayList<>();
        for (byte[] indXorRkey : indXorRkeyList) {
            byte[] ind = Functions.xor(indXorRkey, rkey);
            String indPath = new String(ind, "UTF-8");
            //System.out.println(indPath);
            indList.add(indPath);
        }
        return indList;
    }
}
