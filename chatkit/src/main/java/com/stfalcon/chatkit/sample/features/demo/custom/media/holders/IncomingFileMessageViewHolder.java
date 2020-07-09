package com.stfalcon.chatkit.sample.features.demo.custom.media.holders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;
import live.chatkit.android.R;
import com.stfalcon.chatkit.sample.common.data.model.Message;
import com.stfalcon.chatkit.sample.utils.FormatUtils;
import com.stfalcon.chatkit.utils.DateFormatter;

public class IncomingFileMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<Message> {

    private Button btnDownload;
    private TextView tvName;
    private TextView tvTime;

    public IncomingFileMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        btnDownload = (Button) itemView.findViewById(R.id.download);
        tvName = (TextView) itemView.findViewById(R.id.name);
        tvTime = (TextView) itemView.findViewById(R.id.time);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        btnDownload.setText(
                FormatUtils.getSizeString(
                        message.getFile().getSize()));
        tvName.setText(message.getText());
        tvTime.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
    }
}
