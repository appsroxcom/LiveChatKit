package live.chatkit.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import live.chatkit.android.model.ChatVO;
import live.chatkit.android.model.KeyVO;
import live.chatkit.android.model.MessageVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static live.chatkit.android.Constants.CHATS;
import static live.chatkit.android.Constants.COUNT;
import static live.chatkit.android.Constants.CREATED_AT;
import static live.chatkit.android.Constants.KEYS;
import static live.chatkit.android.Constants.LAST_MESSAGE;
import static live.chatkit.android.Constants.MESSAGES;
import static live.chatkit.android.Constants.PARTICIPANTS;
import static live.chatkit.android.Constants.UPDATED_AT;

/*
 * chats
 * 	{chatId}
 * 	  messages
 * 		{messageId}
 */
public class ChatRepository extends BaseRepository {

    private static final String TAG = "ChatRepository";

    private final Map<String, Integer> countMap;
    private final Map<String, KeyVO> keyMap;

    private static class Lazy {
        private static final ChatRepository INSTANCE = new ChatRepository();
    }
    public static ChatRepository getInstance() {
        return Lazy.INSTANCE;
    }

    private ChatRepository() {
        countMap = new HashMap<>();
        keyMap = new HashMap<>();
    }

    @Override
    public void clear() {
        countMap.clear();
        keyMap.clear();
    }

    private ListenerRegistration mRegistration;

    private void updateCountMap(String chatId, int count) {
        countMap.put(chatId, count);
    }

    private void updateKeyMap(String chatId, KeyVO key) {
        keyMap.put(chatId, key);
    }

    /**
     * Get messages count map
     *
     * @return cached
     */
    public Map<String, Integer> getCountMap() {
        return countMap;
    }

    private int getMessageCount(String chatId) {
        return countMap.containsKey(chatId) ? countMap.get(chatId) : 0;
    }

    public int getUnreadCount(String chatId, Context context) {
        if (!countMap.containsKey(chatId)) return 0;
        SharedPreferences prefs = ChatUtil.getPrefs(context);
        return Math.max(0, getMessageCount(chatId) - prefs.getInt("count_map."+chatId, 0));
    }

    public void resetUnreadCount(String chatId, Context context) {
        if (!countMap.containsKey(chatId) || getUnreadCount(chatId, context) == 0) return;
        SharedPreferences prefs = ChatUtil.getPrefs(context);
        prefs.edit().putInt("count_map."+chatId, getMessageCount(chatId)).commit();
    }

    /**
     * Get key map
     *
     * @return cached
     */
    public Map<String, KeyVO> getKeyMap() {
        return keyMap;
    }

    /**
     * Get chat list in result
     *
     * @param isCached
     * @param userId
     * @param listener
     */
    public void fetchChats(boolean isCached, String userId, final ResultListener listener) {
        db.collection(CHATS).whereArrayContains(PARTICIPANTS, userId).orderBy(UPDATED_AT, Query.Direction.DESCENDING)
                .get(isCached ? Source.CACHE : Source.DEFAULT)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ChatVO> chats = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d(TAG, document.getId() + " => " + document.getData());
                                ChatVO chat = document.toObject(ChatVO.class);
                                chat.id = document.getId();
                                chats.add(chat);
                                updateCountMap(chat.id, chat.count);
                            }

                            if (listener != null) listener.onSuccess(chats);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            if (listener != null) listener.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Get chatId in result
     *
     * @param chat
     * @param listener
     */
    public void addChat(final ChatVO chat, final ResultListener listener) {
        db.collection(CHATS)
                .add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        chat.id = documentReference.getId();
                        if (listener != null) listener.onSuccess(chat.id);
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
     * Get chat in result
     *
     * @param isCached
     * @param chatId
     * @param listener
     */
    public void fetchChat(boolean isCached, final String chatId, final ResultListener listener) {
        db.collection(CHATS).document(chatId)
                .get(isCached ? Source.CACHE : Source.DEFAULT)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                ChatVO chat = document.toObject(ChatVO.class);
                                chat.id = document.getId();
                                if (listener != null) listener.onSuccess(chat);
                            } else {
                                Log.d(TAG, "No such chat: "+chatId);
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
     * Set key
     *
     * @param key
     * @param chatId
     * @param userId
     * @param listener
     */
    public void setKey(final KeyVO key, final String chatId, String userId, final ResultListener listener) {
        db.collection(CHATS).document(chatId).collection(KEYS).document(userId)
                .set(key)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateKeyMap(chatId, key);
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
     * Get key in result
     *
     * @param isCached
     * @param chatId
     * @param userId
     * @param listener
     */
    public void fetchKey(boolean isCached, final String chatId, final String userId, final ResultListener listener) {
        db.collection(CHATS).document(chatId).collection(KEYS).document(userId)
                .get(isCached ? Source.CACHE : Source.DEFAULT)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                KeyVO key = document.toObject(KeyVO.class);
                                updateKeyMap(chatId, key);
                                if (listener != null) listener.onSuccess(key);
                            } else {
                                Log.d(TAG, "No such key for: "+userId);
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
     * Get message list in result
     *
     * @param chatId
     * @param lastLoadedDate
     * @param limit
     * @param listener
     */
    public void fetchMessages(String chatId, Date lastLoadedDate, int limit, final ResultListener listener) {
        Query qry = db.collection(CHATS).document(chatId).collection(MESSAGES);
        if (lastLoadedDate != null) qry = qry.whereLessThan(CREATED_AT, lastLoadedDate);
        qry.orderBy(CREATED_AT, Query.Direction.DESCENDING).limit(limit)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<MessageVO> messages = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            //Log.d(TAG, document.getId() + " => " + document.getData());
                            MessageVO message = document.toObject(MessageVO.class);
                            message.id = document.getId();
                            messages.add(message);
                        }

                        if (listener != null) listener.onSuccess(messages);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        if (listener != null) listener.onFailure(task.getException());
                    }
                }
            });
    }

    /**
     * Get messageId in result
     *
     * @param chatId
     * @param message
     * @param listener
     */
    public void addMessage(final String chatId, final MessageVO message, final ResultListener listener) {
        db.collection(CHATS).document(chatId).collection(MESSAGES)
            .add(message)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    //Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    message.id = documentReference.getId();
                    updateLastMessage(chatId, message, null);
                    updateMessageCount(chatId, 1, null);
                    if (listener != null) listener.onSuccess(message.id);
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

    private void updateLastMessage(String chatId, MessageVO message, final ResultListener listener) {
        db.collection(CHATS).document(chatId)
                .update(LAST_MESSAGE, message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (listener != null) listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating last message", e);
                        if (listener != null) listener.onFailure(e);
                    }
                });
    }

    private void updateMessageCount(final String chatId, final int delta, final ResultListener listener) {
        db.collection(CHATS).document(chatId)
                .update(COUNT, FieldValue.increment(delta))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateCountMap(chatId, getMessageCount(chatId) + delta);
                        if (listener != null) listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating message count", e);
                        if (listener != null) listener.onFailure(e);
                    }
                });
    }

    /**
     * Get message in result
     *
     * @param isCached
     * @param chatId
     * @param messageId
     * @param listener
     */
    public void fetchMessage(boolean isCached, final String chatId, final String messageId, final ResultListener listener) {
        db.collection(CHATS).document(chatId).collection(MESSAGES).document(messageId)
                .get(isCached ? Source.CACHE : Source.DEFAULT)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                MessageVO message = document.toObject(MessageVO.class);
                                message.id = document.getId();
                                if (listener != null) listener.onSuccess(message);
                            } else {
                                Log.d(TAG, "No such message: "+messageId);
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
     * Delete message
     *
     * @param chatId
     * @param messageId
     * @param listener
     */
    public void deleteMessage(final String chatId, final String messageId, final ResultListener listener) {
        db.collection(CHATS).document(chatId).collection(MESSAGES).document(messageId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updateMessageCount(chatId, -1, null);
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

    /**
     * Leave chat
     *
     * @param chatId
     * @param userId
     * @param listener
     */
    public void leaveChat(final String chatId, String userId, final ResultListener listener) {
        db.collection(CHATS).document(chatId)
                .update(PARTICIPANTS, FieldValue.arrayRemove(userId))
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
     * Get message updates in result
     *
     * @param chatId
     * @param firstLoadedDate
     * @param listener
     */
    public void registerMessageListener(String chatId, Date firstLoadedDate, final ResultListener listener) {
        if (mRegistration == null) {
            mRegistration = db.collection(CHATS).document(chatId).collection(MESSAGES)
                .whereGreaterThan(CREATED_AT, firstLoadedDate == null ? new Date(0) : firstLoadedDate).orderBy(CREATED_AT, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            QueryDocumentSnapshot document = dc.getDocument();
                            //Log.d(TAG, document.getId() + " => " + document.getData());
                            MessageVO message = document.toObject(MessageVO.class);
                            message.id = document.getId();
                            switch (dc.getType()) {
                                case ADDED:
                                    if (listener != null) listener.onSuccess(Pair.create(Constants.ADDED, message));
                                    break;
                                case MODIFIED:
                                    if (listener != null) listener.onSuccess(Pair.create(Constants.MODIFIED, message));
                                    break;
                                case REMOVED:
                                    if (listener != null) listener.onSuccess(Pair.create(Constants.REMOVED, message));
                                    break;
                            }
                        }

                    }
                });
        }
    }

    /**
     * Stop listening
     */
    public void unregisterMessageListener() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }
    }

    /**
     * Leave user chats
     *
     * @param userId
     * @param listener
     */
    public void leaveChats(final String userId, final ResultListener listener) {
        db.collection(CHATS).whereArrayContains(PARTICIPANTS, userId)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ChatVO> chats = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d(TAG, document.getId() + " => " + document.getData());
                                db.collection(CHATS).document(document.getId())
                                        .update(PARTICIPANTS, FieldValue.arrayRemove(userId));
                            }

                            if (listener != null) listener.onSuccess(null);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            if (listener != null) listener.onFailure(task.getException());
                        }
                    }
                });
    }

}
