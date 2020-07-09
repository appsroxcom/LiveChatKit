package live.chatkit.android.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.stfalcon.chatkit.sample.common.data.model.Message;

import static live.chatkit.android.Constants.*;

@IgnoreExtraProperties
public class AttachmentVO extends BaseVO {

    @PropertyName(URL) public String url;
    @PropertyName(TYPE) public String type;
    @PropertyName(NAME) public String name;
    @PropertyName(SIZE) public long size;

    public AttachmentVO() {}

    public AttachmentVO(String url, String type, String name, long size) {
        this.url = url;
        this.type = type;
        this.name = name;
        this.size = size;
    }

    public AttachmentVO(@NonNull Message.Image image) {
        this.url = image.getUrl();
        this.type = IMAGE_TYPE;
        //this.name = name;
    }

    public AttachmentVO(@NonNull Message.Voice voice) {
        this.url = voice.getUrl();
        this.type = AUDIO_TYPE;
        //this.name = name;
    }

    public AttachmentVO(@NonNull Message.File file) {
        this.url = file.getUrl();
        this.type = file.getType();
        //this.name = name;
        this.size = file.getSize();
    }

    public static Message.Image toImage(AttachmentVO attachment) {
        if (attachment == null) return null;
        return new Message.Image(attachment.url);
    }

    public static Message.Voice toVoice(AttachmentVO attachment) {
        if (attachment == null) return null;
        return new Message.Voice(attachment.url, 0);
    }

    public static Message.File toFile(AttachmentVO attachment) {
        if (attachment == null) return null;
        return new Message.File(attachment.url, attachment.type, attachment.size);
    }
}
