package org.caonima.event;

public class DataCenterMessageEvent extends MessageEvent{

    /**
     * 接收获取vpn列表命令
     */
    public static final int EVENT_GET_VPN_NODE_LIST_RECV = DataCenterEventStart+1;

    /**
     * 接收获取vpn列表命令
     */
    public static final int EVENT_GET_VPN_NODE_CONFIG_RECV = DataCenterEventStart+2;

    /**
     * 获取vpn列表命令
     */
    public static final int EVENT_GET_VPN_NODE_LIST_COMMAND = DataCenterEventStart+3;

    /**
     * 获取vpn列表命令
     */
    public static final int EVENT_GET_VPN_NODE_CONFIG_COMMAND = DataCenterEventStart+4;
}
