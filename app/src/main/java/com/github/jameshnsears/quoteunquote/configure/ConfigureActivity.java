package com.github.jameshnsears.quoteunquote.configure;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.FragmentCommon;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearanceFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.notifications.NotificationsFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.QuotationsFilterFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.sync.SyncFragment;
import com.github.jameshnsears.quoteunquote.databinding.ActivityConfigureBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;

import timber.log.Timber;

public class ConfigureActivity extends AppCompatActivity {
    public static boolean launcherInvoked;

    public int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Nullable
    public ActivityConfigureBinding activityConfigureBinding;

    public boolean broadcastFinishIntent = true;

    @Nullable
    protected boolean finishCalled;

    @NonNull
    private ActivityResultLauncher<Intent> wikipediaActivityLauncher = this.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    finish();
                }
            });

    @Override
    protected void onResume() {
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
        // back pressed | swipe up | launcher started
        if (!finishCalled && !launcherInvoked) {
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
        QuotationsFilterFragment.ensureFragmentContentSearchConsistency(widgetId, getApplicationContext());
        super.onBackPressed();
    }

    @Override
    public void onCreate(@Nullable final Bundle bundle) {
        Timber.d("onCreate");
        EdgeToEdge.enable(this);
        super.onCreate(bundle);
        init();
    }

    protected void init() {
        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();

        if (extras != null) {
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
        }

        activityConfigureBinding = ActivityConfigureBinding.inflate(this.getLayoutInflater());
        setContentView(activityConfigureBinding.getRoot());

        createListenerBottomNavigationView();

        routeToLastScreen();
    }

    protected void routeToLastScreen() {
        String screen =
                new QuotationsPreferences(widgetId, getApplicationContext()).getScreen();

        if (!screen.equals("")) {
            switch (FragmentCommon.Screen.fromString(screen)) {
                case QuotationsFilter:
                case ContentInternal:
                case ContentFiles:
                case ContentWeb:
                    activityConfigureBinding.configureNavigation.setSelectedItemId(R.id.navigationBarQuotations);
                    break;
                case AppearanceStyle:
                case AppearanceToolbar:
                    activityConfigureBinding.configureNavigation.setSelectedItemId(R.id.navigationBarAppearance);
                    break;
                case Notifications:
                    activityConfigureBinding.configureNavigation.setSelectedItemId(R.id.navigationBarNotification);
                    break;
                case Sync:
                    activityConfigureBinding.configureNavigation.setSelectedItemId(R.id.navigationBarSync);
                    break;
            }
        } else {
            activityConfigureBinding.configureNavigation.setSelectedItemId(R.id.navigationBarQuotations);
        }
    }

    protected void createListenerBottomNavigationView() {
        activityConfigureBinding.configureNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = getFragmentContentNewInstance();

            String screen =
                    new QuotationsPreferences(widgetId, getApplicationContext()).getScreen();

            switch (item.getItemId()) {
                case R.id.navigationBarQuotations:
                    selectedFragment = getFragmentContentNewInstance();
                    enableDisableMenuToHelpWithRouting(false, true, true, true);
                    break;
                case R.id.navigationBarAppearance:
                    selectedFragment = AppearanceFragment.newInstance(widgetId);
                    activityConfigureBinding.configureNavigation.getMenu().findItem(R.id.navigationBarQuotations).setEnabled(true);
                    enableDisableMenuToHelpWithRouting(true, false, true, true);
                    break;
                case R.id.navigationBarNotification:
                    selectedFragment = NotificationsFragment.newInstance(widgetId);
                    activityConfigureBinding.configureNavigation.getMenu().findItem(R.id.navigationBarQuotations).setEnabled(true);
                    enableDisableMenuToHelpWithRouting(true, true, false, true);
                    break;
                case R.id.navigationBarSync:
                    selectedFragment = SyncFragment.newInstance(widgetId);
                    activityConfigureBinding.configureNavigation.getMenu().findItem(R.id.navigationBarQuotations).setEnabled(true);
                    enableDisableMenuToHelpWithRouting(true, true, true, false);
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

    private void enableDisableMenuToHelpWithRouting(
            final boolean navigationBarQuotations,
            final boolean navigationBarAppearance,
            final boolean navigationBarNotification,
            final boolean navigationBarSync
    ) {
        Menu menu = activityConfigureBinding.configureNavigation.getMenu();
        menu.findItem(R.id.navigationBarQuotations).setEnabled(navigationBarQuotations);
        menu.findItem(R.id.navigationBarAppearance).setEnabled(navigationBarAppearance);
        menu.findItem(R.id.navigationBarNotification).setEnabled(navigationBarNotification);
        menu.findItem(R.id.navigationBarSync).setEnabled(navigationBarSync);
    }

    private void linkToWikipedia(@NonNull final String wikipedia) {
        Timber.d("wikipedia=%s", wikipedia);

        if (wikipedia.equals("r/quotes/")) {
            wikipediaActivityLauncher.launch(
                    IntentFactoryHelper.createIntentActionView("https://www.reddit.com/" + wikipedia));
        } else {
            wikipediaActivityLauncher.launch(
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
