package com.liang.victor.myssrlib;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.shadowsocks.constant.Method;
import com.github.shadowsocks.constant.Obfs;
import com.github.shadowsocks.constant.Protocol;
import com.github.shadowsocks.constant.State;

import com.github.shadowsocks.utils.SS_SDK;
import com.github.shadowsocks.utils.VpnCallback;


public class MainActivity extends Activity implements View.OnClickListener, VpnCallback {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.switchButton);
        button.setOnClickListener(this);
        button.setText("未连接");
        SS_SDK.getInstance().setStateCallback(this);
    }

    @Override
    public void onClick(View v) {
        //这里仅提供调用示例，具体请按照自己的配置去设置ip    远程端口    密码         协议类型
        SS_SDK.getInstance().setProfile("2.56.175.160", 24435, "asd123",
                Method.AES_256_CFB, Protocol.AUTH_AES128_SHA1, "", Obfs.TLSL1_2_TICKET_AUTH, "");
//        SS_SDK.getInstance().setProfile("192.168.1.1", 443, "ingress",
//           "auth_sha1");
        SS_SDK.getInstance().switchVpn(this);
       /* Profile profile = new Profile();
        profile.setHost("2.56.175.160");
        profile.setRemotePort(24435);
        profile.setPassword("asd123");
        profile.setMethod(Method.AES_256_CFB);
        profile.setProtocol(Protocol.AUTH_AES128_SHA1);
        profile.setObfs(Obfs.TLSL1_2_TICKET_AUTH);
        Intent intent = new Intent(this,ShadowsocksRunnerActivity.class);
        intent.putExtra(ShadowsocksRunnerActivity.KEY, profile);
        startActivity(intent);*/
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
