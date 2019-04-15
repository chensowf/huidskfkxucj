package com.github.shadowsocks;

import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import com.github.shadowsocks.base.BaseService;
import com.github.shadowsocks.data.Profile;
import com.github.shadowsocks.utils.Action;
import com.github.shadowsocks.utils.Route;
import com.github.shadowsocks.utils.State;
import com.github.shadowsocks.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ShadowsocksVpnService extends BaseService {

    private static final String TAG = ShadowsocksVpnService.class.getSimpleName();
    private static final int VPN_MTU = 1500;
    private static final String PRIVATE_VLAN = "26.26.26.%s";
    private static final String PRIVATE_VLAN6 = "fdfe:dcba:9876::%s";
    private ParcelFileDescriptor conn;
    private ShadowsocksVpnThread vpnThread;
    private GuardProcess sslocalProcess,
    sstunnelProcess,pdnsdProcess,tun2socksProcess;
    private boolean proxychains_enable = false;
    private String host_arg = "";
    private String dns_address = "";
    private int dns_port = 0;
    private String china_dns_address = "";
    private int china_dns_port = 0;

    public ShadowsocksVpnService()
    {
        super();
    }

    @Override
    public void connect() {
        if(new File(getApplicationInfo().dataDir + "/proxychains.conf").exists())
        {
            proxychains_enable = true;
        }
        else
        {
            proxychains_enable = false;
        }

        try
        {
            int index = new Random().nextInt();
            String[] dnss = profile.getDns().split(",");
            String dns = dnss[index % dnss.length];
            dns_address = dns.split(":")[0];
            dns_port = Integer.parseInt(dns.split(":")[1]);

            index = new Random().nextInt();
            String[] china_dnss = profile.getChina_dns().split(",");
            String china_dns = china_dnss[index % dnss.length];
            china_dns_address = china_dns.split(":")[0];
            china_dns_port = Integer.parseInt(china_dns.split(":")[1]);
        }catch (Exception e)
        {
            e.printStackTrace();

            dns_address = "8.8.8.8";
            dns_port = 53;

            china_dns_address = "223.5.5.5";
            china_dns_port = 53;
        }

        vpnThread = new ShadowsocksVpnThread(this);
        vpnThread.start();

        killProcesses();

        host_arg = profile.getHost();
        if(!Utils.isNumeric(profile.getHost()))
        {
            String addr = Utils.resolve(profile.getHost(),true);
            if (addr == null) {
                return;
            }
            profile.setHost(addr);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        if(VpnService.SERVICE_INTERFACE == action)
            return super.onBind(intent);
        else if(Action.SERVICE == action)
            return binder.asBinder();
        return null;
    }

    @Override
    public void onRevoke() {
        stopRunner(true, null);
    }

    @Override
    protected void stopRunner(boolean stopService, String msg) {
        if(vpnThread != null)
        {
            vpnThread.stopThread();
            vpnThread = null;
        }

        changeState(State.STOPPING, null);
        killProcesses();

        if(conn != null) {
            try {
                conn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conn = null;
        }

        super.stopRunner(stopService, msg);
    }

    @Override
    protected void startRunner(Profile profile) {

        if(VpnService.prepare(this) != null)
        {
            Intent intent = new Intent(this, ShadowsocksRunnerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            stopRunner(true,null);
            return;
        }

        super.startRunner(profile);
    }

    private void killProcesses() {
        if (sslocalProcess != null) {
            sslocalProcess.destroy();
            sslocalProcess = null;
        }
        if (sstunnelProcess != null) {
            sstunnelProcess.destroy();
            sstunnelProcess = null;
        }
        if (tun2socksProcess != null) {
            tun2socksProcess.destroy();
            tun2socksProcess = null;
        }
        if (pdnsdProcess != null) {
            pdnsdProcess.destroy();
            pdnsdProcess = null;
        }
    }

    private int startVpn()
    {
        Builder builder = new Builder();
        builder.setSession(profile.getName())
                .setMtu(VPN_MTU)
                .addAddress(String.format(PRIVATE_VLAN,"1"),24);
        if(profile.getRoute().equals(Route.CHINALIST))
            builder.addDnsServer(china_dns_address);
        else
            builder.addDnsServer(dns_address);

        if(profile.isIpv6())
        {
            builder.addAddress(String.format(PRIVATE_VLAN6,"1"),126);
            builder.addRoute("::",0);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if(profile.isProxyApps())
            {
                String[] pkgs = profile.getIndividual().split("\n");
                if(pkgs != null && pkgs.length > 0) {
                    for (String pkg : pkgs) {
                        try {
                            if (!profile.isBypass()) {
                                builder.addAllowedApplication(pkg);
                            } else {
                                builder.addDisallowedApplication(pkg);
                            }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if(profile.getRoute() == Route.ALL || profile.getRoute() == Route.BYPASS_CHN)
        {
            builder.addRoute("0.0.0.0",0);
        }
        else
        {

        }
    }

    private void handleConnection()
    {
        int fd =
    }

    private boolean sendFd(int fd)
    {
        if(fd != -1)
        {
            int tries = 1;
            while (tries < 5)
            {
                try {
                    Thread.sleep(1000*tries);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(System.sendfd(fd,getApplicationInfo().dataDir+"/sock_path") != -1)
                {
                    return true;
                }
                tries += 1;
            }
        }
        return false;
    }

}
