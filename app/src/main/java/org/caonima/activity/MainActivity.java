package org.caonima.activity;

import android.os.Bundle;
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
import android.widget.Button;

import com.github.shadowsocks.SSRSDK;
import com.github.shadowsocks.constant.State;
import com.github.shadowsocks.data.Profile;
import com.github.shadowsocks.interfaces.VpnCallback;

import org.caonima.R;
import org.caonima.event.DataCenterMessageEvent;
import org.caonima.event.MessageEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,VpnCallback {

    Button mVpnConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mVpnConnectButton = findViewById(R.id.main_vpn_connect_button);

        mVpnConnectButton.setOnClickListener(v -> {
            /*MessageEvent messageEvent = new DataCenterMessageEvent();
            messageEvent.event = DataCenterMessageEvent.EVENT_GET_VPN_NODE_CONFIG_COMMAND;
            messageEvent.data = "b3c4c3d62012c11ce7776cf59974fea1";

            EventBus.getDefault().post(messageEvent);*/
            MessageEvent messageEvent = new DataCenterMessageEvent();
            messageEvent.event = DataCenterMessageEvent.EVENT_GET_VPN_NODE_LIST_COMMAND;
            EventBus.getDefault().post(messageEvent);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
                break;
            case State.STOPPED:
                mVpnConnectButton.setText("Stopped");
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
            SSRSDK.startVpn(this,MainActivity.class,profile);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
