package com.github.jameshnsears.quoteunquote.listview;

import android.content.Intent;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;

public class ListViewService extends RemoteViewsService {
    @Override
    @NonNull
    public RemoteViewsFactory onGetViewFactory(@NonNull Intent intent) {
        return new ListViewProvider(this.getApplicationContext(), intent);
    }
}
