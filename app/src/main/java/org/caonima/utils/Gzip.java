package org.caonima.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class Gzip {

    public static String decompress(byte[] data)
    {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
