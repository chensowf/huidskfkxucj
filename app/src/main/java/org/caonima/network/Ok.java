package org.caonima.network;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class Ok{

    private static OkHttpClient okHttpClient = initOkHttpClient();

    private static OkHttpClient initOkHttpClient()
    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
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

    public static void execute(String url, String host, Callback callback)
    {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Host",host)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
