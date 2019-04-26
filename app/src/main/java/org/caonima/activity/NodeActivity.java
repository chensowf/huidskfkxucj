package org.caonima.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import org.caonima.R;
import org.caonima.bean.Node;
import org.caonima.event.DataCenterMessageEvent;
import org.caonima.event.MessageEvent;
import org.caonima.widght.TreeAdapter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


public class NodeActivity extends AppCompatActivity implements TreeAdapter.OnItemClickListener{

    RecyclerView mRecyclerView;

    TreeAdapter mTreeAdapter;

    public static void startNodeActivity(Activity context, int requestCode)
    {
        context.startActivityForResult(new Intent(context, NodeActivity.class),requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node);
        EventBus.getDefault().register(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        mRecyclerView = findViewById(R.id.node_node_list);

        mTreeAdapter = new TreeAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mTreeAdapter);
        mTreeAdapter.setOnItemClickListener(this);
        getVpnNodeList();
    }

    private void getVpnNodeList()
    {
        MessageEvent messageEvent = new DataCenterMessageEvent();
        messageEvent.event = DataCenterMessageEvent.EVENT_GET_VPN_NODE_LIST_COMMAND;
        EventBus.getDefault().post(messageEvent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void recvDataCenter(MessageEvent event)
    {
        Log.e("error","event:"+event.event);
        if(event.event == DataCenterMessageEvent.EVENT_GET_VPN_NODE_LIST_RECV)
        {
            List<Node> nodeList = (List<Node>) event.data;
            mTreeAdapter.setNodeList(nodeList);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onItemClick(Node node) {
        Intent intent = new Intent();
        intent.putExtra(MainActivity.NODE_CONFIG_EXTRA,node);
        setResult(RESULT_OK,intent);
        finish();
    }
}
