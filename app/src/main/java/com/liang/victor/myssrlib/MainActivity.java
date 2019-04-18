package com.liang.victor.myssrlib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.shadowsocks.ShadowsocksRunnerActivity;
import com.github.shadowsocks.constant.Route;
import com.github.shadowsocks.data.Profile;
import com.github.shadowsocks.constant.Method;
import com.github.shadowsocks.constant.Obfs;
import com.github.shadowsocks.constant.Protocol;
import com.github.shadowsocks.constant.State;


public class MainActivity extends Activity implements View.OnClickListener {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.switchButton);
        button.setOnClickListener(this);
        button.setText("未连接");
  //      SS_SDK.getInstance().setStateCallback(this);
    }

    @Override
    public void onClick(View v) {
        //这里仅提供调用示例，具体请按照自己的配置去设置ip    远程端口    密码         协议类型
       /* SS_SDK.getInstance().setProfile("2.56.175.160", 24435, "asd123",
                Method.AES_256_CFB, Protocol.AUTH_AES128_SHA1, "", Obfs.TLSL1_2_TICKET_AUTH, "");*/
//        SS_SDK.getInstance().setProfile("192.168.1.1", 443, "ingress",
//           "auth_sha1");
        //      SS_SDK.getInstance().switchVpn(this);
        Profile profile = new Profile();
        profile.setHost("38.106.22.239");
        profile.setRemotePort(23334);
        profile.setPassword("caonima");
        profile.setMethod(Method.CHACHA20);
        profile.setProtocol(Protocol.AUTH_AES128_SHA1);
        profile.setObfs(Obfs.TLSL1_2_TICKET_AUTH);
        profile.setUdpdns(true);
        profile.setRoute(Route.GFWLIST);
        Intent intent = new Intent(this, ShadowsocksRunnerActivity.class);
        intent.putExtra(ShadowsocksRunnerActivity.KEY, profile);
        startActivity(intent);
    }

    public void callback(int state) {
        switch (state) {
            case State.CONNECTING:
                button.setText("连接中");
                break;
            case State.CONNECTED:
                button.setText("已连接");
                break;
            case State.STOPPED:
                button.setText("已停止");
                break;
            case State.STOPPING:
                button.setText("正在停止");
                break;
        }
    }

}
