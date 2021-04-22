package com.github.jameshnsears.quoteunquote.configure;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearanceFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.footer.FooterFragment;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class ConfigureActivity extends AppCompatActivity {
    public int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public boolean broadcastFinishIntent = true;

    private void scrollBarPositionRemember() {
        ConfigurePreferences configurePreferences = new ConfigurePreferences(widgetId, getApplicationContext());
        ScrollView scrollView = findViewById(R.id.configureScrollView);
        configurePreferences.setScrollY(scrollView.getScrollY());
    }

    private void scrollsBarPositionRestore() {
        ConfigurePreferences configurePreferences = new ConfigurePreferences(widgetId, getApplicationContext());
        ScrollView scrollView = findViewById(R.id.configureScrollView);
        scrollView.post(()
                -> scrollView.scrollTo(scrollView.getScrollX(), configurePreferences.getScrollY()));
    }

    @Override
    public void finish() {
        if (broadcastFinishIntent) {
            broadcastTheFinishIntent();
        }

        ToastHelper.toast = null;

        finishAndRemoveTask();
    }

    public void broadcastTheFinishIntent() {
        sendBroadcast(IntentFactoryHelper.createIntentAction(
                this, widgetId, IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION));

        setResult(RESULT_OK, IntentFactoryHelper.createIntent(widgetId));
    }

    @Override
    public void onBackPressed() {
        scrollBarPositionRemember();

        super.onBackPressed();
    }

    @NonNull
    public ContentFragment getFragmentContent() {
        return (ContentFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentPlaceholderContent);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            broadcastFinishIntent = extras.getBoolean("broadcastFinishIntent", true);
        }

        setContentView(R.layout.activity_configure);

        BottomNavigationView bottomNavigationView = findViewById(R.id.configureNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragmentPlaceholderContent, getFragmentContentNewInstance()).commit();

        scrollsBarPositionRestore();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener
            = item -> {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigationBarAppearance:
                        selectedFragment = AppearanceFragment.newInstance(widgetId);
                        break;
                    case R.id.navigationBarQuotations:
                        selectedFragment = getFragmentContentNewInstance();
                        break;
                    case R.id.navigationBarSchedule:
                        selectedFragment = EventFragment.newInstance(widgetId);
                        break;
                    case R.id.navigationBarAbout:
                        selectedFragment = FooterFragment.newInstance(widgetId);
                        break;
                }

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentPlaceholderContent, selectedFragment)
                        .commit();
                return true;
            };

    @NonNull
    public ContentFragment getFragmentContentNewInstance() {
        return ContentFragment.newInstance(widgetId);
    }
}
