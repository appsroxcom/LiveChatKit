package com.appsrox.livechat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import live.chatkit.android.UserRepository;
import live.chatkit.android.model.UserVO;

public class CommonUtil {

    private static final String TAG = "CommonUtil";

    public static final long REFRESH_INTERVAL = 1000*30;//30s
    public static final int CHATS_LIMIT = 50;
    public static final int MESSAGES_LIMIT = 10;

    public static String getName(Context context) {
        return String.format("%s API %d %s - v%s %s", getNickname(), Build.VERSION.SDK_INT, Locale.getDefault(), BuildConfig.VERSION_NAME, context.getString(R.string.app_name));
    }

    public static String getBio(Context context) {
        return String.format("Since %s; %s", getSince(context), join(Build.MODEL, Build.CPU_ABI, Build.DEVICE));
    }

    public static String getPhoto() {
        return "https://ui-avatars.com/api/?rounded=true&name="+Uri.encode(getNickname());
    }

    public static String getNickname() {
        return any(Build.BRAND, Build.MANUFACTURER);
    }

    public static String getSince(Context context) {
        String installDate = null;
        try {
            long installed = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;//lastUpdateTime
            installDate = DateFormat.getDateInstance(DateFormat.DEFAULT).format(new Date(installed));
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, e);
        }
        return installDate;
    }

    public static void saveProfile(Context context, String currentUserId) {
        UserVO user = new UserVO();
        user.id = currentUserId;
        user.photo = getPhoto();
        user.name = getName(context);
        user.bio = getBio(context);

        UserRepository.getInstance().updateProfile(user, null);
    }

    public static String join(String... strings) {
        if (strings == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String str : strings) {
            if (!TextUtils.isEmpty(str)) sb.append(str).append(" ");
        }
        return sb.toString().trim();
    }

    public static String any(String... strings) {
        if (strings == null) return "";
        for (String str : strings) {
            if (!TextUtils.isEmpty(str)) return str;
        }
        return "";
    }
}
