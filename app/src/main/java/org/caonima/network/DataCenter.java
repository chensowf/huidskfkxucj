package org.caonima.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.caonima.bean.Node;
import org.caonima.bean.QueryResult;
import org.caonima.bean.SSRConfig;
import org.caonima.event.DataCenterMessageEvent;
import org.caonima.event.MessageEvent;
import org.caonima.utils.Gzip;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

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
                    public void onResponse(Call call, Response response) throws IOException {
                        byte[] buffer = response.body().bytes();
                        String config = Gzip.decompress(buffer);
                        Log.e("config",config);
                        List<Node> nodeList = Node.getNodeList(config);
                        MessageEvent messageEvent = new DataCenterMessageEvent();
                        messageEvent.event = DataCenterMessageEvent.EVENT_GET_VPN_NODE_LIST_RECV;
                        messageEvent.data = nodeList;
                        EventBus.getDefault().post(messageEvent);
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
                        Log.e("error",config);
                        Gson gson = new Gson();
                        QueryResult<List<SSRConfig>> result = gson.fromJson(config,new TypeToken<QueryResult<List<SSRConfig>>>(){}.getType());
                        MessageEvent messageEvent = new DataCenterMessageEvent();
                        messageEvent.event = DataCenterMessageEvent.EVENT_GET_VPN_NODE_CONFIG_RECV;
                        messageEvent.data = result.cfgs.get(0).getProfile();
                        /**
                         * 获取配置然后发射到需要这个配置的地方，事件监听为DataCenterMessageEvent.EVENT_GET_VPN_NODE_CONFIG
                         */
                        EventBus.getDefault().post(messageEvent);
                    }
                });
    }
}
