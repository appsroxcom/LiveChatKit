package live.chatkit.android;

import android.util.Log;

import androidx.annotation.NonNull;

import live.chatkit.android.model.UserVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static live.chatkit.android.Constants.BIO;
import static live.chatkit.android.Constants.FCM_TOKEN;
import static live.chatkit.android.Constants.ID;
import static live.chatkit.android.Constants.NAME;
import static live.chatkit.android.Constants.OFFLINE;
import static live.chatkit.android.Constants.ONLINE;
import static live.chatkit.android.Constants.PHOTO;
import static live.chatkit.android.Constants.STATUS;
import static live.chatkit.android.Constants.USERS;

/*
 * users
 *	{userId}
 */
public class UserRepository extends BaseRepository {

    private static final String TAG = "UserRepository";

    private final Map<String, UserVO> userMap;

    private static class Lazy {
        private static final UserRepository INSTANCE = new UserRepository();
    }
    public static UserRepository getInstance() {
        return Lazy.INSTANCE;
    }

    private UserRepository() {
        userMap = new HashMap<>();
    }

    @Override
    public void clear() {
        userMap.clear();
    }

    private void updateUserMap(UserVO user) {
        userMap.put(user.id, user);
    }

    /**
     * Get user map
     *
     * @return cached
     */
    public Map<String, UserVO> getUserMap() {
        return userMap;
    }

    /**
     * Get user list in result
     *
     * @param isCached
     * @param userId excluded
     * @param limit
     * @param listener
     */
    public void fetchUsers(boolean isCached, final String userId, int limit, final ResultListener listener) {
        db.collection(USERS).orderBy(STATUS, Query.Direction.DESCENDING).limit(limit)
            .get(isCached ? Source.CACHE : Source.DEFAULT)
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<UserVO> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                        UserVO user = document.toObject(UserVO.class);
                        user.id = document.getId();
                        if (!userId.equals(user.id)) users.add(user);
                        updateUserMap(user);
                    }

                    if (listener != null) listener.onSuccess(users);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                    if (listener != null) listener.onFailure(task.getException());
                }
                }
            });
    }

    /**
     * Get user list in result
     *
     * @param isCached
     * @param userIdList included
     * @param listener
     */
    public void fetchUsers(boolean isCached, final List<String> userIdList, final ResultListener listener) {
        db.collection(USERS).whereIn(ID, userIdList)
                .get(isCached ? Source.CACHE : Source.DEFAULT)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<UserVO> users = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d(TAG, document.getId() + " => " + document.getData());
                                UserVO user = document.toObject(UserVO.class);
                                user.id = document.getId();
                                users.add(user);
                                updateUserMap(user);
                            }

                            if (listener != null) listener.onSuccess(users);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            if (listener != null) listener.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Add or update user
     *
     * @param user
     * @param listener
     */
    public void addUser(final UserVO user, final ResultListener listener) {
        db.collection(USERS).document(user.id)
            .set(user, SetOptions.merge())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    updateUserMap(user);
                    if (listener != null) listener.onSuccess(null);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error adding document", e);
                    if (listener != null) listener.onFailure(e);
                }
            });
    }

    /**
     * Get user in result
     *
     * @param isCached
     * @param userId
     * @param listener
     */
    public void fetchUser(boolean isCached, final String userId, final ResultListener listener) {
        db.collection(USERS).document(userId)
                .get(isCached ? Source.CACHE : Source.DEFAULT)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                UserVO user = document.toObject(UserVO.class);
                                user.id = document.getId();
                                updateUserMap(user);
                                if (listener != null) listener.onSuccess(user);
                            } else {
                                Log.d(TAG, "No such user: "+userId);
                                if (listener != null) listener.onSuccess(null);
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                            if (listener != null) listener.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Update status
     *
     * @param userId
     * @param status
     * @param listener
     */
    public void updateStatus(String userId, int status, final ResultListener listener) {
        db.collection(USERS).document(userId)
                .update(STATUS, status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (listener != null) listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        if (listener != null) listener.onFailure(e);
                    }
                });
    }

    /**
     * Update profile
     *
     * @param user
     * @param listener
     */
    public void updateProfile(UserVO user, final ResultListener listener) {
        db.collection(USERS).document(user.id)
                .update(NAME, user.name, PHOTO, user.photo, BIO, user.bio)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (listener != null) listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        if (listener != null) listener.onFailure(e);
                    }
                });
    }

    /**
     * Update FCM token
     *
     * @param userId
     * @param fcmToken
     * @param listener
     */
    public void updateFcmToken(String userId, String fcmToken, final ResultListener listener) {
        db.collection(USERS).document(userId)
                .update(FCM_TOKEN, fcmToken)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (listener != null) listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        if (listener != null) listener.onFailure(e);
                    }
                });
    }

    /**
     * Delete user
     *
     * @param userId
     * @param listener
     */
    public void deleteUser(final String userId, final ResultListener listener) {
        db.collection(USERS).document(userId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (listener != null) listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                        if (listener != null) listener.onFailure(e);
                    }
                });
    }

}
