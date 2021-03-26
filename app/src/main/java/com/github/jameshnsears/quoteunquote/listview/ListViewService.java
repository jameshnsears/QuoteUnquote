package com.github.jameshnsears.quoteunquote.listview;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ListViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(final Intent intent) {
        return new ListViewProvider(this, intent);
    }
}