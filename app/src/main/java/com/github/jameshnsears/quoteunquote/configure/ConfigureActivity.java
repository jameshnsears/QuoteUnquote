package com.github.jameshnsears.quoteunquote.configure;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearanceFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventFragment;
import com.github.jameshnsears.quoteunquote.databinding.ActivityConfigureBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import timber.log.Timber;

public class ConfigureActivity extends AppCompatActivity {
    public int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    @Nullable
    public ActivityConfigureBinding activityConfigureBinding;
    public boolean broadcastFinishIntent = true;
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener
            = item -> {
        Fragment selectedFragment;

        switch (item.getItemId()) {
            default:
                selectedFragment = AppearanceFragment.newInstance(this.widgetId);
                break;
            case R.id.navigationBarQuotations:
                selectedFragment = this.getFragmentContentNewInstance();
                break;
            case R.id.navigationBarSchedule:
                selectedFragment = EventFragment.newInstance(this.widgetId);
                break;
        }

        String activeFragment = selectedFragment.getClass().getSimpleName();

        Timber.d("activeFragment=%s", activeFragment);

        ConfigurePreferences configurePreferences
                = new ConfigurePreferences(widgetId, getApplicationContext());
        configurePreferences.setActiveFragment(activeFragment);

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentPlaceholderContent, selectedFragment)
                .commit();

        this.activityConfigureBinding.scrollView.scrollTo(0, 0);

        return true;
    };

    @Override
    public void finish() {
        // back pressed
        if (this.broadcastFinishIntent) {
            this.broadcastTheFinishIntent();
        }

        ToastHelper.toast = null;

        ConfigurePreferences configurePreferences
                = new ConfigurePreferences(widgetId, getApplicationContext());
        configurePreferences.setActiveFragment("AppearanceFragment");

        this.finishAndRemoveTask();
    }

    @Override
    public void onDestroy() {
        // back pressed | swipe up | theme change
        super.onDestroy();
    }

    public void broadcastTheFinishIntent() {
        this.sendBroadcast(IntentFactoryHelper.createIntentAction(
                this, widgetId, IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION));

        this.setResult(RESULT_OK, IntentFactoryHelper.createIntent(widgetId));
    }

    @Override
    public void onBackPressed() {
        ContentFragment.ensureFragmentContentSearchConsistency(this.widgetId, this.getApplicationContext());

        super.onBackPressed();
    }

    @Override
    public void onCreate(final Bundle bundle) {
        Timber.d("onCreate");
        super.onCreate(bundle);

        AuditEventHelper.createInstance(getApplication());

        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            this.widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            this.broadcastFinishIntent = extras.getBoolean("broadcastFinishIntent", true);
        }

        this.activityConfigureBinding = ActivityConfigureBinding.inflate(getLayoutInflater());
        setContentView(this.activityConfigureBinding.getRoot());

        BottomNavigationView bottomNavigationView = this.findViewById(R.id.configureNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this.navigationItemSelectedListener);

        setActiveFragment();
    }

    private void setActiveFragment() {
        // when switching to dark mode, remember which fragment we're on
        ConfigurePreferences configurePreferences
                = new ConfigurePreferences(widgetId, getApplicationContext());

        String activeFragment = configurePreferences.getActiveFragment();
        Timber.d("activeFragment=%s", activeFragment);

        BottomNavigationView bottomNavigationView = this.findViewById(R.id.configureNavigation);

        Fragment fragment;
        switch (activeFragment) {
            default:
                fragment = AppearanceFragment.newInstance(this.widgetId);
                bottomNavigationView.setSelectedItemId(R.id.navigationBarAppearance);
                break;
            case "ContentFragment":
                fragment = getFragmentContentNewInstance();
                bottomNavigationView.setSelectedItemId(R.id.navigationBarQuotations);
                break;
            case "EventFragment":
                fragment = EventFragment.newInstance(this.widgetId);
                bottomNavigationView.setSelectedItemId(R.id.navigationBarSchedule);
                break;
        }

        this.getSupportFragmentManager().beginTransaction().replace(
                R.id.fragmentPlaceholderContent, fragment).commit();

        this.activityConfigureBinding.scrollView.scrollTo(0, 0);
    }

    @NonNull
    public ContentFragment getFragmentContentNewInstance() {
        return ContentFragment.newInstance(this.widgetId);
    }
}
