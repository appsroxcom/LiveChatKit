package live.chatkit.android;

import androidx.annotation.NonNull;

public abstract class ResultListener {

    public void onFailure(@NonNull Exception e) {
    }

    public void onSuccess(Object result) {
    }
}
