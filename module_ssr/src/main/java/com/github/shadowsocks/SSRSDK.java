package com.github.shadowsocks;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.github.shadowsocks.utils.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import eu.chainfire.libsuperuser.Shell;

public class SSRSDK {

    private static ArrayList<String> EXECUTABLES = new ArrayList<>();

    public static void init(Context context)
    {
        EXECUTABLES.add("redsocks");
        EXECUTABLES.add("pdnsd");
        EXECUTABLES.add("ss-local");
    //    EXECUTABLES.add("ss-tunnel");
        EXECUTABLES.add("tun2socks");
  //      EXECUTABLES.add("kcptun");

        copyAssets(context);
    }

    private static void crashRecovery(Context context)
    {
        ArrayList<String> cmd = new ArrayList<>();
        String[] tasks = {"ss-local", "ss-tunnel", "pdnsd", "redsocks", "tun2socks", "proxychains"};
        for (int i = 0; i < tasks.length; i++) {
            cmd.add(String.format(Locale.ENGLISH, "killall %s", tasks[i]));
            cmd.add(String.format(Locale.ENGLISH, "rm -f %1$s/%2$s-nat.conf %1$s/%2$s-vpn.conf",
                    context.getApplicationInfo().dataDir, tasks[i]));
        }
        Shell.SH.run(cmd);
    }


    private static void copyAssets(Context context)
    {
        crashRecovery(context);
        Log.e("error",System.getABI());
        copyAssets(System.getABI(),context);
        copyAssets("acl",context);
        ArrayList<String> cmd = new ArrayList<>();
        for (int i = 0; i < EXECUTABLES.size(); i++) {
            String temp = "chmod 777 " + context.getApplicationInfo().dataDir + "/" + EXECUTABLES.get(i);
            cmd.add(temp);
        }
        Shell.SH.run(cmd);
        cmd.clear();
        EXECUTABLES.clear();
    }


    private static void copyAssets(String path, Context context)
    {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(path);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        if(files != null)
        {
            for (String s : files) {
                InputStream in;
                OutputStream out;
                try {
                    in = assetManager.open(!TextUtils.isEmpty(path) ? path + "/" + s : s);
                    out = new FileOutputStream(context.getApplicationInfo().dataDir + '/' + s);
                    IOUtils.copy(in, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
