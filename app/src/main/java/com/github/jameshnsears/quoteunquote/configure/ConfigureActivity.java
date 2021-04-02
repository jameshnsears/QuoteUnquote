package com.github.jameshnsears.quoteunquote.configure;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.AppearanceFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.EventFragment;
import com.github.jameshnsears.quoteunquote.configure.fragment.footer.FooterFragment;
import com.github.jameshnsears.quoteunquote.utils.ContentSelection;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import timber.log.Timber;


public class ConfigureActivity extends AppCompatActivity {
    public int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public boolean broadcastFinishIntent = true;

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }

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
    public void onDestroy() {
        ToastHelper.toast = null;
        super.onDestroy();
    }

    @Override
    public void finish() {
        ensureFragmentContentSearchConsistency();

        if (broadcastFinishIntent) {
            broadcastTheFinishIntent();
        }

        super.finish();
    }

    public void ensureFragmentContentSearchConsistency() {
        final ContentFragment contentFragment = getFragmentContent();

        if (isSearchSelectedButWithoutResults(contentFragment)) {
            warnUserAboutSearchResults();
            contentFragment.fragmentContentBinding.radioButtonAll.setChecked(true);
            resetContentSelection();
        }

        contentFragment.shutdown();
    }

    public void broadcastTheFinishIntent() {
        sendBroadcast(IntentFactoryHelper.createIntentAction(
                this, widgetId, IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION));

        setResult(RESULT_OK, IntentFactoryHelper.createIntent(widgetId));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        scrollBarPositionRemember();
        finish();
    }

    @NonNull
    public ContentFragment getFragmentContent() {
        return (ContentFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentPlaceholderContent);
    }

    private void resetContentSelection() {
        final ContentPreferences contentPreferences = new ContentPreferences(widgetId, getApplicationContext());
        contentPreferences.setContentSelection(ContentSelection.ALL);
    }

    private void warnUserAboutSearchResults() {
        ToastHelper.makeToast(this, this.getString(R.string.fragment_content_text_no_search_results), Toast.LENGTH_LONG);
    }

    private boolean isSearchSelectedButWithoutResults(
            @NonNull final ContentFragment contentFragment) {
        return contentFragment.fragmentContentBinding.radioButtonSearch.isChecked()
                && contentFragment.countSearchResults == 0;
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

        createFragments();

        setContentView(R.layout.activity_configure);

        scrollsBarPositionRestore();
    }

    public void createFragments() {
        Timber.d("%d", widgetId);

        AuditEventHelper.createInstance(getApplication());

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_action_bar_icon);
        this.setTitle("  " + getString(R.string.activity_configure_title));

        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentPlaceholderAppearance, AppearanceFragment.newInstance(widgetId));
        fragmentTransaction.add(R.id.fragmentPlaceholderContent, getFragmentContentNewInstance());
        fragmentTransaction.add(R.id.fragmentPlaceholderEvent, EventFragment.newInstance(widgetId));
        fragmentTransaction.add(R.id.fragmentPlaceholderFooter, FooterFragment.newInstance(widgetId));
        fragmentTransaction.commit();
    }

    @NonNull
    public ContentFragment getFragmentContentNewInstance() {
        return ContentFragment.newInstance(widgetId);
    }
}
