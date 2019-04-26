package org.caonima.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Node implements Parcelable {

    public static final int CHILE = 0;
    public static final int FATHER = 1;

    /**
     * 折叠状态
     */
    public static final int RETRACT = 0;

    /**
     * 展开状态
     */
    public static final int UNFOLD = 1;

    /**
     * 节点hash
     */
    public String idHash;

    /**
     * 节点国家
     */
    public String country;

    /**
     * 节点名字
     */
    public String name;

    /**
     * 节点总带宽
     */
    public int bandWidth;

    /**
     * 节点1分钟平均带宽
     */
    public int oneBandWidth;

    /**
     * 节点3分钟平均带宽
     */
    public int threeBandWidth;

    /**
     * 节点5分钟平均带宽
     */
    public int fiveBandWidth;

    /**
     * 节点是否被墙
     */
    public boolean isGw;

    /**
     * 节点类型
     *  0表示是父节点
     *  1表示是子节点
     */
    public int type;

    /**
     * 展开还是折叠状态
     */
    public int state;

    public List<Node> mChileNode;

    public Node(){}

    protected Node(Parcel in) {
        idHash = in.readString();
        country = in.readString();
        name = in.readString();
        bandWidth = in.readInt();
        oneBandWidth = in.readInt();
        threeBandWidth = in.readInt();
        fiveBandWidth = in.readInt();
        isGw = in.readByte() != 0;
        type = in.readInt();
        state = in.readInt();
        mChileNode = in.createTypedArrayList(Node.CREATOR);
    }

    public static final Creator<Node> CREATOR = new Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(idHash);
        parcel.writeString(country);
        parcel.writeString(name);
        parcel.writeInt(bandWidth);
        parcel.writeInt(oneBandWidth);
        parcel.writeInt(threeBandWidth);
        parcel.writeInt(fiveBandWidth);
        parcel.writeByte((byte) (isGw ? 1 : 0));
        parcel.writeInt(type);
        parcel.writeInt(state);
        parcel.writeTypedList(mChileNode);
    }

    public static List<Node> getNodeList(String config)
    {
        List<Node> nodeList = new ArrayList<>();
        HashMap<String,List<Node>> hashMap = new HashMap<>();
        config = config.replace("\"","");
        String[] nodes = config.split("\n");
        for(String str : nodes)
        {
            Node node = new Node();
            String[] strs = str.split(",");
            node.idHash = strs[0];
            node.country = strs[1];
            node.name = strs[2];
            node.bandWidth = Integer.parseInt(strs[3]);
            node.oneBandWidth = Integer.parseInt(strs[4]);
            node.threeBandWidth = Integer.parseInt(strs[5]);
            node.fiveBandWidth = Integer.parseInt(strs[6]);
            node.isGw = Boolean.parseBoolean(strs[7]);
            node.type = Node.CHILE;
            if(!hashMap.containsKey(node.country))
            {
                hashMap.put(node.country,new ArrayList<>());
                hashMap.get(node.country).add(node);
            }
            else
            {
                hashMap.get(node.country).add(node);
            }
        }
        Set set = hashMap.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext())
        {
            String country = (String) iterator.next();
            Node node = new Node();
            node.country = country;
            node.mChileNode = hashMap.get(country);
            node.type = Node.FATHER;
            node.state = Node.RETRACT;
            nodeList.add(node);
        }

        return nodeList;
    }
}
