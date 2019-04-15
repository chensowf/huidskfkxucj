package com.github.shadowsocks.utils;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrafficMonitorThread extends Thread {

    private static final String TAG = TrafficMonitorThread.class.getSimpleName();
    private String PATH;

    private volatile LocalServerSocket serverSocket;
    private volatile boolean isRunning;

    public TrafficMonitorThread(Context context)
    {
        PATH = context.getApplicationInfo().dataDir+"/stat_path";
        isRunning = true;
    }

    public void stopThread()
    {
        isRunning = false;
    }

    public void closeServerSocket()
    {
        if(serverSocket != null)
        {
            try{
                serverSocket.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }
            serverSocket = null;
        }
    }

    @Override
    public void run() {
        new File(PATH).delete();

        try{
            LocalSocket localSocket = new LocalSocket();
            localSocket.bind(new LocalSocketAddress(PATH, LocalSocketAddress.Namespace.ABSTRACT));
            serverSocket = new LocalServerSocket(localSocket.getFileDescriptor());
        }catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG,"unbind to bind",e);
            return;
        }

        ExecutorService pool = Executors.newFixedThreadPool(1);

        while (isRunning)
        {
            try
            {
                final LocalSocket socket = serverSocket.accept();

                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream inputStream = socket.getInputStream();
                            OutputStream outputStream = socket.getOutputStream();
                            byte[] buffer = new byte[16];
                            if(inputStream.read(buffer) != 16) throw new IOException("Unexpected traffic stat length");
                            ByteBuffer stat = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
                            TrafficMonitor.update(stat.getLong(0), stat.get(8));

                            outputStream.write(0);
                            inputStream.close();
                            outputStream.close();
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                            Log.e(TAG, "Error when recv traffic stat", e);
                        }

                        try{
                            socket.close();
                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });

            }catch (IOException e)
            {
                e.printStackTrace();
                Log.e(TAG, "Error when accept socket", e);
            }
        }
    }
}
