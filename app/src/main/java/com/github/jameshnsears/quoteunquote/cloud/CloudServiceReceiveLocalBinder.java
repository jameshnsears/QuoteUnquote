package com.github.jameshnsears.quoteunquote.cloud;

import android.os.Binder;

import androidx.annotation.NonNull;

public class CloudServiceReceiveLocalBinder extends Binder {
    private final CloudServiceReceive cloudServiceReceive;

    public CloudServiceReceiveLocalBinder(@NonNull CloudServiceReceive cloudServiceReceive) {
        this.cloudServiceReceive = cloudServiceReceive;
    }

    @NonNull
    public CloudServiceReceive getService() {
        return cloudServiceReceive;
    }
}
