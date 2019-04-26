package org.caonima.bean;

import java.util.ArrayList;
import java.util.List;

public class Node {

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

    public static List<Node> getNodeList(String config)
    {
        List<Node> nodeList = new ArrayList<>();
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
            nodeList.add(node);
        }
        return nodeList;
    }
}
