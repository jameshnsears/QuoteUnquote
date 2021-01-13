package com.github.jameshnsears.quoteunquote.configure;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onPause() {
        Timber.d("widgetId=%d", widgetId);

        finish();

        super.onPause();
    }

    @Override
    public void onDestroy() {
        ToastHelper.toast = null;
        super.onDestroy();
    }

    @Override
    public void finish() {
        ensureFragmentContentSearchConsistency();

        broadcastFinishIntent();

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

    public void broadcastFinishIntent() {
        sendBroadcast(IntentFactoryHelper.createIntentAction(
                this, widgetId, IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION));

        setResult(RESULT_OK, IntentFactoryHelper.createIntent(widgetId));
    }

    @Override
    public void onBackPressed() {
        Timber.d("widgetId=%d", widgetId);
        super.onBackPressed();

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
        }

        createFragments();
    }

    public void createFragments() {
        AuditEventHelper.createInstance(getApplication());

        this.setTitle(getString(R.string.activity_configure_title));

        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentPlaceholderAppearance, AppearanceFragment.newInstance(widgetId));
        fragmentTransaction.add(R.id.fragmentPlaceholderContent, getFragmentContentNewInstance());
        fragmentTransaction.add(R.id.fragmentPlaceholderEvent, EventFragment.newInstance(widgetId));
        fragmentTransaction.add(R.id.fragmentPlaceholderFooter, FooterFragment.newInstance(widgetId));
        fragmentTransaction.commit();

        setContentView(R.layout.activity_configure);
    }

    @NonNull
    public ContentFragment getFragmentContentNewInstance() {
        return ContentFragment.newInstance(widgetId);
    }
}
