package org.caonima.event;

public abstract class MessageEvent{

    public static final int DataCenterEventStart = 100;

    public int event;
    public Object data;
}
