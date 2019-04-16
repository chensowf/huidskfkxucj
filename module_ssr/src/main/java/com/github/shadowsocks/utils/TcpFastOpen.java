package com.github.shadowsocks.utils;

import java.io.File;

public class TcpFastOpen {

    public static boolean sendEnable()
    {
        File file = new File("/proc/sys/net/ipv4/tcp_fastopen");
        if(file.canRead())
        {
            String ret = Utils.readAllLines(file);
            int i = Integer.parseInt(ret);
            return (i & 1) > 0;
        }
        else
            return false;
    }
}
