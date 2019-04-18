package com.liang.victor.myssrlib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.shadowsocks.SSRSDK;
import com.github.shadowsocks.ShadowsocksRunnerActivity;
import com.github.shadowsocks.constant.Route;
import com.github.shadowsocks.data.Profile;
import com.github.shadowsocks.constant.Method;
import com.github.shadowsocks.constant.Obfs;
import com.github.shadowsocks.constant.Protocol;
import com.github.shadowsocks.constant.State;
import com.github.shadowsocks.interfaces.VpnCallback;


public class MainActivity extends Activity implements View.OnClickListener,VpnCallback {

    private Button button;
    boolean mIsStartVpn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.switchButton);
        button.setOnClickListener(this);
        button.setText("未连接");
        SSRSDK.setVpnCallback(this);
    }

    @Override
    public void onClick(View v) {

        if(mIsStartVpn) {
            SSRSDK.stopVpn(this);
            return;
        }

        Profile profile = new Profile();
        profile.setHost("38.106.22.239");
        profile.setRemotePort(23334);
        profile.setPassword("caonima");
        profile.setMethod(Method.CHACHA20);
        profile.setProtocol(Protocol.AUTH_AES128_SHA1);
        profile.setObfs(Obfs.TLSL1_2_TICKET_AUTH);
        profile.setUdpdns(true);
        profile.setRoute(Route.GFWLIST);

        SSRSDK.startVpn(this,profile);
    }

    @Override
    public void onVpnState(int state) {
        switch (state) {
            case State.CONNECTING:
                button.setText("连接中");
                break;
            case State.CONNECTED:
                button.setText("已连接");
                mIsStartVpn = true;
                break;
            case State.STOPPED:
                button.setText("已停止");
                mIsStartVpn = false;
                break;
            case State.STOPPING:
                button.setText("正在停止");
                break;
        }
    }

}
