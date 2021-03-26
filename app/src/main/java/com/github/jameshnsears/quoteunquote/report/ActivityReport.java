package com.github.jameshnsears.quoteunquote.report;


import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteWidget;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.ActivityReportBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.Preferences;
import com.github.jameshnsears.quoteunquote.utils.ToastHelper;
import com.microsoft.appcenter.Flags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityReport extends AppCompatActivity {
    private static final String LOG_TAG = ActivityReport.class.getSimpleName();

    private ActivityReportBinding activityReportBinding;
    private QuoteUnquoteWidget quoteUnquoteWidget;
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, String.format("%d: %s", widgetId,
                new Object() {
                }.getClass().getEnclosingMethod().getName()));

        activityReportBinding = null;

        sendBroadcast(IntentFactoryHelper.createIntentAction(
                this,
                widgetId,
                IntentFactoryHelper.ACTIVITY_FINISHED_REPORT));

        setResult(RESULT_OK, IntentFactoryHelper.createIntent(widgetId));
        finish();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        quoteUnquoteWidget = new QuoteUnquoteWidget();

        final Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        if (hasQuotationAlreadyBeenReported()) {
            finish();
        }

        activityReportBinding = ActivityReportBinding.inflate(getLayoutInflater());

        final View view = activityReportBinding.getRoot();

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(view);

        activityReportBinding.cancelButton.setOnClickListener(view1 -> finish());

        activityReportBinding.buttonOK.setOnClickListener(view1 -> {
            AuditEventHelper.auditAppCenter(AuditEventHelper.REPORT, getAuditProperties(), Flags.CRITICAL);

            quoteUnquoteWidget.getQuoteUnquoteModelInstance(getApplicationContext()).markAsReported(widgetId);

            onBackPressed();
        });
    }

    public boolean hasQuotationAlreadyBeenReported() {
        if (quoteUnquoteWidget.getQuoteUnquoteModelInstance(getApplicationContext()).isReported(widgetId)) {
            ToastHelper.makeToast(getApplicationContext(), getApplicationContext().getString(R.string.activity_report_warning), Toast.LENGTH_SHORT);
            return true;
        }

        return false;
    }

    public ConcurrentHashMap<String, String> getAuditProperties() {
        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();

        final Preferences preferences = new Preferences(widgetId, getApplicationContext());
        final QuotationEntity quotationToReport = quoteUnquoteWidget.getQuoteUnquoteModelInstance(getApplicationContext()).getNext(widgetId, preferences.getSelectedContentType());

        final StringBuilder auditMessage = new StringBuilder(200);
        auditMessage
                .append("digest=")
                .append(quotationToReport.digest)
                .append("; author=")
                .append(quotationToReport.author)
                .append("; reason=")
                .append(activityReportBinding.spinnerReason.getSelectedItem().toString())
                .append("; notes=")
                .append(activityReportBinding.editTextNotes.getText().toString());

        properties.put("Report", auditMessage.toString());

        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            Log.d(LOG_TAG, entry.getKey() + ":" + entry.getValue());
        }

        return properties;
    }
}
