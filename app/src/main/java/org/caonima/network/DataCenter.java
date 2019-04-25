package org.caonima.network;

import android.util.Log;

import com.google.gson.Gson;

import org.caonima.event.DataCenterMessageEvent;
import org.caonima.event.MessageEvent;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DataCenter {

    public static void getVpnList()
    {
        HashMap<String,String> heads = new HashMap<>();
        heads.put("Host",Ok.HOST);
        Ok.execute(Ok.HTTP,
                API.Srvs_Csv_Gz,
                Method.GET,
                heads,
                null,
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) {


                    }
                });
    }

    public static void getNodeConfig(String idHash)
    {
        HashMap<String,String> heads = new HashMap<>();
        heads.put("Host",Ok.HOST);
        HashMap<String,String> pareams = new HashMap<>();
        pareams.put("app","get_cfg");
        pareams.put("id_hash",idHash);
        Ok.execute(Ok.HTTP,
                API.Api_V1_Php,
                Method.POST,
                heads,
                pareams,
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String config = response.body().string();
                        Gson gson = new Gson();
                        SSRConfig ssrConfig = gson.fromJson(config,SSRConfig.class);
                        MessageEvent messageEvent = new DataCenterMessageEvent();
                        messageEvent.event = DataCenterMessageEvent.EVENT_GET_VPN_NODE_CONFIG;
                        messageEvent.data = ssrConfig.getProfile();
                        /**
                         * 获取配置然后发射到需要这个配置的地方，事件监听为DataCenterMessageEvent.EVENT_GET_VPN_NODE_CONFIG
                         */
                        EventBus.getDefault().post(messageEvent);
                    }
                });
    }
}
