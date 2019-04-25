package org.caonima.event;

public class DataCenterMessageEvent extends MessageEvent{

    /**
     * 获取vpn列表命令
     */
    public static final int EVENT_GET_VPN_NODE_LIST = DataCenterEventStart+1;

    /**
     * 获取vpn列表命令
     */
    public static final int EVENT_GET_VPN_NODE_CONFIG = DataCenterEventStart+2;
}
