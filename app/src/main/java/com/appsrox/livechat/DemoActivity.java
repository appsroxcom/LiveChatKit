package com.appsrox.livechat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class DemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
    }

    public void startLiveChat(View view) {
        MessagesActivity.startChatWith(getString(R.string.agent_user_id), this);//TODO: Update agent_user_id
    }
}
