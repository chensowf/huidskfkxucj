package org.caonima.network;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class Ok{

    public static final String HTTP = "https://43.255.107.130:8443";
    public static final String HOST = "hc.apache.org";

    private static OkHttpClient okHttpClient = initOkHttpClient();

    private static OkHttpClient initOkHttpClient()
    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.hostnameVerifier((hostname, session) -> true);
        try {
            SSLContext sslContext;
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null,new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }},null);
            builder.sslSocketFactory(sslContext.getSocketFactory());
        }catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static void execute(String url,
                               String api,
                               int method,
                               HashMap<String,String> heads,
                               HashMap<String,String> pareams,
                               Callback callback)
    {
        Request.Builder builder = new Request.Builder();
        addHead(heads,builder);
        addMethod(method,pareams,builder);
        builder.url(url+api)
                .get();
        okHttpClient.newCall(builder.build()).enqueue(callback);
    }

    private static Request.Builder addHead(HashMap<String,String> heads, Request.Builder builder)
    {
        Set set = heads.keySet();
        Iterator iterable = set.iterator();
        while (iterable.hasNext())
        {
            String name = (String) iterable.next();
            String value = heads.get(name);
            builder.addHeader(name,value);
        }
        return builder;
    }

    private static Request.Builder addMethod(int method, HashMap<String,String> pareams, Request.Builder builder)
    {
        switch (method)
        {
            case Method.GET:
                builder.get();
                break;
            case Method.POST:
                builder.post(buildRequestBody(pareams));
                break;
        }
        return builder;
    }

    private static RequestBody buildRequestBody(HashMap<String,String> pareams)
    {
        FormBody.Builder builder = new FormBody.Builder();
        Set set = pareams.keySet();
        Iterator iterable = set.iterator();
        while (iterable.hasNext())
        {
            String name = (String) iterable.next();
            String value = pareams.get(name);
            builder.add(name,value);
        }
        return builder.build();
    }
}
