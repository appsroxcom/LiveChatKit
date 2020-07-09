package live.chatkit.android.model;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import live.chatkit.android.R;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import com.stfalcon.chatkit.sample.common.data.model.Dialog;
import com.stfalcon.chatkit.sample.common.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static live.chatkit.android.Constants.*;

@IgnoreExtraProperties
public class ChatVO extends BaseVO {

    @Exclude
    @PropertyName(ID) public String id;
    @PropertyName(TITLE) public String title;
    @PropertyName(PARTICIPANTS) public List<String> participants;
    @PropertyName(LAST_MESSAGE) public MessageVO lastMessage;
    @ServerTimestamp
    @PropertyName(UPDATED_AT) public Date updatedAt;
    @PropertyName(PHOTO) public String photo;
    @PropertyName(COUNT) public int count;

    public ChatVO() {}

    public ChatVO(String id) {
        this.id = id;
    }

    public ChatVO(@NonNull Dialog dialog, String chatKey) {
        this.id = dialog.getId();
        this.title = dialog.getDialogName();
        if (dialog.getUsers() != null) {
            this.participants = new ArrayList<>();
            for (User user : dialog.getUsers()) {
                participants.add(user.getId());
            }
        }
        this.lastMessage = dialog.getLastMessage() != null ? new MessageVO(dialog.getLastMessage(), chatKey) : null;
        this.updatedAt = dialog.getLastMessage() != null ? dialog.getLastMessage().getCreatedAt() : null;
        //this.unreadCount = dialog.getUnreadCount();
    }

    public static Dialog toDialog(Context context, ChatVO chat, Map<String, UserVO> userMap, Map<String, Integer> unreadCountMap, String chatKey) {
        if (chat == null) return null;
        ArrayList<User> users = null;
        if (chat.participants != null) {
            users = new ArrayList<>();
            for (String userId : chat.participants) {
                users.add(userMap != null && userMap.containsKey(userId) ? UserVO.toUser(userMap.get(userId)) : new User(userId, null, null, false));
            }
        }
        return new Dialog(chat.id, TextUtils.isEmpty(chat.title) ? context.getString(R.string.guest) : chat.title, chat.photo, users, MessageVO.toMessage(chat.lastMessage, userMap, chatKey), unreadCountMap != null && unreadCountMap.containsKey(chat.id) ? unreadCountMap.get(chat.id) : 0);
    }

    public static List<Dialog> toDialogList(Context context, List<ChatVO> chatList, Map<String, UserVO> userMap, Map<String, Integer> unreadCountMap, Map<String, String> chatKeyMap) {
        List<Dialog> lst = new ArrayList<>();
        if (chatList != null) {
            for (ChatVO chat : chatList) {
                lst.add(toDialog(context, chat, userMap, unreadCountMap, chatKeyMap.get(chat.id)));
            }
        }
        return lst;
    }
}
