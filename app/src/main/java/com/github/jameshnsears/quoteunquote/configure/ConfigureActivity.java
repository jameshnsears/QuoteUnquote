package com.github.jameshnsears.quoteunquote.configure;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearanceFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncFragment;
import com.github.jameshnsears.quoteunquote.databinding.ActivityConfigureBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;

import timber.log.Timber;

public class ConfigureActivity extends AppCompatActivity {
    public static boolean safCalled;

    public int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Nullable
    public ActivityConfigureBinding activityConfigureBinding;

    public boolean broadcastFinishIntent = true;

    @Nullable
    protected boolean finishCalled;

    @NonNull
    private ActivityResultLauncher<Intent> wikipediaActivityLancher = this.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    finish();
                }
            });

    @Override
    protected void onResume() {
        // https://stackoverflow.com/questions/15658687/how-to-use-onresume
        Timber.d("onResume");
        super.onResume();
    }

    @Override
    public void finish() {
        // back pressed
        if (broadcastFinishIntent) {
            broadcastTheFinishIntent();
        }

        finishCalled = true;

        super.finish();
    }

    @Override
    public void onPause() {
        // back pressed | swipe up | export activity started
        if (!this.finishCalled && !ConfigureActivity.safCalled) {
            finish();
        }

        super.onPause();
    }

    public void broadcastTheFinishIntent() {
        // to all widget's in case a restore happened
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        for (int id : appWidgetManager.getAppWidgetIds(new ComponentName(this, QuoteUnquoteWidget.class))) {
            sendBroadcast(IntentFactoryHelper.createIntentAction(
                    this, id, IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION));
        }

        setResult(Activity.RESULT_OK, IntentFactoryHelper.createIntent(this.widgetId));
    }

    @Override
    public void onBackPressed() {
        QuotationsFragment.ensureFragmentContentSearchConsistency(widgetId, getApplicationContext());
        super.onBackPressed();
    }

    @Override
    public void onCreate(@Nullable final Bundle bundle) {
        Timber.d("onCreate");
        super.onCreate(bundle);
        init();
    }

    protected void init() {
        AuditEventHelper.createInstance(getApplication());
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        final String wikipedia = extras.getString("wikipedia");
        if (wikipedia != null && !wikipedia.equals("?") && !wikipedia.equals("")) {
            linkToWikipedia(wikipedia);
        } else {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            broadcastFinishIntent = extras.getBoolean("broadcastFinishIntent", true);

            QuoteUnquoteWidget.currentContentSelection
                    = new QuotationsPreferences(widgetId, getApplicationContext()).getContentSelection();

            QuoteUnquoteWidget.currentAuthorSelection
                    = new QuotationsPreferences(widgetId, getApplicationContext()).getContentSelectionAuthor();
        }

        activityConfigureBinding = ActivityConfigureBinding.inflate(this.getLayoutInflater());
        setContentView(activityConfigureBinding.getRoot());

        createListenerBottomNavigationView();

        activityConfigureBinding.configureNavigation.setSelectedItemId(R.id.navigationBarQuotations);
    }

    protected void createListenerBottomNavigationView() {
        activityConfigureBinding.configureNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = getFragmentContentNewInstance();

            switch (item.getItemId()) {
                case R.id.navigationBarQuotations:
                    selectedFragment = getFragmentContentNewInstance();
                    break;
                case R.id.navigationBarAppearance:
                    selectedFragment = AppearanceFragment.newInstance(widgetId);
                    break;
                case R.id.navigationBarNotification:
                    selectedFragment = NotificationsFragment.newInstance(widgetId);
                    break;
                case R.id.navigationBarSync:
                    selectedFragment = SyncFragment.newInstance(widgetId);
                    break;
                default:
                    Timber.e("%d", item.getItemId());
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentPlaceholderContent, selectedFragment)
                    .commit();

            activityConfigureBinding.scrollView.scrollTo(0, 0);

            return true;
        });
    }

    private void linkToWikipedia(@NonNull final String wikipedia) {
        Timber.d("wikipedia=%s", wikipedia);

        if (wikipedia.equals("r/quotes/")) {
            wikipediaActivityLancher.launch(
                    IntentFactoryHelper.createIntentActionView("https://www.reddit.com/" + wikipedia));
        } else {
            wikipediaActivityLancher.launch(
                    IntentFactoryHelper.createIntentActionView("https://en.wikipedia.org/wiki/" + wikipedia));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @NonNull
    public QuotationsFragment getFragmentContentNewInstance() {
        return QuotationsFragment.newInstance(widgetId);
    }
}
