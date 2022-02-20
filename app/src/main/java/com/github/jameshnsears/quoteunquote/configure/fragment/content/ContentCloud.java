package com.github.jameshnsears.quoteunquote.configure.fragment.content;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.cloud.CloudServiceReceive;
import com.github.jameshnsears.quoteunquote.cloud.CloudServiceReceiveLocalBinder;

public class ContentCloud {
    @Nullable
    public CloudServiceReceive cloudServiceReceive;
    public boolean isServiceReceiveBound;

    @NonNull
    public final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, @Nullable IBinder service) {
            CloudServiceReceiveLocalBinder binder = (CloudServiceReceiveLocalBinder) service;

            if (service != null) {
                ContentCloud.this.cloudServiceReceive = binder.getService();
                ContentCloud.this.isServiceReceiveBound = true;
            } else {
                ContentCloud.this.isServiceReceiveBound = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ContentCloud.this.isServiceReceiveBound = false;
        }
    };
}
