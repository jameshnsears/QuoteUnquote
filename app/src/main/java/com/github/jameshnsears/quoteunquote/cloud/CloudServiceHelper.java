package com.github.jameshnsears.quoteunquote.cloud;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

public class CloudServiceHelper {
    public static void showNoNetworkToast(@NonNull Context context, @NonNull final Handler handler) {
        handler.post(() -> ToastHelper.makeToast(
                context,
                context.getString(R.string.fragment_content_favourites_share_comms),
                Toast.LENGTH_SHORT));
    }
}
