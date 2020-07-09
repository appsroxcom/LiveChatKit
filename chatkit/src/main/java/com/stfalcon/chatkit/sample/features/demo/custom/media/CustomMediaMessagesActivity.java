package com.stfalcon.chatkit.sample.features.demo.custom.media;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import live.chatkit.android.R;
import com.stfalcon.chatkit.sample.common.data.fixtures.MessagesFixtures;
import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.features.demo.BaseMessagesActivity;
import com.stfalcon.chatkit.sample.features.demo.custom.media.holders.IncomingFileMessageViewHolder;
import com.stfalcon.chatkit.sample.features.demo.custom.media.holders.IncomingVoiceMessageViewHolder;
import com.stfalcon.chatkit.sample.features.demo.custom.media.holders.OutcomingFileMessageViewHolder;
import com.stfalcon.chatkit.sample.features.demo.custom.media.holders.OutcomingVoiceMessageViewHolder;

public class CustomMediaMessagesActivity extends BaseMessagesActivity
        implements MessageInput.InputListener,
        MessageInput.AttachmentsListener,
        MessageHolders.ContentChecker<Message>,
        DialogInterface.OnClickListener {

    private static final byte CONTENT_TYPE_VOICE = 1;
    private static final byte CONTENT_TYPE_FILE = 2;

    public static void open(Context context) {
        context.startActivity(new Intent(context, CustomMediaMessagesActivity.class));
    }

    private MessagesList messagesList;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_media_messages);

        messagesList = (MessagesList) findViewById(R.id.messagesList);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        progress = (ProgressBar) findViewById(R.id.progress);
        login();

        MessageInput input = (MessageInput) findViewById(R.id.input);
        input.setInputListener(this);
        input.setAttachmentsListener(this);

        swipeRefresh.setEnabled(false);
        swipeRefresh.setDistanceToTriggerSync(Integer.MAX_VALUE);
        swipeRefresh.setColorSchemeResources(R.color.cornflower_blue);
    }

    protected void showLoader() {
        swipeRefresh.setRefreshing(true);
    }
    protected void hideLoader() {
        swipeRefresh.setRefreshing(false);
    }

    protected void showProgress() {
        progress.setIndeterminate(true);
    }
    protected void hideProgress() {
        progress.setIndeterminate(false);
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        super.messagesAdapter.addToStart(
                MessagesFixtures.getTextMessage(input.toString()), true);
        return true;
    }

    @Override
    public void onAddAttachments() {
        new AlertDialog.Builder(this)
                .setItems(R.array.view_types_dialog, this)
                .show();
    }

    @Override
    public boolean hasContentFor(Message message, byte type) {
        switch (type) {
            case CONTENT_TYPE_VOICE:
                return message.getVoice() != null
                        && message.getVoice().getUrl() != null
                        && !message.getVoice().getUrl().isEmpty();
            case CONTENT_TYPE_FILE:
                return message.getFile() != null
                        && message.getFile().getUrl() != null
                        && !message.getFile().getUrl().isEmpty();
        }
        return false;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case 0:
                messagesAdapter.addToStart(MessagesFixtures.getImageMessage(), true);
                break;
            case 1:
                messagesAdapter.addToStart(MessagesFixtures.getVoiceMessage(), true);
                break;
        }
    }

    @Override
    protected void initAdapter() {
        MessageHolders holders = new MessageHolders()
                .registerContentType(
                        CONTENT_TYPE_VOICE,
                        IncomingVoiceMessageViewHolder.class,
                        R.layout.item_custom_incoming_voice_message,
                        OutcomingVoiceMessageViewHolder.class,
                        R.layout.item_custom_outcoming_voice_message,
                        this)
                .registerContentType(
                        CONTENT_TYPE_FILE,
                        IncomingFileMessageViewHolder.class,
                        R.layout.item_custom_incoming_file_message,
                        OutcomingFileMessageViewHolder.class,
                        R.layout.item_custom_outcoming_file_message,
                        this);


        super.messagesAdapter = new MessagesListAdapter<>(getCurrentUser().getId(), holders, super.imageLoader);
        super.messagesAdapter.enableSelectionMode(this);
        super.messagesAdapter.setLoadMoreListener(this);
        this.messagesList.setAdapter(super.messagesAdapter);
    }
}
