package BuildIndex;

import Base.InvertedIndexValue;
import Base.Label;
import Base.LabelAndData;
import Base.TokenOfCS;
import Utils.Functions;
import Utils.Global;

import java.util.*;

/**
 * forward索引和inverted索引
 * 以及索引上的相关操作
 */
public class OperationOnIndexServer {

    public static HashMap<Label, ArrayList<Label>> forwardIndex = new HashMap<>();
    public static HashMap<Label, ArrayList<InvertedIndexValue>> invertedIndex = new HashMap<>();

    public static int invertedBucketSize = Global.InvertedBucketSize;
    public static int forwardBucketSize = Global.ForwardBucketSize;

    private static InvertedIndexValue invertedFakeValue = new InvertedIndexValue(null, null);
    private static Label forwardFakeValue = new Label();


    /**
     * 更新索引信息，将ind的相关信息发送到server2
     *
     * @param token
     * @return
     */
    public static ArrayList<byte[]> searchAndUpdate(TokenOfCS token) {

        ArrayList<InvertedIndexValue> labelList = new ArrayList<>();
        ArrayList<byte[]> indList = new ArrayList<>();
        ArrayList<InvertedIndexValue> labelListNew = new ArrayList<>();

        // 判断数据是否有更新
        if (token.getLabelUkeyword() != null) {
            System.out.println("有数据更新，需要更新索引");
            Global.updateNum ++;
            // 获取该label对应的数据，等待更新；随后该索引中该label对应的数据删除
            labelList.addAll(getAndDeleteByKeyword(token.getLabelUkeyword(), true));

            // 更新数据
            for (InvertedIndexValue invertedIndexValue : labelList) {
                byte[] newData = Functions.xor(invertedIndexValue.getData(), token.getuKeyAndNkey());
                // 更新前向索引中该keyword对应的数据
                updateForwardIndex(invertedIndexValue.getLabelInd(), token.getLabelUkeyword(), token.getLabelNewKeyword());
                // 将新数据插入inverted 索引中
                labelListNew.add(new InvertedIndexValue(invertedIndexValue.getLabelInd(), newData));
                // 通过异或获得ind与rkey的异或值
                indList.add(Functions.xor(Functions.xor(invertedIndexValue.getData(), token.getuKeyAndNkey()), token.getNkeyAndRkey()));
            }

            // 从索引中获取labelw对应的ind和data
            if (token.getLabelKeyword() != null) {
                labelList.clear();
                // 获取该label对应的数据，等待更新；随后该索引中该label对应的数据删除
                labelList.addAll(getAndDeleteByKeyword(token.getLabelKeyword(), true));

                // 更新数据
                for (InvertedIndexValue invertedIndexValue : labelList) {
                    byte[] newData = Functions.xor(invertedIndexValue.getData(), token.getkAndNkey());
                    // 更新前向索引中该keyword对应的数据
                    updateForwardIndex(invertedIndexValue.getLabelInd(), token.getLabelKeyword(), token.getLabelNewKeyword());
                    // 将新数据插入inverted索引中
                    labelListNew.add(new InvertedIndexValue(invertedIndexValue.getLabelInd(), newData));
                    // 通过异或获得ind与rkey的异或值
                    indList.add(Functions.xor(Functions.xor(invertedIndexValue.getData(), token.getkAndNkey()), token.getNkeyAndRkey()));
                }
            }
            updateInvertedIndex(token.getLabelNewKeyword(), labelListNew);
        } else {
            // 获取该label对应的数据
            labelList.addAll(getAndDeleteByKeyword(token.getLabelKeyword(), false));

            // 获取数据
            for (InvertedIndexValue invertedIndexValue : labelList) {
                indList.add(Functions.xor(invertedIndexValue.getData(), token.getKeyAndRkey()));
            }
        }

        return indList;
    }


    /**
     * 对外部开放的针对索引的操作方法
     *
     * @param list
     * @param op
     * @return
     */
    public static ArrayList operate(ArrayList<LabelAndData> list, String op) {
        if (list == null) {
            return null;
        }
        // 初始化
        if (list.size() > 1 && op.equals("init")) {
            insert(list, true);
        }
        // 增加
        else if (op.equals("add")) {
            insert(list, false);
            smooth(false, list);
        }
        //删
        else if (list.size() == 1 && op.equals("delete")) {
            deleteByInd(list.get(0).getLabelInd());
        }

        return new ArrayList();
    }

    // 将某个文件生成的数据插入到forward index和inverted index中
    private static void insert(ArrayList<LabelAndData> list, boolean isInit) {
        if (list.size() <= 0)
            return;
        ArrayList<Label> labelWs = new ArrayList<>();
        for (LabelAndData labelAndData : list) {
            insert2InvertedIndex(labelAndData, isInit);
            labelWs.add(new Label(labelAndData.getLabelKeyword()));
        }

        insert2ForwardIndex(list.get(0).getLabelInd(), labelWs);

    }

    /**
     * 更新inverted index中某个keyword对应的数据
     *
     * @param labelAndDataList
     */
    private static void updateInvertedIndex(byte[] labelKeyword, ArrayList<InvertedIndexValue> labelAndDataList) {

        int i = 1;
        int sum = labelAndDataList.size();
        while (sum > 0) {
            Label label = new Label(Functions.xor(labelKeyword, i));
            ArrayList list = new ArrayList();
            if (labelAndDataList.size() < invertedBucketSize) {
                list.addAll(labelAndDataList);
                sum -= labelAndDataList.size();
            } else {
                int to = i * invertedBucketSize < labelAndDataList.size() ? i * invertedBucketSize : labelAndDataList.size();
                list.addAll(labelAndDataList.subList((i - 1) * invertedBucketSize, to));
                sum -= list.size();
                i++;
            }
            // 将最后一个桶抹平
            while (list.size() < invertedBucketSize) {
                list.add(invertedFakeValue);
            }
            invertedIndex.put(label, list);
        }
    }

    public static int smooth(boolean isAll, ArrayList<LabelAndData> list) {
        if (isAll) {
            // int i=0;
            for (Map.Entry<Label, ArrayList<InvertedIndexValue>> entry : invertedIndex.entrySet()) {
                //System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue());
                ArrayList<InvertedIndexValue> invertedList = entry.getValue();
                while (invertedList.size() < invertedBucketSize) {
                    invertedList.add(invertedFakeValue);
                }
                // System.out.println(i++);
            }
            return 1;
        } else if (list != null) {
            for (LabelAndData labelAndData : list) {
                int j = 1;
                boolean flag = true;
                // 将数据插入到inverted索引中
                while (flag) {
                    Label label = new Label(Functions.xor(labelAndData.getLabelKeyword(), j));
                    ArrayList invertedList = invertedIndex.get(label);
                    if (invertedList == null)
                        flag = false;
                    else {
                        while (invertedList.size() < invertedBucketSize)
                            invertedList.add(invertedFakeValue);
                    }
                    invertedIndex.put(label, invertedList);
                    j++;
                }
            }
        }
        return 1;
    }


    /**
     * 向inverted index 索引中插入新的数据
     *
     * @param labelAndData
     */
    private static void insert2InvertedIndex(LabelAndData labelAndData, boolean isInit) {
        int j = 1;
        InvertedIndexValue invertedIndexValue = new InvertedIndexValue(labelAndData.getLabelInd(), labelAndData.getData());
        Global.invertedValueSize++;
        // 将数据插入到inverted索引中
        while (true) {
            Label label = new Label(Functions.xor(labelAndData.getLabelKeyword(), j));
            //System.out.println(label.hashCode());
            ArrayList list = invertedIndex.get(label);
            if (list == null) {
                list = new ArrayList<InvertedIndexValue>(invertedBucketSize);
                if (j > 1)
                    Global.cutNum++;
                list.add(invertedIndexValue);
                invertedIndex.put(label, list);
                return;
            } else if (list.size() < invertedBucketSize) {
                list.add(invertedIndexValue);
                return;
            } else if (!isInit) {
                list.remove(invertedFakeValue);
                list.add(invertedIndexValue);
                return;
            } else {
                j++;
            }
        }
    }


    /**
     * 向forward index 索引中插入数据
     *
     * @param labelInd
     * @param labelWs
     */
    public static void insert2ForwardIndex(byte[] labelInd, ArrayList<Label> labelWs) {
        // 将数据插入到forward索引中
        int i = 1;
        int sum = labelWs.size();
        while (sum > 0) {
            Label label = new Label(Functions.xor(labelInd, i));
            ArrayList<Label> list = new ArrayList<>(forwardBucketSize);
            // 加入最新的数据
            if (labelWs.size() < forwardBucketSize) {
                list.addAll(labelWs);
                sum -= labelWs.size();
            } else {
                int from = (i - 1) * forwardBucketSize;
                int to = i * forwardBucketSize < labelWs.size() ? i * forwardBucketSize : labelWs.size();
                list.addAll(labelWs.subList(from, to));
                sum -= list.size();
                i++;
            }
            // 将最后一个桶抹平
            while (list.size() < forwardBucketSize) {
                list.add(forwardFakeValue);
            }
            forwardIndex.put(label, list);
        }
    }


    // 将某个文件的数据从索引中删除
    private static void deleteByInd(byte[] labelInd) {
        int i = 1;
        while (true) {
            Label label = new Label(Functions.xor(labelInd, i));
            ArrayList<Label> list = forwardIndex.get(label);
            if (list == null) {
                return;
            } else {
                // 从inverted索引中删除数据
                for (Label labelW : list) {
                    if (!labelW.equals(forwardFakeValue))
                        deleteByKeywordAndInd(labelW, labelInd);
                }
                // 从forward索引中删除数据
                forwardIndex.remove(label);
                label = null;
                i++;
            }
        }
    }


    // 将inverted索引中某个keyword对应文件为labelInd的更新
    private static void deleteByKeywordAndInd(Label labelKeyword, byte[] labelInd) {
        int j = 1;
        while (true) {
            Label label = new Label(Functions.xor(labelKeyword.getLabel(), j));
            ArrayList<InvertedIndexValue> list = invertedIndex.get(label);
            if (list == null) {
                return;
            } else {
                for (int i = 0; i < list.size(); i++) {
                    InvertedIndexValue invertedIndexValue = list.get(i);
                    if (invertedIndexValue != null && isLabelEquals(labelInd, invertedIndexValue.getLabelInd())) {
                        Collections.replaceAll(list, invertedIndexValue, invertedFakeValue);
                    }
                }
                j++;
            }
        }
    }


    // 从forward索引中获取某个文件对应的所有数据
    private static ArrayList<Label> updateForwardIndex(byte[] labelInd, byte[] labelKeyword, byte[] labelNewKeyword) {
        int i = 1;
        while (true) {
            Label label = new Label(Functions.xor(labelInd, i));
            ArrayList<Label> tempList = forwardIndex.get(label);
            if (tempList == null) {
                return tempList;
            } else {
                Collections.replaceAll(tempList, new Label(labelKeyword), new Label(labelNewKeyword));
                i++;
            }
        }
    }

    // 从inverted索引中获取某个keyword对应的所有数据并删除
    private static ArrayList<InvertedIndexValue> getAndDeleteByKeyword(byte[] labelKeyword, boolean isUpdate) {
        ArrayList<InvertedIndexValue> list = new ArrayList<>();
        int j = 1;
        while (true) {
            Label label = new Label(Functions.xor(labelKeyword, j));
            ArrayList<InvertedIndexValue> tempList = invertedIndex.get(label);
            if (tempList == null) {
                return list;
            } else {
                for (InvertedIndexValue invertedIndexValue : tempList) {
                    if (invertedIndexValue != null && !isLabelEquals(invertedIndexValue.getLabelInd(), invertedFakeValue.getLabelInd())) {
                        list.add(invertedIndexValue);
                    }
                }
                j++;
            }
            if (isUpdate)
                invertedIndex.remove(label);
        }
    }


    private static boolean isLabelEquals(byte[] label1, byte[] label2) {
        if (label1 == null && label2 == null) {
            return true;
        }
        return Arrays.equals(label1, label2);
    }

    public static HashMap<Label, ArrayList<Label>> getForwardIndex() {
        return forwardIndex;
    }

    public static void setForwardIndex(HashMap<Label, ArrayList<Label>> forwardIndex) {
        OperationOnIndexServer.forwardIndex = forwardIndex;
    }

    public static HashMap<Label, ArrayList<InvertedIndexValue>> getInvertedIndex() {
        return invertedIndex;
    }

    public static void setInvertedIndex(HashMap<Label, ArrayList<InvertedIndexValue>> invertedIndex) {
        OperationOnIndexServer.invertedIndex = invertedIndex;
    }

}
