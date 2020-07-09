package live.chatkit.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import live.chatkit.android.crypto.CryptoUtil;
import live.chatkit.android.model.ChatVO;
import live.chatkit.android.model.UserVO;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static live.chatkit.android.Constants.CHATKIT_PREFS;
import static live.chatkit.android.Constants.PRIVATE_KEY;
import static live.chatkit.android.Constants.STORAGE_DIR;

public final class ChatUtil {

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(CHATKIT_PREFS, Context.MODE_PRIVATE);
    }

    public static String getPrivateKey(Context context) {
        return getPrefs(context).getString(PRIVATE_KEY, null);
    }

    public static void setPrivateKey(Context context, String privateKey) {
        getPrefs(context).edit().putString(PRIVATE_KEY, privateKey).apply();
    }

    public static String getStorageDir(Context context) {
        String folder = getPrefs(context).getString(STORAGE_DIR, null);
        if (folder == null) {
            folder = UUID.randomUUID().toString();
            getPrefs(context).edit().putString(STORAGE_DIR, folder).apply();
        }
        return folder;
    }

    public static void populateChatDetails(List<ChatVO> chats, Map<String, UserVO> userMap, String currentUserId) {
        for (ChatVO chat : chats) {
            populateChatDetails(chat, userMap, currentUserId);
        }
    }

    public static void populateChatDetails(ChatVO chat, Map<String, UserVO> userMap, String currentUserId) {
        for (String userId : chat.participants) {
            if (!userMap.containsKey(userId)) continue;

            if (!currentUserId.equals(userId)) {
                if (TextUtils.isEmpty(chat.title)) chat.title = userMap.get(userId).name;
                if (TextUtils.isEmpty(chat.photo)) chat.photo = userMap.get(userId).photo;
                break;
            }
        }
    }

    public static String getParticipantId(ChatVO chat, String currentUserId) {
        for (String userId : chat.participants) {
            if (!currentUserId.equals(userId)) return userId;
        }
        return null;
    }

    public static String decryptKey(Context context, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
        if (TextUtils.isEmpty(key)) return null;
        return CryptoUtil.decrypt(key, ChatUtil.getPrivateKey(context));
    }
}
