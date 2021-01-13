package com.github.jameshnsears.quoteunquote.report;


import android.appwidget.AppWidgetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentPreferences;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.ActivityReportBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class ReportActivity extends AppCompatActivity {
    @Nullable
    private ActivityReportBinding activityReportBinding;
    @Nullable
    public QuoteUnquoteModel quoteUnquoteModel;         // TODO rm replace ContentViewModel with QuoteUnquoteModel?
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onPause() {
        finish();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Timber.d("widgetId=%d", widgetId);

        activityReportBinding = null;

        broadcastFinishIntent();
        finish();
    }

    public void broadcastFinishIntent() {
        sendBroadcast(IntentFactoryHelper.createIntentAction(
                this,
                widgetId,
                IntentFactoryHelper.ACTIVITY_FINISHED_REPORT));

        setResult(RESULT_OK, IntentFactoryHelper.createIntent(widgetId));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        quoteUnquoteModel = getQuoteUnquoteModel();

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
            AuditEventHelper.auditEvent("REPORT", getAuditProperties());

            quoteUnquoteModel.markAsReported(widgetId);

            onBackPressed();
        });
    }

    protected QuoteUnquoteModel getQuoteUnquoteModel() {
        return new QuoteUnquoteModel(getApplicationContext());
    }

    public boolean hasQuotationAlreadyBeenReported() {
        if (quoteUnquoteModel.isReported(widgetId)) {
            ToastHelper.makeToast(getApplicationContext(), getApplicationContext().getString(R.string.activity_report_warning), Toast.LENGTH_SHORT);
            return true;
        }

        return false;
    }

    @NonNull
    public ConcurrentHashMap<String, String> getAuditProperties() {
        final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();

        final ContentPreferences contentPreferences = new ContentPreferences(widgetId, getApplicationContext());
        final QuotationEntity quotationToReport = quoteUnquoteModel.getNext(widgetId, contentPreferences.getContentSelection());

        properties.put("Report", "digest="
                + quotationToReport.digest
                + "; author="
                + quotationToReport.author
                + "; reason="
                + activityReportBinding.spinnerReason.getSelectedItem().toString()
                + "; notes="
                + activityReportBinding.editTextNotes.getText().toString());

        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            Timber.d(entry.getKey() + ":" + entry.getValue());
        }

        return properties;
    }
}
