package com.github.shadowsocks.utils;

public class State {
    public final static int CONNECTING = 0,
    CONNECTED = 1,
    STOPPING = 2,
    STOPPED = 3;

    public boolean isAvailable(int state)
    {
        return state != CONNECTING && state != CONNECTED;
    }
}
