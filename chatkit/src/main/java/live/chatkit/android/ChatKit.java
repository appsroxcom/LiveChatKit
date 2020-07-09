package live.chatkit.android;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import static live.chatkit.android.Constants.OFFLINE;
import static live.chatkit.android.Constants.ONLINE;

public class ChatKit implements LifecycleObserver {

    private static Context sContext;
    private static boolean sPresence;
    private static boolean sEncryption;
    private String currentUserId;
    private boolean isOnline;

    private static class Lazy {
        private static final ChatKit INSTANCE = new ChatKit();
    }
    public static ChatKit getInstance() {
        return Lazy.INSTANCE;
    }

    private ChatKit() {}

    /**
     * Do init in App onCreate()
     *
     * @param context App context
     * @param flags presence, encryption - Must not be changed for an app
     */
    public static void init(Application context, boolean... flags) {
        sContext = context;
        sPresence = (flags != null && flags.length > 0) ? flags[0] : false;
        sEncryption = (flags != null && flags.length > 1) ? flags[1] : false;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(getInstance());
    }

    public static boolean isPresence() {
        return sPresence;
    }

    public static boolean isEncryption() {
        return sEncryption;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        isOnline = true;
        if (sPresence && !TextUtils.isEmpty(currentUserId))
            UserRepository.getInstance().updateStatus(currentUserId, ONLINE, null);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        isOnline = false;
        if (sPresence && !TextUtils.isEmpty(currentUserId))
            UserRepository.getInstance().updateStatus(currentUserId, OFFLINE, null);
    }

    public void onInit(String currentUserId) {
        this.currentUserId = currentUserId;
        if (!isOnline) onAppForegrounded();
    }

    public void reset() {
        currentUserId = null;
        isOnline = false;
    }

    public static Context getContext() {
        return sContext;
    }
}
