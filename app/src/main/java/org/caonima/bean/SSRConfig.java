package org.caonima.bean;

import com.github.shadowsocks.data.Profile;

import org.caonima.utils.IpUtil;

/**
 * 配置转化
 */
public class SSRConfig {

    public String method;

    public String protocol;

    public long server;

    public String password;

    public int server_port;

    public String obfs;


    /**
     * 获取profile
     * @return
     */
    public Profile getProfile()
    {
        Profile profile = new Profile();
        profile.setMethod(method);
        profile.setProtocol(protocol);
        profile.setHost(IpUtil.longToIP(server));
        profile.setPassword(password);
        profile.setRemotePort(server_port);
        profile.setObfs(obfs);
        return profile;
    }
}
