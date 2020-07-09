package live.chatkit.android;

import com.google.firebase.firestore.FirebaseFirestore;

public abstract class BaseRepository {

    protected FirebaseFirestore db;

    public BaseRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public abstract void clear();
}
