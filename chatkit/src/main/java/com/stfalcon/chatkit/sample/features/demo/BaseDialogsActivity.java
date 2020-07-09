package com.stfalcon.chatkit.sample.features.demo;

import android.os.Bundle;

import androidx.annotation.Nullable;

import live.chatkit.android.BaseActivity;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.sample.common.data.fixtures.DialogsFixtures;
import com.stfalcon.chatkit.sample.common.data.model.Dialog;
import com.stfalcon.chatkit.sample.common.data.model.User;
import com.stfalcon.chatkit.sample.utils.AppUtils;

import java.util.List;

/*
 * Created by troy379 on 05.04.17.
 */
public abstract class BaseDialogsActivity extends BaseActivity
        implements DialogsListAdapter.OnDialogClickListener<Dialog>,
        DialogsListAdapter.OnDialogLongClickListener<Dialog> {

    private static final String TAG = "BaseDialogsActivity";

    protected DialogsListAdapter<Dialog> dialogsAdapter;

    protected abstract void initAdapter(List<Dialog> items);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDialogLongClick(Dialog dialog) {
        AppUtils.showToast(
                this,
                dialog.getDialogName(),
                false);
    }

    @Override
    protected void onInit(User currentUser) {
        initAdapter(DialogsFixtures.getDialogs());
    }
}
