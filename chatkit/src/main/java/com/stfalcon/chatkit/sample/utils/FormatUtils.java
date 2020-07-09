package com.stfalcon.chatkit.sample.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/*
 * Created by troy379 on 06.04.17.
 */
public final class FormatUtils {
    private FormatUtils() {
        throw new AssertionError();
    }

    public static String getDurationString(int seconds) {
        Date date = new Date(seconds * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat(seconds >= 3600 ? "HH:mm:ss" : "mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }

    public static String getSizeString(long bytes) {
        long n = 1000;
        String str = "";
        double kb = bytes / n;
        double mb = kb / n;
        double gb = mb / n;
        double tb = gb / n;
        if(bytes < n) {
            str = bytes + " B";
        } else if(bytes >= n && bytes < (n * n)) {
            str =  String.format("%.2f", kb) + " KB";
        } else if(bytes >= (n * n) && bytes < (n * n * n)) {
            str = String.format("%.2f", mb) + " MB";
        } else if(bytes >= (n * n * n) && bytes < (n * n * n * n)) {
            str = String.format("%.2f", gb) + " GB";
        } else if(bytes >= (n * n * n * n)) {
            str = String.format("%.2f", tb) + " TB";
        }
        return str;
    }
}
