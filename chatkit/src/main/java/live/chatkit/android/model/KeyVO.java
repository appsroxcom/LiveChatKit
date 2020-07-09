package live.chatkit.android.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import static live.chatkit.android.Constants.CHAT_KEY;

@IgnoreExtraProperties
public class KeyVO extends BaseVO {

    @PropertyName(CHAT_KEY) public String key;

    public KeyVO() {}

    public KeyVO(String key) {
        this.key = key;
    }
}
