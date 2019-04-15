package com.github.shadowsocks;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShadowsocksVpnThread extends Thread{

    private static final String TAG = ShadowsocksVpnThread.class.getSimpleName();

    private Method getInt;

    public static String PATH;

    private boolean isRunning = true;
    private LocalServerSocket serverSocket;

    private ShadowsocksVpnService vpnService;

    public ShadowsocksVpnThread(ShadowsocksVpnService vpnService)
    {
        this.vpnService = vpnService;
        PATH = vpnService.getApplicationInfo().dataDir+"/protect_path";
        try {
            getInt = FileDescriptor.class.getDeclaredMethod("getInt$");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket()
    {
        if(serverSocket != null)
        {
            try {
                serverSocket.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            serverSocket = null;
        }
    }

    public void stopThread()
    {
        isRunning = false;
        closeServerSocket();
    }

    @Override
    public void run() {
        new File(PATH).delete();

        try
        {
            LocalSocket localSocket = new LocalSocket();
            localSocket.bind(new LocalSocketAddress(PATH, LocalSocketAddress.Namespace.ABSTRACT));
            serverSocket = new LocalServerSocket(localSocket.getFileDescriptor());
        }catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, "unable to bind", e);
            return;
        }

        ExecutorService pool = Executors.newFixedThreadPool(4);
        while (isRunning)
        {
            try{
                final LocalSocket socket = serverSocket.accept();

                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream inputStream = socket.getInputStream();
                            OutputStream outputStream = socket.getOutputStream();

                            inputStream.read();
                            FileDescriptor[] fds = socket.getAncillaryFileDescriptors();

                            if(fds != null && fds.length != 0)
                            {
                                int fd = (int)getInt.invoke(fds[0]);
                                boolean ret = vpnService.protect(fd);
                                System.jniclose(fd);
                                if(ret)
                                    outputStream.write(0);
                                else
                                    outputStream.write(1);

                                inputStream.close();
                                outputStream.close();
                            }
                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            Log.e(TAG, "Error when protect socket", e);
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            Log.e(TAG, "Error when protect socket", e);
                            e.printStackTrace();
                        }
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
