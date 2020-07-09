package live.chatkit.android;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private static final String TAG = "AuthRepository";

    private FirebaseAuth firebaseAuth;

    private static class Lazy {
        private static final AuthRepository INSTANCE = new AuthRepository();
    }
    public static AuthRepository getInstance() {
        return Lazy.INSTANCE;
    }

    private AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Get current user id
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

    /**
     * Is current user anonymous
     */
    public boolean isAnonymous() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return (user != null) ? user.isAnonymous() : false;
    }

    /**
     * Login
     *
     * @param listener
     * @param args email, password
     */
    public void login(final ResultListener listener, String... args) {
        Task<AuthResult> signInTask = (args != null && args.length == 2) ? firebaseAuth.signInWithEmailAndPassword(args[0], args[1]) : firebaseAuth.signInAnonymously();
        signInTask
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d(TAG, "OnComplete : " + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Failed login : ", task.getException());
                            if (listener != null) listener.onFailure(task.getException());
                        } else {
                            if (listener != null) listener.onSuccess(task.getResult().getUser().getUid());
                        }
                    }
                });
    }

    /**
     * Logout
     */
    public void logout() {
        firebaseAuth.signOut();
    }
}
