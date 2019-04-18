package com.github.shadowsocks.constant;

public class ConfigUtils {

    public static String EscapedJson(String OriginString)
    {
        String ProcessString = OriginString.replaceAll("\\\\","\\\\\\\\")
                .replaceAll("\"","\\\\\"");
        return ProcessString;
    }

    public static final String SHADOWSOCKS =
            "{\"server\": \"%s\", \"server_port\": %d, \"local_port\": %d, \"password\": \"%s\", \"method\":\"%s\", \"timeout\": %d, \"protocol\": \"%s\", \"obfs\": \"%s\", \"obfs_param\": \"%s\", \"protocol_param\": \"%s\"}";

    public static final String REDSOCKS = "base {\n" +
            " log_debug = off;\n" +
            " log_info = off;\n" +
            " log = stderr;\n" +
            " daemon = off;\n" +
            " redirector = iptables;\n" +
            "}\n" +
            "redsocks {\n" +
            " local_ip = 127.0.0.1;\n" +
            " local_port = 8123;\n" +
            " ip = 127.0.0.1;\n" +
            " port = %d;\n" +
            " type = socks5;\n" +
            "}\n";

    public static final String PROXYCHAINS = "strict_chain\n" +
            "localnet 127.0.0.0/255.0.0.0\n" +
            "[ProxyList]\n" +
            "%s %s %s %s %s";

    public static final String PDNSD_LOCAL =
        "global {\n"+
            "perm_cache = 2048;\n"+
            "%s\n"+
            "cache_dir = \"%s\";\n"+
            "server_ip = %s;\n"+
            "server_port = %d;\n"+
            "query_method = tcp_only;\n"+
            "min_ttl = 15m;\n"+
            "max_ttl = 1w;\n"+
            "timeout = 10;\n"+
            "daemon = off;\n"+
        "}\n"+

        "server {\n"+
            "label = \"local\";\n"+
            "ip = 127.0.0.1;\n"+
            "port = %d;\n"+
            "reject = %s;\n"+
            "reject_policy = negate;\n"+
            "reject_recursively = on;\n"+
        "}\n"+

        "rr {\n"+
            "name=localhost;\n"+
            "reverse=on;\n"+
            "a=127.0.0.1;\n"+
            "owner=localhost;\n"+
            "soa=localhost,root.localhost,42,86400,900,86400,86400;\n"+
        "}";

    public static final String PDNSD_DIRECT =

        "global {\n"+
            "perm_cache = 2048;\n"+
            "%s \n"+
            "cache_dir = \"%s\";\n"+
            "server_ip = %s;\n"+
            "server_port = %d;\n"+
            "query_method = udp_only;\n"+
            "min_ttl = 15m;\n"+
            "max_ttl = 1w;\n"+
            "timeout = 10;\n"+
            "daemon = off;\n"+
            "par_queries = 4;\n"+
        "}\n"+

        "%s\n"+

        "server {\n"+
            "label = \"local-server\";\n"+
            "ip = 127.0.0.1;\n"+
            "query_method = tcp_only;\n"+
            "port = %d;\n"+
            "reject = %s;\n"+
            "reject_policy = negate;\n"+
            "reject_recursively = on;\n"+
        "}\n"+

        "rr {\n"+
            "name=localhost;\n"+
            "reverse=on;\n"+
            "a=127.0.0.1;\n"+
            "owner=localhost;\n"+
            "soa=localhost,root.localhost,42,86400,900,86400,86400;\n"+
        "}";

    public static final String REMOTE_SERVER =

        "server {\n"+
            "label = \"remote-servers\";\n"+
            "ip = %s;\n"+
            "port = %d;\n"+
            "timeout = 3;\n"+
            "query_method = udp_only;\n"+
            "%s\n"+
            "policy = included;\n"+
            "reject = %s;\n"+
            "reject_policy = fail;\n"+
            "reject_recursively = on;\n"+
        "}";

}
