package live.chatkit.android.model;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.stfalcon.chatkit.sample.common.data.model.User;

import java.util.ArrayList;
import java.util.List;

import static live.chatkit.android.Constants.*;

@IgnoreExtraProperties
public class UserVO extends BaseVO {

    @PropertyName(ID) public String id;
    @PropertyName(NAME) public String name;
    @PropertyName(PHOTO) public String photo;
    @PropertyName(STATUS) public int status;//0=offline, 1=online, -1=invisible
    @PropertyName(BIO) public String bio;
    @PropertyName(PUBLIC_KEY) public String publicKey;

    public UserVO() {}

    public UserVO(String id) {
        this.id = id;
    }

    public UserVO(@NonNull User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.photo = user.getAvatar();
        this.status = user.isOnline() ? ONLINE : status;
    }

    public static User toUser(UserVO user) {
        if (user == null) return null;
        return new User(user.id, user.name, user.photo, user.status == ONLINE);
    }

    public static List<User> toUserList(List<UserVO> userList) {
        List<User> lst = new ArrayList<>();
        if (userList != null) {
            for (UserVO user : userList) {
                lst.add(toUser(user));
            }
        }
        return lst;
    }
}
