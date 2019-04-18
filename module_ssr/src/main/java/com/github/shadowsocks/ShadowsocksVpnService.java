package com.github.shadowsocks;

import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.github.shadowsocks.base.BaseService;
import com.github.shadowsocks.data.Profile;
import com.github.shadowsocks.constant.Action;
import com.github.shadowsocks.interfaces.Callback;
import com.github.shadowsocks.constant.ConfigUtils;
import com.github.shadowsocks.constant.Route;
import com.github.shadowsocks.constant.State;
import com.github.shadowsocks.utils.TcpFastOpen;
import com.github.shadowsocks.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        handleConnection();
        changeState(State.CONNECTED,null);

        if(!profile.getRoute().equals(Route.ALL))
        {

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        if(VpnService.SERVICE_INTERFACE.equals(action))
            return super.onBind(intent);
        else if(Action.SERVICE.equals(action))
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
            startActivity(intent);
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
                .addAddress(String.format(Locale.ENGLISH,PRIVATE_VLAN,"1"),24);
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

        if(profile.getRoute().equals(Route.ALL) || profile.getRoute().equals(Route.BYPASS_CHN))
        {
            builder.addRoute("0.0.0.0",0);
        }
        else
        {
            String[] privateList = getResources().getStringArray(R.array.bypass_private_route);

            for(String route : privateList)
            {
                String[] addr = route.split("/");
                builder.addRoute(addr[0],Integer.parseInt(addr[1]));
            }
        }

        if(profile.getRoute().equals(Route.CHINALIST))
            builder.addRoute(china_dns_address, 32);
        else
            builder.addRoute(dns_address, 32);
        conn = builder.establish();
        if(conn == null) throw new NullPointerException("conn is null");

        final int fd = conn.getFd();

        List<String> cmd = new ArrayList<>();
        cmd.add(getApplicationInfo().dataDir+"/tun2socks");
        cmd.add("--netif-ipaddr");
        cmd.add(String.format(Locale.ENGLISH,PRIVATE_VLAN,"2"));
        cmd.add("--netif-netmask");
        cmd.add( "255.255.255.0");
        cmd.add("--socks-server-addr");
        cmd.add("127.0.0.1:" + profile.getLocalPort());
        cmd.add("--tunfd");
        cmd.add(""+fd);
        cmd.add("--tunmtu");
        cmd.add(""+VPN_MTU);
        cmd.add("--sock-path");
        cmd.add(getApplicationInfo().dataDir+"/sock_path");
        cmd.add("--loglevel");
        cmd.add("3");

        if(profile.isIpv6()) {
            cmd.add("--netif-ip6addr");
            cmd.add(String.format(Locale.ENGLISH,PRIVATE_VLAN6,"2"));
        }

        if(profile.isUdpdns())
            cmd.add("--enable-udprelay");
        else {
            cmd.add("--dnsgw");
            cmd.add(String.format(Locale.ENGLISH,"%s:%d", String.format(Locale.ENGLISH,PRIVATE_VLAN,"1"), profile.getLocalPort()+53));
        }

        tun2socksProcess = new GuardProcess(cmd).start(new Callback() {
            @Override
            public void callback() {
                sendFd(fd);
            }
        });
        Log.e("shadowsocks","this fd :"+fd);
        return fd;
    }

    private void handleConnection()
    {
        int fd = startVpn();
        if(!sendFd(fd)) new Exception("snedFd failed");

        startShadowsocksDaemon();

        if(profile.isUdpdns())
        {
            startShadowsocksUDPDaemon();
        }

        if(!profile.isUdpdns())
        {
            startDnsDaemon();
            startDnsTunnel();
        }
    }

    private void startDnsDaemon()
    {
        String reject = profile.isIpv6()?"224.0.0.0/3":"224.0.0.0/3, ::/0";
        String protect = "protect = \"" + protectPath +"\";";

        String china_dns_settings = "";
        boolean remote_dns = false;

        if(profile.getRoute().equals(Route.ACL))
        {
            List<String> lines = Utils.readAllLine(getApplicationInfo().dataDir + '/' + profile.getRoute() + ".acl");
            for(String line : lines)
            {
                if(line.equals("[remote_dns]"))
                {
                    remote_dns = true;
                }
            }
        }

        String black_list;
        switch (profile.getRoute())
        {
            case Route.BYPASS_CHN:
            case Route.BYPASS_LAN_CHN:
            case Route.GFWLIST:
                black_list = getBlackList();
                break;
            case Route.ACL:
                if(remote_dns)
                    black_list = "";
                else
                    black_list = getBlackList();
                break;
                default:
                    black_list = "";
        }
        String[] china_dnss = profile.getChina_dns().split(",");
        for(String china_dns : china_dnss)
        {
            china_dns_settings += String.format(Locale.ENGLISH,
                    ConfigUtils.REMOTE_SERVER,
                    china_dns.split(":")[0],
                    Integer.parseInt(china_dns.split(":")[1]),
                    black_list, reject);
        }

        String conf;
        switch (profile.getRoute())
        {
            case Route.BYPASS_CHN:
            case Route.BYPASS_LAN_CHN:
            case Route.GFWLIST:
                conf = String.format(ConfigUtils.PDNSD_DIRECT,
                        protect,
                        getApplicationInfo().dataDir,
                        "0.0.0.0",
                        profile.getLocalPort() + 53,
                        china_dns_settings,
                        profile.getLocalPort() + 63,
                        reject);
                break;
            case Route.CHINALIST:
                conf = String.format(ConfigUtils.PDNSD_DIRECT,
                        protect,
                        getApplicationInfo().dataDir,
                        "0.0.0.0",
                        profile.getLocalPort() + 53,
                        china_dns_settings,
                        profile.getLocalPort() + 63,
                        reject);
                break;
            case Route.ACL:
                if(!remote_dns)
                {
                    conf = String.format(ConfigUtils.PDNSD_DIRECT,
                            protect,
                            getApplicationInfo().dataDir,
                            "0.0.0.0",
                            profile.getLocalPort() + 53,
                            china_dns_settings,
                            profile.getLocalPort() + 63,
                            reject);
                }
                else
                {
                    conf = String.format(ConfigUtils.PDNSD_LOCAL,
                            protect,
                            getApplicationInfo().dataDir,
                            "0.0.0.0",
                            profile.getLocalPort() + 53,
                            profile.getLocalPort() + 63,
                            reject);
                }
                break;
                default:
                    conf = String.format(Locale.ENGLISH,ConfigUtils.PDNSD_LOCAL,
                            protect,
                            getApplicationInfo().dataDir,
                            "0.0.0.0",
                            profile.getLocalPort() + 53,
                            profile.getLocalPort() + 63,
                            reject);
        }

        Utils.printToFile(conf,new File(getApplicationInfo().dataDir + "/pdnsd-vpn.conf"));

        List<String> cmd = new ArrayList<>();
        cmd.add(getApplicationInfo().dataDir + "/pdnsd");
        cmd.add("-c");
        cmd.add(getApplicationInfo().dataDir + "/pdnsd-vpn.conf");

        pdnsdProcess = new GuardProcess(cmd).start(null);
    }

    private void startDnsTunnel()
    {
        String conf = String.format(ConfigUtils.SHADOWSOCKS,
                profile.getHost(),
                profile.getRemotePort(),
                profile.getLocalPort() + 63,
                ConfigUtils.EscapedJson(profile.getPassword()),
                profile.getMethod(),
                600,
                profile.getProtocol(),
                profile.getObfs(),
                ConfigUtils.EscapedJson(profile.getObfs_param()),
                ConfigUtils.EscapedJson(profile.getProtocol_param()));

        Utils.printToFile(conf, new File(getApplicationInfo().dataDir + "/ss-tunnel-vpn.conf"));

        List<String> cmd = new ArrayList<>();
        cmd.add(getApplicationInfo().dataDir + "/ss-local");
        cmd.add("-V");
        cmd.add("-u");
        cmd.add("-t");
        cmd.add("60");
        cmd.add("--host");
        cmd.add(host_arg);
        cmd.add("-b");
        cmd.add("127.0.0.1");
        cmd.add("-P");
        cmd.add(getApplicationInfo().dataDir);
        cmd.add("-c");
        cmd.add(getApplicationInfo().dataDir + "/ss-tunnel-vpn.conf");
        cmd.add("-L");
        if(profile.getRoute().equals(Route.CHINALIST))
        {
            cmd.add(china_dns_address + ":" + china_dns_port);
        }
        else
        {
            cmd.add(dns_address + ":" + dns_port);
        }

        if(proxychains_enable)
        {
            cmd.add("LD_PRELOAD=" + getApplicationInfo().dataDir + "/lib/libproxychains4.so");
            cmd.add("PROXYCHAINS_CONF_FILE=" + getApplicationInfo().dataDir + "/proxychains.conf");
            cmd.add("PROXYCHAINS_PROTECT_FD_PREFIX=" + getApplicationInfo().dataDir);
            cmd.add("env");
        }

        sstunnelProcess = new GuardProcess(cmd).start(null);
    }

    private void startShadowsocksDaemon()
    {
        String conf = String.format(ConfigUtils.SHADOWSOCKS,
                profile.getHost(),
                profile.getRemotePort(),
                profile.getLocalPort(),
                ConfigUtils.EscapedJson(profile.getPassword()),
                profile.getMethod(),
                600,
                profile.getProtocol(),
                profile.getObfs(),
                ConfigUtils.EscapedJson(profile.getObfs_param()),
                ConfigUtils.EscapedJson(profile.getProtocol_param())
                );
        Utils.printToFile(conf, new File(getApplicationInfo().dataDir + "/ss-local-vpn.conf"));

        List<String> cmd = new ArrayList<>();
        cmd.add(getApplicationInfo().dataDir + "/ss-local");
        cmd.add("-V");
        cmd.add("-x");
        cmd.add("-b");
        cmd.add("127.0.0.1");
        cmd.add("-t");
        cmd.add("600");
        cmd.add("--host");
        cmd.add(host_arg);
        cmd.add("-P");
        cmd.add(getApplicationInfo().dataDir);
        cmd.add("-c");
        cmd.add(getApplicationInfo().dataDir + "/ss-local-vpn.conf");

        if(profile.isUdpdns())
            cmd.add("-u");
        if(!profile.getRoute().equals(Route.ALL))
        {
            cmd.add("--acl");
            cmd.add(getApplicationInfo().dataDir + '/' + profile.getRoute() + ".acl");
        }

        if(TcpFastOpen.sendEnable())
            cmd.add("--fast-open");

        if(proxychains_enable)
        {
            cmd.add("LD_PRELOAD=" + getApplicationInfo().dataDir + "/lib/libproxychains4.so");
            cmd.add("PROXYCHAINS_CONF_FILE=" + getApplicationInfo().dataDir + "/proxychains.conf");
            cmd.add("PROXYCHAINS_PROTECT_FD_PREFIX=" + getApplicationInfo().dataDir);
            cmd.add("env");
        }

        sslocalProcess = new GuardProcess(cmd).start(null);
    }

    private void startShadowsocksUDPDaemon()
    {
        String conf = String.format(ConfigUtils.SHADOWSOCKS,
                profile.getHost(),
                profile.getRemotePort(),
                profile.getLocalPort(),
                ConfigUtils.EscapedJson(profile.getPassword()),
                profile.getMethod(),
                600,
                profile.getProtocol(),
                profile.getObfs(),
                ConfigUtils.EscapedJson(profile.getObfs_param()),
                ConfigUtils.EscapedJson(profile.getProtocol_param())
                );

        Utils.printToFile(conf, new File(getApplicationInfo().dataDir + "/ss-local-udp-vpn.conf"));

        List<String> cmd = new ArrayList<>();
        cmd.add(getApplicationInfo().dataDir + "/ss-local");
        cmd.add("-V");
        cmd.add("-U");
        cmd.add("-b");
        cmd.add("127.0.0.1");
        cmd.add("-t");
        cmd.add("600");
        cmd.add("--host");
        cmd.add(host_arg);
        cmd.add("-P");
        cmd.add(getApplicationInfo().dataDir);
        cmd.add("-c");
        cmd.add(getApplicationInfo().dataDir + "/ss-local-udp-vpn.conf");

        if(proxychains_enable)
        {
            cmd.add("LD_PRELOAD=" + getApplicationInfo().dataDir + "/lib/libproxychains4.so");
            cmd.add("PROXYCHAINS_CONF_FILE=" + getApplicationInfo().dataDir + "/proxychains.conf");
            cmd.add("PROXYCHAINS_PROTECT_FD_PREFIX=" + getApplicationInfo().dataDir);
            cmd.add("env");
        }

        sstunnelProcess = new GuardProcess(cmd).start(null);
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
