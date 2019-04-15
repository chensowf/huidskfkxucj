package com.github.shadowsocks.base;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.github.shadowsocks.aidl.IShadowsocksService;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.github.shadowsocks.data.Profile;
import com.github.shadowsocks.utils.Action;
import com.github.shadowsocks.utils.State;
import com.github.shadowsocks.utils.TrafficMonitor;
import com.github.shadowsocks.utils.TrafficMonitorThread;

import java.util.Timer;
import java.util.TimerTask;

public abstract class BaseService extends VpnService {

    private int state = State.STOPPED;
    protected Profile profile;
    private Timer timer;
    private TrafficMonitorThread trafficMonitorThread;

    private RemoteCallbackList<IShadowsocksServiceCallback> callbacks = new RemoteCallbackList<>();
    private int callbacksCount;

    protected String protectPath;
    private boolean closeReceiverRegistered;

    private BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    public BaseService()
    {
        protectPath = getApplicationInfo().dataDir + "/protect_path";
    }

    protected IShadowsocksService binder = new IShadowsocksService.Stub() {
        @Override
        public int getState() throws RemoteException {
            return state;
        }

        @Override
        public String getProfileName() throws RemoteException {
            return profile == null?null:profile.getName();
        }

        @Override
        public void registerCallback(IShadowsocksServiceCallback cb) throws RemoteException {
            if(cb != null && callbacks.register(cb))
            {
                callbacksCount++;
                if(callbacksCount != 0 && timer == null)
                {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                           // if(TrafficMonitor.updateRate())
                              //  updateTrafficRate();
                        }
                    };
                    timer = new Timer(true);
                    timer.schedule(task, 1000, 1000);
                }
                TrafficMonitor.updateRate();
                cb.trafficUpdated(TrafficMonitor.txRate, TrafficMonitor.rxRate, TrafficMonitor.txTotal, TrafficMonitor.rxTotal);
            }
        }

        @Override
        public void unregisterCallback(IShadowsocksServiceCallback cb) throws RemoteException {

        }

        @Override
        public void use(Profile profile) throws RemoteException {
            final Profile profile1 = profile;
            if(profile1 == null)
                stopRunner(true, null);
            else
            {
                switch (state)
                {
                    case State.STOPPED:
                        startRunner(profile1);
                        break;
                    case State.CONNECTED:
                        stopRunner(false,null);
                        startRunner(profile);
                        break;
                }
            }
        }

        @Override
        public void useSync(Profile profile) throws RemoteException {
            use(profile);
        }
    };

    /**
     * 停止运作
     * @param stopService
     * @param msg
     */
    protected void stopRunner(boolean stopService, String msg)
    {
        if(closeReceiverRegistered)
        {
            unregisterReceiver(closeReceiver);
            closeReceiverRegistered = false;
        }

        if(trafficMonitorThread != null)
        {
            trafficMonitorThread.stopThread();
            trafficMonitorThread = null;
        }

        changeState(State.STOPPED, msg);

        if(stopService) stopSelf();

        profile = null;
    }

    /**
     * 开启运行
     * @param profile
     */
    protected void startRunner(Profile profile)
    {
        this.profile = profile;

        startService(new Intent(this,getClass()));
        TrafficMonitor.reset();

        trafficMonitorThread = new TrafficMonitorThread(getApplicationContext());
        trafficMonitorThread.start();

        if(!closeReceiverRegistered)
        {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SHUTDOWN);
            intentFilter.addAction(Action.CLOSE);
            registerReceiver(closeReceiver, intentFilter);
            closeReceiverRegistered = true;
        }

        changeState(State.CONNECTING, null);

        connect();
    }

    protected void changeState(final int s, final String msg)
    {
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(state != s || msg != null)
                {
                    if(callbacksCount > 0)
                    {
                        int n = callbacks.beginBroadcast();
                        for(int i = 0; i < n; i++)
                        {
                            try {
                                callbacks.getBroadcastItem(i)
                                        .stateChanged(s,binder.getProfileName(),msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        callbacks.finishBroadcast();
                    }
                    state = s;
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    /**
     * 获取状态值
     * @return
     */
    public int getState()
    {
        return state;
    }

    public abstract void connect();
}
