package com.github.jameshnsears.quoteunquote.configure;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.FragmentAppearance;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.FragmentContent;
import com.github.jameshnsears.quoteunquote.configure.fragment.event.FragmentEvent;
import com.github.jameshnsears.quoteunquote.configure.fragment.footer.FragmentFooter;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.Preferences;
import com.github.jameshnsears.quoteunquote.utils.ToastHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class ActivityConfigure extends AppCompatActivity {
    private static final String LOG_TAG = ActivityConfigure.class.getSimpleName();

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        finishActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ToastHelper.toast = null;
    }

    private void finishActivity() {
        final FragmentContent fragmentContent = (FragmentContent)
                getSupportFragmentManager().findFragmentById(R.id.fragmentPlaceholderContent);

        if (isQuotationTextEmpty(fragmentContent)) {
            final Preferences preferences = new Preferences(widgetId, getApplicationContext());
            preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL, true);
            preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_QUOTATION_TEXT, false);
        }

        sendBroadcast(IntentFactoryHelper.createIntentAction(
                this, widgetId, IntentFactoryHelper.ACTIVITY_FINISHED_CONFIGURATION));

        setResult(RESULT_OK, IntentFactoryHelper.createIntent(widgetId));
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        final FragmentContent fragmentContent = (FragmentContent)
                getSupportFragmentManager().findFragmentById(R.id.fragmentPlaceholderContent);

        if (isQuotationTextEmpty(fragmentContent)) {
            warnUserAboutQuotationText();
            fragmentContent.fragmentContentBinding.radioButtonAll.setChecked(true);

            final Preferences preferences = new Preferences(widgetId, getApplicationContext());
            preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_ALL, true);
            preferences.setSharedPreference(Preferences.FRAGMENT_CONTENT, Preferences.RADIO_BUTTON_QUOTATION_TEXT, false);
        } else {
            finishActivity();
        }
    }

    private void warnUserAboutQuotationText() {
        ToastHelper.makeToast(this, this.getString(R.string.fragment_content_text_no_search_results), Toast.LENGTH_SHORT);
    }

    private boolean isQuotationTextEmpty(final FragmentContent fragmentContent) {
        return fragmentContent.fragmentContentBinding.radioButtonKeywords.isChecked()
                && fragmentContent.countKeywords == 0;
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        AuditEventHelper.createInstance(getApplication());

        this.setTitle(getString(R.string.activity_configure_title));

        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentPlaceholderAppearance, FragmentAppearance.newInstance(widgetId));
        fragmentTransaction.add(R.id.fragmentPlaceholderContent, FragmentContent.newInstance(widgetId));
        fragmentTransaction.add(R.id.fragmentPlaceholderEvent, FragmentEvent.newInstance(widgetId));
        fragmentTransaction.add(R.id.fragmentPlaceholderFooter, FragmentFooter.newInstance(widgetId));
        fragmentTransaction.commit();

        setContentView(R.layout.activity_configure);
    }
}
