package com.github.jameshnsears.quoteunquote.report;


import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.jameshnsears.quoteunquote.QuoteUnquoteModel;
import com.github.jameshnsears.quoteunquote.R;
import com.github.jameshnsears.quoteunquote.database.quotation.QuotationEntity;
import com.github.jameshnsears.quoteunquote.databinding.ActivityReportBinding;
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper;
import com.github.jameshnsears.quoteunquote.utils.audit.AuditEventHelper;
import com.github.jameshnsears.quoteunquote.utils.ui.ToastHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import timber.log.Timber;

public class ReportActivity extends AppCompatActivity {
    @Nullable
    private ActivityReportBinding activityReportBinding;
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onPause() {
        this.finish();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.activityReportBinding = null;

        this.broadcastFinishIntent();
        this.finish();
    }

    public void broadcastFinishIntent() {
        this.sendBroadcast(IntentFactoryHelper.createIntentAction(
                this,
                this.widgetId,
                IntentFactoryHelper.ACTIVITY_FINISHED_REPORT));

        this.setResult(this.RESULT_OK, IntentFactoryHelper.createIntent(this.widgetId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            this.widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (this.widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            this.finish();
        }

        if (this.hasQuotationAlreadyBeenReported()) {
            this.finish();
        }

        this.activityReportBinding = ActivityReportBinding.inflate(this.getLayoutInflater());
        View view = this.activityReportBinding.getRoot();

        this.setContentView(view);

        this.createClickListeners();
    }

    private void createClickListeners() {
        this.activityReportBinding.cancelButton.setOnClickListener(view1 -> this.finish());

        this.activityReportBinding.buttonOK.setOnClickListener(view1 -> {
            AuditEventHelper.auditEvent("REPORT", this.getAuditProperties());

            this.getQuoteUnquoteModel().markAsReported(this.widgetId);

            this.onBackPressed();
        });
    }

    @NonNull
    protected QuoteUnquoteModel getQuoteUnquoteModel() {
        return new QuoteUnquoteModel(this.getApplicationContext());
    }

    public boolean hasQuotationAlreadyBeenReported() {
        if (this.getQuoteUnquoteModel().isReported(this.widgetId)) {
            ToastHelper.makeToast(this.getApplicationContext(), this.getApplicationContext().getString(R.string.activity_report_warning), Toast.LENGTH_SHORT);
            return true;
        }

        return false;
    }

    @NonNull
    public ConcurrentMap<String, String> getAuditProperties() {
        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();

        QuotationEntity quotationToReport = this.getQuoteUnquoteModel().getCurrentQuotation(this.widgetId);

        properties.put("Report", "digest="
                + quotationToReport.digest
                + "; author="
                + quotationToReport.author
                + "; reason="
                + this.activityReportBinding.spinnerReason.getSelectedItem()
                + "; notes="
                + this.activityReportBinding.editTextNotes.getText());

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Timber.d(entry.getKey() + ":" + entry.getValue());
        }

        return properties;
    }
}
