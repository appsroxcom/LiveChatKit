package live.chatkit.android.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import live.chatkit.android.crypto.AESCrypt;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.common.data.model.User;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static live.chatkit.android.Constants.*;

@IgnoreExtraProperties
public class MessageVO extends BaseVO {

    @Exclude
    @PropertyName(ID) public String id;
    @PropertyName(TEXT) public String text;
    @PropertyName(ATTACHMENT) public AttachmentVO attachment;
    @ServerTimestamp
    @PropertyName(CREATED_AT) public Date createdAt;
    @PropertyName(SENDER_ID) public String senderId;

    public MessageVO() {
        //createdAt = new Date();
    }

    public MessageVO(String text) {
        this();
        this.text = text;
    }

    public MessageVO(@NonNull Message message, String chatKey) {
        this.id = message.getId();
        this.text = encrypt(message.getText(), chatKey);
        this.attachment = message.getImageUrl() != null ? new AttachmentVO(new Message.Image(encrypt(message.getImageUrl(), chatKey))) :
                message.getVoice() != null ? new AttachmentVO(new Message.Voice(encrypt(message.getVoice().getUrl(), chatKey), message.getVoice().getDuration())) :
                message.getFile() != null ? new AttachmentVO(new Message.File(encrypt(message.getFile().getUrl(), chatKey), message.getFile().getType(), message.getFile().getSize())) :
                        null;
        this.createdAt = message.getCreatedAt();
        this.senderId = message.getUser() != null ? message.getUser().getId() : null;
    }

    public static Message toMessage(MessageVO message, Map<String, UserVO> userMap, String chatKey) {
        if (message == null) return null;
        Message obj = new Message(message.id,
                userMap != null && userMap.containsKey(message.senderId) ? UserVO.toUser(userMap.get(message.senderId)) : new User(message.senderId, null, null, false),
                decrypt(message.text, chatKey),
                message.createdAt);
        if (message.attachment != null && !TextUtils.isEmpty(message.attachment.type)) {
            if (message.attachment.type.startsWith("image")) {
                obj.setImage(new Message.Image(decrypt(message.attachment.url, chatKey)));
            } else if (message.attachment.type.startsWith("audio")) {
                obj.setVoice(new Message.Voice(decrypt(message.attachment.url, chatKey), 0));
            } else {//file
                obj.setFile(new Message.File(decrypt(message.attachment.url, chatKey), message.attachment.type, message.attachment.size));
            }
        }
        return obj;
    }

    public static List<Message> toMessageList(List<MessageVO> messageList, Map<String, UserVO> userMap, String chatKey) {
        List<Message> lst = new ArrayList<>();
        if (messageList != null) {
            for (MessageVO message : messageList) {
                lst.add(toMessage(message, userMap, chatKey));
            }
        }
        return lst;
    }
}
