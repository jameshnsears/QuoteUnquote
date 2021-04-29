package com.github.jameshnsears.quoteunquote.configure;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

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
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener
            = item -> {
        Fragment selectedFragment = null;
        switch (item.getItemId()) {
            case R.id.navigationBarAppearance:
                selectedFragment = AppearanceFragment.newInstance(this.widgetId);
                break;
            case R.id.navigationBarQuotations:
                selectedFragment = this.getFragmentContentNewInstance();
                break;
            case R.id.navigationBarSchedule:
                selectedFragment = EventFragment.newInstance(this.widgetId);
                break;
            default:
                selectedFragment = FooterFragment.newInstance(this.widgetId);
                break;
        }

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentPlaceholderContent, selectedFragment)
                .commit();
        return true;
    };

    @Override
    public void finish() {
        if (this.broadcastFinishIntent) {
            this.broadcastTheFinishIntent();
        }

        ToastHelper.toast = null;

        this.finishAndRemoveTask();
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
        super.onCreate(bundle);

        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            this.widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            this.broadcastFinishIntent = extras.getBoolean("broadcastFinishIntent", true);
        }

        this.setContentView(R.layout.activity_configure);

        final BottomNavigationView bottomNavigationView = this.findViewById(R.id.configureNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this.navigationItemSelectedListener);

        this.getSupportFragmentManager().beginTransaction().replace(
                R.id.fragmentPlaceholderContent, AppearanceFragment.newInstance(this.widgetId)).commit();
    }

    @NonNull
    public ContentFragment getFragmentContentNewInstance() {
        return ContentFragment.newInstance(this.widgetId);
    }
}
