package com.github.shadowsocks.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

public class Profile implements Parcelable {

    private String name = "Untitled";

    private String host = "";

    private int localPort = 1080;

    private int remotePort = 8388;

    private String password = "";

    private String protocol = "origin";

    private String protocol_param = "";

    private String obfs = "plain";

    private String obfs_param = "";

    private String method = "aes-256-cfb";

    private String route = "all";

    private boolean proxyApps = false;

    private boolean bypass = false;

    private boolean udpdns = false;

    private String url_group = "";

    private String dns = "208.67.222.222:53";

    private String china_dns = "114.114.114.114:53,223.5.5.5:53";

    private boolean ipv6 = false;

    private String individual = "";

    private long tx = 0;

    private long rx = 0;

    private long elapsed = 0;

    private long userOrder;

    protected Profile(Parcel in) {
        name = in.readString();
        host = in.readString();
        localPort = in.readInt();
        remotePort = in.readInt();
        password = in.readString();
        protocol = in.readString();
        protocol_param = in.readString();
        obfs = in.readString();
        obfs_param = in.readString();
        method = in.readString();
        route = in.readString();
        proxyApps = in.readByte() != 0;
        bypass = in.readByte() != 0;
        udpdns = in.readByte() != 0;
        url_group = in.readString();
        dns = in.readString();
        china_dns = in.readString();
        ipv6 = in.readByte() != 0;
        individual = in.readString();
        tx = in.readLong();
        rx = in.readLong();
        elapsed = in.readLong();
        userOrder = in.readLong();
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol_param() {
        return protocol_param;
    }

    public void setProtocol_param(String protocol_param) {
        this.protocol_param = protocol_param;
    }

    public String getObfs() {
        return obfs;
    }

    public void setObfs(String obfs) {
        this.obfs = obfs;
    }

    public String getObfs_param() {
        return obfs_param;
    }

    public void setObfs_param(String obfs_param) {
        this.obfs_param = obfs_param;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public boolean isProxyApps() {
        return proxyApps;
    }

    public void setProxyApps(boolean proxyApps) {
        this.proxyApps = proxyApps;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(boolean bypass) {
        this.bypass = bypass;
    }

    public boolean isUdpdns() {
        return udpdns;
    }

    public void setUdpdns(boolean udpdns) {
        this.udpdns = udpdns;
    }

    public String getUrl_group() {
        return url_group;
    }

    public void setUrl_group(String url_group) {
        this.url_group = url_group;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns;
    }

    public String getChina_dns() {
        return china_dns;
    }

    public void setChina_dns(String china_dns) {
        this.china_dns = china_dns;
    }

    public boolean isIpv6() {
        return ipv6;
    }

    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    public String getIndividual() {
        return individual;
    }

    public void setIndividual(String individual) {
        this.individual = individual;
    }

    public long getTx() {
        return tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

    public long getRx() {
        return rx;
    }

    public void setRx(long rx) {
        this.rx = rx;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public long getUserOrder() {
        return userOrder;
    }

    public void setUserOrder(long userOrder) {
        this.userOrder = userOrder;
    }

    @Override
    public String toString() {
        String en_password = Base64.encodeToString(password.getBytes(),Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
        String en_obfs_param = Base64.encodeToString(obfs_param.getBytes(),Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
        String en_protocol_param = Base64.encodeToString(protocol_param.getBytes(),Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
        String en_name = Base64.encodeToString(name.getBytes(),Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
        String en_url_group = Base64.encodeToString(url_group.getBytes(),Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
        String ssr = "%s:%d:%s:%s:%s:%s/?obfsparam=%s&protoparam=%s&remarks=%s&group=%s";
        ssr = String.format(ssr,host,remotePort,protocol,method,obfs,en_password,en_obfs_param,en_protocol_param,en_name,en_url_group);
        return "ssr://"+Base64.encodeToString(ssr.getBytes(),Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
    }

    public boolean isMethodUnSafe()
    {
        return "table".equalsIgnoreCase(method) || "rc4".equalsIgnoreCase(method);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(host);
        dest.writeInt(localPort);
        dest.writeInt(remotePort);
        dest.writeString(password);
        dest.writeString(protocol);
        dest.writeString(protocol_param);
        dest.writeString(obfs);
        dest.writeString(obfs_param);
        dest.writeString(method);
        dest.writeString(route);
        dest.writeByte((byte) (proxyApps ? 1 : 0));
        dest.writeByte((byte) (bypass ? 1 : 0));
        dest.writeByte((byte) (udpdns ? 1 : 0));
        dest.writeString(url_group);
        dest.writeString(dns);
        dest.writeString(china_dns);
        dest.writeByte((byte) (ipv6 ? 1 : 0));
        dest.writeString(individual);
        dest.writeLong(tx);
        dest.writeLong(rx);
        dest.writeLong(elapsed);
        dest.writeLong(userOrder);
    }
}
