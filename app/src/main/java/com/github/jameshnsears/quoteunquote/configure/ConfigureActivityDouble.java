package com.github.jameshnsears.quoteunquote.configure;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.databinding.ActivityConfigureBinding;

import timber.log.Timber;

public class ConfigureActivityDouble extends ConfigureActivity {
    @Override
    protected void init() {
    }

    @Override
    public void onCreate(@Nullable final Bundle bundle) {
        Timber.d("onCreate");
        super.onCreate(bundle);

        widgetId = 1;

        activityConfigureBinding = ActivityConfigureBinding.inflate(this.getLayoutInflater());
        setContentView(activityConfigureBinding.getRoot());

        createListenerBottomNavigationView();

        activityConfigureBinding.configureNavigation.setSelectedItemId(R.id.navigationBarQuotations);
    }
}
