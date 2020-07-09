package com.appsrox.livechat;

import androidx.multidex.MultiDexApplication;
import live.chatkit.android.ChatKit;

public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ChatKit.init(this);
    }

}
