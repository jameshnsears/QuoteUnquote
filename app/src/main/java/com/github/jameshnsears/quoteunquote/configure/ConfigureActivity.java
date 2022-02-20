package com.github.jameshnsears.quoteunquote.configure;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearanceFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventFragment;
import com.github.jameshnsears.quoteunquote.database.DatabaseRepository;
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

    private boolean finishCalled;

    public static boolean exportCalled;

    public boolean broadcastFinishIntent = true;

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener
            = item -> {
        Fragment selectedFragment = this.getFragmentContentNewInstance();

        switch (item.getItemId()) {
            case R.id.navigationBarQuotations:
                selectedFragment = this.getFragmentContentNewInstance();
                break;

            case R.id.navigationBarAppearance:
                selectedFragment = AppearanceFragment.newInstance(this.widgetId);
                break;

            case R.id.navigationBarSchedule:
                selectedFragment = EventFragment.newInstance(this.widgetId);
                break;

            default:
                Timber.e("%d", item.getItemId());
        }

        String activeFragment = selectedFragment.getClass().getSimpleName();

        Timber.d("activeFragment=%s", activeFragment);

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

        finishCalled = true;

        super.finish();
    }

    @Override
    public void onPause() {
        // back pressed | swipe up | export activity started
        if (!finishCalled && !exportCalled) {
            finish();
        }

        super.onPause();
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
    public void onCreate(final @NonNull Bundle bundle) {
        Timber.d("onCreate");
        super.onCreate(bundle);

        AuditEventHelper.createInstance(getApplication());
        Intent intent = this.getIntent();
        Bundle extras = intent.getExtras();


        String wikipedia = extras.getString("wikipedia");
        if (wikipedia != null && !wikipedia.equals("?") && !wikipedia.equals("")) {
            linkToWikipedia(wikipedia);
        } else {
            this.widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            this.broadcastFinishIntent = extras.getBoolean("broadcastFinishIntent", true);

            this.activityConfigureBinding = ActivityConfigureBinding.inflate(getLayoutInflater());
            setContentView(this.activityConfigureBinding.getRoot());

            BottomNavigationView bottomNavigationView = this.findViewById(R.id.configureNavigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(this.navigationItemSelectedListener);

            bottomNavigationView.setSelectedItemId(R.id.navigationBarQuotations);
        }
    }

    private void linkToWikipedia(@NonNull String wikipedia) {
        Timber.d("wikipedia=%s", wikipedia);

        if (wikipedia.equals("r/quotes/")) {
            wikipediaActivityLancher.launch(
                    IntentFactoryHelper.createIntentActionView("https://www.reddit.com/" + wikipedia));
        } else {
            wikipediaActivityLancher.launch(
                    IntentFactoryHelper.createIntentActionView("https://en.wikipedia.org/wiki/" + wikipedia));
        }
    }

    ActivityResultLauncher<Intent> wikipediaActivityLancher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    finish();
                }
            });

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");

        ToastHelper.toast = null;

        DatabaseRepository.close(getApplicationContext());
        DatabaseRepository.databaseRepository = null;

        super.onDestroy();
    }

    @NonNull
    public ContentFragment getFragmentContentNewInstance() {
        return ContentFragment.newInstance(this.widgetId);
    }
}
