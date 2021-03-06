package org.caonima.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.github.shadowsocks.SSRSDK;
import com.github.shadowsocks.constant.State;
import com.github.shadowsocks.data.Profile;
import com.github.shadowsocks.interfaces.VpnCallback;
import com.wang.avi.AVLoadingIndicatorView;

import org.caonima.R;
import org.caonima.bean.Node;
import org.caonima.event.DataCenterMessageEvent;
import org.caonima.event.MessageEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,VpnCallback {
    public static final int NODE_CONFIG_REQUEST = 0X234;
    public static final String NODE_CONFIG_EXTRA = "node_config";

    Node mNode;
    Button mVpnConnectButton;
    AVLoadingIndicatorView mAVLoadingIndicatorView;
    boolean mIsVpnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);

        SSRSDK.setVpnCallback(this);
        EventBus.getDefault().register(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();
        toolbar.setNavigationOnClickListener(v -> {
            drawer.openDrawer(GravityCompat.START);
        });


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mVpnConnectButton = findViewById(R.id.main_vpn_connect_button);
        mAVLoadingIndicatorView = findViewById(R.id.main_vpn_connect_loading);
        mAVLoadingIndicatorView.setVisibility(View.GONE);

        mVpnConnectButton.setOnClickListener(v -> {
            if(mNode != null) {
                if (!mIsVpnConnect) {
                    MessageEvent messageEvent = new DataCenterMessageEvent();
                    messageEvent.event = DataCenterMessageEvent.EVENT_GET_VPN_NODE_CONFIG_COMMAND;
                    messageEvent.data = mNode.idHash;

                    EventBus.getDefault().post(messageEvent);

                    mAVLoadingIndicatorView.setVisibility(View.VISIBLE);
                } else {
                    SSRSDK.stopVpn(this);
                }
            }
            else
            {
                Snackbar.make(mVpnConnectButton,"没有选择连接节点",Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        item.setIcon(R.drawable.server_icon_am);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            NodeActivity.startNodeActivity(this,NODE_CONFIG_REQUEST);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onVpnState(int state) {
        switch (state)
        {
            case State.CONNECTED:
                mVpnConnectButton.setText("Connected");
                mAVLoadingIndicatorView.setVisibility(View.GONE);
                mIsVpnConnect = true;
                break;
            case State.STOPPED:
                mVpnConnectButton.setText("Stopped");
                mIsVpnConnect = false;
                break;
        }
    }

    /**
     * 接收数据中心服务发射过来的数据
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void recvDataCenter(MessageEvent event)
    {
        if(event.event == DataCenterMessageEvent.EVENT_GET_VPN_NODE_CONFIG_RECV)
        {
            Profile profile = (Profile) event.data;
            profile.setName(mNode.name);
            profile.setUdpdns(true);
            SSRSDK.startVpn(this,MainActivity.class,profile);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK
                && requestCode == NODE_CONFIG_REQUEST)
        {
            mNode = data.getParcelableExtra(NODE_CONFIG_EXTRA);
        }
    }
}
