package com.github.shadowsocks;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Handler;
import android.os.RemoteException;

import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.github.shadowsocks.constant.State;
import com.github.shadowsocks.data.Profile;

import static android.app.Activity.RESULT_OK;

public class ShadowsocksRunner extends ServiceBoundContext {

    public static final String KEY = "ssr_profile";

    private final int REQUEST_CONNECT = 1;
    Handler handler = new Handler();
    BroadcastReceiver receiver;

    Profile profile;

    private IShadowsocksServiceCallback.Stub stateCallback = new IShadowsocksServiceCallback.Stub() {
        @Override
        public void stateChanged(int state, String profileName, String msg) throws RemoteException {
            if(SSRSDK.getVpnCallback() != null)
            {
                SSRSDK.getVpnCallback().onVpnState(state);
            }

            if(state == State.STOPPED)
                onDestroy();
        }

        @Override
        public void trafficUpdated(long txRate, long rxRate, long txTotal, long rxTotal) throws RemoteException {

        }
    };

    public ShadowsocksRunner(Context base) {
        super(base);
    }

    @Override
    public void onServiceConnected() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (bgService != null) {
                    startBackgroundService();
                }
            }
        },1000);
    }

    public void startBackgroundService() {
        Intent prepare = VpnService.prepare(this);
        if (prepare != null) {
            ((Activity)getBaseContext()).startActivityForResult(prepare,REQUEST_CONNECT);
        } else {
            onActivityResult(REQUEST_CONNECT,RESULT_OK,null);
        }
    }

    public void onCreateVpn(Profile profile) {
        this.profile = profile;
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km.inKeyguardRestrictedInputMode();
        if (locked) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() == Intent.ACTION_USER_PRESENT) {
                        attachService(stateCallback);
                    }
                }
            };
            registerReceiver(receiver, filter);
        } else {
            attachService(stateCallback);
        }
    }

    /**
     * 注销vpn
     */
    protected void onDestroy() {
        detachService();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                if (bgService != null) {
                    try {
                        bgService.use(profile);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public void onServiceDisconnected() {

    }
}
