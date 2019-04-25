package org.caonima.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.caonima.event.DataCenterMessageEvent;
import org.caonima.event.MessageEvent;
import org.caonima.network.DataCenter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class DataCenterService extends Service {


    public static void startDataCenterService(Context context)
    {
        Intent intent = new Intent(context,DataCenterService.class);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    public void recvCommand(MessageEvent event)
    {
        switch (event.event)
        {
            case DataCenterMessageEvent
                    .EVENT_GET_VPN_NODE_LIST:
                DataCenter.getVpnList();
                break;
            case DataCenterMessageEvent
                    .EVENT_GET_VPN_NODE_CONFIG:
                DataCenter.getNodeConfig((String) event.data);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
