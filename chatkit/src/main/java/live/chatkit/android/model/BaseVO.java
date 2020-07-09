package live.chatkit.android.model;

import android.text.TextUtils;

import live.chatkit.android.ChatKit;
import live.chatkit.android.crypto.AESCrypt;

public abstract class BaseVO {

    protected static String encrypt(String str, String chatKey) {
        if (!ChatKit.isEncryption()) return str;
        try {
            if (!TextUtils.isEmpty(chatKey) && !TextUtils.isEmpty(str))
                return AESCrypt.encrypt(chatKey, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    protected static String decrypt(String str, String chatKey) {
        if (!ChatKit.isEncryption()) return str;
        try {
            if (!TextUtils.isEmpty(chatKey) && !TextUtils.isEmpty(str))
                return AESCrypt.decrypt(chatKey, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
